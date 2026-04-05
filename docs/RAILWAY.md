# Railway (Spring Boot + MySQL)

Railway runs your **Web Service** (this backend) and can run a **MySQL** database in the same project. MySQL exposes connection details as environment variables on the database service.

## Connect MySQL to your Spring Boot service

1. Add a **MySQL** database from the Railway project (**+ New → Database → MySQL**).
2. Open your **Spring Boot** service → **Variables**.
3. Click **Add variable** (or **Connect** / reference another service, depending on UI).
4. Add a variable so your app receives Railway’s `MYSQL_URL`:

   | Variable name | Value (use Railway’s variable reference UI) |
   |---------------|---------------------------------------------|
   | `MYSQL_URL` | `${{ MySQL.MYSQL_URL }}` |

   The exact reference syntax may show as **`MySQL`** (service name) **`.`** **`MYSQL_URL`**. Pick the MySQL service and the `MYSQL_URL` variable from the dropdown.

5. Redeploy the Spring Boot service.

This app includes **`RailwayMysqlEnvironmentPostProcessor`**, which turns Railway’s `mysql://user:pass@host:port/db` URL into Spring’s `jdbc:mysql://...` plus username/password. You do **not** need to paste JDBC syntax by hand.

### Alternative: individual variables (no `MYSQL_URL`)

If you prefer not to use `MYSQL_URL`, Railway also exposes:

- `MYSQLHOST`, `MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD`, `MYSQLDATABASE`

Those are already wired in `application.properties` via `${MYSQLHOST:localhost}` etc. You can add each to your Spring service with references like `${{ MySQL.MYSQLHOST }}`, or Railway may inject them when you **link** the database to the service (check the Variables tab after connecting).

### If you set `SPRING_DATASOURCE_URL` yourself

If you set **`SPRING_DATASOURCE_URL`** to a full **`jdbc:mysql://...`** URL, that value wins and the `MYSQL_URL` converter is **skipped**.

Do **not** set `SPRING_DATASOURCE_URL` to `${{ MySQL.MYSQL_URL }}` raw — that value is `mysql://`, not `jdbc:mysql://`. Use either:

- **`MYSQL_URL`** = `${{ MySQL.MYSQL_URL }}` (recommended with this project), or  
- **`SPRING_DATASOURCE_URL`** = a full JDBC string you build yourself.

## Other env vars (same as Render)

Set at least:

- `PORT` — Railway sets automatically; app uses `server.port=${PORT:8080}`.
- `APP_CORS_ALLOWED_ORIGINS` — your deployed frontend origin(s).
- `APP_JWT_SECRET` — strong secret for production.
- `APP_FRONTEND_BASE_URL` — public URL of your Angular app (password reset links).
- Mail: `JOBPORTAL_MAIL_ENABLED`, `MAIL_USERNAME`, `MAIL_PASSWORD`, etc.

## Uploads (resumes / profile images)

Railway’s filesystem is **ephemeral**. Plan for a volume or object storage for production, or accept data loss on redeploy.
