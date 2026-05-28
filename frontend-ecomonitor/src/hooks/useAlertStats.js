import { useMemo } from 'react'
import { formatTime, parseApiDate } from '../utils/dates'

export function useAlertStats(alerts) {
  return useMemo(() => {
    if (!alerts.length) return { byLevel: {}, avgCo2: 0, maxCo2: 0, topZone: '—', trend: [] }

    const byLevel = alerts.reduce((acc, a) => {
      acc[a.level] = (acc[a.level] ?? 0) + 1
      return acc
    }, {})

    const avgCo2 = alerts.reduce((s, a) => s + a.averageCo2, 0) / alerts.length
    const maxCo2 = Math.max(...alerts.map(a => a.averageCo2))

    const zoneCount = alerts.reduce((acc, a) => {
      acc[a.zoneId] = (acc[a.zoneId] ?? 0) + 1
      return acc
    }, {})
    const topZone = Object.entries(zoneCount).sort((a, b) => b[1] - a[1])[0]?.[0] ?? '—'

    // últimas 10 alertas ordenadas por fecha para línea de tiempo
    const trend = [...alerts]
      .sort((a, b) => {
        const aDate = parseApiDate(a.triggeredAt)
        const bDate = parseApiDate(b.triggeredAt)
        return (aDate?.getTime() ?? 0) - (bDate?.getTime() ?? 0)
      })
      .slice(-10)
      .map(a => ({
        time:  formatTime(a.triggeredAt),
        co2:   parseFloat(a.averageCo2.toFixed(1)),
        level: a.level
      }))

    return { byLevel, avgCo2: avgCo2.toFixed(1), maxCo2: maxCo2.toFixed(1), topZone, trend }
  }, [alerts])
}
