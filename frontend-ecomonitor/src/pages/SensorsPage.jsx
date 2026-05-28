import { useState, lazy, Suspense } from 'react'
import { LineChart, Line, XAxis, YAxis, Tooltip, ReferenceLine, ResponsiveContainer } from 'recharts'
import Navbar from '../components/Navbar'
import { postReading, getSensorReadings } from '../api/pollution'
import { useSensorSimulator } from '../hooks/useSensorSimulator'
import { formatTime } from '../utils/dates'

const SensorsMap = lazy(() => import('../components/SensorsMap'))

const ZONE_SUGGESTIONS = [
  { label: 'Aranjuez (norte)',  lat: 6.2905, lon: -75.5555 },
  { label: 'Centro',           lat: 6.2526, lon: -75.5696 },
  { label: 'Belén (occidente)',lat: 6.2372, lon: -75.6105 },
  { label: 'El Poblado (sur)', lat: 6.1999, lon: -75.5610 },
  { label: 'Bello',            lat: 6.3376, lon: -75.5678 },
  { label: 'Itagüí',           lat: 6.1685, lon: -75.6444 },
]

const SCENARIOS = [
  { value: 'normal',    label: '🟢 Normal (20–90 µg/m³)',        desc: 'Niveles seguros' },
  { value: 'warning',   label: '🟡 Advertencia (100–145 µg/m³)', desc: 'Niveles elevados' },
  { value: 'emergency', label: '🔴 Emergencia (155–215 µg/m³)',   desc: 'Supera umbral 150' },
  { value: 'critical',  label: '🚨 Crítico — Hospital (155–215 µg/m³)', desc: 'Zona hospital → nivel CRITICAL' },
  { value: 'faulty',    label: '⚡ Sensor fallido (saltos)',       desc: 'Activa mantenimiento' },
]

