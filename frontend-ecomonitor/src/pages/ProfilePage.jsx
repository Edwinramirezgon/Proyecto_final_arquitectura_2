import { useState, useEffect, lazy, Suspense } from 'react'
import Navbar from '../components/Navbar'
import { useAuth }          from '../context/AuthContext'
import { useSubscriptions } from '../hooks/useSubscriptions'
import { useZones }         from '../hooks/useZones'
import { getColombiaAirQuality } from '../api/subscriptions'
import { changePassword }   from '../api/auth'
import PasswordPolicy       from '../components/PasswordPolicy'

const ZoneSubscriptionMap = lazy(() => import('../components/ZoneSubscriptionMap'))

export default function ProfilePage() {
  const { user }                          = useAuth()
  const { zones }                         = useZones()
  const { subscriptions, isSubscribed, toggle, loading: subLoading } = useSubscriptions()
  const [airData,    setAirData]          = useState([])
  const [airLoading, setAirLoading]       = useState(true)
  const [toggling,   setToggling]         = useState({})
  const [feedback,   setFeedback]         = useState(null)
  const [pwForm,     setPwForm]           = useState({ current: '', next: '', confirm: '' })
  const [pwStatus,   setPwStatus]         = useState(null)
  const [pwLoading,  setPwLoading]        = useState(false)

  useEffect(() => {
    getColombiaAirQuality()
      .then(({ data }) => setAirData(data))
      .catch(() => setAirData([]))
      .finally(() => setAirLoading(false))
  }, [])

  async function handleToggle(zoneId, zoneName) {
    setToggling(t => ({ ...t, [zoneId]: true }))
    const wasSub = isSubscribed(zoneId)
    try {
      await toggle(zoneId)
      setFeedback({ ok: true, msg: wasSub
        ? `Te desuscribiste de ${zoneName}. Ya no recibirás alertas de esta zona.`
        : `¡Suscrito a ${zoneName}! Recibirás alertas por email cuando haya emergencias.`
      })
    } catch (err) {
      setFeedback({ ok: false, msg: err.response?.data?.error ?? 'Error al actualizar suscripción.' })
    } finally {
      setToggling(t => ({ ...t, [zoneId]: false }))
      setTimeout(() => setFeedback(null), 4000)
    }
  }

  async function handleChangePassword(e) {
    e.preventDefault()
    setPwStatus(null)
    if (pwForm.next !== pwForm.confirm) {
      setPwStatus({ ok: false, msg: 'Las contraseñas nuevas no coinciden.' })
      return
    }
    setPwLoading(true)
    try {
      await changePassword(pwForm.current, pwForm.next)
      setPwStatus({ ok: true, msg: 'Contraseña actualizada. Recibirás un email de confirmación.' })
      setPwForm({ current: '', next: '', confirm: '' })
    } catch (err) {
      setPwStatus({ ok: false, msg: err.response?.data?.error ?? 'Error al cambiar la contraseña.' })
    } finally {
      setPwLoading(false)
    }
  }

  return (
    <div style={{ minHeight: '100vh', background: '#f0f4f8' }}>
      <Navbar />
      <div style={{ maxWidth: '900px', margin: '0 auto', padding: '24px 16px' }}>

        {/* Encabezado de perfil */}
        <div style={{ background: '#1e3a5f', borderRadius: '14px', padding: '24px',
                      color: '#fff', marginBottom: '24px', display: 'flex',
                      alignItems: 'center', gap: '20px' }}>
          <div style={{ width: '60px', height: '60px', borderRadius: '50%',
                        background: '#3b82f6', display: 'flex', alignItems: 'center',
                        justifyContent: 'center', fontSize: '1.6rem', fontWeight: 700 }}>
            {user?.username?.[0]?.toUpperCase()}
          </div>
          <div>
            <h2 style={{ margin: 0 }}>{user?.username}</h2>
            <p style={{ margin: '4px 0 0', color: '#93c5fd', fontSize: '0.88rem' }}>
              {user?.role === 'ADMIN' ? '👑 Administrador' : '👤 Usuario'} •{' '}
              {subscriptions.length} zona(s) suscritas
            </p>
          </div>
        </div>

        {/* Feedback */}
        {feedback && (
          <div style={{
            padding: '12px 16px', borderRadius: '10px', marginBottom: '16px',
            background: feedback.ok ? '#dcfce7' : '#fee2e2',
            color:      feedback.ok ? '#166534' : '#991b1b', fontWeight: 500
          }}>{feedback.msg}</div>
        )}

        {/* Mapa interactivo de suscripciones */}
        <div style={{ background: '#fff', borderRadius: '12px', padding: '22px',
                      boxShadow: '0 1px 4px rgba(0,0,0,0.07)', marginBottom: '20px' }}>
          <h3 style={{ margin: '0 0 6px', color: '#1e3a5f' }}>🗺️ Mapa de Zonas — Haz clic en una zona para suscribirte</h3>
          <p style={{ margin: '0 0 14px', fontSize: '0.82rem', color: '#6b7280' }}>
            Zonas azules = suscritas. Haz clic en cualquier zona para ver detalles y gestionar tu suscripción.
          </p>
          <Suspense fallback={<div style={{ height: '380px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#6b7280' }}>Cargando mapa...</div>}>
            <ZoneSubscriptionMap
              zones={zones}
              isSubscribed={isSubscribed}
              onToggle={(zoneId) => {
                const zone = zones.find(z => z.id?.toString() === zoneId)
                handleToggle(zoneId, zone?.name ?? zoneId)
              }}
              toggling={toggling}
            />
          </Suspense>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>

          {/* Cambio de contraseña */}
          <div style={{ background: '#fff', borderRadius: '12px', padding: '22px',
                        boxShadow: '0 1px 4px rgba(0,0,0,0.07)' }}>
            <h3 style={{ margin: '0 0 6px', color: '#1e3a5f' }}>🔐 Cambiar Contraseña</h3>
            <p style={{ margin: '0 0 16px', fontSize: '0.82rem', color: '#6b7280' }}>
              Ingresa tu contraseña actual y la nueva.
            </p>
            <form onSubmit={handleChangePassword}>
              {pwStatus && (
                <div style={{ padding: '10px 12px', borderRadius: '8px', marginBottom: '12px',
                              fontSize: '0.85rem', fontWeight: 500,
                              background: pwStatus.ok ? '#dcfce7' : '#fee2e2',
                              color:      pwStatus.ok ? '#166534' : '#991b1b' }}>
                  {pwStatus.msg}
                </div>
              )}
              {[['Contraseña actual', 'current'], ['Nueva contraseña', 'next'], ['Confirmar nueva', 'confirm']].map(([label, key]) => (
                <div key={key} style={{ marginBottom: '10px' }}>
                  <label style={{ display: 'block', fontSize: '0.82rem', color: '#374151',
                                  fontWeight: 500, marginBottom: '4px' }}>{label}</label>
                  <input type="password" value={pwForm[key]} required
                    onChange={e => setPwForm(f => ({ ...f, [key]: e.target.value }))}
                    style={{ width: '100%', padding: '8px 10px', border: '1px solid #d1d5db',
                             borderRadius: '6px', fontSize: '0.9rem', boxSizing: 'border-box' }} />
                </div>
              ))}
              <PasswordPolicy password={pwForm.next} />
              <button type="submit" disabled={pwLoading} style={{
                width: '100%', marginTop: '4px', padding: '9px', border: 'none',
                borderRadius: '8px', background: pwLoading ? '#93c5fd' : '#1e3a5f',
                color: '#fff', fontWeight: 600, cursor: 'pointer'
              }}>
                {pwLoading ? 'Guardando...' : 'Actualizar contraseña'}
              </button>
            </form>
          </div>

          {/* Mis Suscripciones */}
          <div style={{ background: '#fff', borderRadius: '12px', padding: '22px',
                        boxShadow: '0 1px 4px rgba(0,0,0,0.07)' }}>
            <h3 style={{ margin: '0 0 6px', color: '#1e3a5f' }}>🔔 Mis Suscripciones</h3>
            <p style={{ margin: '0 0 18px', fontSize: '0.82rem', color: '#6b7280' }}>
              Activa las zonas donde quieres recibir alertas por email.
            </p>

            {subLoading ? <p style={{ color: '#6b7280' }}>Cargando...</p> : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {zones.map(zone => {
                  const subscribed = isSubscribed(zone.id?.toString() ?? zone.name)
                  const zoneKey    = zone.id?.toString() ?? zone.name
                  return (
                    <div key={zone.id} style={{
                      display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                      padding: '12px 14px', borderRadius: '10px',
                      border: `2px solid ${subscribed ? '#3b82f6' : '#e5e7eb'}`,
                      background: subscribed ? '#eff6ff' : '#fafafa',
                      transition: 'all 0.2s'
                    }}>
                      <div>
                        <p style={{ margin: 0, fontWeight: 600, color: '#1e3a5f', fontSize: '0.9rem' }}>
                          {zone.name}
                        </p>
                        <p style={{ margin: '2px 0 0', fontSize: '0.75rem', color: '#6b7280' }}>
                          {zone.sensitiveType === 'HOSPITAL' ? 'Hospital' : zone.sensitiveType === 'SCHOOL' ? 'Escuela' : zone.sensitiveType === 'PARK' ? 'Parque' : 'Sin tipo'} • Prioridad {zone.priority}
                        </p>
                      </div>
                      <button onClick={() => handleToggle(zoneKey, zone.name)}
                        disabled={toggling[zoneKey]} style={{
                          padding: '6px 14px', borderRadius: '20px', cursor: 'pointer',
                          border: 'none', fontWeight: 600, fontSize: '0.82rem',
                          background: subscribed ? '#fee2e2' : '#dcfce7',
                          color:      subscribed ? '#991b1b' : '#166534',
                          opacity: toggling[zoneKey] ? 0.6 : 1
                        }}>
                        {toggling[zoneKey] ? '...' : subscribed ? '✕ Desuscribirse' : '+ Suscribirse'}
                      </button>
                    </div>
                  )
                })}
                {zones.length === 0 && (
                  <p style={{ color: '#9ca3af', textAlign: 'center', padding: '20px 0' }}>
                    No hay zonas disponibles.
                  </p>
                )}
              </div>
            )}

            {subscriptions.length > 0 && (
              <div style={{ marginTop: '16px', padding: '12px', background: '#f0f9ff',
                            borderRadius: '8px', fontSize: '0.8rem', color: '#0369a1' }}>
                📧 Recibirás emails en tu cuenta cuando se detecten alertas en tus zonas suscritas.
              </div>
            )}
          </div>
        </div>

        {/* Calidad del aire real — Open-Meteo */}
        <div style={{ background: '#fff', borderRadius: '12px', padding: '22px',
                      boxShadow: '0 1px 4px rgba(0,0,0,0.07)' }}>
          <h3 style={{ margin: '0 0 6px', color: '#1e3a5f' }}>🇨🇴 Calidad del Aire — Colombia</h3>
          <p style={{ margin: '0 0 18px', fontSize: '0.82rem', color: '#6b7280' }}>
            Datos en tiempo real de Open-Meteo (modelo CAMS Copernicus) para las principales ciudades del país.
          </p>

          {airLoading ? <p style={{ color: '#6b7280' }}>Obteniendo datos reales...</p> : (
            airData.length === 0
              ? <p style={{ color: '#9ca3af', textAlign: 'center', padding: '20px 0' }}>
                  Datos no disponibles en este momento.
                </p>
              : <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '10px' }}>
                  {airData.map(station => (
                    <div key={station.stationId} style={{
                      padding: '12px 14px', borderRadius: '10px',
                      border: '1px solid #e5e7eb', background: '#fafafa',
                      display: 'flex', justifyContent: 'space-between', alignItems: 'center'
                    }}>
                      <div>
                        <p style={{ margin: 0, fontWeight: 600, color: '#1e3a5f', fontSize: '0.88rem' }}>
                          📍 {station.city}
                        </p>
                        <p style={{ margin: '2px 0 0', fontSize: '0.75rem', color: '#6b7280' }}>
                          AQI US: {Math.round(station.aqiUs)}
                        </p>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <p style={{
                          margin: 0, fontWeight: 700, fontSize: '1rem',
                          color: station.pm25 >= 150 ? '#dc2626' : station.pm25 >= 100 ? '#d97706' : '#16a34a'
                        }}>
                          {station.pm25.toFixed(1)} µg/m³
                        </p>
                        <p style={{ margin: '2px 0 0', fontSize: '0.72rem',
                                    color: station.pm25 >= 150 ? '#dc2626' : station.pm25 >= 100 ? '#d97706' : '#16a34a' }}>
                          {station.pm25 >= 150 ? '🔴 Peligroso' : station.pm25 >= 100 ? '🟡 Moderado' : '🟢 Bueno'}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
          )}
        </div>

      </div>
    </div>
  )
}
