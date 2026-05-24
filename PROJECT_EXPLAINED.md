# KALI – Women's Safety App: Complete Project Explanation

## What Is KALI?

KALI is a full-stack women's safety application. When a woman feels unsafe, she taps a single SOS button on her Android phone. The app immediately:
1. Creates an emergency alert on the server
2. Starts streaming her GPS location every 5 seconds
3. Starts recording audio evidence
4. Notifies her Guardian (family member) in real time
5. Makes the alert visible to Police/Admin on a live map

The system has three layers: an Android app (victim), a web dashboard (guardian + police), and a Node.js backend that connects them all.

---

## Project Structure

```
Kali 4.0/
├── Kali-Backend/       Node.js REST API + Socket.IO server
├── Kali-Web/           Browser dashboards (no build step needed)
├── Kali-Android/       Android app (Kotlin)
├── Doc/                Project report and screenshots
├── README.md           Quick-start guide
├── start.bat           One-click Windows launcher
└── PROJECT_EXPLAINED.md  ← this file
```

---

## Part 1: The Backend (Kali-Backend)

### Technology Stack
- **Runtime:** Node.js 18+
- **Framework:** Express.js
- **Database:** PostgreSQL (via `pg` library)
- **Real-time:** Socket.IO
- **Auth:** JWT (JSON Web Tokens) + bcryptjs for password hashing
- **File uploads:** Multer
- **Security:** Helmet, CORS, express-rate-limit

### How It Starts (`src/server.js`)
1. Loads environment variables from `.env` (DB credentials, JWT secret, port)
2. Creates an Express app wrapped in an HTTP server
3. Attaches Socket.IO to that HTTP server
4. Applies security middleware: Helmet (HTTP headers), CORS (allowed origins), rate limiting on `/api/auth` (max 100 requests per 15 minutes)
5. Calls `initDb()` to create all database tables if they don't exist
6. Starts listening on port 3000 (or `PORT` from env)

### Database Schema (`src/config/initDb.js`)
Six tables are auto-created on first run:

| Table | Purpose | Key Columns |
|-------|---------|-------------|
| `users` | All accounts | id, name, email, phone, password_hash, role (User/Guardian/Admin), guardian_id, fcm_token |
| `alerts` | SOS events | id, user_id, status (active/resolved), timestamp, resolved_at, resolve_note |
| `locations` | GPS trail per alert | id, alert_id, latitude, longitude, accuracy, timestamp |
| `media` | Audio/video evidence | id, alert_id, file_url, media_type, file_hash, upload_status |
| `emergency_contacts` | Victim's contacts | id, user_id, name, phone, relation, is_primary |
| `audit_log` | Tamper-proof log | id, alert_id, action, user_id, entry_hash, timestamp |

The `guardian_id` column in `users` links a victim to their guardian. When a victim triggers SOS, the server looks up their `guardian_id` and sends a targeted Socket.IO event to that guardian's browser.

Demo accounts are seeded automatically:
- `user@kali.com` / `user123` → Victim (Priya Sharma)
- `guardian@kali.com` / `guardian123` → Guardian (Raj Sharma)
- `admin@kali.com` / `admin123` → Police Inspector

### Authentication (`src/middleware/auth.js`)
Every protected route passes through `authenticateToken`:
- Reads the `Authorization: Bearer <token>` header
- Verifies the JWT using `JWT_SECRET` from env
- Attaches `{ id, role }` to `req.user`

`requireRole(...roles)` is a second middleware used on admin-only routes (e.g., resolve/assign alerts).

### API Routes & Controllers

#### Auth (`/api/auth`)
| Endpoint | What it does |
|----------|-------------|
| `POST /register` | Hashes password with bcrypt (cost 10), inserts user, returns user object |
| `POST /login` | Compares password hash, signs a 7-day JWT containing `{ userId, role }` |
| `POST /google-login` | Creates account if first Google sign-in, returns JWT |
| `GET /profile` | Returns logged-in user's profile |
| `PUT /profile` | Updates name, phone, or FCM token |

#### Alerts (`/api/alerts`)
| Endpoint | What it does |
|----------|-------------|
| `POST /` | Creates alert row, optionally inserts first GPS point, writes audit log, broadcasts `newAlert` via Socket.IO to all clients AND specifically to the guardian's room |
| `GET /` | Lists alerts — Users see only their own, Guardians see their linked user's alerts, Admins see all |
| `GET /my` | Shortcut for a user's own alerts |
| `GET /:id` | Single alert with user info joined |
| `POST /:id/resolve` | Sets status to `resolved`, logs audit, broadcasts `alertResolved` |
| `POST /:id/assign` | Logs officer assignment, broadcasts `officerAssigned` |

