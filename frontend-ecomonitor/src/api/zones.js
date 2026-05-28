import api from './client'

export const getAllZones  = ()       => api.get('/zones')
export const createZone  = (zone)   => api.post('/zones', zone)
export const updateZone  = (id, z)  => api.put(`/zones/${id}`, z)
export const deleteZone  = (id)     => api.delete(`/zones/${id}`)
