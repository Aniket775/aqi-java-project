# External REST API Integration Guide
## AQI Monitoring & Prediction System
**Team: Big Coders &bull; Group: 3Q15**  
**Members: Aryan Sharma, Aniket Raj, Chirag Lamba**

---

## 1. Architectural Philosophy

All external REST API integrations are performed from the Java backend using Spring Boot 3's modern **`RestClient`** abstraction. Web browsers do not make direct client-side external API requests, preventing CORS issues, protecting API credentials, and ensuring server-side caching and persistence in MySQL.

---

## 2. Open-Meteo Weather API Integration

- **Documentation**: [https://open-meteo.com/en/docs](https://open-meteo.com/en/docs)
- **Base Endpoint**: `https://api.open-meteo.com/v1/forecast`
- **Authentication**: Free for non-commercial educational use; **no API key required**.
- **Java Service**: `WeatherApiService.java`

### Request Parameters
Using geographic coordinates (`latitude`, `longitude`) queried from the MySQL `city` table:
```
GET https://api.open-meteo.com/v1/forecast?latitude=28.6139&longitude=77.2090&current=temperature_2m,relative_humidity_2m,wind_speed_10m,wind_direction_10m,precipitation
```

### Response Mapping
- `temperature_2m` &rarr; `weather_data.temperature` (in °C)
- `relative_humidity_2m` &rarr; `weather_data.humidity` (in % RH)
- `wind_speed_10m` &rarr; `weather_data.wind_speed` (in km/h)
- `wind_direction_10m` &rarr; `weather_data.wind_direction` (converted to 16-point cardinal compass: N, NE, E, etc.)
- `precipitation` &rarr; `weather_data.precipitation` (in mm)

### Resilient Fallback Strategy
- Configured with a 4-second connection and read timeout.
- If the network request fails, times out, or experiences temporary throttling:
  1. The error is logged as a non-fatal warning.
  2. The service queries the most recent cached record from MySQL `weather_data`.
  3. The UI seamlessly renders the cached data marked with `"Unavailable / Cached"`, ensuring zero user-facing application crashes.

---

## 3. OpenAQ API (Optional Live Atmospheric Ingestion)

- **Documentation**: [https://docs.openaq.org/](https://docs.openaq.org/)
- **Base Endpoint**: `https://api.openaq.org/v3`
- **Authentication**: Configured via environment variable `OPENAQ_API_KEY`.
- **Java Service**: `OpenAqiApiService.java`

### Graceful Degradation Rules
1. **Unconfigured Key**: If `OPENAQ_API_KEY` is not present, the system operates in standard manual/sample mode without warnings or degraded performance.
2. **Missing Sensor Stations**: OpenAQ sensor coverage varies by region. If an Indian monitoring station has no live sensors on OpenAQ, the system records this fact and does not invent fake readings.
3. **Attribution**: Real-world readings imported from OpenAQ are explicitly marked with `data_source = 'OpenAQ Live API'`.

