/**
 * Atribuir grupo a usuários.
 */

import { useEffect, useState } from 'react'
import SiteHeader from '../components/catalog/SiteHeader'
import FeedbackMessage from '../components/FeedbackMessage'
import { atribuirGrupo, listarGrupos, listarUsuarios } from '../api/adminService'
import '../styles/catalog.css'
import '../styles/admin.css'

export default function AdminUsuariosPage() {
  const [usuarios, setUsuarios] = useState([])
  const [grupos, setGrupos] = useState([])
  const [erro, setErro] = useState('')
  const [ok, setOk] = useState('')
  const [loading, setLoading] = useState(true)

  async function carregar() {
    setLoading(true)
    setErro('')
    try {
      const [listaUsuarios, listaGrupos] = await Promise.all([
        listarUsuarios(),
        listarGrupos(),
      ])
      setUsuarios(listaUsuarios)
      setGrupos(listaGrupos)
    } catch (err) {
      setErro(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    carregar()
  }, [])

  async function onChangeGrupo(userId, grupoId) {
    setErro('')
    setOk('')
    try {
      await atribuirGrupo(userId, Number(grupoId))
      setOk('Grupo atualizado.')
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
          <h1>Usuários</h1>
          <p>Escolha o grupo de permissão de cada usuário.</p>
        </header>

        {loading && <p className="catalog-state">Carregando…</p>}
        <FeedbackMessage type="error">{erro}</FeedbackMessage>
        <FeedbackMessage type="success">{ok}</FeedbackMessage>

        <div className="lotes-table-wrap">
          <table className="lotes-table">
            <thead>
              <tr>
                <th>Email</th>
                <th>Grupo atual</th>
                <th>Admin</th>
                <th>Atribuir grupo</th>
              </tr>
            </thead>
            <tbody>
              {usuarios.map((user) => (
                <tr key={user.id}>
                  <td>{user.email}</td>
                  <td>{user.grupo || '—'}</td>
                  <td>{user.admin ? 'Sim' : 'Não'}</td>
                  <td>
                    <select
                      defaultValue=""
                      onChange={(e) => {
                        if (e.target.value) {
                          onChangeGrupo(user.id, e.target.value)
                        }
                      }}
                    >
                      <option value="">Selecionar…</option>
                      {grupos.map((g) => (
                        <option key={g.id} value={g.id}>
                          {g.nome}
                        </option>
                      ))}
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </main>
    </div>
  )
}
