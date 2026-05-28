import { useState, useEffect, useMemo } from 'react'
import Navbar from '../components/Navbar'
import AlertBadge from '../components/AlertBadge'
import { getNotifications } from '../api/notifications'
import { formatDateTime, parseApiDate } from '../utils/dates'

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState([])
  const [loading,       setLoading]       = useState(true)
  const [filter,        setFilter]        = useState('ALL')
  const [search,        setSearch]        = useState('')

  useEffect(() => {
    getNotifications()
      .then(({ data }) => setNotifications(data))
      .catch(() => setNotifications([]))
      .finally(() => setLoading(false))
  }, [])

  const filtered = useMemo(() => {
    let list = filter === 'ALL' ? notifications : notifications.filter(n => n.status === filter)
    if (search.trim())
      list = list.filter(n => n.zoneId.toLowerCase().includes(search.toLowerCase()))
    return [...list].sort((a, b) => {
      const aDate = parseApiDate(a.triggeredAt)
      const bDate = parseApiDate(b.triggeredAt)
      return (bDate?.getTime() ?? 0) - (aDate?.getTime() ?? 0)
    })
  }, [notifications, filter, search])

  const sent   = notifications.filter(n => n.status === 'SENT').length
  const failed = notifications.filter(n => n.status === 'FAILED').length

  return (
    <div style={{ minHeight: '100vh', background: '#f0f4f8' }}>
      <Navbar />
      <div style={{ maxWidth: '1100px', margin: '0 auto', padding: '24px 16px' }}>

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '24px' }}>
          <div>
            <h1 style={{ margin: 0, color: '#1e3a5f' }}>📬 Logs de Notificaciones</h1>
            <p style={{ margin: '4px 0 0', color: '#6b7280', fontSize: '0.88rem' }}>
              Trazabilidad de emails enviados por el servicio de alertas
            </p>
          </div>
        </div>

        {/* KPIs */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', gap: '14px', marginBottom: '24px' }}>
          <KpiCard label="Total enviadas"  value={notifications.length} color="#1e3a5f" />
          <KpiCard label="Entregadas"      value={sent}                 color="#16a34a" />
          <KpiCard label="Fallidas"        value={failed}               color="#dc2626" />
        </div>

        {/* Filtros */}
        <div style={{ display: 'flex', gap: '10px', marginBottom: '16px', flexWrap: 'wrap', alignItems: 'center' }}>
          <div style={{ display: 'flex', gap: '6px' }}>
            {['ALL', 'SENT', 'FAILED'].map(f => (
              <button key={f} onClick={() => setFilter(f)} style={{
                padding: '6px 14px', borderRadius: '20px', cursor: 'pointer', fontSize: '0.82rem',
                border: '1px solid #d1d5db', fontWeight: filter === f ? 700 : 400,
                background: filter === f ? '#1e3a5f' : '#fff',
                color:      filter === f ? '#fff'    : '#374151'
              }}>
                {f === 'ALL' ? 'Todas' : f === 'SENT' ? '✅ Entregadas' : '❌ Fallidas'}
              </button>
            ))}
          </div>
          <input value={search} onChange={e => setSearch(e.target.value)}
            placeholder="Buscar por zona..." style={{
              padding: '6px 12px', border: '1px solid #d1d5db', borderRadius: '20px',
              fontSize: '0.85rem', outline: 'none', minWidth: '180px'
            }} />
          <span style={{ marginLeft: 'auto', fontSize: '0.82rem', color: '#6b7280' }}>
            {filtered.length} resultado(s)
          </span>
        </div>

        {loading ? (
          <p style={{ color: '#6b7280' }}>Cargando...</p>
        ) : (
          <div style={{ background: '#fff', borderRadius: '12px', overflow: 'hidden',
                        boxShadow: '0 1px 4px rgba(0,0,0,0.07)' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr style={{ background: '#f8fafc' }}>
                  {['ID', 'Zona', 'Nivel', 'CO2 (µg/m³)', 'Zona Sensible', 'Fecha', 'Estado'].map(h => (
                    <th key={h} style={{ padding: '11px 14px', textAlign: 'left',
                                         fontSize: '0.78rem', color: '#6b7280',
                                         textTransform: 'uppercase', letterSpacing: '0.04em' }}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {filtered.map((n, i) => (
                  <tr key={n.id} style={{
                    borderTop: '1px solid #f1f5f9',
                    background: n.status === 'FAILED' ? '#fff5f5' : i % 2 === 0 ? '#fff' : '#fafafa'
                  }}>
                    <td style={td}><span style={{ color: '#9ca3af' }}>#{n.id}</span></td>
                    <td style={{ ...td, fontWeight: 600 }}>{n.zoneId}</td>
                    <td style={td}><AlertBadge level={n.level} /></td>
                    <td style={{ ...td, fontWeight: 700,
                                 color: n.averageCo2 >= 150 ? '#dc2626' : n.averageCo2 >= 100 ? '#d97706' : '#374151' }}>
                      {n.averageCo2.toFixed(1)}
                    </td>
                    <td style={td}>{n.nearestZoneName || <span style={{ color: '#d1d5db' }}>—</span>}</td>
                    <td style={{ ...td, fontSize: '0.8rem', color: '#6b7280' }}>
                      {formatDateTime(n.triggeredAt)}
                    </td>
                    <td style={td}>
                      <span style={{
                        padding: '3px 10px', borderRadius: '12px', fontSize: '0.78rem', fontWeight: 700,
                        background: n.status === 'SENT' ? '#dcfce7' : '#fee2e2',
                        color:      n.status === 'SENT' ? '#166534' : '#991b1b'
                      }}>
                        {n.status === 'SENT' ? '✅ Entregada' : '❌ Fallida'}
                      </span>
                    </td>
                  </tr>
                ))}
                {filtered.length === 0 && (
                  <tr><td colSpan={7} style={{ padding: '32px', textAlign: 'center', color: '#9ca3af' }}>
                    Sin notificaciones para este filtro.
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

function KpiCard({ label, value, color }) {
  return (
    <div style={{ background: '#fff', borderRadius: '12px', padding: '18px',
                  boxShadow: '0 1px 4px rgba(0,0,0,0.07)', textAlign: 'center' }}>
      <p style={{ margin: '0 0 4px', fontSize: '0.78rem', color: '#6b7280',
                  textTransform: 'uppercase', letterSpacing: '0.05em' }}>{label}</p>
      <p style={{ margin: 0, fontSize: '2rem', fontWeight: 700, color }}>{value}</p>
    </div>
  )
}

const td = { padding: '11px 14px', fontSize: '0.85rem', color: '#374151' }
