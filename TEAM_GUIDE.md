# Big Coders — Teammate Handover & Viva Preparation Guide

**Project**: AQI Monitoring & Prediction System  
**Team**: Big Coders | **Group**: 3Q15  
**Team Members**: Aryan Sharma, Aniket Raj, Chirag Lamba  

---

## 💡 Hey Team! Read This First

Don't worry if you feel out of the loop with the code. This document was written specifically for you in **simple, plain English**. 

By reading this guide (takes ~10 minutes), you will understand:
1. How to run the project on your laptop in 30 seconds.
2. How the whole application works under the hood.
3. Where every key feature is in the code (so you can open it if a professor asks to see it).
4. Exactly what to say during viva, evaluation, or project presentation.

---

## 1. Quick Start: How to Run It on Your Laptop

You do **not** need Node.js, React, or even a local MySQL server installed to test and demo this project. The system has an intelligent auto-fallback: if MySQL is not running on your laptop, it will automatically launch an embedded database loaded with sample data!

### Step 1: Open your terminal
Navigate into the project folder:
```bash
cd aqi-java-system
```

### Step 2: Start the application
Run the packaged JAR file (easiest):
```bash
java -jar target/aqi-system-1.0.0.jar
```
*(Or if you want to rebuild and run via Maven: `./mvnw spring-boot:run`)*

