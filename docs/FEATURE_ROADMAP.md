# Job Portal — Feature roadmap & implementation guide

This document lists **optional features** you can add to the existing stack (**Angular** + **Spring Boot** + **MySQL** + **JWT**). Each item includes **where to change code** and **rough effort** so you can pick and implement in order.

---

## Already in the project (baseline)

| Area | What exists |
|------|-------------|
| Auth | Register, login, JWT, roles: `ADMIN`, `RECRUITER`, `CANDIDATE` |
| Jobs | CRUD (recruiter/admin), list + search, job detail (login required) |
| Applications | Apply with cover letter + resume, status (APPLIED / SHORTLISTED / REJECTED), recruiter resume download |
| Admin | Dashboard stats, jobs × application counts |
| Profile | User profile, optional profile photo |
| Mail | SMTP hooks for application status (configurable) |

---

## Tier 1 — High value, fits current architecture

### 1. Forgot password / reset password — **Done** (2026-04-03)

- **User value:** Users recover access without admin help.
- **Backend:** New endpoints e.g. `POST /api/auth/forgot-password` (email), `POST /api/auth/reset-password` (token + new password). Store **hashed reset token** + expiry on `User` or separate table. Reuse `JavaMail` if SMTP is on.
- **Frontend:** Pages `Forgot password` → `Reset password?token=…`.
- **Effort:** Medium.
- **Implemented:** `User.passwordResetTokenHash` / `passwordResetExpiresAt`; `POST /api/auth/forgot-password`, `POST /api/auth/reset-password`; email link uses `app.frontend.base-url`; routes `/forgot-password`, `/reset-password`.

### 2. Email when a candidate applies (recruiter + optional candidate confirmation) — **Done** (2026-04-03)

- **User value:** Recruiters see applications immediately in inbox.
- **Backend:** After successful `apply()`, call mailer with job title + candidate summary (no resume in email; link to portal).
- **Config:** `app.mail.enabled=true` and SMTP already sketched in `application.properties`.
- **Effort:** Small–medium.
- **Implemented:** `ApplicationStatusMailer` sends to all `ADMIN`/`RECRUITER` emails plus candidate confirmation when `app.mail.enabled=true`.

### 3. Job closing date + “Closed” jobs — **Done** (2026-04-03)

- **User value:** Stops applications after deadline.
- **Backend:** Add `closingAt` (or `status` OPEN/CLOSED) on `Job`. Validate on apply; filter closed jobs on public list if desired.
- **Frontend:** Date picker on job form; badge on job cards; disable apply when closed.
- **Effort:** Medium.
- **Implemented:** `Job.closingAt`; public job list excludes past deadlines; `apply` rejects closed jobs; job form `datetime-local`; detail shows deadline and hides apply when `closed`.

### 4. Export applications (CSV) for a job — **Done** (2026-04-03)

- **User value:** Recruiters analyze in Excel.
- **Backend:** `GET /api/applications/jobs/{jobId}/export` → CSV stream, `ADMIN`/`RECRUITER` only.
- **Frontend:** Button on job applications page → download file.
- **Effort:** Small.
- **Implemented:** UTF-8 CSV with BOM; columns id, candidate, status, appliedAt, cover letter, resume filename.

### 5. Saved jobs (bookmarks) for candidates — **Done** (2026-04-03)

- **User value:** Candidates return to interesting roles.
- **Backend:** Table `saved_jobs(user_id, job_id)` + list/add/remove APIs.
- **Frontend:** “Save” on job detail / list; page “Saved jobs”.
- **Effort:** Medium.
- **Implemented:** `saved_jobs` entity; `GET/POST/DELETE /api/saved-jobs/...`; job detail save toggle; `/saved-jobs` page.

---

## Tier 2 — Product polish

### 6. Skills / tags on jobs + filter

- **Backend:** `job_skills` or JSON column; filter in `JobRepository.search`.
- **Frontend:** Chips on job card; filter in home search.
- **Effort:** Medium.

### 7. Rich job description (markdown or sanitized HTML)

- **Frontend:** Textarea → preview; use a sanitizer if HTML.
- **Backend:** Store as text; optional validation length.
- **Effort:** Small–medium.

### 8. In-app notifications list

- **Backend:** `notifications` table + mark read; optional WebSocket later; start with poll or fetch on navigation.
- **Frontend:** Bell icon in header + dropdown.
- **Effort:** Medium–large.

### 9. Audit log (admin)

- **Backend:** Log important actions (who posted/edited job, status changes) in `audit_log` or use Spring `@Aspect` + DB append.
- **Effort:** Medium.

### 10. File storage for resumes (S3 / MinIO) instead of local disk only

- **User value:** Safer for production / multiple servers.
- **Backend:** Abstraction over `ResumeStorageService`; env-based provider.
- **Effort:** Large (DevOps + code).

---

## Tier 3 — Advanced / longer term

| Feature | Notes |
|--------|--------|
| **OAuth2 (Google / LinkedIn login)** | Spring Security OAuth2 client + Angular redirect flow |
| **Full-text search** | DB `FULLTEXT` or OpenSearch/Elasticsearch for big scale |
| **Interview scheduling** | New entities: slots, invitations, calendar links (iCal) |
| **Messaging** | Thread per application; WebSocket or polling |
| **Multi-tenant companies** | `Company` entity; recruiters belong to company; jobs scoped |
| **Rate limiting & CAPTCHA** | Bucket4j / Spring Cloud Gateway; reCAPTCHA on register/apply |
| **Mobile app** | Reuse REST API; Flutter or native |
| **Docker Compose** | `mysql` + `backend` + `frontend` nginx for local/prod parity |

---

## Suggested implementation order

1. **Job closing date** — clear business rule, touches one main entity.  
2. **Export CSV** — quick win for recruiters.  
3. **Apply / status emails** — uses existing mail hooks.  
4. **Forgot password** — broad user impact.  
5. **Saved jobs** — candidate retention.  
6. **Skills + filter** — discovery.  
7. **Notifications** — engagement (after core flows are stable).

---

## Per-feature checklist (reuse for each ticket)

- [ ] Database migration / entity fields  
- [ ] API contract (DTOs, validation, security `@PreAuthorize`)  
- [ ] Angular models + service methods  
- [ ] UI + empty/error states  
- [ ] Manual test with `ADMIN` / `RECRUITER` / `CANDIDATE`  
- [ ] Update this doc with “Done” and date (optional)

---

## Key files (reference)

| Layer | Path |
|-------|------|
| Backend API | `src/main/java/com/example/jobportal/controller/` |
| Security | `SecurityConfig.java`, JWT filter |
| Frontend routes | `frontend/src/app/app.routes.ts` |
| API base URL | `frontend/src/app/core/config.ts` |

---

*Last updated: use this file as a living backlog; trim or add rows as you scope work.*
