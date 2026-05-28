import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Navbar() {
  const { user, signOut } = useAuth()
  const navigate          = useNavigate()

  async function handleLogout() {
    await signOut()
    navigate('/login')
  }

  return (
    <nav style={{
      background:    '#1e3a5f',
      color:         '#fff',
      padding:       '0 24px',
      height:        '56px',
      display:       'flex',
      alignItems:    'center',
      justifyContent:'space-between'
    }}>
      <span style={{ fontWeight: 700, fontSize: '1.1rem' }}>🌿 EcoMonitor</span>
      <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
        <Link to="/dashboard" style={{ color: '#93c5fd', textDecoration: 'none' }}>Dashboard</Link>
        <Link to="/alerts"    style={{ color: '#93c5fd', textDecoration: 'none' }}>Alertas</Link>
        {user?.role === 'ADMIN' && (<>
          <Link to="/sensors"              style={{ color: '#fbbf24', textDecoration: 'none' }}>Simulador</Link>
          <Link to="/admin/sensors"        style={{ color: '#fbbf24', textDecoration: 'none' }}>Sensores</Link>
          <Link to="/admin/zones"          style={{ color: '#fbbf24', textDecoration: 'none' }}>Zonas</Link>
          <Link to="/admin/notifications"  style={{ color: '#fbbf24', textDecoration: 'none' }}>Notificaciones</Link>
        </>)}
        <span style={{ fontSize: '0.85rem', color: '#cbd5e1' }}>{user?.username}</span>
        <Link to="/profile" style={{ color: '#93c5fd', textDecoration: 'none', fontSize: '0.85rem' }}>Mi Perfil</Link>
        <button onClick={handleLogout} style={{
          background: 'transparent', border: '1px solid #93c5fd',
          color: '#93c5fd', borderRadius: '6px', padding: '4px 12px', cursor: 'pointer'
        }}>Salir</button>
      </div>
    </nav>
  )
}
