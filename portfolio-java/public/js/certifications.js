// Fetch full certification list from the Java backend and render detail cards
async function loadFullCertifications() {
  const grid = document.getElementById('certs-full-grid');
  try {
    const res = await fetch('/api/certifications');
    const certs = await res.json();
    grid.innerHTML = certs.map(c => `
      <div class="cert-detail-card">
        <h3>${escapeHtml(c.title)}</h3>
        <div class="cert-issuer">${escapeHtml(c.issuer)}</div>
        <div class="cert-date">${escapeHtml(c.date)}</div>
        ${c.credential ? `<div class="cert-credential">${escapeHtml(c.credential)}</div>` : ''}
        ${c.verifyUrl ? `<a class="cert-verify" href="${escapeAttr(c.verifyUrl)}" target="_blank" rel="noopener">Verify credential →</a>` : ''}
      </div>
    `).join('');
  } catch (err) {
    grid.innerHTML = '<div class="body-text">Could not load certifications right now.</div>';
  }
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}
function escapeAttr(str) {
  return String(str).replace(/"/g, '&quot;');
}

loadFullCertifications();
