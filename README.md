# 🌍 AQI Monitoring & Prediction System

**Academic Full-Stack Environmental Informatics Application**  
**Team**: Big Coders &bull; **Group**: 3Q15  
**Team Members**:
1. **Aryan Sharma**
2. **Aniket Raj**
3. **Chirag Lamba**

> 📘 **Looking for an easy teammate guide & viva preparation cheatsheet?**  
> Check out **[TEAM_GUIDE.md](TEAM_GUIDE.md)** for a plain-English explanation of how everything works, how to run it in 30 seconds, and what to say during project presentations!

---

## 🌐 Live Production URL

The application is deployed live 24/7 on Render:  
👉 **[https://aqi-java-project.onrender.com](https://aqi-java-project.onrender.com)**

---

## 📌 Executive Summary

The **AQI Monitoring & Prediction System** is a complete Java-based academic web application built on **Java 21**, **Spring Boot 3.4**, **Spring Security**, **Spring JDBC (JdbcTemplate)**, **Thymeleaf**, and **MySQL 8.x**.

The application manages ambient air quality monitoring across Indian cities and stations. It computes official Indian National AQI values strictly adhering to **Central Pollution Control Board (CPCB)** breakpoint equations, provides transparent linear trend forecasting using **Ordinary Least Squares (OLS) Linear Regression**, integrates live ambient weather conditions via **Open-Meteo REST API**, and showcases advanced relational DBMS paradigms (Stored Functions, Procedures with Cursors, Triggers, Check Constraints, Normalization up to 3NF, and ACID Transactions).

---

## ☁️ 1-Click Cloud Deployment

The repository includes production `Dockerfile`, `Procfile`, `system.properties`, and `render.yaml` for instant deployment:

[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/Aniket775/aqi-java-project)
&nbsp;&nbsp;
[![Deploy on Railway](https://railway.com/button.svg)](https://railway.com/template/new?template=https://github.com/Aniket775/aqi-java-project)

### How to Deploy to Render in 2 Minutes (Free):
1. Go to [dashboard.render.com](https://dashboard.render.com) and log in with your GitHub account.
2. Click **New +** &rarr; **Web Service**.
3. Select your repository: `Aniket775/aqi-java-project`.
4. Render automatically detects the `Dockerfile` or `pom.xml`.
5. Click **Create Web Service**. Within 2 minutes, your live web app is online at `https://<your-service>.onrender.com`!

---

## 🏗 System Architecture

```
                          ┌────────────────────────────────────────┐
                          │          Client Web Browser            │
                          │   Thymeleaf HTML5 + CSS3 + Chart.js    │
                          └───────────────────┬────────────────────┘
                                              │ HTTP Requests (Session + CSRF)
                                              ▼
                          ┌────────────────────────────────────────┐
                          │         Spring MVC Controllers         │
                          │  Auth, Dashboard, DataEntry, Reports,  │
                          │        Predictions, Alerts, Admin      │
                          └───────────────────┬────────────────────┘
                                              │
                                              ▼
                          ┌────────────────────────────────────────┐
                          │            Java Service Layer          │
                          │  ├─ AqiCalculationService (CPCB 8)     │
                          │  ├─ AqiPredictionService (OLS Trend)   │
                          │  ├─ AlertService (Thresholds)          │
                          │  ├─ ReportService (Cursor Procedures)  │
                          │  ├─ WeatherApiService (Open-Meteo API) │
                          │  └─ OpenAqiApiService (Optional)       │
                          └───────────────────┬────────────────────┘
                                              │ Parameterized Queries / Calls
                                              ▼
                          ┌────────────────────────────────────────┐
                          │   Spring JDBC / JdbcTemplate Repos     │
                          └───────────────────┬────────────────────┘
                                              │
                                              ▼
                          ┌────────────────────────────────────────┐
                          │         MySQL 8.x Database             │
                          │  ├─ 10 Relational Tables (3NF)         │
                          │  ├─ Stored Functions (AQI sub-index)   │
                          │  ├─ Stored Procedures (Cursor Report)  │
                          │  ├─ Row-level Triggers (Sensor Quality)│
                          │  └─ Cascading Foreign Keys & Indexes   │
                          └────────────────────────────────────────┘
```

---

## 🛠 Technology Stack

| Layer | Component | Version / Specification |
|---|---|---|
| **Platform** | Java Development Kit (JDK) | Java 21+ LTS |
| **Framework** | Spring Boot | 3.4.x (Spring MVC, Spring Security 6) |
| **Database Access**| Spring JDBC / JdbcTemplate | Direct SQL execution without heavyweight ORM |
| **Database Engine**| MySQL Server | 8.0+ / InnoDB / utf8mb4 |
| **View Template** | Thymeleaf + Security Dialect | Server-rendered HTML5 |
| **Styling** | Custom Modern CSS3 | Dark Glassmorphism (`#0A0E1A`, `#111827`, `#6366F1`) |
| **Charts** | Chart.js | 4.4.x via client-side Canvas |
| **HTTP Client** | Spring RestClient | Non-blocking external REST API calls |
| **Build & Test** | Apache Maven | 3.9+ with JUnit 5 & Mockito |

---

## 📂 Project Directory Structure

```
aqi-java-system/
├── pom.xml                               # Maven project definition and dependencies
├── mvnw                                  # Maven executable wrapper
├── .env.example                          # Sample environment variable template
├── README.md                             # Comprehensive project documentation
├── docs/
│   ├── er_diagram.md                     # Entity-Relationship diagram & cardinality rules
│   ├── normalization.md                  # Normalization proof (1NF → 2NF → 3NF)
│   ├── aqi_calculation.md                # CPCB breakpoint equations and calculation rules
│   └── api_integration.md                # Open-Meteo & OpenAQ REST integration guide
├── src/
│   ├── main/
│   │   ├── java/com/bigcoders/aqi/
│   │   │   ├── AqiApplication.java       # Spring Boot main class
│   │   │   ├── config/
│   │   │   │   ├── DataInitializer.java  # BCrypt demo user seeding on startup
│   │   │   │   ├── SecurityConfig.java   # Role-based Spring Security filter chain
│   │   │   │   └── WebConfig.java        # Resource handlers
│   │   │   ├── controller/
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── AlertsController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── DataEntryController.java
│   │   │   │   ├── PredictionsController.java
│   │   │   │   └── ReportsController.java
│   │   │   ├── dto/
│   │   │   │   ├── AqiResult.java
│   │   │   │   ├── CityReportRow.java
│   │   │   │   ├── DashboardData.java
│   │   │   │   ├── PredictionResult.java
│   │   │   │   └── ReadingSubmission.java
│   │   │   ├── model/                    # 10 Core Entities
│   │   │   │   ├── AlertLog.java
│   │   │   │   ├── AppUser.java
│   │   │   │   ├── AqiHistory.java
│   │   │   │   ├── City.java
│   │   │   │   ├── HealthAdvisory.java
│   │   │   │   ├── Pollutant.java
│   │   │   │   ├── Reading.java
│   │   │   │   ├── ReportLog.java
│   │   │   │   ├── Station.java
│   │   │   │   └── WeatherData.java
│   │   │   ├── repository/               # 10 JdbcTemplate Repositories
│   │   │   ├── security/
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   └── service/
│   │   │       ├── AlertService.java
│   │   │       ├── AqiCalculationService.java
│   │   │       ├── AqiPredictionService.java
│   │   │       ├── DashboardService.java
│   │   │       ├── OpenAqiApiService.java
│   │   │       ├── ReportService.java
│   │   │       └── WeatherApiService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-example.properties
│   │       ├── db/                       # MySQL DDL, Functions, Procedures & Seeds
│   │       │   ├── schema.sql            # 10 tables in 3NF with constraints & indexes
│   │       │   ├── functions.sql         # fn_calculate_aqi_subindex & fn_get_aqi_category
│   │       │   ├── procedures.sql        # sp_generate_city_report (with Cursor)
│   │       │   ├── triggers.sql          # trg_reading_data_quality
│   │       │   ├── seed_data.sql         # 11 cities, 21 stations, 8 pollutants, sample data
│   │       │   └── setup.sql             # Master one-step setup script
│   │       ├── static/
│   │       │   ├── css/style.css         # Dark glassmorphic styling
│   │       │   └── js/main.js            # Dynamic dropdown cascade & Chart.js helper
│   │       └── templates/                # Thymeleaf HTML Templates
│   │           ├── access-denied.html
│   │           ├── admin.html
│   │           ├── alerts.html
│   │           ├── dashboard.html
│   │           ├── data-entry.html
│   │           ├── fragments/layout.html # Reusable navbar & team footer
│   │           ├── login.html
│   │           ├── predictions.html
│   │           └── reports.html
│   └── test/java/com/bigcoders/aqi/      # JUnit 5 & Mockito Unit Tests
```

---

## 📊 Database Design & DBMS Concepts

### 1. 10 Relational Tables (Normalized to 3NF)
1. **`city`**: Geographic cities, states, and coordinates.
2. **`station`**: Ambient air quality monitoring stations linked via foreign key to `city`.
3. **`pollutant`**: Official CPCB parameters (`PM2.5`, `PM10`, `NO2`, `SO2`, `CO`, `O3`, `NH3`, `Pb`), safe thresholds, and averaging windows.
4. **`reading`**: Individual pollutant sensor readings with quality flags and source attribution.
5. **`aqi_history`**: Computed consolidated daily AQI values, categories, and dominant pollutants.
6. **`alert_log`**: Real-time notifications for threshold exceedances and sensor anomalies.
7. **`app_user`**: System actors with BCrypt-hashed credentials and assigned roles.
8. **`weather_data`**: Atmospheric observations (temperature, humidity, wind, rainfall).
9. **`health_advisory`**: Category-specific health recommendations.
10. **`report_log`**: Audit record of generated environmental reports.

### 2. Advanced SQL Routines
- **Stored Function (`fn_calculate_aqi_subindex`)**: Implements piecewise linear breakpoint interpolation without decimal gaps for all 8 criteria pollutants.
- **Stored Procedure (`sp_generate_city_report`) with CURSOR**: Explicitly declares a `CURSOR FOR SELECT station_id, station_name FROM station WHERE city_id = p_city_id`, creates a temporary table, loops through active stations, aggregates 30-day pollutant concentrations, and records generation metrics in `report_log`.
- **Row-level Trigger (`trg_reading_data_quality`)**: Fires `AFTER INSERT ON reading`. Automatically logs a `Data_Quality` warning into `alert_log` when a sensor reading has poor or invalid quality.
- **ACID Transactions (`@Transactional`)**: Ensures atomic updates when recording new pollutant readings, updating daily AQI history, and emitting threshold alerts.

---

## 🔐 User Authentication & Roles

User authentication is secured using Spring Security 6 with **BCrypt password hashing**. Four distinct roles enforce principle of least privilege:

| Role | Username | Demo Password | Granted Permissions |
|---|---|---|---|
| **Admin** | `admin` | `admin123` | Master control: manage cities, stations, pollutants, users, alerts, reports, data entry |
| **Analyst** | `analyst` | `analyst123` | Dashboard, historical data analytics, predictions, generate reports, view advisories |
| **Operator** | `operator` | `operator123` | Dashboard, submit new sensor readings (`/data-entry`), view stations, view alerts |
| **Viewer** | `viewer` | `viewer123` | Read-only access: view dashboard cards, charts, advisories, and prediction trends |

*(Note: Change demo passwords prior to any production deployment).*

---

## 📈 CPCB AQI Calculation & Prediction Methodology

### 1. Indian National AQI Breakpoints
- Utilizes the CPCB formula: $I = \left(\frac{I_{\text{hi}} - I_{\text{lo}}}{C_{\text{hi}} - C_{\text{lo}}}\right) \times (C - C_{\text{lo}}) + I_{\text{lo}}$.
- Continuous brackets avoid integer gaps (e.g. `30.5` $\mu\text{g/m}^3$ of PM2.5 maps smoothly to $50.83$).
- **CPCB Sufficiency Rule**: Overall AQI requires **at least 3 pollutants**, and at least one **must be PM2.5 or PM10**. Stations with insufficient data show `"Insufficient Data"` rather than fabricated overall numbers.

### 2. OLS Linear Regression Forecasting
- Forecasts the upcoming **1 to 3 days** based on actual historical daily observations.
- Requires **at least 7 valid historical daily records** before computing a forecast.
- Calculates slope ($m$), intercept ($c$), and trend direction (*Increasing*, *Decreasing*, *Stable*).
- Displays clear academic disclaimer: statistical trend extrapolation based on historical samples; not a numerical meteorological dispersion simulation.

---

## 🌐 External REST API Integrations

### 1. Open-Meteo Weather API (Active)
- Live weather conditions (temperature, humidity, wind speed, wind direction, rainfall) queried using city coordinates.
- Non-commercial, requires no API key.
- Includes 4-second timeout and graceful database fallback.

### 2. OpenAQ Atmospheric API (Optional)
- Configurable via `OPENAQ_API_KEY`.
- If key is omitted, the application operates in standard manual/sample mode without errors.

---

## 🚀 Step-by-Step Setup & Execution

### Prerequisites
- **Java 21+** (`java -version`)
- **MySQL 8.0+** (`mysql --version`)
- **Maven 3.9+** (or use the included `./mvnw`)

### Step 1: Initialize Database
```bash
# Connect to your MySQL instance
mysql -u root -p

# Execute the master setup script
mysql -u root -p < src/main/resources/db/setup.sql
```

### Step 2: Configure Environment
Copy `.env.example` to `.env` or set environment variables:
```bash
export DB_URL="jdbc:mysql://localhost:3306/aqi_system?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata"
export DB_USERNAME="root"
export DB_PASSWORD="your_mysql_password"
```

### Step 3: Compile and Test
```bash
./mvnw clean test
```

### Step 4: Run Application
```bash
./mvnw spring-boot:run
```

Open your browser at:  
👉 **`http://localhost:8080`**

Log in using `admin` / `admin123`.

---

## 👥 Development Team

Built as a DBMS & Software Engineering academic project by **Big Coders** (Group **3Q15**):
- **Aryan Sharma**
- **Aniket Raj**
- **Chirag Lamba**