#### Locations (`/api/locations`)
| Endpoint | What it does |
|----------|-------------|
| `POST /` | Inserts a GPS point, computes **speed** (Haversine distance ÷ time elapsed) and **bearing** (compass direction) from the previous point, broadcasts enriched `locationUpdate` event |
| `POST /batch` | Accepts an array of GPS points (for offline sync), inserts all, broadcasts only the latest |
| `GET /:alert_id` | Returns all points for an alert in chronological order |
| `GET /:alert_id/stats` | Computes total distance, average speed, max speed, duration, point count, last bearing |

The Haversine formula is used throughout to compute great-circle distances between two lat/lng coordinates on Earth's surface.

#### Media (`/api/media`)
| Endpoint | What it does |
|----------|-------------|
| `POST /` | Accepts file upload via Multer, computes SHA-256 hash of the file (for tamper detection), stores file in `/uploads/`, saves record to DB |
| `GET /:alert_id` | Returns all media records for an alert |

The SHA-256 hash is a forensic integrity feature — it proves the file hasn't been modified since upload.

#### Other Routes
- `GET /api/health` — Simple health check, returns `{ status: 'ok', time: ... }`
- `/api/contacts` — CRUD for a victim's emergency contacts
- `/api/guardian` — Guardian-specific endpoints (get linked user, get active alert, link to user)
- `/api/simulate` — Simulation endpoints for demo/testing

### Real-time with Socket.IO
The server creates named rooms per user: when a browser connects and calls `socket.emit('join', userId)`, it joins room `user_<userId>`. This allows targeted notifications:
- `newAlert` → broadcast to all + specifically to `user_<guardianId>`
- `locationUpdate` → broadcast to all (both guardian and police dashboards update)
- `alertResolved` → broadcast to all
- `officerAssigned` → broadcast to all

### Audit Log (`src/utils/audit.js`)
Every significant action (alert created, alert resolved, officer assigned) is written to `audit_log` with a hash. This creates a tamper-evident chain of custody for legal/forensic purposes.

---

## Part 2: The Web Dashboards (Kali-Web)

Pure HTML/CSS/JavaScript — no framework, no build step. Open in any browser.

### File Structure
```
Kali-Web/
├── login.html          Entry point for all users
├── register.html       Account creation
├── css/style.css       Shared dark-theme styles
├── js/api.js           Shared API client library
├── user/index.html     Victim's web view
├── guardian/index.html Guardian dashboard
└── police/index.html   Police/Admin dashboard
```

### Shared API Client (`js/api.js`)
All pages include this file. It provides:
- `login(email, password)` — POSTs to `/api/auth/login`, stores token/role/name in `localStorage`
- `logout()` — Clears localStorage, redirects to login
- `requireAuth(role)` — Checks token exists; redirects to login if not
- `apiGet(path)`, `apiPost(path, body)`, `apiPut(path, body)`, `apiDelete(path)` — Fetch wrappers that automatically attach the `Authorization` header
- Helper functions: `formatDate()`, `timeElapsed()`, `formatDuration()`
- Convenience wrappers: `getAlerts()`, `createAlert()`, `resolveAlert()`, `getContacts()`, etc.

### Login Page (`login.html`)
- Animated UI with pulsing orbs and floating shield icon
- Auto-redirects if already logged in (checks localStorage)
- Shows success banner if redirected from registration
- Demo quick-fill buttons for all three roles
- On successful login, routes by role:
  - `User` → `user/index.html`
  - `Guardian` → `guardian/index.html`
  - `Admin` → `police/index.html`

### Guardian Dashboard (`guardian/index.html`)
This is the most technically complex page. It shows a live map of the victim's location.

**Layout:** 3-column grid — Alert list | Live Map | Evidence + Safety Monitor

**Advanced Tracking Engine (Extended Kalman Filter):**
The dashboard implements a full EKF in vanilla JavaScript. GPS signals are noisy; the EKF smooths them by maintaining a state vector `[lat, lng, velocityLat, velocityLng]` and a 4×4 covariance matrix. On each new GPS point:
1. **Predict step:** Projects the state forward using the state transition matrix F (constant velocity model)
2. **Update step:** Corrects the prediction using the new measurement, weighted by the Kalman gain K

This produces smooth, physically plausible movement instead of jittery GPS jumps.

**Features:**
- **Directional marker:** A pulsing pink circle with an arrow that rotates to show the direction of movement (bearing from EKF velocity vector)
- **Gradient trail:** The movement path is drawn as 3 polyline segments — old (20% opacity), middle (50%), recent (90%) — giving a visual fade effect
- **Predictive trajectory:** 3 cyan dots showing where the person will be in 5, 10, and 15 seconds based on current velocity
- **Geofence:** A 500m dashed circle drawn around the SOS origin point. If the person moves outside it, a flashing red warning appears
- **Safety Score (0–100):** Computed from speed (high speed = penalty), distance from SOS origin (far = penalty), and time of day (night hours = penalty)
- **Movement pattern classification:** Stationary (<1 km/h), Walking (<7), Running (<30), In Vehicle (>30)
- **Smart auto-pan:** Map follows the marker unless the user has manually dragged the map (resets after 10 seconds)
- **Connection quality indicator:** Shows connected/disconnected status, last update time, and updates-per-minute frequency
- **Animation queue:** Marker movements are queued and animated with cubic ease-out interpolation, speed-adaptive duration (slow movement = 2s animation, fast = 0.5s)

