
let currentShipment = null;
let shipmentId = null;
let allPackages = [];

document.addEventListener('DOMContentLoaded', () => {
    shipmentId = new URLSearchParams(location.search).get('id');

    if (!shipmentId) {
        alert('Shipment ID is missing.');
        return;
    }

    document.getElementById('editShipmentButton')
        ?.addEventListener('click', openEditForm);

    document.getElementById('cancelEditShipmentButton')
        ?.addEventListener('click', () => toggle('editShipmentSection', false));

    document.getElementById('editShipmentForm')
        ?.addEventListener('submit', saveShipment);

    document.getElementById('markDeliveredButton')
        ?.addEventListener('click', markDelivered);

    document.getElementById('editPackagesButton')
        ?.addEventListener('click', openPackageEditor);

    document.getElementById('cancelEditPackagesButton')
        ?.addEventListener('click', () => toggle('editPackagesSection', false));

    document.getElementById('savePackagesButton')
        ?.addEventListener('click', savePackages);
    

    loadShipment();
    applyShipmentEditability(currentShipment)
});

async function api(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        }
    });

    const text = await response.text();
    let data = null;

    try {
        data = text ? JSON.parse(text) : null;
    } catch {
        data = text;
    }

    if (!response.ok) {
        throw new Error(
            data?.message ||
            data?.error ||
            `Request failed (${response.status})`
        );
    }

    return data;
}

// =========================
// LOAD SHIPMENT
// =========================

async function loadShipment() {
    
    try {
        currentShipment = await api(
            `/erpflow/api/shipments/${encodeURIComponent(shipmentId)}`
        );
        const isDelivered =
    String(currentShipment.status || "").toUpperCase() === "DELIVERED";

const editPackagesButton =
    document.getElementById("editPackagesButton");

if (editPackagesButton) {
    editPackagesButton.disabled = isDelivered;
    editPackagesButton.hidden = isDelivered;
}

if (isDelivered) {
    document.getElementById("editPackagesSection").hidden = true;
}

        displayShipment(currentShipment);
    } catch (e) {
        console.error(e);
        alert(`Unable to load shipment details: ${e.message}`);
    }
}

// =========================
// DISPLAY SHIPMENT
// =========================

function displayShipment(shipment) {
    setText(
        'shipmentNumber',
        shipment.shipmentNumber || `Shipment #${shipment.id}`
    );

    setText('shipmentDate', formatDateTime(shipment.shipmentDate));
    setText('shippingCharge', formatMoney(shipment.shippingCharge));
    setText('estimatedDeliveryDate', formatDate(shipment.estimatedDeliveryDate));
    setText('actualDeliveryDate', formatDate(shipment.actualDeliveryDate));

    const status = shipment.status || 'UNKNOWN';
    const statusEl = document.getElementById('shipmentStatus');

    if (statusEl) {
        statusEl.textContent = status;
        statusEl.className = `status-badge ${getStatusClass(status)}`;
    }

    const firstPackage = shipment.packages?.[0];
    const order = firstPackage?.salesOrder;
    const orderLink = document.getElementById('salesOrderLink');

    if (orderLink) {
        if (order) {
            orderLink.textContent = `#${order.id}`;
            orderLink.href =
                `/erpflow/salesOrderDetails.jsp?id=${encodeURIComponent(order.id)}`;
        } else {
            orderLink.textContent = '-';
            orderLink.href = '#';
        }
    }

    setText('customerName', order?.customer?.name || '-');
    setText('carrierName', shipment.carrier?.name || '-');
    setText('carrierService', shipment.carrierService?.name || '-');
    setText('trackingNumber', shipment.trackingNumber || 'Not available');
    setText('dispatchAddress', shipment.dispatchAddress || '-');
    setText('destinationAddress', shipment.destinationAddress || '-');

    const tracking = document.getElementById('trackingLink');

    if (tracking) {
        if (shipment.trackingUrl) {
            tracking.href = shipment.trackingUrl;
            tracking.style.display = 'inline-block';
        } else {
            tracking.style.display = 'none';
            tracking.href = '#';
        }
    }

    displayPackages(shipment.packages || []);

    const delivered = String(status).toUpperCase() === 'DELIVERED';
    const deliveredButton = document.getElementById('markDeliveredButton');

    if (deliveredButton) {
        deliveredButton.disabled = delivered;
        deliveredButton.textContent = delivered
            ? 'Already Delivered'
            : 'Mark as Delivered';
    }
}

