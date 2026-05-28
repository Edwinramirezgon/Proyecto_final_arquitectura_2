export default function PasswordPolicy({ password = '' }) {
  const rules = [
    { label: 'Mínimo 8 caracteres',       ok: password.length >= 8 },
    { label: 'Mayúsculas (A-Z)',           ok: /[A-Z]/.test(password) },
    { label: 'Minúsculas (a-z)',           ok: /[a-z]/.test(password) },
    { label: 'Números (0-9)',              ok: /[0-9]/.test(password) },
    { label: 'Caracteres especiales (!@#$%&*?)', ok: /[!@#$%&*?]/.test(password) },
  ]

  const typesOk = [rules[1], rules[2], rules[3], rules[4]].filter(r => r.ok).length
  const valid   = rules[0].ok && typesOk >= 3

  if (!password) return null

  return (
    <div style={{ marginTop: '8px', padding: '10px 12px', background: '#f8fafc',
                  borderRadius: '8px', fontSize: '0.78rem' }}>
      <p style={{ margin: '0 0 6px', fontWeight: 600,
                  color: valid ? '#166534' : '#374151' }}>
        {valid ? '✅ Contraseña válida' : 'Requisitos de contraseña:'}
      </p>
      {rules.map(r => (
        <div key={r.label} style={{ display: 'flex', alignItems: 'center', gap: '6px',
                                    marginBottom: '2px', color: r.ok ? '#16a34a' : '#9ca3af' }}>
          <span>{r.ok ? '✓' : '○'}</span>
          <span>{r.label}</span>
        </div>
      ))}
      {!valid && typesOk < 3 && (
        <p style={{ margin: '6px 0 0', color: '#d97706', fontSize: '0.75rem' }}>
          Se requieren al menos 3 tipos de caracteres ({typesOk}/3 cumplidos)
        </p>
      )}
    </div>
  )
}
