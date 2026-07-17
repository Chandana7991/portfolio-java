// Fetch skills from the Java backend
async function loadSkills() {
  const grid = document.getElementById('skills-grid');
  try {
    const res = await fetch('/api/skills');
    const skills = await res.json();
    grid.innerHTML = skills.map(s => `
      <div class="skill-card">
        <span class="cat">${escapeHtml(s.category)}</span>
        <div class="items">${escapeHtml(s.items)}</div>
      </div>
    `).join('');
  } catch (err) {
    grid.innerHTML = '<div class="body-text">Could not load skills right now.</div>';
  }
}

// Fetch projects from the Java backend
async function loadProjects() {
  const grid = document.getElementById('projects-grid');
  try {
    const res = await fetch('/api/projects');
    const projects = await res.json();
    grid.innerHTML = projects.map(p => `
      <div class="project-card">
        <h3>${escapeHtml(p.title)}</h3>
        <p>${escapeHtml(p.description)}</p>
        <div class="tech">${escapeHtml(p.tech)}</div>
        <a class="repo-link" href="${escapeAttr(p.link)}" target="_blank" rel="noopener">View on GitHub →</a>
      </div>
    `).join('');
  } catch (err) {
    grid.innerHTML = '<div class="body-text">Could not load projects right now.</div>';
  }
}

// Fetch internships from the Java backend
async function loadInternships() {
  const grid = document.getElementById('internships-grid');
  try {
    const res = await fetch('/api/internships');
    const internships = await res.json();
    grid.innerHTML = internships.map(i => `
      <div class="project-card">
        <h3>${escapeHtml(i.title)}</h3>
        <div class="tech">${escapeHtml(i.org)} · ${escapeHtml(i.duration)}</div>
        <p>${escapeHtml(i.description)}</p>
        <div class="credential">${escapeHtml(i.credential)}</div>
      </div>
    `).join('');
  } catch (err) {
    grid.innerHTML = '<div class="body-text">Could not load internships right now.</div>';
  }
}

// Fetch a short preview of certifications from the Java backend
async function loadCertsPreview() {
  const row = document.getElementById('certs-preview');
  try {
    const res = await fetch('/api/certifications');
    const certs = await res.json();
    row.innerHTML = certs.map(c => `
      <span class="cert-chip">${escapeHtml(c.title)}</span>
    `).join('');
  } catch (err) {
    row.innerHTML = '<div class="body-text">Could not load certifications right now.</div>';
  }
}

// Wire up the contact form to POST /api/contact
function setupContactForm() {
  const form = document.getElementById('contact-form');
  const status = document.getElementById('form-status');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    status.textContent = 'Sending...';
    status.className = 'form-status';

    const payload = {
      name: document.getElementById('name').value,
      email: document.getElementById('email').value,
      message: document.getElementById('message').value,
    };

    try {
      const res = await fetch('/api/contact', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      const data = await res.json();
      if (res.ok) {
        status.textContent = data.message || 'Message sent!';
        status.className = 'form-status ok';
        form.reset();
      } else {
        status.textContent = data.error || 'Something went wrong.';
        status.className = 'form-status err';
      }
    } catch (err) {
      status.textContent = 'Network error — please try again.';
      status.className = 'form-status err';
    }
  });
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}
function escapeAttr(str) {
  return String(str).replace(/"/g, '&quot;');
}

loadSkills();
loadProjects();
loadInternships();
loadCertsPreview();
setupContactForm();
