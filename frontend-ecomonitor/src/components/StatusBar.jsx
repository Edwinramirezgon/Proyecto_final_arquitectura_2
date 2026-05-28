import { useActiveAlerts } from '../hooks/useActiveAlerts'

export default function StatusBar() {
  const { alerts } = useActiveAlerts()
  const critical   = alerts.filter(a => a.level === 'CRITICAL').length
  const high       = alerts.filter(a => a.level === 'HIGH').length

  if (!alerts.length) return (
    <div style={{ background: '#dcfce7', color: '#166534', padding: '6px 24px',
                  fontSize: '0.82rem', fontWeight: 600, textAlign: 'center' }}>
      ✅ Sistema operando con normalidad — Sin alertas activas
    </div>
  )

  if (critical > 0) return (
    <div style={{
      background: '#dc2626', color: '#fff', padding: '8px 24px',
      fontSize: '0.85rem', fontWeight: 700, textAlign: 'center',
      animation: 'pulse 1.5s infinite'
    }}>
      🚨 EMERGENCIA AMBIENTAL ACTIVA — {critical} zona(s) crítica(s) — Restricción vehicular en efecto
      <style>{`@keyframes pulse { 0%,100%{opacity:1} 50%{opacity:0.75} }`}</style>
    </div>
  )

  return (
    <div style={{ background: '#f59e0b', color: '#fff', padding: '6px 24px',
                  fontSize: '0.82rem', fontWeight: 600, textAlign: 'center' }}>
      ⚠️ Alerta ambiental activa — {high} zona(s) con niveles elevados de CO2
    </div>
  )
}
