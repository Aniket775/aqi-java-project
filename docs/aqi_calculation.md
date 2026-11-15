# CPCB National Air Quality Index (AQI) Calculation Methodology
## AQI Monitoring & Prediction System
**Team: Big Coders &bull; Group: 3Q15**  
**Members: Aryan Sharma, Aniket Raj, Chirag Lamba**

---

## 1. Regulatory Standard & Scope

The Indian National Air Quality Index (AQI) was formulated by the **Central Pollution Control Board (CPCB)**, Ministry of Environment, Forest & Climate Change (MoEFCC), Government of India.

The index monitors **8 criteria pollutants**:
1. **PM2.5**: Particulate Matter $\leq 2.5\ \mu\text{m}$ (24-hour average)
2. **PM10**: Particulate Matter $\leq 10\ \mu\text{m}$ (24-hour average)
3. **NO2**: Nitrogen Dioxide (24-hour average)
4. **SO2**: Sulfur Dioxide (24-hour average)
5. **CO**: Carbon Monoxide (8-hour average)
6. **O3**: Ozone (8-hour average)
7. **NH3**: Ammonia (24-hour average)
8. **Pb**: Lead (24-hour average)

---

## 2. Mathematical Breakpoint Formula

Individual pollutant concentrations are converted into an AQI Sub-index ($I$) using piecewise linear interpolation:

$$I = \left(\frac{I_{\text{hi}} - I_{\text{lo}}}{C_{\text{hi}} - C_{\text{lo}}}\right) \times (C - C_{\text{lo}}) + I_{\text{lo}}$$

Where:
- $C$ = Measured pollutant concentration
- $C_{\text{lo}}$ = Lower concentration breakpoint corresponding to the bracket
- $C_{\text{hi}}$ = Upper concentration breakpoint corresponding to the bracket
- $I_{\text{lo}}$ = Lower AQI index breakpoint corresponding to the bracket
- $I_{\text{hi}}$ = Upper AQI index breakpoint corresponding to the bracket

---

## 3. CPCB Breakpoint Reference Table

| AQI Category | AQI Range ($I$) | PM2.5 ($\mu\text{g/m}^3$) | PM10 ($\mu\text{g/m}^3$) | NO2 ($\mu\text{g/m}^3$) | SO2 ($\mu\text{g/m}^3$) | CO ($\text{mg/m}^3$) | O3 ($\mu\text{g/m}^3$) | NH3 ($\mu\text{g/m}^3$) | Pb ($\mu\text{g/m}^3$) |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| **Good** | 0 – 50 | 0 – 30 | 0 – 50 | 0 – 40 | 0 – 40 | 0.0 – 1.0 | 0 – 50 | 0 – 200 | 0.0 – 0.5 |
| **Satisfactory** | 51 – 100 | 31 – 60 | 51 – 100 | 41 – 80 | 41 – 80 | 1.1 – 2.0 | 51 – 100 | 201 – 400 | 0.6 – 1.0 |
| **Moderately Polluted** | 101 – 200 | 61 – 90 | 101 – 250 | 81 – 180 | 81 – 380 | 2.1 – 10.0 | 101 – 168 | 401 – 800 | 1.1 – 2.0 |
| **Poor** | 201 – 300 | 91 – 120 | 251 – 350 | 181 – 280 | 381 – 800 | 10.1 – 17.0 | 169 – 208 | 801 – 1200 | 2.1 – 3.0 |
| **Very Poor** | 301 – 400 | 121 – 250 | 351 – 430 | 281 – 400 | 801 – 1600 | 17.1 – 34.0 | 209 – 748 | 1201 – 1800 | 3.1 – 3.5 |
| **Severe** | 401 – 500 | 250+ | 430+ | 400+ | 1600+ | 34.0+ | 748+ | 1800+ | 3.5+ |

---

## 4. Elimination of Decimal Boundary Gaps

In naive implementations, integer `BETWEEN 0 AND 30` and `BETWEEN 31 AND 60` cause decimal values like `30.5` to fall into an undefined void.

In our Java `AqiCalculationService` and MySQL `fn_calculate_aqi_subindex`, intervals are defined continuously with upper inclusive bounds ($C \leq C_{\text{hi}}$):
- $[0.0, 30.0]$ &rarr; $[0, 50]$
- $(30.0, 60.0]$ &rarr; $(50, 100]$
- $(60.0, 90.0]$ &rarr; $(100, 200]$

Thus, a concentration of `30.5` smoothly enters the $(30.0, 60.0]$ bracket and yields an accurate sub-index of `50.83`.

---

## 5. CPCB Overall AQI Formulation Rules

To publish an authoritative **Overall AQI** for a monitoring station:
1. **Minimum 3 Pollutants**: Data must be recorded for at least three distinct criteria pollutants.
2. **Mandatory Particulate Matter**: At least one of the recorded pollutants **MUST be PM2.5 or PM10**.
3. **Maximum Pooling**: The overall station AQI is defined by the **highest sub-index** among all monitored pollutants:
   $$\text{AQI} = \max\left(I_{\text{PM2.5}}, I_{\text{PM10}}, I_{\text{NO2}}, I_{\text{SO2}}, I_{\text{CO}}, I_{\text{O3}}, I_{\text{NH3}}, I_{\text{Pb}}\right)$$
4. **Dominant Pollutant**: The parameter responsible for the highest sub-index is designated as the dominant pollutant.
5. **Insufficient Data Fallback**: When fewer than 3 pollutants or no particulate matter is available, the application visibly displays **"Insufficient Data"** while continuing to show individual sub-indices.

