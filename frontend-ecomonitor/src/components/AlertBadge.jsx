const COLORS = {
  CRITICAL: { bg: '#fee2e2', color: '#991b1b', label: '🚨 CRÍTICO' },
  HIGH:     { bg: '#fef3c7', color: '#92400e', label: '⚠️ ALTO' },
  MEDIUM:   { bg: '#dbeafe', color: '#1e40af', label: '🔵 MEDIO' },
}

export default function AlertBadge({ level }) {
  const style = COLORS[level] ?? { bg: '#f3f4f6', color: '#374151', label: level }
  return (
    <span style={{
      background:   style.bg,
      color:        style.color,
      padding:      '2px 10px',
      borderRadius: '12px',
      fontWeight:   700,
      fontSize:     '0.78rem'
    }}>
      {style.label}
    </span>
  )
}
