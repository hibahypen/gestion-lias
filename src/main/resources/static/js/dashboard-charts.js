(function () {
    var data = window.LIAS_CHARTS || {};
    var statusData = data.status || {};
    var eventsData = data.events || {};
    var pubsData = data.pubs || {};

    var STATUS_COLORS = {
        PERMANENT: '#2D6A0A',
        DOCTORANT: '#1558A0',
        ASSOCIE: '#8A4D0A',
        RETRAITE: '#5A5855',
        ANCIEN: '#A02D20'
    };

    function sum(obj) {
        return Object.values(obj).reduce(function (a, b) { return a + b; }, 0);
    }

    // Si pas de données : on affiche un message au lieu d'un graphique vide
    function showEmpty(canvasId, message) {
        var canvas = document.getElementById(canvasId);
        if (!canvas) return;
        canvas.parentElement.innerHTML =
            '<div style="height:100%;display:flex;align-items:center;justify-content:center;' +
            'color:#8a8a8a;font-size:13px;text-align:center;">' + message + '</div>';
    }

    if (typeof Chart === 'undefined') {
        var panel = document.getElementById('chartsPanel');
        if (panel) {
            panel.insertAdjacentHTML('beforeend',
                '<p style="color:#A02D20;">Graphiques indisponibles : /js/chart.umd.js est introuvable.</p>');
        }
        return;
    }

    // 1. Membres par statut (doughnut)
    if (sum(statusData) === 0) {
        showEmpty('statusChart', 'Aucun membre enregistré');
    } else {
        var statusLabels = Object.keys(statusData);
        new Chart(document.getElementById('statusChart'), {
            type: 'doughnut',
            data: {
                labels: statusLabels,
                datasets: [{
                    data: Object.values(statusData),
                    backgroundColor: statusLabels.map(function (s) { return STATUS_COLORS[s] || '#999999'; })
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { title: { display: true, text: 'Membres par statut' } }
            }
        });
    }

    // 2. Événements par type (bar)
    if (Object.keys(eventsData).length === 0) {
        showEmpty('eventsChart', 'Aucun événement enregistré');
    } else {
        new Chart(document.getElementById('eventsChart'), {
            type: 'bar',
            data: {
                labels: Object.keys(eventsData),
                datasets: [{
                    label: 'Événements',
                    data: Object.values(eventsData),
                    backgroundColor: '#8A4D0A'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: { display: true, text: 'Événements par type' },
                    legend: { display: false }
                },
                scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
            }
        });
    }

    // 3. Publications par année (line)
    if (Object.keys(pubsData).length === 0) {
        showEmpty('pubsChart', 'Aucune publication enregistrée');
    } else {
        new Chart(document.getElementById('pubsChart'), {
            type: 'line',
            data: {
                labels: Object.keys(pubsData),
                datasets: [{
                    label: 'Publications',
                    data: Object.values(pubsData),
                    borderColor: '#3C3489',
                    backgroundColor: 'rgba(60,52,137,0.15)',
                    fill: true,
                    tension: 0.3
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { title: { display: true, text: 'Publications par année' } },
                scales: { y: { beginAtZero: true, ticks: { precision: 0 } } }
            }
        });
    }
})();