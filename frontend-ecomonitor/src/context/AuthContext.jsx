import { createContext, useContext, useState } from 'react'
import { login as apiLogin, logout as apiLogout } from '../api/auth'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const u = localStorage.getItem('user')
    return u ? JSON.parse(u) : null
  })

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
    <AuthContext.Provider value={{ user, signIn, signOut }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
