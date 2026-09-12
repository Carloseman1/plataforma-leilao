#!/usr/bin/env python3
"""
Simulador de compradores disputando um pregão.

Cada comprador é uma thread que fica olhando o lote aberto e dando lances. Os
lances saem pela mesma API que a tela usa, então o caminho exercitado é o de
verdade: REST -> Kafka -> avaliador.

    pip install requests
    python simular_pregao.py --leilao <uuid-do-leilao> --compradores 5

Para provocar os casos de borda de propósito:

    python simular_pregao.py --leilao <uuid> --caos

O modo caos intercala lances repetidos, abaixo do pedido, fora do incremento e
em lote já encerrado — cada um deve aparecer na tela de recusas com o motivo
correspondente.
"""

import argparse
import random
import threading
import time
import uuid
from dataclasses import dataclass

import requests

API = "http://localhost:8080"


class ErroDeApi(Exception):
    pass


@dataclass
class Comprador:
    email: str
    senha: str
    token: str = ""

    @property
    def cabecalhos(self):
        return {"Authorization": f"Bearer {self.token}"}


# --------------------------------------------------------------------- acesso


def cadastrar_se_preciso(email, senha):
    resposta = requests.post(
        f"{API}/api/user/cadastrar", json={"email": email, "password": senha}, timeout=10
    )
    # 400 aqui quase sempre é "email já cadastrado", que é o caso normal na
    # segunda execução do script.
    if resposta.status_code not in (200, 201, 400):
        raise ErroDeApi(f"cadastro de {email} falhou: {resposta.text}")


def entrar(email, senha):
    cadastrar_se_preciso(email, senha)

    resposta = requests.post(
        f"{API}/api/user/login", json={"email": email, "password": senha}, timeout=10
    )
    if resposta.status_code != 200:
        raise ErroDeApi(f"login de {email} falhou: {resposta.text}")

    return Comprador(email=email, senha=senha, token=resposta.json()["token"])


def montar_compradores(quantidade):
    compradores = []
    for numero in range(1, quantidade + 1):
        email = f"comprador{numero}@simulador.local"
        compradores.append(entrar(email, "Simulador@2026"))
        print(f"  comprador {numero}: {email}")
    return compradores


# --------------------------------------------------------------------- pregão


def ler_pregao(comprador, uuid_leilao):
    resposta = requests.get(
        f"{API}/api/leiloes/{uuid_leilao}/pregao", headers=comprador.cabecalhos, timeout=10
    )
    if resposta.status_code != 200:
        raise ErroDeApi(f"não consegui ler o pregão: {resposta.text}")
    return resposta.json()


def dar_lance(comprador, uuid_lote, valor, lance_uuid=None):
    """Devolve a chave usada, para conseguir reenviar o mesmo lance depois."""
    chave = lance_uuid or str(uuid.uuid4())

    requests.post(
        f"{API}/api/lotes/{uuid_lote}/lances",
        json={"lanceUuid": chave, "valor": valor},
        headers=comprador.cabecalhos,
        timeout=10,
    )
    return chave


# ---------------------------------------------------------------- compradores


def disputar(comprador, uuid_leilao, parar, agressividade):
    """Um comprador: olha o lote aberto e cobre enquanto tiver disposição."""
    while not parar.is_set():
        try:
            pregao = ler_pregao(comprador, uuid_leilao)
        except ErroDeApi:
            time.sleep(1)
            continue

        lote = pregao.get("loteAtual")

        if not lote or pregao.get("status") != "EM_ANDAMENTO":
            if pregao.get("status") == "ENCERRADO":
                return
            time.sleep(1)
            continue

        # Quem já está ganhando não cobre o próprio lance.
        if lote.get("comprador") == comprador.email:
            time.sleep(0.5)
            continue

        if random.random() < agressividade:
            dar_lance(comprador, lote["uuid"], float(lote["proximoLance"]))

        time.sleep(random.uniform(0.4, 2.0))


# --------------------------------------------------------------------- caos


