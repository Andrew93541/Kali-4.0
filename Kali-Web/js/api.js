const API_BASE = 'http://localhost:3000/api';

/* ── Storage helpers ──────────────────────────────────────── */
function getToken()  { return localStorage.getItem('kali_token'); }
function getRole()   { return localStorage.getItem('kali_role'); }
function getName()   { return localStorage.getItem('kali_name'); }
function getUserId() { return localStorage.getItem('kali_user_id'); }

/* ── Auth headers ─────────────────────────────────────────── */
function authHeaders() {
  return {
    'Authorization': `Bearer ${getToken()}`,
    'Content-Type': 'application/json'
  };
}

/* ── Core HTTP helpers ────────────────────────────────────── */
async function apiGet(path) {
  try {
    const res = await fetch(`${API_BASE}${path}`, { headers: authHeaders() });
    if (res.status === 401 || res.status === 403) { logout(); return null; }
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      return { error: err.message || `HTTP ${res.status}` };
    }
    return res.json();
  } catch (e) {
    return { error: e.message };
  }
}

async function apiPost(path, body) {
  try {
    const res = await fetch(`${API_BASE}${path}`, {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify(body)
    });
    return res.json();
  } catch (e) {
    return { error: e.message };
  }
}

async function apiPut(path, body) {
  try {
    const res = await fetch(`${API_BASE}${path}`, {
      method: 'PUT',
      headers: authHeaders(),
      body: JSON.stringify(body)
    });
    return res.json();
  } catch (e) {
    return { error: e.message };
  }
}

async function apiDelete(path) {
  try {
    const res = await fetch(`${API_BASE}${path}`, {
      method: 'DELETE',
      headers: authHeaders()
    });
    if (res.status === 204) return { success: true };
    return res.json();
  } catch (e) {
    return { error: e.message };
  }
}

/* ── Auth ─────────────────────────────────────────────────── */
async function login(email, password) {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  const data = await res.json();
  if (data.token) {
    localStorage.setItem('kali_token', data.token);
    localStorage.setItem('kali_role', data.role);
    localStorage.setItem('kali_name', data.name);
    localStorage.setItem('kali_user_id', data.userId || data.id || '');
  }
  return data;
}

async function register(name, email, phone, password, role) {
  const res = await fetch(`${API_BASE}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, email, phone, password, role })
  });
  return res.json();
}

function logout() {
  localStorage.removeItem('kali_token');
  localStorage.removeItem('kali_role');
  localStorage.removeItem('kali_name');
  localStorage.removeItem('kali_user_id');
  const path = window.location.pathname;
  const segments = path.split('/');
  const isSubdir = segments.includes('user') || segments.includes('guardian') || segments.includes('police');
  window.location.href = isSubdir ? '../login.html' : 'login.html';
}

function requireAuth(expectedRole) {
  if (!getToken()) {
    const path = window.location.pathname;
    const segments = path.split('/');
    const isSubdir = segments.includes('user') || segments.includes('guardian') || segments.includes('police');
    window.location.href = isSubdir ? '../login.html' : 'login.html';
    return false;
  }
  if (expectedRole && getRole() !== expectedRole && getRole() !== 'Admin') {
    const path = window.location.pathname;
    const segments = path.split('/');
    const isSubdir = segments.includes('user') || segments.includes('guardian') || segments.includes('police');
    window.location.href = isSubdir ? '../login.html' : 'login.html';
    return false;
  }
  return true;
}

/* ── Utilities ────────────────────────────────────────────── */
function formatDate(ts) {
  if (!ts) return 'N/A';
  return new Date(ts).toLocaleString();
}

function timeElapsed(ts) {
  if (!ts) return 'N/A';
  const diff = Date.now() - new Date(ts).getTime();
  const secs  = Math.floor(diff / 1000);
  const mins  = Math.floor(secs / 60);
  const hrs   = Math.floor(mins / 60);
  if (hrs  > 0) return `${hrs}h ${mins % 60}m ago`;
  if (mins > 0) return `${mins}m ago`;
  if (secs > 0) return `${secs}s ago`;
  return 'just now';
}

function formatDuration(startTs, endTs) {
  const ms   = new Date(endTs || Date.now()).getTime() - new Date(startTs).getTime();
  const secs = Math.floor(ms / 1000);
  const mins = Math.floor(secs / 60);
  const hrs  = Math.floor(mins / 60);
  if (hrs > 0) return `${hrs}h ${mins % 60}m ${secs % 60}s`;
  if (mins > 0) return `${mins}m ${secs % 60}s`;
  return `${secs}s`;
}

/* ── Profile ──────────────────────────────────────────────── */
function getProfile()        { return apiGet('/auth/profile'); }
function updateProfile(body) { return apiPut('/auth/profile', body); }

/* ── Contacts ─────────────────────────────────────────────── */
function getContacts()           { return apiGet('/contacts'); }
function addContact(body)        { return apiPost('/contacts', body); }
function deleteContact(id)       { return apiDelete(`/contacts/${id}`); }

/* ── Guardian ─────────────────────────────────────────────── */
function getMyUser()             { return apiGet('/guardian/my-user'); }
function getActiveAlert()        { return apiGet('/guardian/active-alert'); }
function linkToUser(body)        { return apiPost('/guardian/link', body); }

/* ── Alerts ───────────────────────────────────────────────── */
function getAlerts(status)       { return apiGet(`/alerts${status ? '?status=' + status : ''}`); }
function getMyAlerts()           { return apiGet('/alerts/my'); }
function createAlert(body)       { return apiPost('/alerts', body || {}); }
function resolveAlert(id, note)  { return apiPost(`/alerts/${id}/resolve`, { note: note || '' }); }
