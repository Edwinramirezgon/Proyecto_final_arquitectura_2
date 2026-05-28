import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function LoginPage() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error,    setError]    = useState('')
  const [loading,  setLoading]  = useState(false)
  const { signIn }  = useAuth()
  const navigate    = useNavigate()

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await signIn(username, password)
      navigate('/dashboard')
    } catch (err) {
      const errorMsg = err.response?.data?.error || err.response?.data?.mensaje || 'Credenciales inválidas.'
      setError(errorMsg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center',
                  justifyContent: 'center', background: '#f0f9ff' }}>
      <form onSubmit={handleSubmit} style={{
        background: '#fff', padding: '40px', borderRadius: '12px',
        boxShadow: '0 4px 20px rgba(0,0,0,0.1)', width: '340px'
      }}>
        <h2 style={{ margin: '0 0 8px', color: '#1e3a5f' }}>🌿 EcoMonitor</h2>
        <p style={{ margin: '0 0 24px', color: '#6b7280', fontSize: '0.9rem' }}>
          Red de Alerta Ambiental
        </p>
        {error && (
          <p style={{ color: '#dc2626', background: '#fee2e2', padding: '8px 12px',
                      borderRadius: '6px', fontSize: '0.85rem', margin: '0 0 16px' }}>
            {error}
          </p>
        )}
        <label style={{ display: 'block', marginBottom: '4px', fontSize: '0.85rem', color: '#374151' }}>
          Usuario
        </label>
        <input value={username} onChange={e => setUsername(e.target.value)}
          required style={inputStyle} placeholder="admin" />
        <label style={{ display: 'block', margin: '12px 0 4px', fontSize: '0.85rem', color: '#374151' }}>
          Contraseña
        </label>
        <input type="password" value={password} onChange={e => setPassword(e.target.value)}
          required style={inputStyle} placeholder="••••••••" />
        <button type="submit" disabled={loading} style={{
          width: '100%', marginTop: '20px', padding: '10px',
          background: loading ? '#93c5fd' : '#1e3a5f', color: '#fff',
          border: 'none', borderRadius: '8px', fontSize: '1rem', cursor: 'pointer'
        }}>
          {loading ? 'Ingresando...' : 'Ingresar'}
        </button>
        <p style={{ textAlign: 'center', marginTop: '16px', fontSize: '0.85rem', color: '#6b7280' }}>
          <Link to="/forgot-password" style={{ color: '#6b7280', textDecoration: 'none' }}>
            ¿Olvidaste tu contraseña?
          </Link>
        </p>
        <p style={{ textAlign: 'center', marginTop: '8px', fontSize: '0.85rem', color: '#6b7280' }}>
          ¿No tienes cuenta?{' '}
          <Link to="/register" style={{ color: '#1e3a5f', fontWeight: 600 }}>Regístrate</Link>
        </p>
      </form>
    </div>
  )
}

const inputStyle = {
  width: '100%', padding: '9px 12px', border: '1px solid #d1d5db',
  borderRadius: '6px', fontSize: '0.95rem', boxSizing: 'border-box'
}