def provocar_casos(comprador, uuid_leilao, parar):
    """Dispara de propósito cada tipo de recusa, uma a cada poucos segundos."""
    while not parar.is_set():
        try:
            pregao = ler_pregao(comprador, uuid_leilao)
        except ErroDeApi:
            time.sleep(2)
            continue

        lote = pregao.get("loteAtual")
        if not lote:
            time.sleep(2)
            continue

        proximo = float(lote["proximoLance"])
        incremento = float(lote["incrementoMinimo"])
        inicial = float(lote["valorInicial"])

        # Lance repetido: mesma chave enviada duas vezes.
        chave = dar_lance(comprador, lote["uuid"], proximo)
        time.sleep(0.2)
        dar_lance(comprador, lote["uuid"], proximo, lance_uuid=chave)
        print("  caos: reenviei o mesmo lance (deve recusar como repetido)")

        time.sleep(2)
        dar_lance(comprador, lote["uuid"], max(proximo - incremento, 1))
        print("  caos: lance abaixo do pedido")

        time.sleep(2)
        dar_lance(comprador, lote["uuid"], proximo + incremento / 2)
        print("  caos: lance fora do incremento")

        time.sleep(2)
        dar_lance(comprador, lote["uuid"], inicial * 500)
        print("  caos: valor absurdo")

        # Lote já encerrado: guarda o lote de agora e volta nele depois que o
        # pregão tiver passado adiante.
        lote_antigo = lote["uuid"]
        time.sleep(8)
        dar_lance(comprador, lote_antigo, proximo + incremento)
        print("  caos: lance em lote que já pode ter fechado")

        time.sleep(4)


def disputa_simultanea(compradores, uuid_lote, valor):
    """
    Todos dão o mesmo valor ao mesmo tempo.

    Um só pode entrar: os outros chegam depois na partição e encontram o preço
    já subido.
    """
    threads = [
        threading.Thread(target=dar_lance, args=(comprador, uuid_lote, valor))
        for comprador in compradores
    ]
    for thread in threads:
        thread.start()
    for thread in threads:
        thread.join()


# --------------------------------------------------------------------- início


def main():
    parser = argparse.ArgumentParser(description="Simula compradores num pregão")
    parser.add_argument("--leilao", required=True, help="uuid do leilão")
    parser.add_argument("--compradores", type=int, default=4)
    parser.add_argument("--minutos", type=float, default=5)
    parser.add_argument("--agressividade", type=float, default=0.6,
                        help="chance de cobrir a cada rodada, de 0 a 1")
    parser.add_argument("--caos", action="store_true",
                        help="intercala lances inválidos de propósito")
    parser.add_argument("--empate", action="store_true",
                        help="dispara um lance simultâneo de todos e sai")
    argumentos = parser.parse_args()

    print("Entrando com os compradores…")
    compradores = montar_compradores(argumentos.compradores)

    pregao = ler_pregao(compradores[0], argumentos.leilao)
    print(f"\nLeilão: {pregao['titulo']} — {pregao['status']}")

    if pregao["status"] != "EM_ANDAMENTO":
        print("O pregão não está no ar. Abra pelo botão 'Abrir pregão' na tela e rode de novo.")
        return

    if argumentos.empate:
        lote = pregao["loteAtual"]
        print(f"Disparando {len(compradores)} lances iguais em {lote['nomeAnimal']}…")
        disputa_simultanea(compradores, lote["uuid"], float(lote["proximoLance"]))
        print("Pronto. Só um deve ter entrado — os outros estão na tela de recusas.")
        return

    parar = threading.Event()
    threads = [
        threading.Thread(
            target=disputar,
            args=(comprador, argumentos.leilao, parar, argumentos.agressividade),
            daemon=True,
        )
        for comprador in compradores
    ]

    if argumentos.caos:
        threads.append(
            threading.Thread(
                target=provocar_casos,
                args=(compradores[0], argumentos.leilao, parar),
                daemon=True,
            )
        )

    for thread in threads:
        thread.start()

    print(f"Rodando por {argumentos.minutos} min. Ctrl+C para parar.\n")

    try:
        time.sleep(argumentos.minutos * 60)
    except KeyboardInterrupt:
        print("\nParando…")
    finally:
        parar.set()
        for thread in threads:
            thread.join(timeout=3)

    print("Fim.")


if __name__ == "__main__":
    main()
