/**
 * Formulário simples para criar leilão (somente quem tem CRIAR_LEILAO).
 */

import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import SiteHeader from '../components/catalog/SiteHeader'
import Button from '../components/Button'
import FeedbackMessage from '../components/FeedbackMessage'
import { criarLeilao } from '../api/leilaoService'
import '../styles/catalog.css'
import '../styles/admin.css'

function dataPadrao(dias) {
  const d = new Date()
  d.setDate(d.getDate() + dias)
  d.setMinutes(0, 0, 0)
  const iso = new Date(d.getTime() - d.getTimezoneOffset() * 60000)
    .toISOString()
    .slice(0, 16)
  return iso
}

export default function NovoLeilaoPage() {
  const navigate = useNavigate()
  const [erro, setErro] = useState('')
  const [loading, setLoading] = useState(false)
  const [form, setForm] = useState({
    titulo: '',
    subtitulo: '',
    local: 'Haras Real — Campinas, SP',
    dataInicio: dataPadrao(7),
    dataFim: dataPadrao(7),
    status: 'AGENDADO',
    loteNumero: 1,
    loteNome: '',
    loteRaca: '',
    loteSexo: 'Macho',
    loteIdade: 3,
    loteLance: 10000,
  })

  function setCampo(campo, valor) {
    setForm((atual) => ({ ...atual, [campo]: valor }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setErro('')
    setLoading(true)

    try {
      const criado = await criarLeilao({
        titulo: form.titulo,
        subtitulo: form.subtitulo,
        local: form.local,
        dataInicio: form.dataInicio,
        dataFim: form.dataFim,
        status: form.status,
        lotes: [
          {
            numero: Number(form.loteNumero),
            nomeAnimal: form.loteNome,
            sexo: form.loteSexo,
            raca: form.loteRaca,
            idadeAnos: Number(form.loteIdade),
            pelagem: '',
            registro: '',
            descricaoCurta: '',
            lanceInicial: Number(form.loteLance),
          },
        ],
      })
      navigate(`/leiloes/${criado.id}`)
    } catch (err) {
      setErro(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="catalog-page">
      <SiteHeader />
      <main className="catalog-main">
        <header className="catalog-intro">
          <h1>Novo leilão</h1>
          <p>Cadastre o evento e o primeiro lote. Depois você pode evoluir o formulário.</p>
        </header>

        <FeedbackMessage type="error">{erro}</FeedbackMessage>

        <form className="admin-form" onSubmit={handleSubmit}>
          <label>
            Título
            <input
              value={form.titulo}
              onChange={(e) => setCampo('titulo', e.target.value)}
              required
            />
          </label>
          <label>
            Subtítulo
            <input
              value={form.subtitulo}
              onChange={(e) => setCampo('subtitulo', e.target.value)}
            />
          </label>
          <label>
            Local
            <input
              value={form.local}
              onChange={(e) => setCampo('local', e.target.value)}
            />
          </label>

          <div className="admin-form-row">
            <label>
              Início
              <input
                type="datetime-local"
                value={form.dataInicio}
                onChange={(e) => setCampo('dataInicio', e.target.value)}
                required
              />
            </label>
            <label>
              Fim
              <input
                type="datetime-local"
                value={form.dataFim}
                onChange={(e) => setCampo('dataFim', e.target.value)}
                required
              />
            </label>
          </div>

          <h2 className="admin-subtitle">Primeiro lote</h2>

          <div className="admin-form-row">
            <label>
              Nº
              <input
                type="number"
                min="1"
                value={form.loteNumero}
                onChange={(e) => setCampo('loteNumero', e.target.value)}
                required
              />
            </label>
            <label>
              Animal
              <input
                value={form.loteNome}
                onChange={(e) => setCampo('loteNome', e.target.value)}
                required
              />
            </label>
          </div>

          <div className="admin-form-row">
            <label>
              Raça
              <input
                value={form.loteRaca}
                onChange={(e) => setCampo('loteRaca', e.target.value)}
              />
            </label>
            <label>
              Sexo
              <select
                value={form.loteSexo}
                onChange={(e) => setCampo('loteSexo', e.target.value)}
              >
                <option>Macho</option>
                <option>Fêmea</option>
              </select>
            </label>
            <label>
              Idade
              <input
                type="number"
                min="0"
                value={form.loteIdade}
                onChange={(e) => setCampo('loteIdade', e.target.value)}
              />
            </label>
            <label>
              Lance inicial
              <input
                type="number"
                min="0"
                step="100"
                value={form.loteLance}
                onChange={(e) => setCampo('loteLance', e.target.value)}
                required
              />
            </label>
          </div>

          <Button type="submit" disabled={loading}>
            {loading ? 'Salvando…' : 'Criar leilão'}
          </Button>
        </form>
      </main>
    </div>
  )
}
