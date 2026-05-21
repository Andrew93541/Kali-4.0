# Kali – Women's Safety App

## Project Structure
```
Kali 4.0/
├── Kali-Backend/     Node.js/Express REST API + Socket.IO
├── Kali-Web/         Guardian & Police web dashboards (HTML/CSS/JS)
└── Kali-Android/     Android app (Kotlin, MVVM, Room, WorkManager)
```

---

## 1. Backend Setup

**Requirements:** Node.js 18+, PostgreSQL

```bash
cd Kali-Backend
npm install
cp .env.example .env        # fill in DB credentials and JWT_SECRET
npm run dev                 # starts on http://localhost:3000
```

The server auto-creates all tables on first run via `initDb.js`.

**Key endpoints:**
| Method | Path | Description |
|--------|------|-------------|
| POST | /api/auth/register | Register user (role: User/Guardian/Admin) |
| POST | /api/auth/login | Login → returns JWT |
| POST | /api/alerts | Create SOS alert |
| GET  | /api/alerts | List alerts |
| POST | /api/locations | Add GPS point |
| GET  | /api/locations/:alert_id | Get location trail |
| POST | /api/media | Upload evidence file |
| GET  | /api/media/:alert_id | List evidence |
| POST | /api/alerts/:id/assign | Assign officer (Admin only) |
| POST | /api/alerts/:id/resolve | Resolve alert (Admin only) |

---

## 2. Web Dashboard Setup

No build step needed — pure HTML/CSS/JS.

Open `Kali-Web/login.html` in a browser (or serve with any static server):
```bash
cd Kali-Web
npx serve .     # or just open login.html directly
```

- **Guardian login** → redirects to `guardian/index.html` (live map + evidence)
- **Admin login** → redirects to `police/index.html` (all alerts + assign/resolve)

Uses [Leaflet.js](https://leafletjs.com/) for maps (OpenStreetMap, no API key needed) and Socket.IO for real-time updates.

---

## 3. Android App Setup

**Requirements:** Android Studio Hedgehog+, JDK 17

1. Open `Kali-Android/` in Android Studio
2. Sync Gradle
3. Update `RetrofitClient.kt` BASE_URL if not using emulator (`10.0.2.2` = localhost from emulator)
4. Run on emulator or device (API 26+)

**Flow:**
- App launches → LoginActivity
- After login → SosActivity (big red SOS button)
- Tap SOS → LocationService + RecordingService start as foreground services
- SyncWorker uploads all pending data when network is available

---

## Demo Seed Data

Register users via the API:
```bash
# Victim
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Priya","email":"priya@test.com","password":"test123","role":"User","phone":"9999999999"}'

# Guardian
curl -X POST http://localhost:3000/api/auth/register \
  -d '{"name":"Anita","email":"anita@test.com","password":"test123","role":"Guardian"}' \
  -H "Content-Type: application/json"

# Admin/Police
curl -X POST http://localhost:3000/api/auth/register \
  -d '{"name":"Officer Singh","email":"admin@test.com","password":"test123","role":"Admin"}' \
  -H "Content-Type: application/json"
```
