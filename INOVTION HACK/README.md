# DevPulse — AI-Powered Developer Productivity Platform

[![Full Stack Architecture](https://img.shields.io/badge/Stack-React%20%7C%20Node.js%20%7C%20Express%20%7C%20Supabase%20PostgreSQL-0071E3?style=for-the-badge)](https://innovationhacks.in)
[![Innovation Hacks](https://img.shields.io/badge/Innovation%20Hacks-Full%20Stack%20Internship-AF52DE?style=for-the-badge)](https://innovationhacks.in)

> **DevPulse** is a production-grade, AI-powered developer productivity & sprint management platform built for the **Innovation Hacks 1-Month Full Stack Development Internship (Tasks 1 – 4 Capstone)**. It features an Apple-grade HIG frontend interface, Express REST API, live Supabase PostgreSQL data persistence, JWT authentication, and an AI-assisted task breakdown & prioritization engine.

---

## 🌟 Key Internship Tasks Completed

### Task 1 — Modern Frontend Development
* **Apple HIG Design System:** Crafted with SF Pro typography, Apple System colors (`#0071E3`, `#34C759`, `#FF9500`, `#AF52DE`), and frosted acrylic glass (`backdrop-blur-xl`).
* **Responsive Dashboard:** Dashboard overview, project cards, backlog task management, and recent activity timeline.
* **Interactive Controls:** Check-off tasks live, search with instant filter pills, status filters (`All`, `Pending`, `Completed`), and modal dialogs.
* **State Management & Edge Cases:** Dynamic loading skeleton screens, empty search state graphics, and error boundaries.

### Task 2 — Backend & REST API Development
* **Express REST Server (`server.js`):** Modular REST API running on port `5001`.
* **JWT Authentication:** `POST /api/auth/signup`, `POST /api/auth/login`, `GET /api/auth/me` with `bcryptjs` password hashing and signed JWT bearer tokens.
* **Input Validation & Error Handling:** Centralized error handling, validation, and standard HTTP status codes (`200 OK`, `201 Created`, `400 Bad Request`, `401 Unauthorized`, `500 Server Error`).

### Task 3 — Database Integration (Supabase PostgreSQL)
* **Supabase Cloud DB:** Connected via `pg` connection pool to `db.fwgcmdmxughkhddpirbb.supabase.co:5432/postgres`.
* **Schema Design & CRUD:**
  * `users` — Authentication & credentials (`id UUID`, `email`, `password_hash`, `name`, `role`, `avatar`)
  * `user_profile` — Telemetry & streak metrics
  * `projects` — Active repositories & milestones
  * `tasks` — Sprint backlog work items
  * `activities` — Git commits & deployment timeline
  * `metrics` — Velocity, tasks completed, and coding hours

### Task 4 — AI-Powered Capstone Feature Engine
* **🤖 AI Task Generator (`POST /api/ai/generate-tasks`):** Enter a high-level feature prompt (e.g. *"Build OAuth2 token rotation middleware"*), and the AI Engine automatically breaks down the goal into 3 prioritized sprint tasks with time estimates, auto-saves them into Supabase PostgreSQL, and updates the backlog live!
* **⏱️ Real OS Session Tracker:** Tracks real active laptop session time second-by-second using page visibility events, updating **Coding Hours** dynamically.

---

## 🏗️ Architecture & Technology Stack

| Layer | Technologies Used |
| :--- | :--- |
| **Frontend UI** | React 18, Vite, Tailwind CSS, Lucide React Icons |
| **Design System** | Apple Human Interface Guidelines (HIG), Glassmorphism |
| **REST API Server** | Node.js, Express, CORS, dotenv |
| **Authentication** | JSON Web Tokens (JWT), BcryptJS password hashing |
| **Database Layer** | Supabase Cloud PostgreSQL, `pg` (node-postgres) |
| **AI Capabilities** | Rule-assisted AI Task Generation & Productivity Insights Engine |

---

## 📁 Repository Structure

```text
INOVTION HACK/
├── public/                     # Favicon and static assets
├── scripts/                    # Database migration & seed scripts
│   ├── dbSetup.js              # Schema migration for projects, tasks, metrics
│   ├── authSetup.js            # User authentication table migration
│   └── clearTesterData.js      # Purges sample tester data
├── src/                        # React Frontend Source Code
│   ├── components/             # Reusable UI components
│   │   ├── Header.jsx          # Apple HIG navigation bar & search
│   │   ├── Sidebar.jsx         # macOS collapsible sidebar
│   │   ├── MetricsOverview.jsx # 4 Clickable progress stat cards
│   │   ├── ProjectGrid.jsx     # Project cards grid
│   │   ├── TaskList.jsx        # Filterable & checkable backlog
│   │   ├── WeeklyProductivityChart.jsx # 7-Day velocity bar chart
│   │   ├── ActivityFeed.jsx    # Live git commit & deployment timeline
│   │   ├── UserProfile.jsx     # Developer stats & rank card
│   │   ├── AddTaskModal.jsx    # Create task modal form
│   │   ├── AIAssistantModal.jsx# AI Task Generator modal
│   │   ├── ExportStandupModal.jsx# Standup report generator
│   │   ├── LoginModal.jsx      # Apple HIG Login/Signup screen
│   │   └── states/             # Skeleton, Empty, & Error views
│   ├── data/                   # Initial fallback data structures
│   ├── services/               # API service abstraction (telemetryService.js)
│   ├── App.jsx                 # App root & state container
│   ├── main.jsx                # React mount entry point
│   └── index.css               # Apple HIG CSS design tokens & animations
├── .env.example                # Template for environment configuration
├── package.json                # Project scripts & dependencies
├── server.js                   # Express REST API & Supabase Connection
└── vite.config.js              # Vite server configuration
```

---

## ⚡ Quick Start & Local Setup Instructions

### 1. Clone & Install Dependencies
```bash
# Install frontend & backend dependencies
npm install
```

### 2. Configure Environment Variables
Create a `.env` file in the root directory (based on `.env.example`):
```env
PORT=5001
DATABASE_URL=postgresql://postgres:Taswnama%40.3@db.fwgcmdmxughkhddpirbb.supabase.co:5432/postgres
JWT_SECRET=devpulse-super-secret-key-2026
```

### 3. Run Database Migrations (Supabase PostgreSQL)
```bash
# Setup Supabase PostgreSQL tables and auth schema
node scripts/dbSetup.js
node scripts/authSetup.js
```

### 4. Start Backend API & Frontend Development Server
```bash
# Terminal 1: Start Express REST API Backend (Port 5001)
node server.js

# Terminal 2: Start Vite Frontend (Port 3000)
npm run dev
```

Open **`http://localhost:3000`** in your browser!

---

## 🔑 Demo Account Credentials

| Field | Demo Credential |
| :--- | :--- |
| **Email** | `alex.rivera@innovationhacks.dev` |
| **Password** | `Password123!` |

*(Or click the **"Click to Fill Demo Credentials"** button on the login screen for instant access!)*

---

## 📡 REST API Documentation

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/health` | `GET` | Health check & Supabase connection status |
| `/api/auth/signup` | `POST` | Register a new user account & return JWT |
| `/api/auth/login` | `POST` | Authenticate user credentials & return JWT |
| `/api/telemetry/metrics` | `GET` | Retrieve developer productivity metrics |
| `/api/projects` | `GET / POST / DELETE` | Manage active projects in Supabase |
| `/api/tasks` | `GET / POST / PATCH / DELETE` | CRUD operations for sprint backlog tasks |
| `/api/ai/generate-tasks` | `POST` | AI-assisted task breakdown & generation |
| `/api/activities` | `GET` | Retrieve recent activity stream |

---

## 📋 Evaluation Checklist & Deliverables

- [x] **Task 1 (Frontend):** Modern Apple HIG responsive dashboard with dynamic states.
- [x] **Task 2 (REST API):** Node.js Express API with validation, status codes, and JWT auth.
- [x] **Task 3 (Database):** Supabase PostgreSQL database integration with persistent CRUD.
- [x] **Task 4 (AI Capstone):** AI Task Generator & Live OS Session Telemetry Engine.
- [x] **GitHub Quality:** Clean `README.md`, `.env.example`, modular components.

---

*Built with ❤️ for the Innovation Hacks Full Stack Development Internship.*
