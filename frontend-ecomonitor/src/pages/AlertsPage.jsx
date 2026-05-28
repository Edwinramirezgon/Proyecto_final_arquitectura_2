import { useState, useMemo } from 'react'
import Navbar           from '../components/Navbar'
import AlertBadge       from '../components/AlertBadge'
import AlertDetailModal from '../components/AlertDetailModal'
import { useAllAlerts }  from '../hooks/useAllAlerts'
import { useAlertStats } from '../hooks/useAlertStats'
import { formatDateTime, parseApiDate } from '../utils/dates'

export default function AlertsPage() {
  const { alerts, loading }     = useAllAlerts()
  const stats                   = useAlertStats(alerts)
  const [filter,   setFilter]   = useState('ALL')
  const [search,   setSearch]   = useState('')
  const [sortDesc, setSortDesc] = useState(true)
  const [selected, setSelected] = useState(null)

  const filtered = useMemo(() => {
    let list = filter === 'ALL' ? alerts : alerts.filter(a => a.level === filter)
    if (search.trim())
      list = list.filter(a => a.zoneId.toLowerCase().includes(search.toLowerCase()) ||
                               (a.nearestZoneName ?? '').toLowerCase().includes(search.toLowerCase()))
    return [...list].sort((a, b) => {
      const aDate = parseApiDate(a.triggeredAt)
      const bDate = parseApiDate(b.triggeredAt)
      const diff = (aDate?.getTime() ?? 0) - (bDate?.getTime() ?? 0)
      return sortDesc ? -diff : diff
    })
  }, [alerts, filter, search, sortDesc])

  function exportCsv() {
    const header = 'ID,Zona,CO2,Nivel,Zona Sensible,Fecha,Estado'
    const rows   = filtered.map(a =>
      `${a.id},${a.zoneId},${a.averageCo2.toFixed(1)},${a.level},${a.nearestZoneName ?? ''},${formatDateTime(a.triggeredAt)},${a.active ? 'Activa' : 'Resuelta'}`
    )
    const blob = new Blob([[header, ...rows].join('\n')], { type: 'text/csv' })
    const url  = URL.createObjectURL(blob)
    const a    = document.createElement('a')
    a.href = url; a.download = 'alertas-ecomonitor.csv'; a.click()
    URL.revokeObjectURL(url)
  }

  return (
    <div style={{ minHeight: '100vh', background: '#f0f4f8' }}>
      <Navbar />
      <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '24px 16px' }}>

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '24px' }}>
          <div>
            <h1 style={{ margin: 0, color: '#1e3a5f' }}>📋 Historial de Alertas</h1>
            <p style={{ margin: '4px 0 0', color: '#6b7280', fontSize: '0.88rem' }}>
              {alerts.length} alertas registradas en total
            </p>
          </div>
          <button onClick={exportCsv} style={{
            padding: '8px 16px', background: '#fff', border: '1px solid #d1d5db',
            borderRadius: '8px', cursor: 'pointer', fontSize: '0.85rem', color: '#374151',
            display: 'flex', alignItems: 'center', gap: '6px'
          }}>
            ⬇️ Exportar CSV
          </button>
        </div>

        {/* Estadísticas rápidas */}
        {alerts.length > 0 && (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4,1fr)', gap: '12px', marginBottom: '20px' }}>
            {[
              { label: 'Total',      value: alerts.length,                                        color: '#1e3a5f' },
              { label: 'Críticas',   value: stats.byLevel['CRITICAL'] ?? 0,                       color: '#dc2626' },
              { label: 'Altas',      value: stats.byLevel['HIGH']     ?? 0,                       color: '#f59e0b' },
              { label: 'Zona + afectada', value: stats.topZone,                                   color: '#7c3aed' },
            ].map(s => (
              <div key={s.label} style={{ background: '#fff', borderRadius: '10px', padding: '14px',
                                          boxShadow: '0 1px 3px rgba(0,0,0,0.06)', textAlign: 'center' }}>
                <p style={{ margin: '0 0 2px', fontSize: '0.75rem', color: '#9ca3af', textTransform: 'uppercase' }}>{s.label}</p>
                <p style={{ margin: 0, fontSize: '1.4rem', fontWeight: 700, color: s.color }}>{s.value}</p>
              </div>
            ))}
          </div>
        )}

        {/* Filtros y búsqueda */}
        <div style={{ display: 'flex', gap: '10px', marginBottom: '16px', flexWrap: 'wrap', alignItems: 'center' }}>
          <div style={{ display: 'flex', gap: '6px' }}>
            {['ALL', 'CRITICAL', 'HIGH', 'MEDIUM'].map(lvl => (
              <button key={lvl} onClick={() => setFilter(lvl)} style={{
                padding: '6px 14px', borderRadius: '20px', cursor: 'pointer', fontSize: '0.82rem',
                border: '1px solid #d1d5db', fontWeight: filter === lvl ? 700 : 400,
                background: filter === lvl ? '#1e3a5f' : '#fff',
                color:      filter === lvl ? '#fff'    : '#374151'
              }}>
                {lvl === 'ALL' ? 'Todas' : lvl}
              </button>
            ))}
          </div>
          <input value={search} onChange={e => setSearch(e.target.value)}
            placeholder="Buscar por zona..." style={{
              padding: '6px 12px', border: '1px solid #d1d5db', borderRadius: '20px',
              fontSize: '0.85rem', outline: 'none', minWidth: '180px'
            }} />
          <button onClick={() => setSortDesc(d => !d)} style={{
            padding: '6px 14px', border: '1px solid #d1d5db', borderRadius: '20px',
            background: '#fff', cursor: 'pointer', fontSize: '0.82rem', color: '#374151'
          }}>
            {sortDesc ? '↓ Más recientes' : '↑ Más antiguas'}
          </button>
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
                <tr style={{ background: '#f1f5f9' }}>
                  {['ID', 'Zona', 'CO2 (µg/m³)', 'Nivel', 'Zona Sensible', 'Fecha', 'Estado', ''].map(h => (
                    <th key={h} style={{ padding: '12px 14px', textAlign: 'left',
                                         fontSize: '0.78rem', color: '#6b7280', fontWeight: 600,
                                         textTransform: 'uppercase', letterSpacing: '0.04em' }}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {filtered.map((a, i) => (
                  <tr key={a.id} style={{
                    borderTop: '1px solid #f1f5f9',
                    background: a.level === 'CRITICAL' ? '#fff5f5' : i % 2 === 0 ? '#fff' : '#fafafa',
                    transition: 'background 0.15s'
                  }}>
                    <td style={td}><span style={{ color: '#9ca3af' }}>#{a.id}</span></td>
                    <td style={{ ...td, fontWeight: 600 }}>{a.zoneId}</td>
                    <td style={{ ...td, fontWeight: 700, color: a.averageCo2 >= 150 ? '#dc2626' : a.averageCo2 >= 100 ? '#d97706' : '#374151' }}>
                      {a.averageCo2.toFixed(1)}
                    </td>
                    <td style={td}><AlertBadge level={a.level} /></td>
                    <td style={td}>{a.nearestZoneName ?? <span style={{ color: '#d1d5db' }}>—</span>}</td>
                    <td style={{ ...td, fontSize: '0.8rem', color: '#6b7280' }}>
                      {formatDateTime(a.triggeredAt)}
                    </td>
                    <td style={td}>
                      <span style={{ color: a.active ? '#16a34a' : '#9ca3af', fontWeight: 600, fontSize: '0.82rem' }}>
                        {a.active ? '● Activa' : a.resolvedReason === 'NORMALIZED' ? '✅ Normalizada' : '⏱️ Timeout'}
                      </span>
                    </td>
                    <td style={td}>
                      <button onClick={() => setSelected(a)} style={{
                        padding: '4px 10px', background: '#eff6ff', color: '#1d4ed8',
                        border: 'none', borderRadius: '6px', cursor: 'pointer', fontSize: '0.78rem'
                      }}>Ver</button>
                    </td>
                  </tr>
                ))}
                {filtered.length === 0 && (
                  <tr><td colSpan={8} style={{ padding: '32px', textAlign: 'center', color: '#9ca3af' }}>
                    Sin alertas para este filtro.
                  </td></tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <AlertDetailModal alert={selected} onClose={() => setSelected(null)} />
    </div>
  )
}

const td = { padding: '11px 14px', fontSize: '0.85rem', color: '#374151' }
