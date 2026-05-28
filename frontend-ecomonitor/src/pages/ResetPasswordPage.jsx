import { useState } from 'react'
import { useNavigate, useSearchParams, Link } from 'react-router-dom'
import { resetPassword } from '../api/auth'
import PasswordPolicy   from '../components/PasswordPolicy'

export default function ResetPasswordPage() {
  const [searchParams]              = useSearchParams()
  const token                       = searchParams.get('token') ?? ''
  const navigate                    = useNavigate()
  const [form,    setForm]          = useState({ newPassword: '', confirm: '' })
  const [loading, setLoading]       = useState(false)
  const [error,   setError]         = useState('')

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    if (form.newPassword !== form.confirm) {
      setError('Las contraseñas no coinciden.')
      return
    }
    setLoading(true)
    try {
      await resetPassword(token, form.newPassword)
      navigate('/login', { state: { passwordReset: true } })
    } catch (err) {
      setError(err.response?.data?.error ?? 'El enlace es inválido o ha expirado.')
    } finally {
      setLoading(false)
    }
  }

  if (!token) {
    return (
      <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center',
                    justifyContent: 'center', background: '#f0f9ff' }}>
        <div style={{ background: '#fff', padding: '40px', borderRadius: '12px',
                      boxShadow: '0 4px 20px rgba(0,0,0,0.1)', width: '360px', textAlign: 'center' }}>
          <p style={{ color: '#dc2626' }}>Enlace inválido. Solicita uno nuevo.</p>
          <Link to="/forgot-password" style={{ color: '#1e3a5f', fontWeight: 600 }}>
            Solicitar enlace
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center',
                  justifyContent: 'center', background: '#f0f9ff' }}>
      <form onSubmit={handleSubmit} style={{ background: '#fff', padding: '40px', borderRadius: '12px',
                    boxShadow: '0 4px 20px rgba(0,0,0,0.1)', width: '360px' }}>
        <h2 style={{ margin: '0 0 8px', color: '#1e3a5f' }}>🌿 EcoMonitor</h2>
        <p style={{ margin: '0 0 24px', color: '#6b7280', fontSize: '0.9rem' }}>
          Crear nueva contraseña
        </p>

        {error && (
          <p style={{ color: '#dc2626', background: '#fee2e2', padding: '8px 12px',
                      borderRadius: '6px', fontSize: '0.85rem', margin: '0 0 16px' }}>{error}</p>
        )}

        {[
          { label: 'Nueva contraseña',        name: 'newPassword' },
          { label: 'Confirmar nueva contraseña', name: 'confirm'  },
        ].map(({ label, name }) => (
          <div key={name}>
            <label style={{ display: 'block', marginBottom: '4px', fontSize: '0.85rem', color: '#374151' }}>
              {label}
            </label>
            <input type="password" value={form[name]} required
              onChange={e => setForm(f => ({ ...f, [name]: e.target.value }))}
              style={{ ...inputStyle, marginBottom: '12px' }} />
          </div>
        ))}
        <PasswordPolicy password={form.newPassword} />

        <button type="submit" disabled={loading} style={{
          width: '100%', marginTop: '4px', padding: '10px',
          background: loading ? '#93c5fd' : '#1e3a5f', color: '#fff',
          border: 'none', borderRadius: '8px', fontSize: '1rem', cursor: 'pointer'
        }}>
          {loading ? 'Guardando...' : 'Guardar nueva contraseña'}
        </button>
      </form>
    </div>
  )
}

const inputStyle = {
  width: '100%', padding: '9px 12px', border: '1px solid #d1d5db',
  borderRadius: '6px', fontSize: '0.95rem', boxSizing: 'border-box'
}
