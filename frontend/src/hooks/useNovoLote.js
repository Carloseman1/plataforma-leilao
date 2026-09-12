import { useState } from 'react'
import { adicionarLote } from '../api/leilaoService'
import { nascimentoPorIdade } from '../utils/lote'

const FORM_VAZIO = {
  nome: '',
  raca: 'Mangalarga Marchador',
  sexo: 'Macho',
  idade: '',
  registro: '',
  pelagem: '',
  imagemPrincipal: '',
  tipoOferta: 'VENDA_ANIMAL',
  descricaoOferta: '',
  valorInicial: '',
  incrementoMinimo: 500,
}

/** Campos em branco viram null para não gravar string vazia no banco. */
function ouNulo(texto) {
  return texto.trim() || null
}

export default function useNovoLote(uuidLeilao, aoCriar) {
  const [form, setForm] = useState(FORM_VAZIO)
  const [erro, setErro] = useState('')
  const [salvando, setSalvando] = useState(false)

  function setCampo(campo, valor) {
    setForm((atual) => ({ ...atual, [campo]: valor }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setErro('')
    setSalvando(true)

    try {
      // O número do lote fica por conta do backend, que sabe qual é o próximo livre.
      await adicionarLote(uuidLeilao, {
        tipoOferta: form.tipoOferta,
        descricaoOferta: ouNulo(form.descricaoOferta),
        valorInicial: Number(form.valorInicial),
        incrementoMinimo: Number(form.incrementoMinimo),
        animal: {
          nome: form.nome.trim(),
          raca: ouNulo(form.raca),
          sexo: form.sexo,
          registro: ouNulo(form.registro),
          pelagem: ouNulo(form.pelagem),
          imagemPrincipal: ouNulo(form.imagemPrincipal),
          dataNascimento: nascimentoPorIdade(form.idade),
        },
      })

      setForm(FORM_VAZIO)
      aoCriar()
    } catch (err) {
      setErro(err.message)
    } finally {
      setSalvando(false)
    }
  }

  return { form, setCampo, erro, salvando, handleSubmit }
}
