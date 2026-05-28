import api from './client'

export const getAllSensors = () => api.get('/sensors/management')

export const getSensor = (sensorId) => api.get(`/sensors/management/${sensorId}`)

export const getActiveSensors = () => api.get('/sensors/management/active')

export const getSensorsByZone = (zoneId) => api.get(`/sensors/management/zone/${zoneId}`)

export const createSensor = (sensorData) => api.post('/sensors/management', sensorData)

export const updateSensor = (sensorId, sensorData) => 
  api.put(`/sensors/management/${sensorId}`, sensorData)

export const deleteSensor = (sensorId) => api.delete(`/sensors/management/${sensorId}`)

export const activateSensor = (sensorId) => 
  api.post(`/sensors/management/${sensorId}/activate`)

export const deactivateSensor = (sensorId) => 
  api.post(`/sensors/management/${sensorId}/deactivate`)
