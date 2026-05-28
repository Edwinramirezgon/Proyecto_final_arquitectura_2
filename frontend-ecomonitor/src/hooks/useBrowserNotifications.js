import { useEffect, useRef } from 'react'

export function useBrowserNotifications(alerts) {
  const knownIds = useRef(new Set())

  useEffect(() => {
    if (!('Notification' in window)) return

    if (Notification.permission === 'default') {
      Notification.requestPermission()
    }

    if (Notification.permission !== 'granted') return

    alerts
      .filter(a => a.level === 'CRITICAL' && !knownIds.current.has(a.id))
      .forEach(a => {
        knownIds.current.add(a.id)
        new Notification('🚨 Emergencia Ambiental — EcoMonitor', {
          body: `Zona ${a.zoneId} · CO2: ${a.averageCo2.toFixed(1)} µg/m³\nRestricción vehicular activa.`,
          icon: '/favicon.ico',
          tag:  `alert-${a.id}`,
        })
      })
  }, [alerts])
}
