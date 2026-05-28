import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider }    from './context/AuthContext'
import PrivateRoute        from './components/PrivateRoute'
import LoginPage           from './pages/LoginPage'
import DashboardPage       from './pages/DashboardPage'
import SensorsPage         from './pages/SensorsPage'
import AlertsPage          from './pages/AlertsPage'
import AdminZonesPage      from './pages/AdminZonesPage'
import AdminSensorsPage    from './pages/AdminSensorsPage'
import NotificationsPage   from './pages/NotificationsPage'
import ProfilePage         from './pages/ProfilePage'

import ForgotPasswordPage from './pages/ForgotPasswordPage'
import ResetPasswordPage  from './pages/ResetPasswordPage'
import RegisterPage        from './pages/RegisterPage'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login"           element={<LoginPage />} />
          <Route path="/register"         element={<RegisterPage />} />
          <Route path="/forgot-password"  element={<ForgotPasswordPage />} />
          <Route path="/reset-password"   element={<ResetPasswordPage />} />
          <Route path="/dashboard" element={
            <PrivateRoute><DashboardPage /></PrivateRoute>
          } />
          <Route path="/sensors" element={
            <PrivateRoute adminOnly><SensorsPage /></PrivateRoute>
          } />
          <Route path="/alerts" element={
            <PrivateRoute><AlertsPage /></PrivateRoute>
          } />
          <Route path="/profile" element={
            <PrivateRoute><ProfilePage /></PrivateRoute>
          } />
          <Route path="/admin/zones" element={
            <PrivateRoute adminOnly><AdminZonesPage /></PrivateRoute>
          } />
          <Route path="/admin/sensors" element={
            <PrivateRoute adminOnly><AdminSensorsPage /></PrivateRoute>
          } />
          <Route path="/admin/notifications" element={
            <PrivateRoute adminOnly><NotificationsPage /></PrivateRoute>
          } />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