### Step 3: Open in your browser
Go to: **[http://localhost:8080](http://localhost:8080)**

### Step 4: Log in with any of these demo accounts:
| Username | Password | Role | What this role can do |
|---|---|---|---|
| `admin` | `admin123` | **Admin** | Can access everything (including the Admin Console to add cities/stations) |
| `analyst` | `analyst123` | **Analyst** | Can view Dashboard, Reports, CSV export, and AI Predictions |
| `operator` | `operator123` | **Operator** | Can submit new sensor readings and view/resolve alerts |
| `viewer` | `viewer123` | **Viewer** | Read-only public access to Dashboard, Predictions, and Reports |

*(Tip for the demo: Log in with `admin` so you have access to every button and page!)*

---

## 2. What Is This Project? (The 30-Second Elevator Pitch)

> *"Our project is a Java-based Environmental Monitoring & Prediction System built according to India's official Central Pollution Control Board (CPCB) methodology. It ingests sensor readings for 8 air pollutants, calculates continuous Air Quality Index (AQI) values, uses Linear Regression to predict 1-to-3 day future trends, triggers real-time alerts for dangerous pollution levels, and demonstrates advanced DBMS concepts including 3NF normalization, stored procedures with cursors, database triggers, and transactions."*

---

## 3. How Does the Architecture Work?

We built this as a **Single Spring Boot Application**. We do **not** use React or Node.js because the requirement was a pure, robust Java ecosystem.

Here is the simple journey of a web request:
```
1. USER BROWSER
   └── Types http://localhost:8080 or clicks a button
          │
          ▼
2. SPRING MVC CONTROLLER  (e.g., DashboardController)
   └── Receives the URL request and asks the Service layer for data
          │
          ▼
3. SERVICE LAYER  (e.g., AqiCalculationService, ReportService)
   └── Does the math, business logic, CPCB AQI formulas, and predictions
          │
          ▼
4. REPOSITORY LAYER  (e.g., ReadingRepository, StationRepository)
   └── Runs clean SQL queries using Spring JdbcTemplate
          │
          ▼
5. DATABASE (MySQL / Embedded Engine)
   └── Stores the tables, procedures, triggers, and history
          │
          ▼
6. THYMELEAF TEMPLATE ENGINE
   └── Injects the data into HTML and sends a finished dark-mode web page back to the browser
```

---

## 4. The 5 Core Academic Highlights (What the Professor WILL Ask)

### 🌟 1. "How do you calculate AQI?"
- **The Old Mistake**: Many basic student projects only check 3 to 6 pollutants and use integer ranges (e.g. `BETWEEN 30 AND 50`), which causes decimal concentrations like `30.5` to fail or produce errors.
- **Our Implementation**: We follow the **official CPCB India standard** using all **8 recognized pollutants**:
  1. $\text{PM}_{2.5}$ (Fine particles)
  2. $\text{PM}_{10}$ (Coarse dust)
  3. $\text{NO}_2$ (Nitrogen Dioxide)
  4. $\text{SO}_2$ (Sulphur Dioxide)
  5. $\text{CO}$ (Carbon Monoxide)
  6. $\text{O}_3$ (Ozone)
  7. $\text{NH}_3$ (Ammonia)
  8. $\text{Pb}$ (Lead)
- **The Math Formula**: Linear interpolation between concentration breakpoints:
  $$\text{Sub-Index} = \left(\frac{I_{\text{high}} - I_{\text{low}}}{C_{\text{high}} - C_{\text{low}}}\right) \times (C - C_{\text{low}}) + I_{\text{low}}$$
- **Sufficiency Rule**: Overall AQI requires **at least 3 pollutants**, and at least one must be a particulate ($\text{PM}_{2.5}$ or $\text{PM}_{10}$). If not met, the system displays *"Insufficient Data"* instead of making up a fake number.
- **Code file**: `src/main/java/com/bigcoders/aqi/service/AqiCalculationService.java`

---

### 🌟 2. "How does the Prediction work? Is it real AI or hardcoded?"
- **It is real math**: We implemented **Ordinary Least Squares (OLS) Linear Regression** in pure Java.
- **How it works**:
  - It fetches historical daily AQI values for a station over time ($x = \text{day index}$, $y = \text{AQI value}$).
  - It calculates the slope $\beta = \frac{\sum (x - \bar{x})(y - \bar{y})}{\sum (x - \bar{x})^2}$ and intercept $\alpha = \bar{y} - \beta \bar{x}$.
  - It projects AQI for Day $+1$, Day $+2$, and Day $+3$.
  - It calculates the trend (*Improving* if slope is negative, *Deteriorating* if slope is positive, *Stable* if near 0).
- **Academic Safeguard**: If a station has fewer than 7 historical days of data, the system refuses to predict and warns that at least 7 days are required for statistical significance.
- **Code file**: `src/main/java/com/bigcoders/aqi/service/AqiPredictionService.java`

---

### 🌟 3. "Show me your Advanced DBMS Concepts (Procedures, Cursors, Triggers)"
If the DBMS professor asks: *"Did you just use basic SELECT and INSERT, or did you use DBMS features?"* — you have 4 major answers:

1. **MySQL Stored Procedure with CURSOR (`sp_generate_city_report`)**:
   - *What is it?* A database procedure that opens a cursor to loop row-by-row through all monitoring stations in a chosen city, calculating minimum, maximum, and average AQI across all stations.
   - *File*: `src/main/resources/db/procedures.sql` (Line ~50)
2. **Database Trigger (`trg_reading_data_quality`)**:
   - *What is it?* An `AFTER INSERT ON reading` trigger. If a sensor inserts a reading flagged with data quality `'Poor'` or `'Invalid'`, the database automatically creates a warning alert in `alert_log`.
   - *File*: `src/main/resources/db/triggers.sql`
3. **Database Stored Functions**:
   - `fn_calculate_aqi_subindex`: Calculates pollutant sub-indices inside MySQL.
   - `fn_get_aqi_category`: Translates AQI number (e.g. 150) to category (*Moderately Polluted*).
   - *File*: `src/main/resources/db/functions.sql`
4. **Database Normalization (3NF)**:
   - 10 tables organized so there is zero repeating groups (1NF), no partial dependencies (2NF), and no transitive dependencies (3NF).
   - *File*: Check `docs/normalization.md` for full functional dependency proofs!

---

### 🌟 4. "How do Transactions work?"
- When a user submits a new reading on the `/data-entry` page:
  1. The raw reading is saved to `reading`.
  2. The station's daily AQI is recalculated.
  3. The `aqi_history` table is updated.
  4. If AQI exceeds 100/200/300, a health alert is generated (with 4-hour duplicate suppression).
- All 4 steps run inside a single Spring `@Transactional` block in `DashboardService.java`. If any step fails, the entire transaction rolls back so the database is never left in a corrupted or half-saved state (ACID compliance).

---

### 🌟 5. "How is Security handled?"
- **Spring Security 6**: URLs are protected by role.
- **Passwords**: Never stored as plain text. Every password is encrypted using the industry-standard **BCrypt hashing algorithm** with salt.
- **Role separation**:
  - Only `Admin` can access `/admin`.
  - Only `Admin` and `Operator` can submit readings on `/data-entry`.
  - `Viewer` trying to access `/admin` or `/data-entry` gets an HTTP 403 Forbidden screen.
- **Code file**: `src/main/java/com/bigcoders/aqi/security/SecurityConfig.java`

---

## 5. Cheat Sheet: "If the Professor Asks X, Open File Y"

Keep this table handy during your presentation!

| When the examiner says... | You open this file |
|---|---|
| *"Show me your database tables / schema"* | `src/main/resources/db/schema.sql` |
| *"Show me your Stored Procedure / Cursor"* | `src/main/resources/db/procedures.sql` |
| *"Show me your Database Trigger"* | `src/main/resources/db/triggers.sql` |
| *"Show me the AQI calculation formula"* | `src/main/java/com/bigcoders/aqi/service/AqiCalculationService.java` |
| *"Show me the Linear Regression prediction code"* | `src/main/java/com/bigcoders/aqi/service/AqiPredictionService.java` |
| *"Show me your SQL queries / JdbcTemplate"* | `src/main/java/com/bigcoders/aqi/repository/` (e.g., `ReadingRepository.java`) |
| *"Show me how you secure the pages / passwords"* | `src/main/java/com/bigcoders/aqi/security/SecurityConfig.java` |
| *"Show me your HTML / UI design"* | `src/main/resources/templates/dashboard.html` |
| *"Show me your ER diagram or normalization doc"* | `docs/er_diagram.md` & `docs/normalization.md` |

---

## 6. Live Presentation Demo Script (Step-by-Step)

Follow this 5-minute flow during your presentation:

1. **Log in as Admin**:
   - Go to `http://localhost:8080/login`.
   - Enter `admin` / `admin123`.
2. **Dashboard (`/`)**:
   - Point out the dark modern dashboard UI with glassmorphic cards.
   - Show the summary statistics: Total Stations, Average National AQI, Active Alerts, and Weather widget (fetched via Open-Meteo REST API).
   - Show the interactive Chart.js graph of 7-day AQI trends.
   - Point out our team names in the footer: **Big Coders — Aryan Sharma, Aniket Raj, Chirag Lamba (Group 3Q15)**.
3. **Data Entry (`/data-entry`)**:
   - Select a city (e.g., *Delhi*).
   - Notice how the station dropdown **dynamically cascades** to show only Delhi stations (ITO, Anand Vihar, etc.).
   - Choose a pollutant ($\text{PM}_{2.5}$), enter a concentration (e.g. `120.5`), select Data Quality `Good`, and click **"Record Measurement"**.
   - Show the green success message and explain that the transaction updated both the reading and recalculates the station's AQI.
4. **Reports & Analytics (`/reports`)**:
   - Show the historical records table with filtering by city and date.
   - Click the tab **"City Stored Procedure Report"**, select *Delhi*, and click **"Run Stored Procedure"**.
   - Explain to the professor: *"This just executed our MySQL Stored Procedure `sp_generate_city_report` using a database CURSOR to iterate over all active stations in Delhi."*
   - Click the green **"Export CSV"** button to show that it generates a clean downloadable CSV file.
5. **Predictions (`/predictions`)**:
   - Select a station (e.g. *ITO, Delhi*).
   - Show the OLS Linear Regression graph showing the historical 7-day line and the dashed 3-day future projection.
   - Explain the slope calculation and trend badge (*Stable / Deteriorating / Improving*).
6. **Alerts (`/alerts`)**:
   - Show the list of system alerts.
   - Click **"Resolve"** on an alert to show real-time state mutation.
7. **Admin Console (`/admin`)**:
   - Show the administrative tabs: Cities, Stations, Pollutants, and Users.
   - Mention that only users with the `Admin` role can see this tab.

---

## 7. Viva Q&A Quick Practice

**Q1: Why did you rebuild the project in Java instead of keeping React + Node.js?**  
*Answer*: Java with Spring Boot is the enterprise standard for high-reliability backend systems. By using Spring MVC and Thymeleaf, we eliminated bloated Node modules and duplicate models. Using `JdbcTemplate` allows us to demonstrate transparent, raw SQL queries and relational DBMS concepts directly without an ORM hiding the database mechanics.

**Q2: What is the CPCB AQI methodology?**  
*Answer*: The Central Pollution Control Board (CPCB) methodology calculates individual sub-indices for up to 8 pollutants ($\text{PM}_{2.5}, \text{PM}_{10}, \text{NO}_2, \text{SO}_2, \text{CO}, \text{O}_3, \text{NH}_3, \text{Pb}$) using linear interpolation breakpoints. The maximum sub-index becomes the overall AQI, provided there are at least 3 pollutants present including either $\text{PM}_{2.5}$ or $\text{PM}_{10}$.

**Q3: What are the 6 CPCB AQI categories?**  
*Answer*: 
- 0–50: Good (Green)
- 51–100: Satisfactory (Lime/Light Green)
- 101–200: Moderately Polluted (Yellow)
- 201–300: Poor (Orange)
- 301–400: Very Poor (Red)
- 401–500+: Severe (Dark Red)

**Q4: What is a SQL Cursor and why did you use one?**  
*Answer*: A cursor is a database control structure that allows sequential row-by-row traversal over a query result set. In our procedure `sp_generate_city_report`, the cursor loops through every station within a target city to compute station-specific pollutant statistics and aggregate an overall city health report.

**Q5: How are passwords protected?**  
*Answer*: Passwords are encrypted using Spring Security's `BCryptPasswordEncoder` with a work factor of 10. Passwords are never stored or logged in plain text.

---

*Good luck with the presentation, team! You have a rock-solid, fully functioning project.* 🚀

