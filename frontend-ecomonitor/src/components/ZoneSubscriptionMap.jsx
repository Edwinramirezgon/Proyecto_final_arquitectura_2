import { MapContainer, TileLayer, CircleMarker, Popup } from 'react-leaflet'
import 'leaflet/dist/leaflet.css'

export default function ZoneSubscriptionMap({ zones = [], isSubscribed, onToggle, toggling = {} }) {
  return (
    <MapContainer
      center={[4.5709, -74.2973]}
      zoom={6}
      style={{ height: '380px', width: '100%', borderRadius: '12px', zIndex: 0 }}
      scrollWheelZoom={false}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      {zones.map(zone => {
        const zoneKey  = zone.id?.toString() ?? zone.name
        const subbed   = isSubscribed(zoneKey)
        const loading  = toggling[zoneKey]

        return (
          <CircleMarker
            key={zone.id}
            center={[zone.latitude, zone.longitude]}
            radius={subbed ? 18 : 12}
            pathOptions={{
              color:       subbed ? '#1d4ed8' : '#6b7280',
              fillColor:   subbed ? '#3b82f6' : '#d1d5db',
              fillOpacity: subbed ? 0.6 : 0.4,
              weight:      subbed ? 3 : 1,
            }}
          >
            <Popup>
              <div style={{ minWidth: '180px', textAlign: 'center' }}>
                <p style={{ margin: '0 0 4px', fontWeight: 700, color: '#1e3a5f' }}>
                  {zone.sensitiveType === 'HOSPITAL' ? '🏥' :
                   zone.sensitiveType === 'SCHOOL'   ? '🏫' : '🌳'} {zone.name}
                </p>
                <p style={{ margin: '0 0 2px', fontSize: '0.78rem', color: '#6b7280' }}>
                  {zone.sensitiveType === 'HOSPITAL' ? 'Hospital' : zone.sensitiveType === 'SCHOOL' ? 'Escuela' : zone.sensitiveType === 'PARK' ? 'Parque' : 'Sin tipo'} • Prioridad {zone.priority}
                </p>
                <p style={{ margin: '0 0 10px', fontSize: '0.78rem', color: '#6b7280' }}>
                  Radio: {zone.radiusKm} km
                </p>
                <div style={{
                  padding: '4px 8px', borderRadius: '12px', fontSize: '0.75rem',
                  marginBottom: '10px', fontWeight: 600,
                  background: subbed ? '#dbeafe' : '#f3f4f6',
                  color:      subbed ? '#1d4ed8' : '#6b7280'
                }}>
                  {subbed ? '🔔 Suscrito' : '🔕 No suscrito'}
                </div>
                <button
                  onClick={() => onToggle(zoneKey)}
                  disabled={loading}
                  style={{
                    width: '100%', padding: '7px', border: 'none', borderRadius: '8px',
                    cursor: loading ? 'not-allowed' : 'pointer', fontWeight: 600,
                    fontSize: '0.82rem', opacity: loading ? 0.6 : 1,
                    background: subbed ? '#fee2e2' : '#dcfce7',
                    color:      subbed ? '#991b1b' : '#166534',
                  }}
                >
                  {loading ? 'Procesando...' : subbed ? '✕ Desuscribirse' : '+ Suscribirse'}
                </button>
              </div>
            </Popup>
          </CircleMarker>
        )
      })}
    </MapContainer>
  )
}
