# Deploying on Render

Render gives you a **Static Site** (Angular) and a **Web Service** (Spring Boot). It does **not** host MySQL; use an external MySQL (PlanetScale, Aiven, Railway MySQL, RDS, etc.) and point `SPRING_DATASOURCE_URL` at it.

## Layout (two public URLs)

| Service | Example URL | What it is |
|--------|----------------|------------|
| **Static Site** | `https://job-portal.onrender.com` | Built Angular app (HTML/JS/CSS) |
| **Web Service** | `https://job-portal-api.onrender.com` | Spring Boot API (`/api/...`) |

Use **two domains** so the browser calls the API on a different origin; CORS must allow the Static Site origin on the API.

**Alternative (one domain):** Put both behind Cloudflare or another reverse proxy that routes `/` → static and `/api` → backend. That needs more infra; the two-Render-service layout is the simplest on Render alone.

## 1. MySQL (external)

1. Create a MySQL database elsewhere (e.g. **PlanetScale** or **Aiven** MySQL).
2. Note JDBC URL, user, password, and **SSL** query params if required (often `?sslMode=REQUIRED` or similar for cloud hosts).

## 2. Backend — Web Service (Docker)

1. Push this repo (`Job-project`) to GitHub/GitLab/Bitbucket.
2. In Render: **New → Web Service**, connect the repo, **root directory** = repository root (where `Dockerfile` lives).
3. **Runtime:** Docker (Render will use `./Dockerfile`).
4. **Instance type:** Free tier is OK for testing (cold starts after idle).

### Environment variables (Web Service)

Set these in the Render dashboard (**Environment** tab). Do **not** commit secrets.

| Key | Example / notes |
|-----|-------------------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://HOST:3306/DB?useSSL=true&serverTimezone=UTC` |
| `SPRING_DATASOURCE_USERNAME` | DB user from your provider (**required** — if missing, the app used to fall back to a local dev name and MySQL returned “Access denied”) |
| `SPRING_DATASOURCE_PASSWORD` | DB password (**required** in prod — same as above) |
| `APP_JWT_SECRET` | Long random string (new for production; not your dev key) |
| `APP_CORS_ALLOWED_ORIGINS` | `https://YOUR-STATIC-SITE.onrender.com` (exact origin, no trailing slash) |
| `APP_FRONTEND_BASE_URL` | `https://YOUR-STATIC-SITE.onrender.com` (password-reset links) |
| `JOBPORTAL_MAIL_ENABLED` | `true` if you send mail |
| `MAIL_USERNAME` | SMTP user |
| `MAIL_PASSWORD` | SMTP / App Password |
| `PORT` | **Do not set manually** — Render injects this; the app uses `server.port=${PORT:8080}` |

Optional mail / DB tuning: `SPRING_JPA_HIBERNATE_DDL_AUTO`, etc.

5. Deploy. Your API base URL will look like `https://job-portal-api.onrender.com` and the REST prefix is **`/api`** (e.g. `https://job-portal-api.onrender.com/api/auth/login`).

### Ephemeral disk (uploads)

Render’s filesystem is **ephemeral**. New resumes/profile images under `uploads/` can **disappear** on redeploy or restart. For production, add a **Render Disk** (paid) mounted at `/app/uploads` and set `app.resume.upload-dir` / `app.profile.upload-dir` to that path, or move files to S3 later.

## 3. Frontend — Static Site

Your Angular app may live in a **separate folder** (`frontend/`) or a **separate repo**. Adjust paths below.

### A. Frontend in the same repo (recommended)

Copy or move the Angular project under the backend repo, e.g. `Job-project/frontend/`, commit, and push.

1. **New → Static Site** on Render, same repo.
2. **Root directory:** `frontend` (if the Angular `package.json` is there).
3. **Build command:** `npm install && npx ng build --configuration production`
4. **Publish directory:** `dist/job-portal-ui/browser`  
   If `index.html` is not there, check `dist/job-portal-ui` (Angular version differences) and set Publish directory accordingly.

### B. Frontend in a separate repo

Create a **second Static Site** connected to the frontend repo with the same build/publish settings.

### Point the SPA at the API

Before building, set **`frontend/src/environments/environment.prod.ts`**:

```typescript
apiBaseUrl: 'https://job-portal-api.onrender.com/api',
```

Use your **actual** Web Service hostname. Commit, push, and let Render rebuild the Static Site.

## 4. Checklist

- [ ] `APP_CORS_ALLOWED_ORIGINS` matches the Static Site URL exactly (`https://...`).
- [ ] `environment.prod.ts` `apiBaseUrl` ends with **`/api`** and matches the Web Service URL.
- [ ] `APP_FRONTEND_BASE_URL` matches the Static Site URL (emails).
- [ ] JWT secret and DB credentials are production-only values.
- [ ] Test login and one API call from the deployed SPA (browser devtools → Network).

## 5. Files in this repo

- **`Dockerfile`** — builds the JAR and runs Java 17 in a slim image.
- **`application.properties`** — `server.port=${PORT:8080}` for Render’s `PORT`.
- **`application-prod.properties`** — `server.forward-headers-strategy=framework` for HTTPS behind Render’s proxy.

See also **`DEPLOYMENT.md`** for general env and security notes.

## 6. Troubleshooting deployment logs

### `Cannot resolve reference to bean 'jpaSharedEM_entityManagerFactory'` (and `userRepository`)

That message is almost never the real root cause. Spring Data JPA wires repositories to a shared `EntityManager` only **after** Hibernate’s `entityManagerFactory` starts. Scroll **up** in the Render log for the **first** `Caused by:` — typical cases:

| Root cause | What to do |
|------------|------------|
| **No DB URL on Render** | You must set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`. If they are missing, the app falls back to `localhost` in `application.properties`, which does not exist inside the Render container → connection failure → EMF never starts. |
| **DB unreachable / SSL** | Allow Render’s outbound access to your MySQL host; use the provider’s required JDBC params (e.g. `useSSL=true`, `sslMode=REQUIRED`). |
| **`ddl-auto=validate` on an empty database** | Hibernate exits before the schema exists; EMF fails; you then see the `jpaSharedEM_entityManagerFactory` / `userRepository` error. Use `update` for the first deploy, or set Render env `SPRING_JPA_HIBERNATE_DDL_AUTO=update`, then switch to `validate` when the schema is stable. |
| **`Access denied for user 'Sajjad'`** | Older builds used a local dev username when env vars were missing. Pull the latest code (no hardcoded DB user in the default config), set **`SPRING_DATASOURCE_URL`**, **`SPRING_DATASOURCE_USERNAME`**, and **`SPRING_DATASOURCE_PASSWORD`** to your provider’s values, **`SPRING_PROFILES_ACTIVE=prod`**, save env vars, and redeploy. |

After fixing the underlying error, redeploy and confirm the log shows a clean Spring banner and no Hibernate connection exception above the repository line.
