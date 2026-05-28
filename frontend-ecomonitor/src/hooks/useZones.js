import { useState, useEffect, useCallback } from 'react'
import { getAllZones, createZone, updateZone, deleteZone } from '../api/zones'

export function useZones() {
  const [zones,   setZones]   = useState([])
  const [loading, setLoading] = useState(true)
  const [error,   setError]   = useState(null)

  const load = useCallback(() => {
    setLoading(true)
    getAllZones()
      .then(({ data }) => setZones(data))
      .catch(e => setError(e))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { load() }, [load])

  const add    = async (zone)    => { await createZone(zone);     load() }
  const edit   = async (id, z)   => { await updateZone(id, z);   load() }
  const remove = async (id)      => { await deleteZone(id);       load() }

  return { zones, loading, error, add, edit, remove }
}
