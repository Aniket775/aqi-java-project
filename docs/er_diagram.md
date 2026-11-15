# Entity-Relationship (ER) Documentation
## AQI Monitoring & Prediction System
**Team: Big Coders &bull; Group: 3Q15**  
**Members: Aryan Sharma, Aniket Raj, Chirag Lamba**

---

## 1. Mermaid ER Diagram

```mermaid
erDiagram
    CITY ||--o{ STATION : "hosts"
    CITY ||--o{ WEATHER_DATA : "records"
    STATION ||--o{ READING : "captures"
    STATION ||--o{ AQI_HISTORY : "computes"
    STATION ||--o{ ALERT_LOG : "triggers"
    POLLUTANT ||--o{ READING : "measured_in"
    APP_USER ||--o{ REPORT_LOG : "generates"

    CITY {
        int city_id PK
        varchar city_name
        varchar state
        varchar country
        decimal latitude
        decimal longitude
    }

    STATION {
        int station_id PK
        int city_id FK
        varchar station_name
        varchar location_desc
        enum station_type
        date installation_date
        boolean is_active
    }

    POLLUTANT {
        int pollutant_id PK
        varchar pollutant_name
        varchar chemical_formula
        varchar unit
        decimal safe_limit
        varchar averaging_period
        text description
    }

    READING {
        int reading_id PK
        int station_id FK
        int pollutant_id FK
        decimal reading_value
        datetime reading_timestamp
        enum data_quality
        varchar data_source
    }

    AQI_HISTORY {
        int aqi_id PK
        int station_id FK
        decimal aqi_value
        varchar aqi_category
        date record_date
        varchar dominant_pollutant
        varchar data_source
    }

    ALERT_LOG {
        int alert_id PK
        int station_id FK
        enum alert_type
        enum severity
        text message
        datetime alert_timestamp
        boolean is_resolved
    }

    APP_USER {
        int user_id PK
        varchar username
        varchar email
        varchar password_hash
        enum role
        datetime created_at
    }

    WEATHER_DATA {
        int weather_id PK
        int city_id FK
        decimal temperature
        decimal humidity
        decimal wind_speed
        varchar wind_direction
        decimal precipitation
        datetime recorded_at
        varchar data_source
    }

    HEALTH_ADVISORY {
        int advisory_id PK
        varchar aqi_category
        text general_message
        text sensitive_group_message
        text outdoor_guidance
    }

    REPORT_LOG {
        int report_id PK
        int user_id FK
        enum report_type
        text parameters
        datetime generated_at
        enum status
    }
```

---

## 2. Entity Dictionary & Cardinalities

| Relationship | Cardinality | Participation | Description |
|---|---|---|---|
| `city` &rarr; `station` | 1 : N | Mandatory (Every station belongs to 1 city) | A geographic city encompasses multiple atmospheric monitoring stations. |
| `station` &rarr; `reading` | 1 : N | Mandatory (Every reading belongs to 1 station) | Stations continuously log pollutant sensor observations. |
| `pollutant` &rarr; `reading` | 1 : N | Mandatory (Every reading identifies 1 pollutant) | 8 CPCB parameters (`PM2.5`, `PM10`, `NO2`, `SO2`, `CO`, `O3`, `NH3`, `Pb`). |
| `station` &rarr; `aqi_history` | 1 : N | Optional | Daily consolidated station-level AQI calculations. |
| `station` &rarr; `alert_log` | 1 : N | Optional | Automated threshold alerts and sensor anomaly notifications. |
| `city` &rarr; `weather_data` | 1 : N | Optional | Local ambient conditions (temperature, humidity, wind, rain). |
| `app_user` &rarr; `report_log` | 1 : N | Mandatory | User-initiated audit trail of generated reports. |

---

## 3. Referential Constraints & Integrity Rules
- **Foreign Keys**: Configured with `ON DELETE CASCADE ON UPDATE CASCADE` to guarantee strict referential integrity.
- **Unique Constraints**:
  - `city(city_name, state)`: Disallows duplicate city records within the same state.
  - `station(station_name)`: Enforces distinct station nomenclature.
  - `pollutant(chemical_formula)`: Ensures standardized chemical identifiers.
  - `aqi_history(station_id, record_date)`: Strictly permits exactly one consolidated AQI entry per station per calendar day.
  - `app_user(username)` & `app_user(email)`: Enforces distinct user identities.
  - `health_advisory(aqi_category)`: Enforces single advisory set per CPCB category.