**Socket.IO events handled:**
- `newAlert` → reload alert list, show browser notification
- `locationUpdate` → run EKF update, animate marker, update trail/predictions/safety dashboard
- `alertResolved` → reload alert list

### Police Dashboard (`police/index.html`)
Similar to Guardian but with additional admin capabilities.

**Layout:** 2-column — Alert list + Case detail panel | Live Map

**Additional features over Guardian:**
- Shows ALL alerts (not just linked user's)
- Case detail panel with full victim info
- **Telemetry HUD:** Shows current speed, average speed, max speed, total distance traveled, bearing/heading, estimated rescue ETA (simulated dispatch squad distance ÷ 40 km/h), and EKF prediction confidence
- **Signal quality bar:** Visual bar derived from EKF estimation error (lower covariance = better signal quality)
- **Assign Officer:** Input field + button to assign an officer ID to an alert
- **Resolve button:** Marks alert as resolved, hides detail panel
- Uses full 4×4 matrix inverse (general cofactor expansion) for the EKF, vs. the guardian's optimized 2×2 inverse

---

## Part 3: The Android App (Kali-Android)

### Technology Stack
- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel)
- **Local DB:** Room (SQLite wrapper)
- **Background work:** WorkManager
- **Networking:** Retrofit 2 + OkHttp
- **Location:** Google Play Services FusedLocationProvider / Android LocationManager
- **Maps:** OSMDroid (OpenStreetMap, no API key)
- **Min SDK:** 26 (Android 8.0)

### App Flow
```
Launch → LoginActivity
           ↓ (on success)
         SosActivity  ←──────────────────────────────┐
           ↓ (tap SOS button)                         │
         Creates AlertEntity in Room DB               │
         Starts LocationService (foreground)          │
         Starts RecordingService (foreground)         │
         SyncWorker uploads everything to server      │
           ↓ (tap Stop)                               │
         Stops services → back to SosActivity ────────┘
```

### Local Database (`model/Entities.kt`)
Room entities mirror the server schema but are local-first:

- **AlertEntity:** `localId` (auto-increment), `serverId` (null until synced), lat/lng of SOS origin, status, `synced` flag
- **LocationEntity:** `alertLocalId` (foreign key to local alert), lat/lng, timestamp, `synced` flag
- **MediaEntity:** `alertLocalId`, `filePath` (local file path), `mediaType`, `synced` flag

The `synced` flag is the key to offline-first operation. Everything is saved locally first, then uploaded when network is available.

### Location Service (`service/LocationService.kt`)
Runs as a foreground service (shows persistent notification "KALI – SOS Active"):
- Requests GPS updates every 5 seconds / 5 meters
- On each location fix, runs it through the **ExtendedKalmanFilter** (same algorithm as the web dashboards, implemented in Kotlin)
- Saves the filtered position to Room DB
- Calls `SyncWorker.enqueue()` to trigger an upload attempt

### Recording Service (`service/RecordingService.kt`)
Runs as a foreground service with microphone type:
- Records audio using Android's MediaRecorder
- Saves to a local file
- Saves a MediaEntity to Room DB for later sync

### Sync Worker (`service/SyncWorker.kt`)
A WorkManager `CoroutineWorker` that runs when network is available:
1. **syncAlerts():** Finds unsynced alerts, POSTs to `/api/alerts`, saves returned `serverId` back to Room
2. **syncLocations():** Finds unsynced locations, looks up the corresponding `serverId`, POSTs to `/api/locations`
3. **syncMedia():** Finds unsynced media files, builds a multipart form upload, POSTs to `/api/media`

If any step throws an exception, it returns `Result.retry()` — WorkManager will try again with exponential backoff.

`enqueue()` uses `ExistingWorkPolicy.KEEP` — if a sync is already queued, it won't queue another one.

### Activities
- **LoginActivity:** Email/password form, calls `/api/auth/login`, saves token to `SessionManager` (SharedPreferences wrapper)
- **RegisterActivity:** Registration form with role selection
- **SosActivity:** The main screen — big red SOS button. Tap starts both services and creates a local alert. Shows active alert status.
- **TrackingActivity:** Shows the victim's own trail on an OSMDroid map
- **ProfileActivity:** View/edit profile, manage emergency contacts
- **DecoyActivity:** A fake "calculator" or innocent-looking screen that can be launched to hide the app from an abuser

