import AlertBadge from './AlertBadge'
import { formatDateTime } from '../utils/dates'

export default function AlertDetailModal({ alert, onClose }) {
  if (!alert) return null

  return (
    <div style={{
      position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)',
      display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000
    }} onClick={onClose}>
      <div style={{
        background: '#fff', borderRadius: '14px', padding: '28px',
        width: '480px', maxWidth: '95vw', boxShadow: '0 20px 60px rgba(0,0,0,0.2)'
      }} onClick={e => e.stopPropagation()}>

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' }}>
          <div>
            <h2 style={{ margin: '0 0 6px', color: '#1e3a5f' }}>Detalle de Alerta #{alert.id}</h2>
            <AlertBadge level={alert.level} />
          </div>
          <button onClick={onClose} style={{
            background: 'none', border: 'none', fontSize: '1.4rem',
            cursor: 'pointer', color: '#9ca3af', lineHeight: 1
          }}>✕</button>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
          <InfoRow label="Zona"           value={alert.zoneId} />
          <InfoRow label="CO2 promedio"   value={`${alert.averageCo2.toFixed(1)} µg/m³`} highlight />
          <InfoRow label="Latitud"        value={alert.latitude?.toFixed(4)} />
          <InfoRow label="Longitud"       value={alert.longitude?.toFixed(4)} />
          <InfoRow label="Zona sensible"  value={alert.nearestZoneName ?? 'Ninguna cercana'} />
          <InfoRow label="Estado"         value={alert.active ? '● Activa' : '○ Resuelta'} />
        </div>

        <div style={{ marginTop: '16px', padding: '12px', background: '#f8fafc',
                      borderRadius: '8px', fontSize: '0.85rem', color: '#6b7280' }}>
          <strong>Detectada:</strong> {formatDateTime(alert.triggeredAt)}
        </div>

        {!alert.active && alert.resolvedAt && (
          <div style={{ marginTop: '10px', padding: '12px', background: '#f0fdf4',
                        borderRadius: '8px', fontSize: '0.85rem', color: '#166534' }}>
            <strong>Resuelta:</strong> {formatDateTime(alert.resolvedAt)}{' — '}
            {alert.resolvedReason === 'NORMALIZED'
              ? '✅ Niveles de CO2 volvieron a parámetros normales'
              : '⏱️ Resuelta automáticamente por tiempo máximo (6 horas)'}
          </div>
        )}

        {alert.level === 'CRITICAL' && (
          <div style={{ marginTop: '12px', padding: '12px', background: '#fee2e2',
                        borderRadius: '8px', fontSize: '0.85rem', color: '#991b1b', fontWeight: 600 }}>
            🚨 Zona sensible afectada — Se requiere restricción vehicular inmediata
          </div>
        )}

        <button onClick={onClose} style={{
          width: '100%', marginTop: '20px', padding: '10px',
          background: '#1e3a5f', color: '#fff', border: 'none',
          borderRadius: '8px', cursor: 'pointer', fontSize: '0.95rem'
        }}>Cerrar</button>
      </div>
    </div>
  )
}

function InfoRow({ label, value, highlight }) {
  return (
    <div style={{ padding: '10px', background: '#f8fafc', borderRadius: '8px' }}>
      <p style={{ margin: '0 0 2px', fontSize: '0.75rem', color: '#9ca3af', textTransform: 'uppercase' }}>
        {label}
      </p>
      <p style={{ margin: 0, fontWeight: 600, color: highlight ? '#dc2626' : '#1e3a5f',
                  fontSize: highlight ? '1.1rem' : '0.9rem' }}>
        {value}
      </p>
    </div>
  )
}
