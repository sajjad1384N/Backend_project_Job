# Deploying Job Portal (Angular + Spring Boot + MySQL)

This guide moves the project from local development to a typical production setup: HTTPS, a public database, and environment-based secrets.

**Deploying on Render:** see **[RENDER.md](./RENDER.md)** for a minimal two-service layout (Static Site + Web Service), external MySQL, and env vars.

**Deploying on Railway:** see **[RAILWAY.md](./RAILWAY.md)** for connecting MySQL via `MYSQL_URL` (`${{ MySQL.MYSQL_URL }}`) and Spring configuration.

## 1. What you deploy

| Piece | Role |
|--------|------|
| **MySQL** | Hosted database (RDS, Cloud SQL, managed VPS MySQL, etc.) |
| **Spring Boot JAR** | API on a server or container (`/api`, file uploads) |
| **Angular static files** | Built with `ng build`; served by nginx, S3+CloudFront, Netlify, etc. |

## 2. Backend (Spring Boot)

### Build

```bash
cd Job-project
./mvnw -DskipTests package
```

Artifact: `target/*.jar`

### Run on the server

```bash
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL='jdbc:mysql://YOUR_DB_HOST:3306/job_portal?useSSL=true&serverTimezone=UTC'
export SPRING_DATASOURCE_USERNAME=...
export SPRING_DATASOURCE_PASSWORD=...
export APP_JWT_SECRET='long-random-base64-secret-DO-NOT-REUSE-DEV-KEY'
export APP_CORS_ALLOWED_ORIGINS='https://jobs.yourdomain.com'
export JOBPORTAL_MAIL_ENABLED=true
export MAIL_USERNAME=...
export MAIL_PASSWORD=...
export APP_FRONTEND_BASE_URL='https://jobs.yourdomain.com'
java -jar job-portal-backend-*.jar
```

**Important**

- **Never** use the dev JWT secret or DB password from `application.properties` in production.
- Set **`app.cors.allowed-origins`** (env: `APP_CORS_ALLOWED_ORIGINS`) to your **exact** frontend origin (scheme + host + port), e.g. `https://jobs.yourdomain.com` — no trailing slash.
- **`app.frontend.base-url`** (env: `APP_FRONTEND_BASE_URL`) is used in password-reset emails; it must be your public Angular URL.
- **`jobportal.mail.*`** and **`spring.mail.*`** for real emails (Gmail App Password or transactional provider).
- Persist **`uploads/resumes`** and **`uploads/profile-images`** on disk or move to object storage later; put them on a volume that survives restarts.

Spring Boot maps env vars to properties, e.g. `APP_CORS_ALLOWED_ORIGINS` → `app.cors.allowed-origins` (relaxed binding).

### Production profile

`application-prod.properties` turns off SQL logging and defaults `ddl-auto` to `validate`. Set `JPA_DDL_AUTO=none` if you manage schema with migrations.

## 3. Frontend (Angular)

### Point the UI at your API

1. Edit **`frontend/src/environments/environment.prod.ts`** and set `apiBaseUrl` to your public API, e.g. `https://api.yourdomain.com/api` (must end with `/api` to match the backend context).

2. Build:

```bash
cd frontend
npm ci
npx ng build --configuration production
```

3. Deploy the contents of **`dist/job-portal-ui/browser`** (or `dist/job-portal-ui` depending on Angular version) to your static host or nginx `root`.

### Same-origin option

You can put the API and SPA behind one domain (e.g. nginx: `/` → Angular, `/api` → Spring Boot). Then `apiBaseUrl` can be `/api` (relative). Adjust `config.ts` / environment if you choose that pattern.

## 4. Reverse proxy (nginx) sketch

TLS termination at nginx; proxy to Spring on `127.0.0.1:8080`:

```nginx
server {
    listen 443 ssl http2;
    server_name api.yourdomain.com;
    # ssl_certificate / ssl_certificate_key ...

    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Enable **`server.forward-headers-strategy=framework`** (or `native`) in Spring if you need correct redirects/schemes behind a proxy.

## 5. Checklist before go-live

- [ ] Strong random **`app.jwt.secret`** (env `APP_JWT_SECRET`)
- [ ] MySQL user with least privilege; DB reachable only from app network
- [ ] **`app.cors.allowed-origins`** = production frontend URL only
- [ ] **`app.frontend.base-url`** = production site URL (password reset links)
- [ ] **`jobportal.mail.enabled`** + SMTP for real mail
- [ ] HTTPS everywhere
- [ ] Firewall: only 80/443 public; DB not exposed to internet if possible
- [ ] Backups for MySQL and upload directories

## 6. Optional: Docker / PaaS

The FEATURE_ROADMAP Tier 3 mentions Docker Compose — you can package the JAR + MySQL + nginx in Compose or use **Railway**, **Render**, **Fly.io**, **AWS ECS**, etc. The same env vars apply; mount volumes for `uploads/`.

---

*For local development, keep using `environment.ts` and `http://localhost:8080/api`.*
