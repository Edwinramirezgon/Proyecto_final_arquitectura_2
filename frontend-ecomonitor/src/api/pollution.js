import api from './client'

export const getActiveAlerts = () => api.get('/alerts/active')
export const getAllAlerts    = () => api.get('/alerts')

export const postReading        = (reading)              => api.post('/sensors/readings', reading)
export const getSensorReadings  = (sensorId, minutes=60) => api.get(`/sensors/${sensorId}/readings?minutes=${minutes}`)
