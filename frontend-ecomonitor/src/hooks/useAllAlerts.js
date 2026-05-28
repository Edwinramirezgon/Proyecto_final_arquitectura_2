import { useState, useEffect } from 'react'
import { getAllAlerts } from '../api/pollution'

export function useAllAlerts() {
  const [alerts,  setAlerts]  = useState([])
  const [loading, setLoading] = useState(true)
  const [error,   setError]   = useState(null)

  useEffect(() => {
    getAllAlerts()
      .then(({ data }) => setAlerts(data))
      .catch(e => setError(e))
      .finally(() => setLoading(false))
  }, [])

  return { alerts, loading, error }
}