### SessionManager (`util/SessionManager.kt`)
Wraps SharedPreferences to store/retrieve: JWT token, user ID, user role, user name.

### ExtendedKalmanFilter (`util/ExtendedKalmanFilter.kt`)
Kotlin implementation of the same EKF used in the web dashboards. State: `[lat, lng, vLat, vLng]`. Used in LocationService to smooth GPS noise before saving to DB.

---

## Data Flow: End-to-End SOS Scenario

```
1. Victim taps SOS on Android
   → AlertEntity saved to Room (synced=false)
   → LocationService starts, RecordingService starts
   → SyncWorker runs:
       POST /api/alerts → server creates alert, returns alert_id
       Room updated: synced=true, serverId=<alert_id>

2. GPS fix arrives every 5 seconds
   → EKF filters the raw GPS
   → LocationEntity saved to Room (synced=false)
   → SyncWorker runs:
       POST /api/locations { alert_id, lat, lng }
       Server computes speed & bearing from previous point
       Server emits Socket.IO "locationUpdate" to all connected browsers
       Room updated: synced=true

3. Guardian's browser receives "locationUpdate"
   → EKF update in browser (second layer of smoothing)
   → Marker animates to new position
   → Trail redrawn, predictions updated, safety score recalculated

4. Police dashboard also receives "locationUpdate"
   → Same marker animation
   → Telemetry HUD updates (speed, bearing, distance, ETA)

5. Audio recording saved locally
   → SyncWorker uploads as multipart to POST /api/media
   → SHA-256 hash computed server-side for forensic integrity
   → Guardian can play audio in evidence panel

6. Police Admin clicks "Resolve"
   → POST /api/alerts/:id/resolve
   → Audit log entry written
   → Socket.IO "alertResolved" broadcast
   → Both dashboards reload alert list
```

---

## Security Features

| Feature | Where | How |
|---------|-------|-----|
| Password hashing | Backend auth | bcrypt with cost factor 10 |
| JWT authentication | All protected routes | 7-day tokens, verified on every request |
| Role-based access | Middleware | `requireRole()` blocks non-admins from resolve/assign |
| Rate limiting | `/api/auth` | Max 100 requests per 15 minutes per IP |
| HTTP security headers | All routes | Helmet middleware |
| CORS control | All routes | Configurable allowed origins via env |
| File integrity | Media uploads | SHA-256 hash stored with each file |
| Audit trail | Alert lifecycle | Tamper-evident log with hashed entries |
| Decoy screen | Android app | Hides app from abusers |
| Offline-first | Android app | Data saved locally before upload |

---

## Key Technical Concepts

### Extended Kalman Filter (EKF)
Used in 3 places: Android LocationService, Guardian dashboard, Police dashboard. GPS receivers have noise of ±5–15 meters. The EKF models movement as a constant-velocity system and uses Bayesian estimation to produce smooth, physically realistic positions. The state `[lat, lng, vLat, vLng]` also gives velocity for free, which is used to compute speed and bearing without needing two consecutive raw GPS points.

### Offline-First Architecture
The Android app never depends on network availability. Every piece of data (alert, location, media) is saved to Room DB first. WorkManager handles upload with automatic retry and backoff. This is critical for safety apps — the victim may be in a low-signal area.

### Real-Time with Socket.IO
The server maintains persistent WebSocket connections with all connected browsers. When a new GPS point arrives via REST API, the server immediately pushes it to all dashboards via Socket.IO. This gives sub-second latency from phone → guardian's screen.

### Haversine Formula
Used in both backend (locationController) and frontend (both dashboards) to compute the straight-line distance between two GPS coordinates on Earth's curved surface. Used for: speed calculation, distance-from-SOS-origin, geofence breach detection, rescue ETA estimation.

---

## Environment Configuration

The backend requires a `.env` file in `Kali-Backend/`:

```
PORT=3000
DATABASE_URL=postgresql://user:password@localhost:5432/kali
JWT_SECRET=your_secret_key_here
ALLOWED_ORIGINS=http://localhost:5500,http://127.0.0.1:5500
```

The Android app's `RetrofitClient.kt` has `BASE_URL = "http://10.0.2.2:3000/api/"` — `10.0.2.2` is the Android emulator's alias for the host machine's localhost.

---

## Demo Credentials

| Role | Email | Password | Access |
|------|-------|----------|--------|
| Victim (User) | user@kali.com | user123 | Android app + user web view |
| Guardian | guardian@kali.com | guardian123 | Guardian dashboard |
| Police/Admin | admin@kali.com | admin123 | Police dashboard (all alerts) |

Web demo quick-fill uses `victim@kali.app` / `guardian@kali.app` / `admin@kali.app` with password `password123` — register these separately if needed.
