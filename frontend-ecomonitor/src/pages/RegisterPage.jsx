import { useState } from 'react'
import { Link } from 'react-router-dom'
import { register } from '../api/auth'

export default function RegisterPage() {
  const [email,   setEmail]   = useState('')
  const [done,    setDone]    = useState(false)
  const [error,   setError]   = useState('')
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await register(email)
      setDone(true)
    } catch (err) {
      const msg = err.response?.data?.error
      setError(msg || 'No se pudo crear la cuenta. Intenta de nuevo.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center',
                  justifyContent: 'center', background: '#f0f9ff' }}>
      <div style={{ background: '#fff', padding: '40px', borderRadius: '12px',
                    boxShadow: '0 4px 20px rgba(0,0,0,0.1)', width: '360px' }}>
        <h2 style={{ margin: '0 0 8px', color: '#1e3a5f' }}>🌿 EcoMonitor</h2>
        <p style={{ margin: '0 0 24px', color: '#6b7280', fontSize: '0.9rem' }}>
          Crear cuenta
        </p>

        {done ? (
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '3rem', marginBottom: '12px' }}>📧</div>
            <p style={{ color: '#166534', background: '#dcfce7', padding: '14px',
                        borderRadius: '8px', fontWeight: 500, lineHeight: 1.5 }}>
              ¡Cuenta creada! Revisa tu correo — te enviamos tu usuario y contraseña temporal para iniciar sesión.
            </p>
            <Link to="/login" style={{ display: 'block', marginTop: '16px',
                                       color: '#1e3a5f', fontWeight: 600, textDecoration: 'none' }}>
              Ir al inicio de sesión →
            </Link>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            {error && (
              <p style={{ color: '#dc2626', background: '#fee2e2', padding: '8px 12px',
                          borderRadius: '6px', fontSize: '0.85rem', margin: '0 0 16px' }}>
                {error}
              </p>
            )}

            <p style={{ color: '#374151', fontSize: '0.88rem', margin: '0 0 16px', lineHeight: 1.5 }}>
              Ingresa tu correo electrónico. Tu usuario y contraseña serán generados automáticamente y enviados a tu correo.
            </p>

            <label style={{ display: 'block', marginBottom: '4px', fontSize: '0.85rem', color: '#374151' }}>
              Correo electrónico
            </label>
            <input type="email" value={email} onChange={e => setEmail(e.target.value)}
              required placeholder="correo@ejemplo.com" style={inputStyle} />

            <button type="submit" disabled={loading} style={{
              width: '100%', marginTop: '16px', padding: '10px',
              background: loading ? '#93c5fd' : '#1e3a5f', color: '#fff',
              border: 'none', borderRadius: '8px', fontSize: '1rem', cursor: 'pointer'
            }}>
              {loading ? 'Creando cuenta...' : 'Crear cuenta'}
            </button>

            <p style={{ textAlign: 'center', marginTop: '16px', fontSize: '0.85rem', color: '#6b7280' }}>
              ¿Ya tienes cuenta?{' '}
              <Link to="/login" style={{ color: '#1e3a5f', fontWeight: 600, textDecoration: 'none' }}>
                Inicia sesión
              </Link>
            </p>
          </form>
        )}
      </div>
    </div>
  )
}

const inputStyle = {
  width: '100%', padding: '9px 12px', border: '1px solid #d1d5db',
  borderRadius: '6px', fontSize: '0.95rem', boxSizing: 'border-box'
}
