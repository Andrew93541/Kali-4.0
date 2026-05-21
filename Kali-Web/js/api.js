const API_BASE = 'http://localhost:3000/api';

function getToken() { return localStorage.getItem('kali_token'); }
function getRole()  { return localStorage.getItem('kali_role'); }
function getName()  { return localStorage.getItem('kali_name'); }

function authHeaders() {
  return { 'Authorization': `Bearer ${getToken()}`, 'Content-Type': 'application/json' };
}

async function apiGet(path) {
  const res = await fetch(`${API_BASE}${path}`, { headers: authHeaders() });
  if (res.status === 401 || res.status === 403) { logout(); return null; }
  return res.json();
}

async function apiPost(path, body) {
  const res = await fetch(`${API_BASE}${path}`, {
    method: 'POST', headers: authHeaders(), body: JSON.stringify(body)
  });
  return res.json();
}

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
  }
  return data;
}

function logout() {
  localStorage.removeItem('kali_token');
  localStorage.removeItem('kali_role');
  localStorage.removeItem('kali_name');
  window.location.href = '/login.html';
}

function requireAuth(expectedRole) {
  if (!getToken()) { window.location.href = '../login.html'; return false; }
  if (expectedRole && getRole() !== expectedRole && getRole() !== 'Admin') {
    window.location.href = '../login.html'; return false;
  }
  return true;
}

function formatDate(ts) {
  return new Date(ts).toLocaleString();
}
