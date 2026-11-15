# Normalization Analysis (1NF &rarr; 2NF &rarr; 3NF)
## AQI Monitoring & Prediction System
**Team: Big Coders &bull; Group: 3Q15**  
**Members: Aryan Sharma, Aniket Raj, Chirag Lamba**

---

## 1. Normalization Journey

### Step 1: First Normal Form (1NF)
- **Principle**: All attributes must be atomic (no multi-valued attributes, no repeating groups).
- **Unnormalized State**:
  In early naive spreadsheets, a single row might hold:
  `Station | Pollutants (PM2.5, PM10, SO2) | Readings (145, 220, 25)`
- **1NF Resolution**:
  Each cell contains exactly one scalar value. Repeating pollutant readings are decomposed into individual rows in the `reading` table with foreign key references to `station_id` and `pollutant_id`.

---

### Step 2: Second Normal Form (2NF)
- **Principle**: The schema must be in 1NF, and all non-prime attributes must be **fully functionally dependent** on the primary key (no partial dependencies on a composite candidate key).
- **Potential 1NF Anomaly**:
  If readings were keyed by `(station_id, pollutant_id, reading_timestamp)` while storing `station_name` and `city_name` in the same table:
  - `station_name` would depend only on `station_id` (a partial key).
  - Modification of a station name would cause update anomalies across millions of reading records.
- **2NF Resolution**:
  `station` attributes are factored into a dedicated `station` entity. `reading` references `station_id` as a foreign key. Every attribute in `reading` depends on the whole primary key `reading_id`.

---

### Step 3: Third Normal Form (3NF)
- **Principle**: The schema must be in 2NF, and no non-prime attribute may be **transitively dependent** on a primary key ($X \rightarrow Y \rightarrow Z$).
- **Potential 2NF Anomaly**:
  If the `station` table held `(station_id, station_name, city_name, state, country, latitude, longitude)`:
  - Functional dependency: `station_id` &rarr; `city_name`
  - Transitive dependency: `city_name` &rarr; `{state, country, latitude, longitude}`
  - Anomaly: Updating Delhi's coordinates would necessitate updating every station located in Delhi.
- **3NF Resolution**:
  Transitive geographic attributes are extracted into the independent `city` entity (`city_id` PK). The `station` table maintains only `city_id` as a foreign key.

---

## 2. Table-by-Table 3NF Verification

| Table Name | Primary Key | Foreign Keys | Determinants & Functional Dependencies | 3NF Status |
|---|---|---|---|:---:|
| `city` | `city_id` | *None* | `city_id` &rarr; `{city_name, state, country, latitude, longitude}` | ✅ Verified 3NF |
| `station` | `station_id` | `city_id` | `station_id` &rarr; `{city_id, station_name, location_desc, station_type, installation_date, is_active}` | ✅ Verified 3NF |
| `pollutant` | `pollutant_id` | *None* | `pollutant_id` &rarr; `{pollutant_name, chemical_formula, unit, safe_limit, averaging_period, description}` | ✅ Verified 3NF |
| `reading` | `reading_id` | `station_id`, `pollutant_id` | `reading_id` &rarr; `{station_id, pollutant_id, reading_value, reading_timestamp, data_quality, data_source}` | ✅ Verified 3NF |
| `aqi_history` | `aqi_id` | `station_id` | `aqi_id` &rarr; `{station_id, aqi_value, aqi_category, record_date, dominant_pollutant, data_source}` | ✅ Verified 3NF |
| `alert_log` | `alert_id` | `station_id` | `alert_id` &rarr; `{station_id, alert_type, severity, message, alert_timestamp, is_resolved}` | ✅ Verified 3NF |
| `app_user` | `user_id` | *None* | `user_id` &rarr; `{username, email, password_hash, role, created_at}` | ✅ Verified 3NF |
| `weather_data` | `weather_id` | `city_id` | `weather_id` &rarr; `{city_id, temperature, humidity, wind_speed, wind_direction, precipitation, recorded_at, data_source}` | ✅ Verified 3NF |
| `health_advisory` | `advisory_id` | *None* | `advisory_id` &rarr; `{aqi_category, general_message, sensitive_group_message, outdoor_guidance}` | ✅ Verified 3NF |
| `report_log` | `report_id` | `user_id` | `report_id` &rarr; `{user_id, report_type, parameters, generated_at, status}` | ✅ Verified 3NF |

---

## 3. Conclusion
Every functional dependency in the schema is of the form $X \rightarrow Y$, where $X$ is a superkey. There are no partial dependencies and no transitive dependencies. Hence, the relational database schema strictly complies with **Third Normal Form (3NF)**.

