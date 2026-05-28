import { useState, lazy, Suspense } from 'react'
import Navbar from '../components/Navbar'
import { useZones } from '../hooks/useZones'

const AdminZonesMap = lazy(() => import('../components/AdminZonesMap'))

const TYPES = [
  { value: 'HOSPITAL', label: 'Hospital' },
  { value: 'SCHOOL',   label: 'Escuela'  },
  { value: 'PARK',     label: 'Parque'   },
  { value: 'NONE',     label: 'Sin tipo' },
]

const TIPO_LABEL = { HOSPITAL: 'Hospital', SCHOOL: 'Escuela', PARK: 'Parque', NONE: 'Sin tipo' }

const EMPTY = { name: '', latitude: '', longitude: '', radiusKm: '1.0', sensitiveType: 'HOSPITAL' }

export default function AdminZonesPage() {
  const { zones, loading, add, edit, remove } = useZones()
  const [form,    setForm]    = useState(EMPTY)
  const [editing, setEditing] = useState(null)
  const [error,   setError]   = useState('')

  // Cuando el usuario hace clic en el mapa, rellena lat/lon en el formulario
  function handleMapClick({ lat, lng }) {
    setForm(f => ({
      ...f,
      latitude:  parseFloat(lat.toFixed(6)),
      longitude: parseFloat(lng.toFixed(6)),
    }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    const payload = {
      ...form,
      latitude:  parseFloat(form.latitude),
      longitude: parseFloat(form.longitude),
      radiusKm:  parseFloat(form.radiusKm),
    }
    try {
      editing ? await edit(editing, payload) : await add(payload)
      setForm(EMPTY)
      setEditing(null)
    } catch (err) {
      setError(err.response?.data?.error ?? 'Error al guardar la zona.')
    }
  }

  function startEdit(zone) {
    setEditing(zone.id)
    setForm({
      name:          zone.name,
      latitude:      zone.latitude,
      longitude:     zone.longitude,
      radiusKm:      zone.radiusKm,
      sensitiveType: zone.sensitiveType,
    })
  }

  function cancelEdit() {
    setEditing(null)
    setForm(EMPTY)
    setError('')
  }

  const hasCoords = form.latitude !== '' && form.longitude !== ''

  return (
    <div style={{ minHeight: '100vh', background: '#f8fafc' }}>
      <Navbar />
      <div style={{ maxWidth: '1100px', margin: '0 auto', padding: '24px 16px' }}>
        <h1 style={{ color: '#1e3a5f', marginBottom: '6px' }}>🗺️ Gestión de Zonas Sensibles</h1>
        <p style={{ color: '#6b7280', marginBottom: '24px', fontSize: '0.88rem' }}>
          Haz clic en el mapa para seleccionar la ubicación de la nueva zona.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.4fr', gap: '20px', marginBottom: '28px' }}>

          {/* Formulario */}
          <form onSubmit={handleSubmit} style={{
            background: '#fff', padding: '24px', borderRadius: '12px',
            boxShadow: '0 1px 4px rgba(0,0,0,0.07)', alignSelf: 'start'
          }}>
            <h3 style={{ margin: '0 0 16px', color: '#374151' }}>
              {editing ? '✏️ Editar zona' : '➕ Nueva zona'}
            </h3>

            {error && (
              <p style={{ color: '#dc2626', fontSize: '0.85rem', margin: '0 0 12px',
                          background: '#fee2e2', padding: '8px 12px', borderRadius: '6px' }}>
                {error}
              </p>
            )}

            <Field label="Nombre">
              <input value={form.name} required placeholder="Ej: Hospital San Ignacio"
                onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                style={inputStyle} />
            </Field>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <Field label="Latitud">
                <input value={form.latitude} required placeholder="Clic en el mapa"
                  onChange={e => setForm(f => ({ ...f, latitude: e.target.value }))}
                  style={{ ...inputStyle, background: hasCoords ? '#f0fdf4' : '#fff' }} />
              </Field>
              <Field label="Longitud">
                <input value={form.longitude} required placeholder="Clic en el mapa"
                  onChange={e => setForm(f => ({ ...f, longitude: e.target.value }))}
                  style={{ ...inputStyle, background: hasCoords ? '#f0fdf4' : '#fff' }} />
              </Field>
            </div>

            <Field label="Radio (km)">
              <input type="number" min="0.1" max="50" step="0.1" value={form.radiusKm} required
                onChange={e => setForm(f => ({ ...f, radiusKm: e.target.value }))}
                style={inputStyle} />
            </Field>

            <Field label="Tipo de zona sensible">
              <select value={form.sensitiveType}
                onChange={e => setForm(f => ({ ...f, sensitiveType: e.target.value }))}
                style={inputStyle}>
                {TYPES.map(t => <option key={t.value} value={t.value}>{t.label}</option>)}
              </select>
            </Field>

            <div style={{ display: 'flex', gap: '8px', marginTop: '4px' }}>
              <button type="submit" style={{
                flex: 1, padding: '9px', background: '#1e3a5f', color: '#fff',
                border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 600
              }}>
                {editing ? 'Actualizar' : 'Agregar zona'}
              </button>
              {editing && (
                <button type="button" onClick={cancelEdit} style={{
                  padding: '9px 16px', background: '#f3f4f6', color: '#374151',
                  border: '1px solid #d1d5db', borderRadius: '8px', cursor: 'pointer'
                }}>Cancelar</button>
              )}
            </div>
          </form>

          {/* Mapa interactivo */}
          <div style={{ background: '#fff', borderRadius: '12px', overflow: 'hidden',
                        boxShadow: '0 1px 4px rgba(0,0,0,0.07)' }}>
            <div style={{ padding: '14px 18px', borderBottom: '1px solid #f1f5f9',
                          fontSize: '0.82rem', color: '#6b7280' }}>
              📍 Haz clic en el mapa para colocar la zona — arrastra el marcador para ajustar
            </div>
            <Suspense fallback={
              <div style={{ height: '400px', display: 'flex', alignItems: 'center',
                            justifyContent: 'center', color: '#6b7280' }}>Cargando mapa...</div>
            }>
              <AdminZonesMap
                zones={zones}
                selectedLat={form.latitude !== '' ? parseFloat(form.latitude) : null}
                selectedLng={form.longitude !== '' ? parseFloat(form.longitude) : null}
                editingId={editing}
                onMapClick={handleMapClick}
              />
            </Suspense>
          </div>
        </div>

        {/* Tabla de zonas */}
        {loading ? <p style={{ color: '#6b7280' }}>Cargando...</p> : (
          <div style={{ background: '#fff', borderRadius: '12px', overflow: 'hidden',
                        boxShadow: '0 1px 4px rgba(0,0,0,0.07)' }}>
            <div style={{ padding: '14px 18px', borderBottom: '1px solid #f1f5f9',
                          fontWeight: 600, color: '#374151', fontSize: '0.9rem' }}>
              {zones.length} zona(s) registrada(s)
            </div>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ background: '#f8fafc' }}>
                  {['Nombre', 'Tipo', 'Radio km', 'Prioridad', 'Coordenadas', 'Acciones'].map(h => (
                    <th key={h} style={{ padding: '10px 16px', textAlign: 'left',
                                         fontSize: '0.78rem', color: '#6b7280',
                                         textTransform: 'uppercase', letterSpacing: '0.04em' }}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {zones.map(z => (
                  <tr key={z.id} style={{
                    borderTop: '1px solid #f1f5f9',
                    background: editing === z.id ? '#eff6ff' : 'transparent'
                  }}>
                    <td style={td}><strong>{z.name}</strong></td>
                    <td style={td}>
                      <span style={{
                        padding: '2px 10px', borderRadius: '12px', fontSize: '0.78rem', fontWeight: 600,
                        background: z.sensitiveType === 'HOSPITAL' ? '#f3e8ff' :
                                    z.sensitiveType === 'SCHOOL'   ? '#e0f2fe' :
                                    z.sensitiveType === 'PARK'     ? '#ffedd5' : '#f3f4f6',
                        color:      z.sensitiveType === 'HOSPITAL' ? '#7c3aed' :
                                    z.sensitiveType === 'SCHOOL'   ? '#0369a1' :
                                    z.sensitiveType === 'PARK'     ? '#c2410c' : '#6b7280',
                      }}>
                        {TIPO_LABEL[z.sensitiveType] ?? z.sensitiveType}
                      </span>
                    </td>
                    <td style={td}>{z.radiusKm} km</td>
                    <td style={{ ...td, fontWeight: 700, color: '#1e3a5f' }}>{z.priority}</td>
                    <td style={{ ...td, fontSize: '0.78rem', color: '#6b7280' }}>
                      {z.latitude?.toFixed(4)}, {z.longitude?.toFixed(4)}
                    </td>
                    <td style={{ ...td, display: 'flex', gap: '6px' }}>
                      <button onClick={() => startEdit(z)} style={btnEdit}>Editar</button>
                      <button onClick={() => remove(z.id)} style={btnDel}>Eliminar</button>
                    </td>
                  </tr>
                ))}
                {zones.length === 0 && (
                  <tr><td colSpan={6} style={{ padding: '32px', textAlign: 'center', color: '#9ca3af' }}>
                    No hay zonas registradas. Agrega la primera usando el formulario.
                  </td></tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

function Field({ label, children }) {
  return (
    <div style={{ marginBottom: '12px' }}>
      <label style={{ display: 'block', fontSize: '0.82rem', color: '#374151',
                      fontWeight: 500, marginBottom: '4px' }}>{label}</label>
      {children}
    </div>
  )
}

const inputStyle = {
  width: '100%', padding: '8px 10px', border: '1px solid #d1d5db',
  borderRadius: '6px', fontSize: '0.9rem', boxSizing: 'border-box',
}
const td      = { padding: '11px 16px', fontSize: '0.85rem', color: '#374151' }
const btnEdit = { padding: '4px 12px', background: '#dbeafe', color: '#1e40af',
                  border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '0.82rem' }
const btnDel  = { padding: '4px 12px', background: '#fee2e2', color: '#991b1b',
                  border: 'none', borderRadius: '4px', cursor: 'pointer', fontSize: '0.82rem' }
