import { useState, useRef, useEffect } from 'react'
import { postReading } from '../api/pollution'
import { getActiveSensors } from '../api/sensors'

export function useSensorSimulator() {
  const [running,  setRunning]  = useState(false)
  const [logs,     setLogs]     = useState([])
  const [scenario, setScenario] = useState('normal')
  const [sensors,  setSensors]  = useState([])
  const [loading,  setLoading]  = useState(true)
  const intervalRef = useRef(null)

  // Cargar sensores activos de la BD
  useEffect(() => {
    const fetchSensors = async () => {
      try {
        const response = await getActiveSensors()
        setSensors(response.data)
        setLoading(false)
      } catch (err) {
        console.error('Error cargando sensores:', err)
        addLog('Error al cargar sensores de la BD', false)
        setLoading(false)
      }
    }
    fetchSensors()
  }, [])

  function addLog(msg, ok) {
    setLogs(prev => [{ msg, ok, time: new Date().toLocaleTimeString('es-CO') }, ...prev].slice(0, 50))
  }

  function generateCo2(scenario) {
    switch (scenario) {
      case 'critical':   return 155 + Math.random() * 60
      case 'emergency':  return 155 + Math.random() * 60
      case 'warning':    return 100 + Math.random() * 45
      case 'faulty':     return Math.random() > 0.3 ? 20 + Math.random() * 30 : 450 + Math.random() * 100
      default:           return 20  + Math.random() * 70
    }
  }

  function start() {
    if (running || sensors.length === 0) return
    setRunning(true)

    const byZone = sensors.reduce((acc, s) => {
      ;(acc[s.zoneId] = acc[s.zoneId] || []).push(s)
      return acc
    }, {})
    const zonesWithThree  = Object.values(byZone).filter(g => g.length >= 3)
    const hospitalZone    = byZone['ZONA-HOSPITAL-001'] ?? []

    intervalRef.current = setInterval(async () => {
      let targets
      if (scenario === 'critical' && hospitalZone.length >= 3) {
        // CRITICAL: siempre la zona hospital (prioridad 10 → nivel CRITICAL garantizado)
        targets = hospitalZone
      } else if ((scenario === 'emergency' || scenario === 'critical') && zonesWithThree.length > 0) {
        targets = zonesWithThree[Math.floor(Math.random() * zonesWithThree.length)]
      } else {
        targets = [sensors[Math.floor(Math.random() * sensors.length)]]
      }

      for (const sensor of targets) {
        const co2Level = parseFloat(generateCo2(scenario).toFixed(1))
        try {
          await postReading({
            sensorId: sensor.sensorId,
            zoneId: sensor.zoneId,
            latitude: sensor.latitude,
            longitude: sensor.longitude,
            co2Level
          })
          addLog(`${sensor.sensorId} → ${sensor.zoneId}: ${co2Level} µg/m³`, true)
        } catch (err) {
          const msg = err.response?.data?.error ?? 'Error de conexión'
          addLog(`${sensor.sensorId}: ${msg}`, false)
        }
      }
    }, 2000)
  }

  function stop() {
    clearInterval(intervalRef.current)
    setRunning(false)
  }

  function clearLogs() { setLogs([]) }

  return { running, logs, scenario, setScenario, start, stop, clearLogs, sensors, loading }
}
