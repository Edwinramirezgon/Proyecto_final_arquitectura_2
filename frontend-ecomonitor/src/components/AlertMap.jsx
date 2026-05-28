import { useEffect } from 'react'
import { MapContainer, TileLayer, CircleMarker, Circle, Popup, Tooltip, useMap } from 'react-leaflet'
import 'leaflet/dist/leaflet.css'
import { formatDateTime } from '../utils/dates'

const LEVEL_COLOR = {
  CRITICAL: '#dc2626',
  HIGH:     '#f59e0b',
  MEDIUM:   '#3b82f6',
}

const TIPO_LABEL = {
  HOSPITAL: 'Hospital',
  SCHOOL:   'Escuela',
  PARK:     'Parque',
  NONE:     'Sin tipo',
}

function ColombiaView() {
  const map = useMap()
  useEffect(() => { map.setView([4.5709, -74.2973], 6) }, [map])
  return null
}

export default function AlertMap({ alerts = [], zones = [], airData = [] }) {
  const criticalAlerts = alerts.filter(a => a.level === 'CRITICAL')

  return (
    <MapContainer
      center={[4.5709, -74.2973]}
      zoom={6}
      style={{ height: '420px', width: '100%', borderRadius: '12px', zIndex: 0 }}
      scrollWheelZoom={false}
    >
      <ColombiaView />
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      {/* Restricción vehicular — zona roja semitransparente para alertas CRITICAL */}
      {criticalAlerts.map(alert => (
        <Circle
          key={`restriction-${alert.id}`}
          center={[alert.latitude, alert.longitude]}
          radius={3000}
          pathOptions={{
            color:       '#dc2626',
            fillColor:   '#dc2626',
            fillOpacity: 0.15,
            weight:      2,
            dashArray:   '6 4',
          }}
        >
          <Tooltip direction="top" permanent>
            🚫 Restricción vehicular activa
          </Tooltip>
          <Popup>
            <div style={{ minWidth: '180px' }}>
              <strong style={{ color: '#dc2626' }}>🚫 Restricción Vehicular</strong>
              <p style={{ margin: '6px 0 2px' }}><strong>Zona:</strong> {alert.zoneId}</p>
              <p style={{ margin: '2px 0' }}>Circulación vehicular restringida por emergencia ambiental.</p>
              <p style={{ margin: '2px 0' }}><strong>CO2:</strong> {alert.averageCo2.toFixed(1)} µg/m³</p>
            </div>
          </Popup>
        </Circle>
      ))}

      {/* Alertas activas — círculos de colores */}
      {alerts.map(alert => (
        <CircleMarker
          key={alert.id}
          center={[alert.latitude, alert.longitude]}
          radius={alert.level === 'CRITICAL' ? 22 : alert.level === 'HIGH' ? 16 : 12}
          pathOptions={{
            color:       LEVEL_COLOR[alert.level] ?? '#6b7280',
            fillColor:   LEVEL_COLOR[alert.level] ?? '#6b7280',
            fillOpacity: 0.45,
            weight:      2,
          }}
        >
          <Tooltip permanent={alert.level === 'CRITICAL'} direction="top">
            <strong>{alert.zoneId}</strong><br />
            CO2: {alert.averageCo2.toFixed(1)} µg/m³<br />
            Nivel: {alert.level}
          </Tooltip>
          <Popup>
            <div style={{ minWidth: '180px' }}>
              <strong style={{ color: LEVEL_COLOR[alert.level] }}>
                {alert.level === 'CRITICAL' ? '🚨' : alert.level === 'HIGH' ? '⚠️' : '🔵'} {alert.level}
              </strong>
              <p style={{ margin: '6px 0 2px' }}><strong>Zona:</strong> {alert.zoneId}</p>
              <p style={{ margin: '2px 0' }}><strong>CO2:</strong> {alert.averageCo2.toFixed(1)} µg/m³</p>
              {alert.nearestZoneName && (
                <p style={{ margin: '2px 0' }}><strong>Zona sensible:</strong> {alert.nearestZoneName}</p>
              )}
              <p style={{ margin: '2px 0', fontSize: '0.75rem', color: '#6b7280' }}>
                 {formatDateTime(alert.triggeredAt)}
              </p>
            </div>
          </Popup>
        </CircleMarker>
      ))}

      {/* Zonas sensibles */}
      {zones.map(zone => (
        <CircleMarker
          key={zone.id}
          center={[zone.latitude, zone.longitude]}
          radius={6}
          pathOptions={{
            color:       zone.sensitiveType === 'HOSPITAL' ? '#7c3aed' :
                         zone.sensitiveType === 'SCHOOL'   ? '#0891b2' : '#ea580c',
            fillColor:   zone.sensitiveType === 'HOSPITAL' ? '#7c3aed' :
                         zone.sensitiveType === 'SCHOOL'   ? '#0891b2' : '#ea580c',
            fillOpacity: 0.8,
            weight:      1,
          }}
        >
          <Tooltip direction="top">
            {zone.sensitiveType === 'HOSPITAL' ? '🏥' :
             zone.sensitiveType === 'SCHOOL'   ? '🏫' : '🌳'} {zone.name}
          </Tooltip>
          <Popup>
            <strong>{zone.name}</strong><br />
            Tipo: {TIPO_LABEL[zone.sensitiveType] ?? zone.sensitiveType}<br />
            Prioridad: {zone.priority}
          </Popup>
        </CircleMarker>
      ))}

      {/* Datos reales Open-Meteo */}
      {airData.map(station => (
        <CircleMarker
          key={station.stationId}
          center={[station.latitude, station.longitude]}
          radius={8}
          pathOptions={{
            color:       station.pm25 >= 150 ? '#dc2626' : station.pm25 >= 100 ? '#f59e0b' : '#16a34a',
            fillColor:   station.pm25 >= 150 ? '#dc2626' : station.pm25 >= 100 ? '#f59e0b' : '#16a34a',
            fillOpacity: 0.7,
            weight:      2,
            dashArray:   '4 2',
          }}
        >
          <Tooltip direction="top">
            📡 {station.city} (Open-Meteo)<br />
            {station.pm25.toFixed(1)} µg/m³
          </Tooltip>
          <Popup>
            <strong>📡 {station.city}</strong><br />
            Fuente: Open-Meteo CAMS (tiempo real)<br />
            PM2.5: {station.pm25.toFixed(1)} µg/m³<br />
            AQI US: {station.aqiUs}
          </Popup>
        </CircleMarker>
      ))}
    </MapContainer>
  )
}
