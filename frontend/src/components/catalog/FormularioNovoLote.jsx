import Button from '../Button'
import FeedbackMessage from '../FeedbackMessage'
import useNovoLote from '../../hooks/useNovoLote'
import { TIPOS_DE_OFERTA } from '../../utils/lote'

export default function FormularioNovoLote({ uuidLeilao, onCancelar, onCriado }) {
  const { form, setCampo, erro, salvando, handleSubmit } = useNovoLote(uuidLeilao, onCriado)

  return (
    <section className="lote-form">
      <h3 className="admin-subtitle">Novo lote</h3>

      <FeedbackMessage type="error">{erro}</FeedbackMessage>

      <form className="admin-form" onSubmit={handleSubmit}>
        <label>
          Animal
          <input
            value={form.nome}
            onChange={(e) => setCampo('nome', e.target.value)}
            placeholder="Nome do animal"
            required
          />
        </label>

        <div className="admin-form-row">
          <label>
            Raça
            <input value={form.raca} onChange={(e) => setCampo('raca', e.target.value)} />
          </label>
          <label>
            Sexo
            <select value={form.sexo} onChange={(e) => setCampo('sexo', e.target.value)}>
              <option>Macho</option>
              <option>Fêmea</option>
            </select>
          </label>
          <label>
            Idade
            <input
              type="number"
              min="0"
              value={form.idade}
              onChange={(e) => setCampo('idade', e.target.value)}
            />
          </label>
        </div>

        <div className="admin-form-row">
          <label>
            Registro
            <input
              value={form.registro}
              onChange={(e) => setCampo('registro', e.target.value)}
            />
          </label>
          <label>
            Pelagem
            <input
              value={form.pelagem}
              onChange={(e) => setCampo('pelagem', e.target.value)}
            />
          </label>
        </div>

        <label>
          Foto
          <input
            value={form.imagemPrincipal}
            onChange={(e) => setCampo('imagemPrincipal', e.target.value)}
            placeholder="/animais/nome-do-animal.png"
          />
        </label>

        <div className="admin-form-row">
          <label>
            Tipo de oferta
            <select
              value={form.tipoOferta}
              onChange={(e) => setCampo('tipoOferta', e.target.value)}
            >
              {TIPOS_DE_OFERTA.map((tipo) => (
                <option key={tipo.valor} value={tipo.valor}>
                  {tipo.rotulo}
                </option>
              ))}
            </select>
          </label>
          <label>
            Lance inicial
            <input
              type="number"
              min="0"
              step="100"
              value={form.valorInicial}
              onChange={(e) => setCampo('valorInicial', e.target.value)}
              required
            />
          </label>
          <label>
            Incremento mínimo
            <input
              type="number"
              min="0"
              step="100"
              value={form.incrementoMinimo}
              onChange={(e) => setCampo('incrementoMinimo', e.target.value)}
              required
            />
          </label>
        </div>

        <label>
          Descrição da oferta
          <input
            value={form.descricaoOferta}
            onChange={(e) => setCampo('descricaoOferta', e.target.value)}
            placeholder="Matriz premiada, pronta para pista e criação."
          />
        </label>

        <div className="admin-form-actions">
          <Button type="submit" disabled={salvando}>
            {salvando ? 'Salvando…' : 'Adicionar lote'}
          </Button>
          <button type="button" className="link-btn" onClick={onCancelar}>
            Cancelar
          </button>
        </div>
      </form>
    </section>
  )
}
