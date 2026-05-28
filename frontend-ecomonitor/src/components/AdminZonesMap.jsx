import { useRef } from 'react'
import { MapContainer, TileLayer, CircleMarker, Tooltip, Marker, useMapEvents } from 'react-leaflet'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

const TIPO_COLOR = {
  HOSPITAL: '#7c3aed',
  SCHOOL:   '#0891b2',
  PARK:     '#ea580c',
  NONE:     '#6b7280',
}

const TIPO_LABEL = { HOSPITAL: 'Hospital', SCHOOL: 'Escuela', PARK: 'Parque', NONE: 'Sin tipo' }

const TIPO_ICON = { HOSPITAL: '🏥', SCHOOL: '🏫', PARK: '🌳', NONE: '📍' }

// Marcador arrastrable para la posición seleccionada
const dragIcon = new L.DivIcon({
  html: '<div style="width:22px;height:22px;background:#1e3a5f;border:3px solid #fff;border-radius:50%;box-shadow:0 2px 6px rgba(0,0,0,0.4)"></div>',
  iconSize:   [22, 22],
  iconAnchor: [11, 11],
  className:  '',
})

const COLOMBIA_BOUNDS = [[-4.23, -81.73], [12.45, -66.87]]
const COLOMBIA_CENTER = [4.5709, -74.2973]

function ClickHandler({ onMapClick }) {
  useMapEvents({ click: e => onMapClick(e.latlng) })
  return null
}

export default function AdminZonesMap({
  zones = [],
  selectedLat,
  selectedLng,
  editingId,
  onMapClick,
}) {
  const markerRef = useRef(null)

  // Cuando el marcador se arrastra, actualiza las coordenadas igual que un clic
  function handleDragEnd() {
    const latlng = markerRef.current?.getLatLng()
    if (latlng) onMapClick(latlng)
  }

  return (
    <MapContainer
      center={COLOMBIA_CENTER}
      zoom={6}
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

      {/* Zonas existentes */}
      {zones.map(zone => (
        <CircleMarker
          key={zone.id}
          center={[zone.latitude, zone.longitude]}
          radius={editingId === zone.id ? 14 : 9}
          pathOptions={{
            color:       editingId === zone.id ? '#f59e0b' : TIPO_COLOR[zone.sensitiveType] ?? '#6b7280',
            fillColor:   editingId === zone.id ? '#fef3c7' : TIPO_COLOR[zone.sensitiveType] ?? '#6b7280',
            fillOpacity: 0.7,
            weight:      editingId === zone.id ? 3 : 2,
          }}
        >
          <Tooltip direction="top" permanent={false}>
            {TIPO_ICON[zone.sensitiveType] ?? '📍'} <strong>{zone.name}</strong><br />
            {TIPO_LABEL[zone.sensitiveType] ?? zone.sensitiveType} · Prioridad {zone.priority}
          </Tooltip>
        </CircleMarker>
      ))}

      {/* Marcador arrastrable para la posición nueva/editada */}
      {selectedLat !== null && selectedLng !== null && (
        <Marker
          position={[selectedLat, selectedLng]}
          icon={dragIcon}
          draggable
          ref={markerRef}
          eventHandlers={{ dragend: handleDragEnd }}
        >
          <Tooltip direction="top" permanent>
            📍 {selectedLat.toFixed(5)}, {selectedLng.toFixed(5)}
          </Tooltip>
        </Marker>
      )}
    </MapContainer>
  )
}
