import { useRef } from 'react'
import { MapContainer, TileLayer, CircleMarker, Tooltip, Marker, useMapEvents } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const COLOMBIA_BOUNDS = [[-4.23, -81.73], [12.45, -66.87]]
const COLOMBIA_CENTER = [6.2442, -75.5812]

const ZONE_COLOR = {
  'ZONA-ARANJUEZ': '#7c3aed',
  'ZONA-CENTRO':   '#0891b2',
  'ZONA-BELEN':    '#ea580c',
  'ZONA-POBLADO':  '#16a34a',
  'ZONA-BELLO':    '#dc2626',
  'ZONA-ITAGUI':   '#d97706',
}

const dragIcon = new L.DivIcon({
  html: '<div style="width:22px;height:22px;background:#1e3a5f;border:3px solid #fff;border-radius:50%;box-shadow:0 2px 6px rgba(0,0,0,0.4)"></div>',
  iconSize:   [22, 22],
  iconAnchor: [11, 11],
  className:  '',
})

function ClickHandler({ onMapClick }) {
  useMapEvents({ click: e => onMapClick(e.latlng) })
  return null
}

export default function SensorsMap({
  sensors = [],
  selectedLat,
  selectedLng,
  onMapClick,
}) {
  const markerRef = useRef(null)

  function handleDragEnd() {
    const latlng = markerRef.current?.getLatLng()
    if (latlng) onMapClick(latlng)
  }

  return (
    <MapContainer
      center={COLOMBIA_CENTER}
      zoom={11}
      minZoom={5}
      maxBounds={COLOMBIA_BOUNDS}
      maxBoundsViscosity={1.0}
      style={{ height: '400px', width: '100%', zIndex: 0 }}
      scrollWheelZoom
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      <ClickHandler onMapClick={onMapClick} />

      {/* Sensores existentes del simulador */}
      {sensors.map(s => (
        <CircleMarker
          key={s.sensorId}
          center={[s.latitude, s.longitude]}
          radius={7}
          pathOptions={{
            color:       ZONE_COLOR[s.zoneId] ?? '#6b7280',
            fillColor:   ZONE_COLOR[s.zoneId] ?? '#6b7280',
            fillOpacity: 0.7,
            weight:      2,
          }}
        >
          <Tooltip direction="top">
            📡 <strong>{s.sensorId}</strong><br />
            Zona: {s.zoneId}
          </Tooltip>
        </CircleMarker>
      ))}

      {/* Marcador arrastrable para nuevo sensor */}
      {selectedLat !== null && selectedLng !== null && (
        <Marker
          position={[selectedLat, selectedLng]}
          icon={dragIcon}
          draggable
          ref={markerRef}
          eventHandlers={{ dragend: handleDragEnd }}
        >
          <Tooltip direction="top" permanent>
            📡 {selectedLat.toFixed(5)}, {selectedLng.toFixed(5)}
          </Tooltip>
        </Marker>
      )}
    </MapContainer>
  )
}
