import api from './client'

export const login    = (username, password) =>
  api.post('/auth/login', { username, password })

export const register = (email) =>
  api.post('/auth/register', { email })

export const logout   = () =>
  api.post('/auth/logout')

export const changePassword = (currentPassword, newPassword) =>
  api.post('/auth/change-password', { currentPassword, newPassword })

export const forgotPassword = (email) =>
  api.post('/auth/forgot-password', { email })

export const resetPassword = (token, newPassword) =>
  api.post('/auth/reset-password', { token, newPassword })
