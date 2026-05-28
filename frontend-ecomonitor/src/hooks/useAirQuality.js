import { useState, useEffect } from 'react'
import { getColombiaAirQuality } from '../api/subscriptions'

export function useAirQuality() {
  const [airData,  setAirData]  = useState([])
  const [loading,  setLoading]  = useState(true)

  useEffect(() => {
    getColombiaAirQuality()
      .then(({ data }) => setAirData(data))
      .catch(() => setAirData([]))
      .finally(() => setLoading(false))
  }, [])

  return { airData, loading }
}
