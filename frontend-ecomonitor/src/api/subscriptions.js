import api from './client'

export const getSubscriptions  = (username)         => api.get(`/users/${username}/subscriptions`)
export const subscribe         = (username, zoneId) => api.post(`/users/${username}/subscriptions`, { zoneId })
export const unsubscribe       = (username, zoneId) => api.delete(`/users/${username}/subscriptions/${zoneId}`)
export const getColombiaAirQuality = ()             => api.get('/air-quality/colombia')
