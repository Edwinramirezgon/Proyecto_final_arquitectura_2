import { useState, useEffect } from 'react'
import { getActiveAlerts } from '../api/pollution'

export function useActiveAlerts() {
  const [alerts,  setAlerts]  = useState([])
  const [loading, setLoading] = useState(true)
  const [error,   setError]   = useState(null)

  useEffect(() => {
    let cancelled = false

    async function fetch() {
      try {
        const { data } = await getActiveAlerts()
        if (!cancelled) setAlerts(data)
      } catch (e) {
        if (!cancelled) setError(e)
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    fetch()
    const interval = setInterval(fetch, 10000)
    return () => { cancelled = true; clearInterval(interval) }
  }, [])

  return { alerts, loading, error }
}
