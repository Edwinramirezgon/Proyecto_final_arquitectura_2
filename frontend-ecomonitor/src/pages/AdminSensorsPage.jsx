import { useState } from 'react'
import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet'
import 'leaflet/dist/leaflet.css'
import L from 'leaflet'
import { useSensors } from '../hooks/useSensors'
import { useZones } from '../hooks/useZones'
import Navbar from '../components/Navbar'

// Fix Leaflet default icon
delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png'
})

function MapClickHandler({ onMapClick }) {
  useMapEvents({
    click: (e) => {
      onMapClick(e.latlng)
    }
  })
  return null
}

export default function AdminSensorsPage() {
  const { sensors, loading, error, addSensor, editSensor, removeSensor, toggleSensorStatus } = useSensors()
  const { zones } = useZones()
  const [showForm, setShowForm] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [formData, setFormData] = useState({
    sensorId: '',
    name: '',
    zoneId: '',
    latitude: '',
    longitude: ''
  })
  const [formError, setFormError] = useState('')
  const [mapPosition, setMapPosition] = useState(null)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setFormError('')

    const data = {
      ...formData,
      latitude: parseFloat(formData.latitude),
      longitude: parseFloat(formData.longitude)
    }

    const result = editingId 
      ? await editSensor(editingId, data)
      : await addSensor(data)

    if (result.success) {
      resetForm()
    } else {
      setFormError(result.error)
    }
  }

  const handleEdit = (sensor) => {
    setFormData({
      sensorId: sensor.sensorId,
      name: sensor.name,
      zoneId: sensor.zoneId,
      latitude: sensor.latitude.toString(),
      longitude: sensor.longitude.toString()
    })
    setEditingId(sensor.sensorId)
    setShowForm(true)
  }

  const handleDelete = async (sensorId) => {
    if (!confirm('¿Estás seguro de eliminar este sensor?')) return
    await removeSensor(sensorId)
  }

  const handleToggleStatus = async (sensorId, currentStatus) => {
    await toggleSensorStatus(sensorId, !currentStatus)
  }

  const resetForm = () => {
    setFormData({ sensorId: '', name: '', zoneId: '', latitude: '', longitude: '' })
    setEditingId(null)
    setShowForm(false)
    setFormError('')
    setMapPosition(null)
  }

  const handleMapClick = (latlng) => {
    setFormData({
      ...formData,
      latitude: latlng.lat.toFixed(6),
      longitude: latlng.lng.toFixed(6)
    })
    setMapPosition([latlng.lat, latlng.lng])
  }

  if (loading) return <div style={{ padding: '2rem', textAlign: 'center' }}>Cargando sensores...</div>

  return (
    <div style={{ minHeight: '100vh', background: '#f0f4f8' }}>
      <Navbar />
      <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ margin: 0 }}>Gestión de Sensores</h1>
        <button
          onClick={() => setShowForm(!showForm)}
          style={{
            padding: '0.75rem 1.5rem',
            backgroundColor: '#10b981',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            cursor: 'pointer',
            fontWeight: '600'
          }}
        >
          {showForm ? 'Cancelar' : '+ Nuevo Sensor'}
        </button>
      </div>

      {error && (
        <div style={{
          padding: '1rem',
          backgroundColor: '#fee2e2',
          color: '#991b1b',
          borderRadius: '8px',
          marginBottom: '1rem'
        }}>
          {error}
        </div>
      )}

      {showForm && (
        <div style={{
          backgroundColor: 'white',
          padding: '2rem',
          borderRadius: '12px',
          boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
          marginBottom: '2rem'
        }}>
          <h2 style={{ marginTop: 0 }}>{editingId ? 'Editar Sensor' : 'Nuevo Sensor'}</h2>
          {formError && (
            <div style={{
              padding: '0.75rem',
              backgroundColor: '#fee2e2',
              color: '#991b1b',
              borderRadius: '6px',
              marginBottom: '1rem',
              fontSize: '0.875rem'
            }}>
              {formError}
            </div>
          )}
          
          <div style={{ marginBottom: '1.5rem' }}>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>
              Ubicación en el mapa (haz clic para seleccionar)
            </label>
            <div style={{ height: '400px', borderRadius: '8px', overflow: 'hidden', border: '2px solid #d1d5db' }}>
              <MapContainer
                center={[6.2447, -75.5748]}
                zoom={12}
                style={{ height: '100%', width: '100%' }}
              >
                <TileLayer
                  url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                  attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                />
                <MapClickHandler onMapClick={handleMapClick} />
                {mapPosition && <Marker position={mapPosition} />}
                {sensors.map(sensor => (
                  <Marker
                    key={sensor.id}
                    position={[sensor.latitude, sensor.longitude]}
                    opacity={0.5}
                  />
                ))}
              </MapContainer>
            </div>
          </div>

          <form onSubmit={handleSubmit}>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>
                  ID del Sensor *
                </label>
                <input
                  type="text"
                  value={formData.sensorId}
                  onChange={(e) => setFormData({ ...formData, sensorId: e.target.value })}
                  disabled={!!editingId}
                  required
                  placeholder="SENSOR-MED-009"
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '6px',
                    fontSize: '1rem'
                  }}
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>
                  Nombre *
                </label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  required
                  placeholder="Sensor Laureles Norte"
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '6px',
                    fontSize: '1rem'
                  }}
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>
                  Zona *
                </label>
                <select
                  value={formData.zoneId}
                  onChange={(e) => setFormData({ ...formData, zoneId: e.target.value })}
                  required
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '6px',
                    fontSize: '1rem'
                  }}
                >
                  <option value="">Seleccionar zona...</option>
                  {zones.map(zone => (
                    <option key={zone.id} value={zone.zoneId}>
                      {zone.name} ({zone.zoneId})
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>
                  Latitud *
                </label>
                <input
                  type="number"
                  step="0.000001"
                  value={formData.latitude}
                  onChange={(e) => setFormData({ ...formData, latitude: e.target.value })}
                  required
                  placeholder="6.2447"
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '6px',
                    fontSize: '1rem'
                  }}
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>
                  Longitud *
                </label>
                <input
                  type="number"
                  step="0.000001"
                  value={formData.longitude}
                  onChange={(e) => setFormData({ ...formData, longitude: e.target.value })}
                  required
                  placeholder="-75.5907"
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '6px',
                    fontSize: '1rem'
                  }}
                />
              </div>
            </div>
            <div style={{ display: 'flex', gap: '1rem', marginTop: '1.5rem' }}>
              <button
                type="submit"
                style={{
                  padding: '0.75rem 2rem',
                  backgroundColor: '#3b82f6',
                  color: 'white',
                  border: 'none',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  fontWeight: '600'
                }}
              >
                {editingId ? 'Actualizar' : 'Crear Sensor'}
              </button>
              <button
                type="button"
                onClick={resetForm}
                style={{
                  padding: '0.75rem 2rem',
                  backgroundColor: '#6b7280',
                  color: 'white',
                  border: 'none',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  fontWeight: '600'
                }}
              >
                Cancelar
              </button>
            </div>
          </form>
        </div>
      )}

      <div style={{
        backgroundColor: 'white',
        borderRadius: '12px',
        boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
        overflow: 'hidden'
      }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ backgroundColor: '#f3f4f6' }}>
              <th style={{ padding: '1rem', textAlign: 'left', fontWeight: '600' }}>ID</th>
              <th style={{ padding: '1rem', textAlign: 'left', fontWeight: '600' }}>Nombre</th>
              <th style={{ padding: '1rem', textAlign: 'left', fontWeight: '600' }}>Zona</th>
              <th style={{ padding: '1rem', textAlign: 'left', fontWeight: '600' }}>Ubicación</th>
              <th style={{ padding: '1rem', textAlign: 'left', fontWeight: '600' }}>Estado</th>
              <th style={{ padding: '1rem', textAlign: 'left', fontWeight: '600' }}>Última Lectura</th>
              <th style={{ padding: '1rem', textAlign: 'center', fontWeight: '600' }}>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {sensors.map((sensor, idx) => (
              <tr key={sensor.id} style={{ borderTop: idx > 0 ? '1px solid #e5e7eb' : 'none' }}>
                <td style={{ padding: '1rem', fontFamily: 'monospace', fontSize: '0.875rem' }}>
                  {sensor.sensorId}
                </td>
                <td style={{ padding: '1rem' }}>{sensor.name}</td>
                <td style={{ padding: '1rem', fontSize: '0.875rem' }}>{sensor.zoneId}</td>
                <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                  {sensor.latitude.toFixed(4)}, {sensor.longitude.toFixed(4)}
                </td>
                <td style={{ padding: '1rem' }}>
                  <span style={{
                    padding: '0.25rem 0.75rem',
                    borderRadius: '9999px',
                    fontSize: '0.75rem',
                    fontWeight: '600',
                    backgroundColor: sensor.active ? '#d1fae5' : '#fee2e2',
                    color: sensor.active ? '#065f46' : '#991b1b'
                  }}>
                    {sensor.active ? 'Activo' : 'Inactivo'}
                  </span>
                </td>
                <td style={{ padding: '1rem', fontSize: '0.875rem', color: '#6b7280' }}>
                  {sensor.lastReadingAt 
                    ? new Date(sensor.lastReadingAt).toLocaleString('es-CO')
                    : 'Sin lecturas'}
                </td>
                <td style={{ padding: '1rem' }}>
                  <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'center' }}>
                    <button
                      onClick={() => handleEdit(sensor)}
                      style={{
                        padding: '0.5rem 1rem',
                        backgroundColor: '#3b82f6',
                        color: 'white',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: 'pointer',
                        fontSize: '0.875rem'
                      }}
                    >
                      Editar
                    </button>
                    <button
                      onClick={() => handleToggleStatus(sensor.sensorId, sensor.active)}
                      style={{
                        padding: '0.5rem 1rem',
                        backgroundColor: sensor.active ? '#f59e0b' : '#10b981',
                        color: 'white',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: 'pointer',
                        fontSize: '0.875rem'
                      }}
                    >
                      {sensor.active ? 'Desactivar' : 'Activar'}
                    </button>
                    <button
                      onClick={() => handleDelete(sensor.sensorId)}
                      style={{
                        padding: '0.5rem 1rem',
                        backgroundColor: '#ef4444',
                        color: 'white',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: 'pointer',
                        fontSize: '0.875rem'
                      }}
                    >
                      Eliminar
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {sensors.length === 0 && (
          <div style={{ padding: '3rem', textAlign: 'center', color: '#6b7280' }}>
            No hay sensores registrados
          </div>
        )}
      </div>
      </div>
    </div>
  )
}
