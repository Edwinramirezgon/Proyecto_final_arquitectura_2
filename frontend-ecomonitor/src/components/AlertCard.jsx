import AlertBadge from './AlertBadge'
import { formatDateTime } from '../utils/dates'

export default function AlertCard({ alert }) {
  return (
    <div style={{
      border:       '1px solid #e5e7eb',
      borderRadius: '10px',
      padding:      '16px',
      background:   alert.level === 'CRITICAL' ? '#fff5f5' : '#fff',
      boxShadow:    '0 1px 4px rgba(0,0,0,0.07)'
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <strong style={{ fontSize: '1rem' }}>Zona: {alert.zoneId}</strong>
        <AlertBadge level={alert.level} />
      </div>
      <p style={{ margin: '8px 0 4px', fontSize: '1.4rem', fontWeight: 700, color: '#dc2626' }}>
        {alert.averageCo2.toFixed(1)} µg/m³
      </p>
      {alert.nearestZoneName && (
        <p style={{ margin: 0, fontSize: '0.85rem', color: '#6b7280' }}>
          📍 Zona sensible: <strong>{alert.nearestZoneName}</strong>
        </p>
      )}
      <p style={{ margin: '4px 0 0', fontSize: '0.78rem', color: '#9ca3af' }}>
        {formatDateTime(alert.triggeredAt)}
      </p>
    </div>
  )
}
