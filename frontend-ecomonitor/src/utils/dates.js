const DEFAULT_LOCALE = 'es-CO'
const DEFAULT_TIME_ZONE = 'America/Bogota'

function hasTimeZone(value) {
  return /[zZ]|[+-]\d{2}:\d{2}$/.test(value)
}

export function parseApiDate(value) {
  if (!value) return null
  const text = String(value)
  const normalized = hasTimeZone(text) ? text : `${text}Z`
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date
}

export function formatDateTime(value, locale = DEFAULT_LOCALE, timeZone = DEFAULT_TIME_ZONE) {
  const date = parseApiDate(value)
  if (!date) return '—'
  return date.toLocaleString(locale, { timeZone })
}

export function formatTime(
  value,
  { locale = DEFAULT_LOCALE, timeZone = DEFAULT_TIME_ZONE, withSeconds = false } = {}
) {
  const date = parseApiDate(value)
  if (!date) return '--:--'
  return date.toLocaleTimeString(locale, {
    timeZone,
    hour: '2-digit',
    minute: '2-digit',
    ...(withSeconds ? { second: '2-digit' } : {}),
  })
}
