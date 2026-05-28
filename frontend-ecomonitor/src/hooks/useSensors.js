import { useState, useEffect } from 'react'
import { getAllSensors, getActiveSensors, createSensor, updateSensor, deleteSensor, activateSensor, deactivateSensor } from '../api/sensors'

export const useSensors = (activeOnly = false) => {
  const [sensors, setSensors] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const fetchSensors = async () => {
    try {
      setLoading(true)
      const response = activeOnly ? await getActiveSensors() : await getAllSensors()
      setSensors(response.data)
      setError(null)
    } catch (err) {
      setError(err.response?.data?.error || 'Error al cargar sensores')
      console.error('Error fetching sensors:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchSensors()
  }, [activeOnly])

  const addSensor = async (sensorData) => {
    try {
      const response = await createSensor(sensorData)
      setSensors([...sensors, response.data])
      return { success: true, data: response.data }
    } catch (err) {
      const errorMsg = err.response?.data?.error || 'Error al crear sensor'
      setError(errorMsg)
      return { success: false, error: errorMsg }
    }
  }

  const editSensor = async (sensorId, sensorData) => {
    try {
      const response = await updateSensor(sensorId, sensorData)
      setSensors(sensors.map(s => s.sensorId === sensorId ? response.data : s))
      return { success: true, data: response.data }
    } catch (err) {
      const errorMsg = err.response?.data?.error || 'Error al actualizar sensor'
      setError(errorMsg)
      return { success: false, error: errorMsg }
    }
  }

  const removeSensor = async (sensorId) => {
    try {
      await deleteSensor(sensorId)
      setSensors(sensors.filter(s => s.sensorId !== sensorId))
      return { success: true }
    } catch (err) {
      const errorMsg = err.response?.data?.error || 'Error al eliminar sensor'
      setError(errorMsg)
      return { success: false, error: errorMsg }
    }
  }

  const toggleSensorStatus = async (sensorId, activate) => {
    try {
      if (activate) {
        await activateSensor(sensorId)
      } else {
        await deactivateSensor(sensorId)
      }
      setSensors(sensors.map(s => 
        s.sensorId === sensorId ? { ...s, active: activate } : s
      ))
      return { success: true }
    } catch (err) {
      const errorMsg = err.response?.data?.error || 'Error al cambiar estado del sensor'
      setError(errorMsg)
      return { success: false, error: errorMsg }
    }
  }

  return {
    sensors,
    loading,
    error,
    addSensor,
    editSensor,
    removeSensor,
    toggleSensorStatus,
    refresh: fetchSensors
  }
}
