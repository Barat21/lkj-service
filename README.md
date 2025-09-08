# Van Rental Service (JPA) - Spring Boot

This project is a Spring Boot REST API for the Van Rental app using **Spring Data JPA** and **Supabase (Postgres)** as the database.

## How to run

1. Set your Supabase DB password in an environment variable:
   ```bash
   export DB_PASSWORD=your_supabase_db_password
   ```
   On Windows (PowerShell):
   ```powershell
   $env:DB_PASSWORD = 'your_supabase_db_password'
   ```

2. Build and run:
   ```bash
   ./mvnw clean package
   ./mvnw spring-boot:run
   ```

3. API endpoints:
   - `GET /api/trips`
   - `POST /api/trips`
   - `PUT /api/trips/{id}`
   - `DELETE /api/trips/{id}`
   - `GET /api/trips/search?query=...`
   - `GET /api/trips/filter?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD&vanNumber=VAN-101`

## Notes
- The `application.properties` uses `spring.jpa.hibernate.ddl-auto=update` to create/update tables automatically.
- The DB password is read from the `DB_PASSWORD` environment variable or you can replace the placeholder in `application.properties`.