export default function SensorsPage() {
  const { running, logs, scenario, setScenario, start, stop, clearLogs, sensors } = useSensorSimulator()

  const [form, setForm] = useState({
    sensorId: '', zoneId: '', latitude: '', longitude: '', co2Level: ''
  })
  const [status,  setStatus]  = useState(null)
  const [loading, setLoading] = useState(false)

  // Historial de sensor
  const [histSensorId, setHistSensorId] = useState('MED-ARN-001')
  const [histMinutes,  setHistMinutes]  = useState(60)
  const [histData,     setHistData]     = useState([])
  const [histLoading,  setHistLoading]  = useState(false)
  const [histLoaded,   setHistLoaded]   = useState(false)

  function handleMapClick({ lat, lng }) {
    // Sugiere zona más cercana como texto editable, el usuario puede cambiarlo
    let closest = ZONE_SUGGESTIONS[0]
    let minDist = Infinity
    ZONE_SUGGESTIONS.forEach(z => {
      const d = Math.hypot(z.lat - lat, z.lon - lng)
      if (d < minDist) { minDist = d; closest = z }
    })
    const suggested = `ZONA-${closest.label.toUpperCase().replace(/[^A-Z0-9]/g, '-').replace(/-+/g, '-').replace(/-$/, '')}`
    setForm(f => ({
      ...f,
      latitude:  parseFloat(lat.toFixed(6)),
      longitude: parseFloat(lng.toFixed(6)),
      zoneId:    f.zoneId || suggested,
    }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setLoading(true)
    setStatus(null)
    try {
      await postReading({ ...form, co2Level: parseFloat(form.co2Level) })
      setStatus({ ok: true, msg: 'Lectura enviada correctamente.' })
      setForm(f => ({ ...f, sensorId: '', co2Level: '' }))
    } catch (err) {
      setStatus({ ok: false, msg: err.response?.data?.error ?? 'Error al enviar la lectura.' })
    } finally {
      setLoading(false)
    }
  }

  async function loadHistory() {
    setHistLoading(true)
    try {
      const { data } = await getSensorReadings(histSensorId, histMinutes)
      setHistData(data)
      setHistLoaded(true)
    } catch {
      setHistData([])
      setHistLoaded(true)
    } finally {
      setHistLoading(false)
    }
  }

  const chartData = histData.map(r => ({
    time: formatTime(r.recordedAt, { withSeconds: true }),
    co2:  parseFloat(r.co2Level.toFixed(1)),
  }))

  const hasFaulty = histData.some((r, i) => {
    if (i === 0) return false
    return Math.abs(r.co2Level - histData[i - 1].co2Level) > 200
  })

  return (
    <div style={{ minHeight: '100vh', background: '#f0f4f8' }}>
      <Navbar />
      <div style={{ maxWidth: '1100px', margin: '0 auto', padding: '24px 16px' }}>
        <h1 style={{ color: '#1e3a5f', marginBottom: '6px' }}>📡 Gestión de Sensores</h1>
        <p style={{ color: '#6b7280', marginBottom: '28px', fontSize: '0.9rem' }}>
          Envía lecturas manualmente o activa el simulador para generar datos de prueba.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '24px' }}>

          {/* Simulador automático */}
          <div style={{ background: '#fff', borderRadius: '12px', padding: '24px',
                        boxShadow: '0 1px 4px rgba(0,0,0,0.07)' }}>
            <h3 style={{ margin: '0 0 4px', color: '#1e3a5f' }}>🤖 Simulador Automático</h3>
            <p style={{ margin: '0 0 16px', fontSize: '0.82rem', color: '#6b7280' }}>
              Envía lecturas cada 2 segundos desde 6 sensores virtuales
            </p>

            <div style={{ marginBottom: '16px' }}>
              <label style={{ display: 'block', fontSize: '0.82rem', color: '#374151', marginBottom: '8px', fontWeight: 600 }}>
                Escenario de simulación
              </label>
              {SCENARIOS.map(s => (
                <label key={s.value} style={{
                  display: 'flex', alignItems: 'center', gap: '10px',
                  padding: '10px 12px', marginBottom: '6px', borderRadius: '8px', cursor: 'pointer',
                  border: `2px solid ${scenario === s.value ? '#1e3a5f' : '#e5e7eb'}`,
                  background: scenario === s.value ? '#eff6ff' : '#fff'
                }}>
                  <input type="radio" value={s.value} checked={scenario === s.value}
                    onChange={e => setScenario(e.target.value)} disabled={running} />
                  <div>
                    <div style={{ fontSize: '0.88rem', fontWeight: 600 }}>{s.label}</div>
                    <div style={{ fontSize: '0.75rem', color: '#6b7280' }}>{s.desc}</div>
                  </div>
                </label>
              ))}
            </div>

            <div style={{ display: 'flex', gap: '8px' }}>
              <button onClick={running ? stop : start} style={{
                flex: 1, padding: '10px', border: 'none', borderRadius: '8px',
                background: running ? '#dc2626' : '#16a34a',
                color: '#fff', fontWeight: 700, cursor: 'pointer', fontSize: '0.95rem'
              }}>
                {running ? '⏹ Detener simulador' : '▶ Iniciar simulador'}
              </button>
              <button onClick={clearLogs} style={{
                padding: '10px 14px', border: '1px solid #d1d5db', borderRadius: '8px',
                background: '#fff', cursor: 'pointer', fontSize: '0.85rem', color: '#6b7280'
              }}>Limpiar</button>
            </div>

            <div style={{
              marginTop: '16px', height: '200px', overflowY: 'auto',
              background: '#0f172a', borderRadius: '8px', padding: '12px',
              fontFamily: 'monospace', fontSize: '0.75rem'
            }}>
              {logs.length === 0
                ? <span style={{ color: '#475569' }}>Esperando lecturas...</span>
                : logs.map((l, i) => (
                  <div key={i} style={{ color: l.ok ? '#4ade80' : '#f87171', marginBottom: '2px' }}>
                    <span style={{ color: '#64748b' }}>[{l.time}] </span>{l.msg}
                  </div>
                ))
              }
            </div>
          </div>

          {/* Formulario manual + mapa */}
          <div style={{ background: '#fff', borderRadius: '12px', padding: '24px',
                        boxShadow: '0 1px 4px rgba(0,0,0,0.07)' }}>
            <h3 style={{ margin: '0 0 4px', color: '#1e3a5f' }}>✏️ Lectura Manual</h3>
            <p style={{ margin: '0 0 16px', fontSize: '0.82rem', color: '#6b7280' }}>
              Ingresa una lectura específica de un sensor
            </p>

            {/* Mapa interactivo */}
            <div style={{ borderRadius: '8px', overflow: 'hidden', marginBottom: '16px',
                          border: '1px solid #e5e7eb' }}>
              <div style={{ padding: '8px 12px', background: '#f8fafc',
                            fontSize: '0.78rem', color: '#6b7280', borderBottom: '1px solid #e5e7eb' }}>
                📍 Haz clic en el mapa para colocar el sensor — arrastra el marcador para ajustar
              </div>
              <Suspense fallback={
                <div style={{ height: '400px', display: 'flex', alignItems: 'center',
                              justifyContent: 'center', color: '#6b7280' }}>Cargando mapa...</div>
              }>
                <SensorsMap
                  sensors={sensors}
                  selectedLat={form.latitude !== '' ? parseFloat(form.latitude) : null}
                  selectedLng={form.longitude !== '' ? parseFloat(form.longitude) : null}
                  onMapClick={handleMapClick}
                />
              </Suspense>
            </div>

            <form onSubmit={handleSubmit}>
              {status && (
                <div style={{
                  padding: '10px 14px', borderRadius: '8px', marginBottom: '14px', fontSize: '0.85rem',
                  background: status.ok ? '#dcfce7' : '#fee2e2',
                  color:      status.ok ? '#166534' : '#991b1b'
                }}>{status.msg}</div>
              )}

              <Field label="ID del Sensor">
                <input value={form.sensorId} required style={inputStyle} placeholder="MED-ARN-001"
                  onChange={e => setForm(f => ({ ...f, sensorId: e.target.value }))} />
              </Field>

              <Field label="ID de Zona">
                <input value={form.zoneId} required style={inputStyle}
                  placeholder="Ej: ZONA-NORTE, ZONA-INDUSTRIAL-01"
                  onChange={e => setForm(f => ({ ...f, zoneId: e.target.value }))} />
                <div style={{ marginTop: '4px', fontSize: '0.75rem', color: '#6b7280' }}>
                  Identificador libre — no tiene que coincidir con zonas sensibles del mapa
                </div>
              </Field>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                <Field label="Latitud">
                  <input value={form.latitude}
                    onChange={e => setForm(f => ({ ...f, latitude: e.target.value }))}
                    style={{ ...inputStyle, background: form.latitude !== '' ? '#f0fdf4' : '#fff' }}
                    placeholder="Clic en el mapa" />
                </Field>
                <Field label="Longitud">
                  <input value={form.longitude}
                    onChange={e => setForm(f => ({ ...f, longitude: e.target.value }))}
                    style={{ ...inputStyle, background: form.longitude !== '' ? '#f0fdf4' : '#fff' }}
                    placeholder="Clic en el mapa" />
                </Field>
              </div>

              <Field label="Nivel CO2 (µg/m³)">
                <input type="number" min="0" max="1000" step="0.1" required
                  value={form.co2Level} style={inputStyle} placeholder="ej. 175.5"
                  onChange={e => setForm(f => ({ ...f, co2Level: e.target.value }))} />
                <div style={{ marginTop: '6px', display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                  {[50, 120, 160, 200].map(v => (
                    <button key={v} type="button"
                      onClick={() => setForm(f => ({ ...f, co2Level: String(v) }))}
                      style={{ padding: '3px 10px', fontSize: '0.75rem', border: '1px solid #d1d5db',
                               borderRadius: '12px', background: '#f9fafb', cursor: 'pointer',
                               color: v >= 150 ? '#dc2626' : v >= 100 ? '#d97706' : '#374151' }}>
                      {v} µg/m³
                    </button>
                  ))}
                </div>
              </Field>

              <button type="submit" disabled={loading} style={{
                width: '100%', marginTop: '8px', padding: '10px', border: 'none',
                borderRadius: '8px', background: loading ? '#93c5fd' : '#1e3a5f',
                color: '#fff', fontSize: '1rem', cursor: 'pointer', fontWeight: 600
              }}>
                {loading ? 'Enviando...' : 'Enviar Lectura'}
              </button>
            </form>

            <div style={{ marginTop: '20px', padding: '14px', background: '#f8fafc',
                          borderRadius: '8px', fontSize: '0.8rem' }}>
              <p style={{ margin: '0 0 8px', fontWeight: 600, color: '#374151' }}>Referencia de umbrales:</p>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <span style={{ color: '#16a34a' }}>🟢 0–99 µg/m³ — Nivel normal</span>
                <span style={{ color: '#d97706' }}>🟡 100–149 µg/m³ — Advertencia</span>
                <span style={{ color: '#dc2626' }}>🔴 ≥150 µg/m³ — Emergencia (requiere 3 sensores)</span>
              </div>
            </div>
          </div>
        </div>

        {/* Historial de lecturas por sensor */}
        <div style={{ background: '#fff', borderRadius: '12px', padding: '24px',
                      boxShadow: '0 1px 4px rgba(0,0,0,0.07)' }}>
          <h3 style={{ margin: '0 0 4px', color: '#1e3a5f' }}>📊 Historial de Lecturas por Sensor</h3>
          <p style={{ margin: '0 0 16px', fontSize: '0.82rem', color: '#6b7280' }}>
            Detecta patrones de sensores fallidos antes de que disparen mantenimiento.
          </p>

          <div style={{ display: 'flex', gap: '10px', marginBottom: '16px', flexWrap: 'wrap', alignItems: 'flex-end' }}>
            <div>
              <label style={{ display: 'block', fontSize: '0.82rem', color: '#374151', marginBottom: '4px' }}>ID del Sensor</label>
              <input value={histSensorId} onChange={e => setHistSensorId(e.target.value)}
                style={{ ...inputStyle, width: '160px' }} placeholder="MED-ARN-001" />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '0.82rem', color: '#374151', marginBottom: '4px' }}>Ventana</label>
              <select value={histMinutes} onChange={e => setHistMinutes(Number(e.target.value))} style={{ ...inputStyle, width: '130px' }}>
                <option value={15}>Últimos 15 min</option>
                <option value={60}>Última hora</option>
                <option value={180}>Últimas 3 horas</option>
                <option value={360}>Últimas 6 horas</option>
              </select>
            </div>
            <button onClick={loadHistory} disabled={histLoading} style={{
              padding: '9px 20px', background: '#1e3a5f', color: '#fff',
              border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 600
            }}>
              {histLoading ? 'Cargando...' : '🔍 Consultar'}
            </button>
          </div>

          {histLoaded && hasFaulty && (
            <div style={{ padding: '10px 14px', background: '#fef3c7', borderRadius: '8px',
                          marginBottom: '14px', fontSize: '0.85rem', color: '#92400e', fontWeight: 600 }}>
              ⚡ Se detectaron saltos bruscos (&gt;200 µg/m³) — posible sensor fallido
            </div>
          )}

          {histLoaded && histData.length === 0 && (
            <p style={{ color: '#9ca3af', textAlign: 'center', padding: '32px 0' }}>
              Sin lecturas para este sensor en el período seleccionado.
            </p>
          )}

          {histData.length > 0 && (
            <>
              <ResponsiveContainer width="100%" height={220}>
                <LineChart data={chartData}>
                  <XAxis dataKey="time" tick={{ fontSize: 10 }} interval="preserveStartEnd" />
                  <YAxis tick={{ fontSize: 11 }} domain={[0, 'auto']} />
                  <Tooltip formatter={v => [`${v} µg/m³`, 'CO2']} />
                  <ReferenceLine y={150} stroke="#dc2626" strokeDasharray="4 2"
                    label={{ value: 'Emergencia 150', fill: '#dc2626', fontSize: 10 }} />
                  <ReferenceLine y={100} stroke="#f59e0b" strokeDasharray="4 2"
                    label={{ value: 'Advertencia 100', fill: '#f59e0b', fontSize: 10 }} />
                  <Line type="monotone" dataKey="co2" stroke="#3b82f6" strokeWidth={2}
                        dot={{ r: 3 }} activeDot={{ r: 5 }} />
                </LineChart>
              </ResponsiveContainer>

              <div style={{ marginTop: '16px', maxHeight: '220px', overflowY: 'auto',
                            border: '1px solid #f1f5f9', borderRadius: '8px' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.82rem' }}>
                  <thead>
                    <tr style={{ background: '#f8fafc', position: 'sticky', top: 0 }}>
                      {['Hora', 'CO2 (µg/m³)', 'Zona', 'Estado'].map(h => (
                        <th key={h} style={{ padding: '8px 12px', textAlign: 'left',
                                             color: '#6b7280', fontWeight: 600, fontSize: '0.78rem',
                                             textTransform: 'uppercase' }}>{h}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {[...histData].reverse().map((r, i) => {
                      const prev    = histData[histData.length - 2 - i]
                      const faulty  = prev && Math.abs(r.co2Level - prev.co2Level) > 200
                      return (
                        <tr key={i} style={{ borderTop: '1px solid #f1f5f9',
                                             background: faulty ? '#fffbeb' : 'transparent' }}>
                          <td style={td}>{formatTime(r.recordedAt)}</td>
                          <td style={{ ...td, fontWeight: 700,
                                       color: r.co2Level >= 150 ? '#dc2626' : r.co2Level >= 100 ? '#d97706' : '#16a34a' }}>
                            {r.co2Level.toFixed(1)}
                          </td>
                          <td style={td}>{r.zoneId}</td>
                          <td style={td}>
                            {faulty
                              ? <span style={{ color: '#d97706', fontWeight: 600 }}>⚡ Salto detectado</span>
                              : r.co2Level >= 150
                                ? <span style={{ color: '#dc2626' }}>🔴 Crítico</span>
                                : r.co2Level >= 100
                                  ? <span style={{ color: '#d97706' }}>🟡 Advertencia</span>
                                  : <span style={{ color: '#16a34a' }}>🟢 Normal</span>
                            }
                          </td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  )
}

function Field({ label, children }) {
  return (
    <div style={{ marginBottom: '12px' }}>
      <label style={{ display: 'block', marginBottom: '4px', fontSize: '0.82rem',
                      color: '#374151', fontWeight: 500 }}>{label}</label>
      {children}
    </div>
  )
}

const inputStyle = {
  width: '100%', padding: '9px 12px', border: '1px solid #d1d5db',
  borderRadius: '6px', fontSize: '0.9rem', boxSizing: 'border-box'
}
const td = { padding: '8px 12px', color: '#374151' }
