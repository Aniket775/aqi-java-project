/**
 * AQI Monitoring & Prediction System — "The Air You Breathe"
 * Human-Crafted, Nature-Inspired Frontend Controllers & Chart Renderers
 * Developed by Big Coders (Group 3Q15): Aryan Sharma, Aniket Raj, Chirag Lamba
 */

document.addEventListener('DOMContentLoaded', () => {
    // -------------------------------------------------------------------------
    // 1. Sun/Moon Theme Toggle with LocalStorage Persistence
    // -------------------------------------------------------------------------
    const toggleBtn = document.getElementById('themeToggleBtn');
    const sunIcon = document.getElementById('themeIconSun');
    const moonIcon = document.getElementById('themeIconMoon');
    const savedTheme = localStorage.getItem('aqi-theme') || 'light';

    function applyTheme(theme) {
        if (theme === 'dark') {
            document.documentElement.setAttribute('data-theme', 'dark');
            if (sunIcon && moonIcon) {
                sunIcon.style.display = 'none';
                moonIcon.style.display = 'block';
            }
        } else {
            document.documentElement.removeAttribute('data-theme');
            if (sunIcon && moonIcon) {
                sunIcon.style.display = 'block';
                moonIcon.style.display = 'none';
            }
        }
    }

    applyTheme(savedTheme);

    if (toggleBtn) {
        toggleBtn.addEventListener('click', () => {
            const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
            const nextTheme = isDark ? 'light' : 'dark';
            localStorage.setItem('aqi-theme', nextTheme);
            applyTheme(nextTheme);

            // Re-render charts on theme change if they exist
            if (window.activeAqiChart) {
                window.activeAqiChart.destroy();
                if (window.lastBarChartArgs) {
                    renderStationBarChart(...window.lastBarChartArgs);
                }
            }
        });
    }

    // -------------------------------------------------------------------------
    // 2. Dynamic Station Cascade on Data Entry Page
    // -------------------------------------------------------------------------
    const citySelect = document.getElementById('citySelect');
    const stationSelect = document.getElementById('stationSelect');

    if (citySelect && stationSelect) {
        citySelect.addEventListener('change', async () => {
            const cityId = citySelect.value;
            stationSelect.innerHTML = '<option value="">Loading stations...</option>';

            if (!cityId) {
                stationSelect.innerHTML = '<option value="">Select a municipal jurisdiction first...</option>';
                return;
            }

            try {
                const res = await fetch(`/data-entry/stations-by-city?cityId=${cityId}`);
                if (res.ok) {
                    const stations = await res.json();
                    if (stations.length === 0) {
                        stationSelect.innerHTML = '<option value="">No stations registered for this jurisdiction</option>';
                    } else {
                        stationSelect.innerHTML = '<option value="">Select monitoring station...</option>';
                        stations.forEach(st => {
                            const opt = document.createElement('option');
                            opt.value = st.stationId;
                            opt.textContent = `${st.stationName} (${st.stationType})`;
                            stationSelect.appendChild(opt);
                        });
                    }
                } else {
                    stationSelect.innerHTML = '<option value="">Failed to load stations</option>';
                }
            } catch (err) {
                console.error('Error fetching stations:', err);
                stationSelect.innerHTML = '<option value="">Error loading stations</option>';
            }
        });
    }

    // -------------------------------------------------------------------------
    // 3. Auto-fade alerts
    // -------------------------------------------------------------------------
    const alerts = document.querySelectorAll('.alert-success');
    alerts.forEach(el => {
        setTimeout(() => {
            el.style.transition = 'opacity 0.6s ease';
            el.style.opacity = '0';
            setTimeout(() => el.remove(), 600);
        }, 5000);
    });
});

/**
 * Chart.js Helper: Renders Station AQI Bar Chart with Earthy CPCB Color-Coded Bars
 */
