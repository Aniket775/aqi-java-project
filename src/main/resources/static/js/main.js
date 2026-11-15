/**
 * AQI Monitoring & Prediction System
 * JavaScript Helper Functions (Minimal & Clean)
 * Built by Big Coders (Group 3Q15)
 */

document.addEventListener('DOMContentLoaded', () => {
    // 1. Dynamic Station Cascade on Data Entry page
    const citySelect = document.getElementById('citySelect');
    const stationSelect = document.getElementById('stationSelect');

    if (citySelect && stationSelect) {
        citySelect.addEventListener('change', async () => {
            const cityId = citySelect.value;
            stationSelect.innerHTML = '<option value="">Loading stations...</option>';

            if (!cityId) {
                stationSelect.innerHTML = '<option value="">Select a city first...</option>';
                return;
            }

            try {
                const res = await fetch(`/data-entry/stations-by-city?cityId=${cityId}`);
                if (res.ok) {
                    const stations = await res.json();
                    if (stations.length === 0) {
                        stationSelect.innerHTML = '<option value="">No stations registered for this city</option>';
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

    // 2. Auto-fade success alerts
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
 * Chart.js Helper: Renders AQI Trend Chart
 */
function renderAqiTrendChart(canvasId, labels, dataPoints, labelName = 'AQI Value') {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;

    new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: labelName,
                data: dataPoints,
                borderColor: '#6366F1',
                backgroundColor: 'rgba(99, 102, 241, 0.1)',
                borderWidth: 2.5,
                fill: true,
                tension: 0.35,
                pointBackgroundColor: '#818CF8',
                pointRadius: 4,
                pointHoverRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    labels: { color: '#94A3B8', font: { family: 'Inter', size: 12 } }
                },
                tooltip: {
                    backgroundColor: '#111827',
                    titleColor: '#F1F5F9',
                    bodyColor: '#94A3B8',
                    borderColor: 'rgba(255,255,255,0.1)',
                    borderWidth: 1
                }
            },
            scales: {
                x: {
                    grid: { color: 'rgba(255,255,255,0.04)' },
                    ticks: { color: '#64748B', font: { family: 'Inter' } }
                },
                y: {
                    beginAtZero: true,
                    max: 500,
                    grid: { color: 'rgba(255,255,255,0.04)' },
                    ticks: { color: '#64748B', font: { family: 'Inter' } }
                }
            }
        }
    });
}

