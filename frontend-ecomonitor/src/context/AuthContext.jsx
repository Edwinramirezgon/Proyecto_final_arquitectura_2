import { createContext, useContext, useState, useEffect } from 'react'
import { login as apiLogin, logout as apiLogout } from '../api/auth'
import api from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('token')
    const stored = localStorage.getItem('user')
    if (!token || !stored) { setLoading(false); return }
    api.get('/auth/validate')
      .then(() => setUser(JSON.parse(stored)))
      .catch(() => localStorage.clear())
      .finally(() => setLoading(false))
  }, [])

  async function signIn(username, password) {
    const { data } = await apiLogin(username, password)
    localStorage.setItem('token',        data.token)
    localStorage.setItem('refreshToken', data.refreshToken)
    const profile = { username: data.username, role: data.role }
    localStorage.setItem('user', JSON.stringify(profile))
    setUser(profile)
  }

  async function signOut() {
    try { await apiLogout() } catch { /* ignorar */ }
    localStorage.clear()
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, signIn, signOut, loading }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