function renderStationBarChart(canvasId, labels, aqiValues) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;

    window.lastBarChartArgs = [canvasId, labels, aqiValues];

    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    const gridColor = isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(47, 74, 58, 0.08)';
    const textColor = isDark ? '#C5BFB5' : '#5C584E';

    // Earthy desaturated CPCB palette
    const colors = aqiValues.map(v => {
        if (v <= 50) return '#2E7D32'; // Good (Foliage Emerald)
        if (v <= 100) return '#558B2F'; // Satisfactory (Meadow Olive)
        if (v <= 200) return '#B26A00'; // Moderate (Ochre Gold)
        if (v <= 300) return '#BF360C'; // Poor (Terracotta Ember)
        if (v <= 400) return '#A71D2A'; // Very Poor (Autumn Crimson)
        return '#581845'; // Severe (Deep Mulberry)
    });

    if (window.activeAqiChart) {
        window.activeAqiChart.destroy();
    }

    window.activeAqiChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Air Quality Index',
                data: aqiValues,
                backgroundColor: colors,
                borderRadius: 8,
                borderWidth: 0,
                maxBarThickness: 42
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: isDark ? '#1D2E37' : '#FFFFFF',
                    titleColor: isDark ? '#EDEBE6' : '#2B2B26',
                    bodyColor: isDark ? '#C5BFB5' : '#5C584E',
                    borderColor: isDark ? 'rgba(221, 215, 202, 0.2)' : '#DDD7CA',
                    borderWidth: 1,
                    padding: 12,
                    cornerRadius: 8,
                    titleFont: { family: 'Fraunces', size: 14, weight: '600' },
                    bodyFont: { family: 'DM Sans', size: 13, weight: '500' },
                    callbacks: {
                        label: (c) => ` AQI: ${c.parsed.y}`
                    }
                }
            },
            scales: {
                x: {
                    grid: { display: false },
                    ticks: {
                        color: textColor,
                        font: { family: 'DM Sans', size: 12, weight: '500' }
                    }
                },
                y: {
                    beginAtZero: true,
                    max: 500,
                    grid: { color: gridColor },
                    ticks: {
                        color: textColor,
                        font: { family: 'DM Sans', size: 11 }
                    }
                }
            }
        }
    });
}

/**
 * Chart.js Helper: Renders AQI Trend Chart with Soft Nature Gradient
 */
function renderAqiTrendChart(canvasId, labels, dataPoints, labelName = 'AQI Value') {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;

    const isDark = document.documentElement.getAttribute('data-theme') === 'dark';
    const gridColor = isDark ? 'rgba(255, 255, 255, 0.08)' : 'rgba(47, 74, 58, 0.08)';
    const textColor = isDark ? '#C5BFB5' : '#5C584E';

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: labelName,
                data: dataPoints,
                borderColor: '#2F4A3A',
                backgroundColor: 'rgba(47, 74, 58, 0.08)',
                borderWidth: 2.6,
                fill: true,
                tension: 0.35,
                pointBackgroundColor: '#C8744F',
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    labels: {
                        color: textColor,
                        font: { family: 'DM Sans', size: 13, weight: '600' }
                    }
                },
                tooltip: {
                    backgroundColor: isDark ? '#1D2E37' : '#FFFFFF',
                    titleColor: isDark ? '#EDEBE6' : '#2B2B26',
                    bodyColor: isDark ? '#C5BFB5' : '#5C584E',
                    borderColor: isDark ? 'rgba(221, 215, 202, 0.2)' : '#DDD7CA',
                    borderWidth: 1,
                    cornerRadius: 8
                }
            },
            scales: {
                x: {
                    grid: { color: gridColor },
                    ticks: { color: textColor, font: { family: 'DM Sans', size: 11 } }
                },
                y: {
                    beginAtZero: true,
                    max: 500,
                    grid: { color: gridColor },
                    ticks: { color: textColor, font: { family: 'DM Sans', size: 11 } }
                }
            }
        }
    });
}