// =========================
// DISPLAY PACKAGES
// =========================

function displayPackages(packages) {
    const tbody = document.getElementById('packagesTableBody');
    if (!tbody) return;

    tbody.replaceChildren();

    if (!packages.length) {
        tbody.innerHTML =
            '<tr><td colspan="4" class="empty-message">No packages found.</td></tr>';
        return;
    }

    packages.forEach(pkg => {
        const row = document.createElement('tr');

        const dimensions =
            `${formatNumber(pkg.length)} × ${formatNumber(pkg.width)} × ${formatNumber(pkg.height)} cm`;

        row.innerHTML = `
            <td class="package-number">${escapeHtml(pkg.packageNumber || '-')}</td>
            <td>${formatNumber(pkg.weight)} kg</td>
            <td>${dimensions}</td>
            <td>
                <span class="status-badge ${getStatusClass(pkg.status)}">
                    ${escapeHtml(pkg.status || '-')}
                </span>
            </td>
        `;

        tbody.appendChild(row);
    });
}

// =========================
// EDIT SHIPMENT FORM
// =========================

function openEditForm() {
    if (!currentShipment) return;

    const s = currentShipment;

    putValue('editShipmentDate', toDateTimeLocal(s.shipmentDate));
    putValue('editShipmentStatus', s.status || 'CREATED');
    putValue('editShippingMethod', s.shippingMethod || '');
    putValue('editShippingCharge', s.shippingCharge ?? 0);
    putValue('editCarrier', s.carrier?.name || '');
    putValue('editCarrierService', s.carrierService?.name || '');
    putValue('editTrackingNumber', s.trackingNumber || '');
    putValue('editTrackingUrl', s.trackingUrl || '');
    putValue('editEstimatedDeliveryDate', toDateInput(s.estimatedDeliveryDate));
    putValue('editActualDeliveryDate', toDateInput(s.actualDeliveryDate));
    putValue('editDispatchAddress', s.dispatchAddress || '');
    putValue('editDestinationAddress', s.destinationAddress || '');
    putValue('editShipmentNotes', s.notes || '');

    showMessage('editShipmentMessage', '');
    toggle('editShipmentSection', true);

    document.getElementById('editShipmentSection')
        ?.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

async function saveShipment(event) {
    event.preventDefault();

    if (
    String(currentShipment.status || "").toUpperCase() === "DELIVERED"
) {
    alert("Packages cannot be changed after delivery.");
    return;
}

    if (
    String(currentShipment.status || "").toUpperCase() === "DELIVERED"
) {
    alert("Delivered shipments cannot be edited.");
    return;

}

if (
    String(currentShipment.status || "").toUpperCase() === "DELIVERED"
) {
    alert("Packages cannot be changed after delivery.");
    return;
}

    if (!currentShipment) return;

    const s = currentShipment;
    const charge = Number(value('editShippingCharge'));

    if (!Number.isFinite(charge) || charge < 0) {
        showMessage(
            'editShipmentMessage',
            'Enter a valid non-negative shipping charge.'
        );
        return;
    }

    const payload = {
        id: s.id,
        shipmentNumber: s.shipmentNumber,
        shipmentDate: fromDateTimeLocal(value('editShipmentDate')),
        status: value('editShipmentStatus'),
        shippingMethod: value('editShippingMethod'),

        // Preserve the existing Carrier and CarrierService associations.
        carrier: s.carrier ? { id: s.carrier.id } : null,
        carrierService: s.carrierService ? { id: s.carrierService.id } : null,

        trackingNumber: value('editTrackingNumber'),
        trackingUrl: value('editTrackingUrl'),
        shippingCharge: charge,
        dispatchAddress: value('editDispatchAddress'),
        destinationAddress: value('editDestinationAddress'),
        estimatedDeliveryDate: value('editEstimatedDeliveryDate') || null,
        actualDeliveryDate: value('editActualDeliveryDate') || null,
        notes: value('editShipmentNotes'),
        packages: s.packages || []
    };

    try {
        disable('saveShipmentButton', true);

        await api(
            `/erpflow/api/shipments/${encodeURIComponent(shipmentId)}`,
            {
                method: 'PUT',
                body: JSON.stringify(payload)
            }
        );

        toggle('editShipmentSection', false);
        await loadShipment();
        showMessage('editShipmentMessage', 'Shipment saved.');

    } catch (e) {
        showMessage('editShipmentMessage', e.message);
    } finally {
        disable('saveShipmentButton', false);
    }
}

// =========================
// MARK AS DELIVERED
// =========================

async function markDelivered() {
    if (!currentShipment ||
        String(currentShipment.status).toUpperCase() === 'DELIVERED') {
        return;
    }

    if (!confirm('Mark this shipment as delivered?')) return;

    try {
        disable('markDeliveredButton', true);

        await api(
            `/erpflow/api/shipments/${encodeURIComponent(shipmentId)}/deliver`,
            {
                method: 'POST',
                body: '{}'
            }
        );

        await loadShipment();

    } catch (e) {
        alert(`Could not mark shipment delivered: ${e.message}`);
    } finally {
        disable('markDeliveredButton', false);
    }
}

// =========================
// PACKAGE EDITOR
// =========================

async function openPackageEditor() {
    if (!currentShipment) return;

    toggle('editPackagesSection', true);
    showMessage('packageEditMessage', 'Loading packages…');

    try {
        const result = await api('/erpflow/api/packages');

        allPackages = Array.isArray(result)
            ? result
            : (result?.packages || result?.data || []);

        renderPackageChoices(currentShipment.packages || []);

        showMessage(
            'packageEditMessage',
            'Select the packages for this shipment.'
        );

    } catch (e) {
        showMessage(
            'packageEditMessage',
            `Could not load packages: ${e.message}. Check that GET /api/packages returns the package list.`
        );

        const container = document.getElementById('availablePackagesContainer');
        if (container) container.textContent = '';
    }
}

function renderPackageChoices(assigned) {
    const container = document.getElementById('availablePackagesContainer');
    if (!container) return;

    const assignedIds = new Set(assigned.map(p => Number(p.id)));

    const eligible = allPackages.filter(pkg => {
        const items = pkg.packageItems || pkg.items || [];

        const containsIneligibleItem = items.some(packageItem => {
            const item = packageItem.item || packageItem;

            return item.itemType === 'SERVICE' ||
                item.trackInventory === false;
        });

        const linkedToOtherShipment =
            pkg.shipmentId &&
            Number(pkg.shipmentId) !== Number(shipmentId);

        return !containsIneligibleItem && !linkedToOtherShipment;
    });

    container.replaceChildren();

    if (!eligible.length) {
        container.textContent = 'No eligible packages are available.';
        return;
    }

    eligible.forEach(pkg => {
        const label = document.createElement('label');
        label.className = 'package-choice';

        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.value = pkg.id;
        checkbox.checked = assignedIds.has(Number(pkg.id));
        checkbox.dataset.packageId = pkg.id;

        const text = document.createElement('span');
        text.textContent =
            `${pkg.packageNumber || `Package #${pkg.id}`} — ${Number(pkg.weight || 0).toFixed(2)} kg`;

        label.append(checkbox, text);
        container.appendChild(label);
    });
}

async function savePackages() {
    const selected = [
        ...document.querySelectorAll(
            '#availablePackagesContainer input[type="checkbox"]:checked'
        )
    ].map(el => Number(el.dataset.packageId));

    try {
        disable('savePackagesButton', true);

        await api(
            `/erpflow/api/shipments/${encodeURIComponent(shipmentId)}/packages`,
            {
                method: 'PUT',
                body: JSON.stringify({ packageIds: selected })
            }
        );

        toggle('editPackagesSection', false);
        await loadShipment();

    } catch (e) {
        showMessage('packageEditMessage', e.message);
    } finally {
        disable('savePackagesButton', false);
    }
}

// =========================
// HELPERS
// =========================

function value(id) {
    return document.getElementById(id)?.value ?? '';
}

function putValue(id, v) {
    const element = document.getElementById(id);
    if (element) element.value = v ?? '';
}

function toggle(id, visible) {
    const element = document.getElementById(id);
    if (element) element.style.display = visible ? 'block' : 'none';
}

function disable(id, state) {
    const element = document.getElementById(id);
    if (element) element.disabled = state;
}

function showMessage(id, message) {
    const element = document.getElementById(id);
    if (element) element.textContent = message;
}

function setText(id, v) {
    const element = document.getElementById(id);

    if (element) {
        element.textContent = v == null || v === '' ? '-' : v;
    }
}

function formatMoney(v) {
    const n = Number(v);
    return `₹${(Number.isFinite(n) ? n : 0).toFixed(2)}`;
}

function formatNumber(v) {
    const n = Number(v);
    return Number.isFinite(n) ? n.toFixed(2) : '-';
}

function formatDateTime(v) {
    if (!v) return '-';

    if (Array.isArray(v)) {
        return `${pad(v[2])}/${pad(v[1])}/${v[0]} ${pad(v[3] || 0)}:${pad(v[4] || 0)}`;
    }

    const d = new Date(v);
    return Number.isNaN(d.getTime()) ? String(v) : d.toLocaleString();
}

function formatDate(v) {
    if (!v) return '-';

    if (Array.isArray(v)) {
        return `${pad(v[2])}/${pad(v[1])}/${v[0]}`;
    }

    const d = new Date(v);
    return Number.isNaN(d.getTime()) ? String(v) : d.toLocaleDateString();
}

function toDateInput(v) {
    if (!v) return '';

    if (Array.isArray(v)) {
        return `${v[0]}-${pad(v[1])}-${pad(v[2])}`;
    }

    return String(v).slice(0, 10);
}

function toDateTimeLocal(v) {
    if (!v) return '';

    if (Array.isArray(v)) {
        return `${v[0]}-${pad(v[1])}-${pad(v[2])}T${pad(v[3] || 0)}:${pad(v[4] || 0)}:${pad(v[5] || 0)}`;
    }

    return String(v).slice(0, 19);
}

function fromDateTimeLocal(v) {
    return v ? (v.length === 16 ? `${v}:00` : v) : null;
}

function pad(v) {
    return String(v).padStart(2, '0');
}

function getStatusClass(status) {
    switch (String(status || '').toUpperCase()) {
        case 'CREATED':
            return 'status-created';
        case 'SHIPPED':
            return 'status-shipped';
        case 'DELIVERED':
            return 'status-delivered';
        default:
            return 'status-default';
    }
}

function escapeHtml(v) {
    return String(v ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function goBack() {
    location.href = '/erpflow/shipments.jsp';
}


function applyShipmentEditability(shipment) {
    const isDelivered =
        String(shipment.status || "").toUpperCase() === "DELIVERED";

    const editShipmentButton =
        document.getElementById("editShipmentButton");

    const editPackagesButton =
        document.getElementById("editPackagesButton");

    const markDeliveredButton =
        document.getElementById("markDeliveredButton");

    const editShipmentSection =
        document.getElementById("editShipmentSection");

    const editPackagesSection =
        document.getElementById("editPackagesSection");

    if (editShipmentButton) {
        editShipmentButton.disabled = isDelivered;
        editShipmentButton.hidden = isDelivered;
    }

    if (editPackagesButton) {
        editPackagesButton.disabled = isDelivered;
        editPackagesButton.hidden = isDelivered;
    }

    if (markDeliveredButton) {
        markDeliveredButton.disabled = isDelivered;
        markDeliveredButton.hidden = isDelivered;
    }

    if (isDelivered) {
        if (editShipmentSection) {
            editShipmentSection.hidden = true;
        }

        if (editPackagesSection) {
            editPackagesSection.hidden = true;
        }
    }
}