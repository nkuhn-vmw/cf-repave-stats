(function () {
    'use strict';

    var MAX_HISTORY = 25;
    var INSTANCE_COLORS = [
        '#6c8cff', '#f472b6', '#4ade80', '#fbbf24',
        '#a78bfa', '#fb923c', '#22d3ee', '#f87171'
    ];

    var history = [];
    var seenInstances = {};
    var pollTimer = null;
    var servicesLoaded = false;

    function $(sel) { return document.querySelector(sel); }
    function $$(sel) { return document.querySelectorAll(sel); }

    function getInstanceColor(index) {
        var i = parseInt(index, 10);
        return INSTANCE_COLORS[isNaN(i) ? 0 : i % INSTANCE_COLORS.length];
    }

    function formatUptime(seconds) {
        var d = Math.floor(seconds / 86400);
        var h = Math.floor((seconds % 86400) / 3600);
        var m = Math.floor((seconds % 3600) / 60);
        var s = seconds % 60;
        var parts = [];
        if (d > 0) parts.push(d + 'd');
        if (h > 0) parts.push(h + 'h');
        if (m > 0) parts.push(m + 'm');
        parts.push(s + 's');
        return parts.join(' ');
    }

    function formatTime(isoString) {
        try {
            return new Date(isoString).toLocaleTimeString();
        } catch (e) {
            return isoString;
        }
    }

    function truncateGuid(guid) {
        if (!guid || guid.length <= 12) return guid;
        return guid.substring(0, 8) + '...';
    }

    function formatMb(mb) {
        if (mb >= 1024) return (mb / 1024).toFixed(1) + ' GB';
        return mb + ' MB';
    }

    function updateInstanceDisplay(data) {
        var color = getInstanceColor(data.instanceIndex);

        $('#badgeIndex').textContent = data.instanceIndex;
        $('#badgeIndex').style.color = color;
        $('.instance-badge').style.borderColor = color;

        $('#instanceId').textContent = data.instanceId;
        $('#instanceIp').textContent = data.instanceIp;
        $('#instancePort').textContent = data.instancePort;
        $('#uptime').textContent = formatUptime(data.uptimeSeconds);
        $('#appName').textContent = data.appName;
        $('#appId').textContent = data.appId;

        // Track unique instances seen
        seenInstances[data.instanceIndex] = true;
        var count = Object.keys(seenInstances).length;
        $('#statInstances').textContent = count;

        // Resource stats
        $('#statMemLimit').textContent = formatMb(data.memoryLimitMb);
        $('#statMemUsed').textContent = formatMb(data.memoryUsedMb);
        $('#statDiskLimit').textContent = formatMb(data.diskLimitMb);
        $('#statDiskUsed').textContent = formatMb(data.diskUsedMb);
    }

    function addHistoryRow(data) {
        history.unshift(data);
        if (history.length > MAX_HISTORY) {
            history.pop();
        }

        var tbody = $('#historyBody');
        var color = getInstanceColor(data.instanceIndex);

        var row = document.createElement('tr');
        row.classList.add('highlight');
        row.innerHTML =
            '<td>' + formatTime(data.timestamp) + '</td>' +
            '<td><span class="instance-tag" style="background:' + color + '">' + data.instanceIndex + '</span></td>' +
            '<td>' + truncateGuid(data.instanceId) + '</td>' +
            '<td>' + data.instanceIp + '</td>' +
            '<td>' + formatUptime(data.uptimeSeconds) + '</td>';

        tbody.insertBefore(row, tbody.firstChild);

        while (tbody.children.length > MAX_HISTORY) {
            tbody.removeChild(tbody.lastChild);
        }
    }

    function setStatus(ok) {
        var indicator = $('#statusIndicator');
        var text = indicator.querySelector('.status-text');
        if (ok) {
            indicator.classList.remove('error');
            text.textContent = 'Connected';
        } else {
            indicator.classList.add('error');
            text.textContent = 'Error';
        }
    }

    function fetchInstance() {
        fetch('/api/instance')
            .then(function (res) {
                if (!res.ok) throw new Error('HTTP ' + res.status);
                return res.json();
            })
            .then(function (data) {
                updateInstanceDisplay(data);
                addHistoryRow(data);
                setStatus(true);
            })
            .catch(function () {
                setStatus(false);
            });
    }

    function fetchServices() {
        var grid = $('#servicesGrid');
        fetch('/api/services')
            .then(function (res) {
                if (!res.ok) throw new Error('HTTP ' + res.status);
                return res.json();
            })
            .then(function (services) {
                if (services.length === 0) {
                    grid.innerHTML = '<p class="no-services">No services bound to this application.</p>';
                    return;
                }
                grid.innerHTML = '';
                services.forEach(function (svc) {
                    var tags = (svc.tags || [])
                        .map(function (t) { return '<span class="tag">' + escapeHtml(t) + '</span>'; })
                        .join('');
                    var card = document.createElement('div');
                    card.className = 'service-card';
                    card.innerHTML =
                        '<h3>' + escapeHtml(svc.name) + '</h3>' +
                        '<div class="service-meta">' +
                        '  <div class="meta-row"><span class="meta-label">Label</span><span class="meta-value">' + escapeHtml(svc.label) + '</span></div>' +
                        '  <div class="meta-row"><span class="meta-label">Plan</span><span class="meta-value">' + escapeHtml(svc.plan) + '</span></div>' +
                        (tags ? '  <div class="tags">' + tags + '</div>' : '') +
                        '</div>';
                    grid.appendChild(card);
                });
            })
            .catch(function () {
                grid.innerHTML = '<p class="no-services">Failed to load services.</p>';
            });
    }

    function escapeHtml(str) {
        var div = document.createElement('div');
        div.appendChild(document.createTextNode(str));
        return div.innerHTML;
    }

    function startPolling() {
        stopPolling();
        var interval = parseInt($('#refreshInterval').value, 10);
        pollTimer = setInterval(fetchInstance, interval);
    }

    function stopPolling() {
        if (pollTimer !== null) {
            clearInterval(pollTimer);
            pollTimer = null;
        }
    }

    // Tab switching
    $$('.tab').forEach(function (tab) {
        tab.addEventListener('click', function () {
            $$('.tab').forEach(function (t) { t.classList.remove('active'); });
            $$('.tab-content').forEach(function (c) { c.classList.remove('active'); });

            tab.classList.add('active');
            var target = tab.getAttribute('data-tab');
            $('#tab-' + target).classList.add('active');

            if (target === 'services' && !servicesLoaded) {
                fetchServices();
                servicesLoaded = true;
            }
        });
    });

    // Manual refresh button
    $('#manualRefresh').addEventListener('click', function () {
        fetchInstance();
    });

    // Auto refresh checkbox
    $('#autoRefreshToggle').addEventListener('change', function () {
        if (this.checked) {
            startPolling();
        } else {
            stopPolling();
        }
    });

    // Interval dropdown change — restart polling only if auto-refresh is on
    $('#refreshInterval').addEventListener('change', function () {
        if ($('#autoRefreshToggle').checked) {
            startPolling();
        }
    });

    // Initial fetch only — no auto-refresh until checkbox is checked
    fetchInstance();
})();
