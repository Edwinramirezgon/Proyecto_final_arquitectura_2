import { useState, lazy, Suspense } from 'react'
import {
  BarChart, Bar, LineChart, Line, PieChart, Pie, Cell,
  XAxis, YAxis, Tooltip, ReferenceLine, ResponsiveContainer
} from 'recharts'
import Navbar           from '../components/Navbar'
import StatusBar        from '../components/StatusBar'
import AlertCard        from '../components/AlertCard'
import AlertDetailModal from '../components/AlertDetailModal'
import { useActiveAlerts } from '../hooks/useActiveAlerts'
import { useAllAlerts }    from '../hooks/useAllAlerts'
import { useAlertStats }   from '../hooks/useAlertStats'
import { useZones }        from '../hooks/useZones'
import { useAirQuality }   from '../hooks/useAirQuality'
import { useBrowserNotifications } from '../hooks/useBrowserNotifications'

// Lazy load del mapa para evitar SSR issues con Leaflet
const AlertMap = lazy(() => import('../components/AlertMap'))

const LEVEL_COLORS = { CRITICAL: '#dc2626', HIGH: '#f59e0b', MEDIUM: '#3b82f6' }

export default function DashboardPage() {
  const { alerts: active, loading } = useActiveAlerts()
  const { alerts: all }             = useAllAlerts()
  const stats                       = useAlertStats(all)
  const { zones }                   = useZones()
  const { airData }                 = useAirQuality()
  const [selected, setSelected]     = useState(null)

  useBrowserNotifications(active)

  const barData = active.map(a => ({
    zona: a.zoneId, co2: parseFloat(a.averageCo2.toFixed(1)), level: a.level
  }))

  const pieData = Object.entries(stats.byLevel).map(([name, value]) => ({ name, value }))

  return (
    <div style={{ minHeight: '100vh', background: '#f0f4f8' }}>
      <Navbar />
      <StatusBar />

      <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '24px 16px' }}>

        {/* Encabezado */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
          <div>
            <h1 style={{ margin: 0, color: '#1e3a5f', fontSize: '1.6rem' }}>Dashboard Ambiental</h1>
            <p style={{ margin: '4px 0 0', color: '#6b7280', fontSize: '0.88rem' }}>
              Monitoreo en tiempo real — Colombia
            </p>
          </div>
          <span style={{ background: '#dcfce7', color: '#166534', padding: '4px 12px',
                         borderRadius: '20px', fontSize: '0.8rem', fontWeight: 600 }}>
            ● Actualización cada 10s
          </span>
        </div>

        {/* KPIs — 5 tarjetas */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(5,1fr)', gap: '14px', marginBottom: '24px' }}>
          <KpiCard label="Alertas activas"   value={active.length}                                          color="#1e3a5f" icon="🔔" />
          <KpiCard label="Críticas"          value={active.filter(a=>a.level==='CRITICAL').length}          color="#dc2626" icon="🚨" />
          <KpiCard label="Altas"             value={active.filter(a=>a.level==='HIGH').length}              color="#f59e0b" icon="⚠️" />
          <KpiCard label="CO2 máx (µg/m³)"  value={active.length ? Math.max(...active.map(a=>a.averageCo2)).toFixed(1) : '—'} color="#7c3aed" icon="📊" />
          <KpiCard label="Total histórico"   value={all.length}                                             color="#0891b2" icon="📋" />
        </div>

        {/* Mapa de alertas */}
        <ChartCard title="🗺️ Mapa de Alertas — Colombia" style={{ marginBottom: '24px' }}>
          <div style={{ fontSize: '0.78rem', color: '#6b7280', marginBottom: '10px', display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
            <span><span style={{ color: '#dc2626' }}>●</span> CRÍTICO</span>
            <span><span style={{ color: '#f59e0b' }}>●</span> ALTO</span>
            <span><span style={{ color: '#3b82f6' }}>●</span> MEDIO</span>
            <span><span style={{ color: '#7c3aed' }}>●</span> Hospital</span>
            <span><span style={{ color: '#0891b2' }}>●</span> Escuela</span>
            <span><span style={{ color: '#ea580c' }}>●</span> Parque</span>
            <span style={{ borderBottom: '2px dashed #6b7280', paddingBottom: '1px' }}>- - Open-Meteo</span>
            <span><span style={{ color: '#dc2626', opacity: 0.5 }}>⬤</span> Restricción vehicular</span>
          </div>
          <Suspense fallback={<div style={{ height: '420px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#6b7280' }}>Cargando mapa...</div>}>
            <AlertMap alerts={active} zones={zones} airData={airData} />
          </Suspense>
        </ChartCard>

        {/* Gráficas — fila */}
        <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '16px', marginBottom: '24px' }}>

          {/* Barras CO2 activo */}
          <ChartCard title="CO2 promedio por zona — alertas activas (µg/m³)">
            {barData.length === 0
              ? <Empty msg="Sin alertas activas" />
              : <ResponsiveContainer width="100%" height={220}>
                  <BarChart data={barData}>
                    <XAxis dataKey="zona" tick={{ fontSize: 11 }} />
                    <YAxis tick={{ fontSize: 11 }} domain={[0, 300]} />
                    <Tooltip formatter={v => [`${v} µg/m³`, 'CO2']} />
                    <ReferenceLine y={150} stroke="#dc2626" strokeDasharray="4 2"
                      label={{ value: 'Umbral emergencia 150', fill: '#dc2626', fontSize: 10 }} />
                    <ReferenceLine y={100} stroke="#f59e0b" strokeDasharray="4 2"
                      label={{ value: 'Umbral advertencia 100', fill: '#f59e0b', fontSize: 10 }} />
                    <Bar dataKey="co2" radius={[4,4,0,0]}
                      fill="#3b82f6"
                      label={{ position: 'top', fontSize: 10, formatter: v => `${v}` }} />
                  </BarChart>
                </ResponsiveContainer>
            }
          </ChartCard>

          {/* Pie distribución histórica */}
          <ChartCard title="Distribución histórica por nivel">
            {pieData.length === 0
              ? <Empty msg="Sin datos históricos" />
              : <ResponsiveContainer width="100%" height={220}>
                  <PieChart>
                    <Pie data={pieData} dataKey="value" nameKey="name"
                         cx="50%" cy="50%" outerRadius={80} label={({ name, value }) => `${name}: ${value}`}>
                      {pieData.map(entry => (
                        <Cell key={entry.name} fill={LEVEL_COLORS[entry.name] ?? '#94a3b8'} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
            }
          </ChartCard>
        </div>

        {/* Línea de tiempo CO2 */}
        {stats.trend.length > 0 && (
          <ChartCard title="Tendencia de CO2 — últimas 10 alertas" style={{ marginBottom: '24px' }}>
            <ResponsiveContainer width="100%" height={180}>
              <LineChart data={stats.trend}>
                <XAxis dataKey="time" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} domain={[0, 300]} />
                <Tooltip formatter={v => [`${v} µg/m³`, 'CO2']} />
                <ReferenceLine y={150} stroke="#dc2626" strokeDasharray="4 2" />
                <Line type="monotone" dataKey="co2" stroke="#3b82f6" strokeWidth={2}
                      dot={{ r: 4 }} activeDot={{ r: 6 }} />
              </LineChart>
            </ResponsiveContainer>
          </ChartCard>
        )}

        {/* Estadísticas rápidas */}
        {all.length > 0 && (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3,1fr)', gap: '14px', marginBottom: '24px' }}>
            <StatCard label="CO2 promedio histórico" value={`${stats.avgCo2} µg/m³`} />
            <StatCard label="CO2 máximo registrado"  value={`${stats.maxCo2} µg/m³`} />
            <StatCard label="Zona más afectada"       value={stats.topZone} />
          </div>
        )}

        {/* Alertas activas */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
          <h3 style={{ margin: 0, color: '#374151' }}>Alertas activas</h3>
          {active.length > 0 && (
            <span style={{ fontSize: '0.82rem', color: '#6b7280' }}>
              Haz clic en una alerta para ver el detalle
            </span>
          )}
        </div>

        {loading ? (
          <p style={{ color: '#6b7280' }}>Cargando...</p>
        ) : active.length === 0 ? (
          <div style={{ background: '#dcfce7', borderRadius: '10px', padding: '24px',
                        textAlign: 'center', color: '#166534', fontWeight: 600 }}>
            ✅ No hay alertas activas en este momento
          </div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill,minmax(300px,1fr))', gap: '16px' }}>
            {active.map(a => (
              <div key={a.id} onClick={() => setSelected(a)} style={{ cursor: 'pointer' }}>
                <AlertCard alert={a} />
              </div>
            ))}
          </div>
        )}
      </div>

      <AlertDetailModal alert={selected} onClose={() => setSelected(null)} />
    </div>
  )
}

function KpiCard({ label, value, color, icon }) {
  return (
    <div style={{ background: '#fff', borderRadius: '12px', padding: '18px',
                  boxShadow: '0 1px 4px rgba(0,0,0,0.07)', textAlign: 'center' }}>
      <div style={{ fontSize: '1.4rem', marginBottom: '4px' }}>{icon}</div>
      <p style={{ margin: '0 0 4px', fontSize: '0.78rem', color: '#6b7280', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{label}</p>
      <p style={{ margin: 0, fontSize: '1.8rem', fontWeight: 700, color }}>{value}</p>
    </div>
  )
}

function ChartCard({ title, children, style }) {
  return (
    <div style={{ background: '#fff', borderRadius: '12px', padding: '20px',
                  boxShadow: '0 1px 4px rgba(0,0,0,0.07)', ...style }}>
      <h3 style={{ margin: '0 0 16px', color: '#374151', fontSize: '0.9rem', fontWeight: 600 }}>{title}</h3>
      {children}
    </div>
  )
}

function StatCard({ label, value }) {
  return (
    <div style={{ background: '#fff', borderRadius: '12px', padding: '16px',
                  boxShadow: '0 1px 4px rgba(0,0,0,0.07)' }}>
      <p style={{ margin: '0 0 4px', fontSize: '0.78rem', color: '#9ca3af', textTransform: 'uppercase' }}>{label}</p>
      <p style={{ margin: 0, fontSize: '1.2rem', fontWeight: 700, color: '#1e3a5f' }}>{value}</p>
    </div>
  )
}

function Empty({ msg }) {
  return <p style={{ textAlign: 'center', color: '#9ca3af', padding: '40px 0', margin: 0 }}>{msg}</p>
}
