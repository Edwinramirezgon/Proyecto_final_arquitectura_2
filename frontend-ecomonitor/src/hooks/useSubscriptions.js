import { useState, useEffect, useCallback } from 'react'
import { getSubscriptions, subscribe, unsubscribe } from '../api/subscriptions'
import { useAuth } from '../context/AuthContext'

export function useSubscriptions() {
  const { user }                    = useAuth()
  const [subscriptions, setSubs]    = useState([])
  const [loading,       setLoading] = useState(true)
  const [error,         setError]   = useState(null)

  const load = useCallback(() => {
    if (!user) return
    setLoading(true)
    getSubscriptions(user.username)
      .then(({ data }) => setSubs(data))
      .catch(e => setError(e))
      .finally(() => setLoading(false))
  }, [user])

  useEffect(() => { load() }, [load])

  const isSubscribed = (zoneId) => subscriptions.some(s => s.zoneId === zoneId)

  const toggle = async (zoneId) => {
    if (isSubscribed(zoneId)) {
      await unsubscribe(user.username, zoneId)
    } else {
      await subscribe(user.username, zoneId)
    }
    load()
  }

  return { subscriptions, loading, error, isSubscribed, toggle }
}
