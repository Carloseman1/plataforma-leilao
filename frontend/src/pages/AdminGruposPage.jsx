/**
 * Tela de grupos — criar e editar permissões do grupo.
 */

import { useEffect, useState } from 'react'
import SiteHeader from '../components/catalog/SiteHeader'
import Button from '../components/Button'
import FeedbackMessage from '../components/FeedbackMessage'
import {
  atualizarGrupo,
  criarGrupo,
  listarGrupos,
  listarPermissoesDisponiveis,
} from '../api/adminService'
import '../styles/catalog.css'
import '../styles/admin.css'

const PERMISSAO_LABEL = {
  CRIAR_LEILAO: 'Criar leilões',
  GERENCIAR_GRUPOS: 'Gerenciar grupos',
  GERENCIAR_USUARIOS: 'Gerenciar usuários',
}

const formVazio = {
  nome: '',
  descricao: '',
  permissoes: [],
}

export default function AdminGruposPage() {
  const [grupos, setGrupos] = useState([])
  const [todasPermissoes, setTodasPermissoes] = useState([])
  const [form, setForm] = useState(formVazio)
  const [editandoId, setEditandoId] = useState(null)
  const [erro, setErro] = useState('')
  const [ok, setOk] = useState('')
  const [loading, setLoading] = useState(true)

  async function carregar() {
    setLoading(true)
    setErro('')
    try {
      const [listaGrupos, listaPerms] = await Promise.all([
        listarGrupos(),
        listarPermissoesDisponiveis(),
      ])
      setGrupos(listaGrupos)
      setTodasPermissoes(listaPerms)
    } catch (err) {
      setErro(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    carregar()
  }, [])

  function togglePermissao(permissao) {
    setForm((atual) => {
      const jaTem = atual.permissoes.includes(permissao)
      return {
        ...atual,
        permissoes: jaTem
          ? atual.permissoes.filter((p) => p !== permissao)
          : [...atual.permissoes, permissao],
      }
    })
  }

  function editar(grupo) {
    setEditandoId(grupo.id)
    setForm({
      nome: grupo.nome,
      descricao: grupo.descricao || '',
      permissoes: grupo.permissoes || [],
    })
    setOk('')
    setErro('')
  }

  function limparForm() {
    setEditandoId(null)
    setForm(formVazio)
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setErro('')
    setOk('')

    try {
      if (editandoId) {
        await atualizarGrupo(editandoId, form)
        setOk('Grupo atualizado.')
      } else {
        await criarGrupo(form)
        setOk('Grupo criado.')
      }
      limparForm()
      await carregar()
    } catch (err) {
      setErro(err.message)
    }
  }

  return (
    <div className="catalog-page">
      <SiteHeader />
      <main className="catalog-main">
        <header className="catalog-intro">
          <h1>Grupos de permissão</h1>
          <p>Defina o que cada grupo pode fazer na plataforma.</p>
        </header>

        {loading && <p className="catalog-state">Carregando…</p>}
        <FeedbackMessage type="error">{erro}</FeedbackMessage>
        <FeedbackMessage type="success">{ok}</FeedbackMessage>

        <section className="admin-panel">
          <h2>{editandoId ? 'Editar grupo' : 'Novo grupo'}</h2>
          <form className="admin-form" onSubmit={handleSubmit}>
            <label>
              Nome
              <input
                value={form.nome}
                onChange={(e) => setForm({ ...form, nome: e.target.value })}
                required
              />
            </label>
            <label>
              Descrição
              <input
                value={form.descricao}
                onChange={(e) => setForm({ ...form, descricao: e.target.value })}
              />
            </label>

            <fieldset className="perm-fieldset">
              <legend>Permissões</legend>
              {todasPermissoes.map((p) => (
                <label key={p} className="perm-check">
                  <input
                    type="checkbox"
                    checked={form.permissoes.includes(p)}
                    onChange={() => togglePermissao(p)}
                  />
                  {PERMISSAO_LABEL[p] || p}
                </label>
              ))}
            </fieldset>

            <div className="admin-form-actions">
              <Button type="submit">
                {editandoId ? 'Salvar' : 'Criar grupo'}
              </Button>
              {editandoId && (
                <button type="button" className="site-logout dark" onClick={limparForm}>
                  Cancelar
                </button>
              )}
            </div>
          </form>
        </section>

        <section className="admin-panel">
          <h2>Grupos existentes</h2>
          <ul className="grupo-list">
            {grupos.map((grupo) => (
              <li key={grupo.id}>
                <div>
                  <strong>{grupo.nome}</strong>
                  <p>{grupo.descricao}</p>
                  <small>
                    {(grupo.permissoes || [])
                      .map((p) => PERMISSAO_LABEL[p] || p)
                      .join(', ') || 'Sem permissões'}
                  </small>
                </div>
                <button type="button" className="link-btn" onClick={() => editar(grupo)}>
                  Editar
                </button>
              </li>
            ))}
          </ul>
        </section>
      </main>
    </div>
  )
}
