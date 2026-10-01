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
        ?.addEventListener('click', () =>
            toggle('editShipmentSection', false)
        );

    document.getElementById('editShipmentForm')
        ?.addEventListener('submit', saveShipment);

    document.getElementById('markDeliveredButton')
        ?.addEventListener('click', markDelivered);

    document.getElementById('editPackagesButton')
        ?.addEventListener('click', openPackageEditor);

    document.getElementById('cancelEditPackagesButton')
        ?.addEventListener('click', () =>
            toggle('editPackagesSection', false)
        );

    document.getElementById('savePackagesButton')
        ?.addEventListener('click', savePackages);

    loadShipment();

    document
        .getElementById('editCarrier')
        ?.addEventListener(
            'change',
            async function () {
                await loadEditCarrierServices(this.value);
            }
        );
});


// =========================
// API HELPER
// =========================

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
            const section =
                document.getElementById("editPackagesSection");

            if (section) {
                section.hidden = true;
            }
        }

        displayShipment(currentShipment);
        applyShipmentEditability(currentShipment);

    } catch (e) {

        console.error(e);

        alert(
            `Unable to load shipment details: ${e.message}`
        );
    }
}


// =========================
// DISPLAY SHIPMENT
// =========================

function displayShipment(shipment) {

    setText(
        'shipmentNumber',
        shipment.shipmentNumber ||
        `Shipment #${shipment.id}`
    );

    setText(
        'shipmentDate',
        formatDate(shipment.shipmentDate)
    );

    setText(
        'shippingCharge',
        formatMoney(shipment.shippingCharge)
    );

    setText(
        'estimatedDeliveryDate',
        formatDate(shipment.estimatedDeliveryDate)
    );

    setText(
        'actualDeliveryDate',
        formatDate(shipment.actualDeliveryDate)
    );

    const status =
        shipment.status || 'UNKNOWN';

    const statusEl =
        document.getElementById('shipmentStatus');

    if (statusEl) {

        statusEl.textContent = status;

        statusEl.className =
            `status-badge ${getStatusClass(status)}`;
    }

    const firstPackage =
        shipment.packages?.[0];

    const order =
        firstPackage?.salesOrder;

    const orderLink =
        document.getElementById('salesOrderLink');

    if (orderLink) {

        if (order) {

            orderLink.textContent =
                `#${order.id}`;

            orderLink.href =
                `/erpflow/salesOrderDetails.jsp?id=${encodeURIComponent(order.id)}`;

        } else {

            orderLink.textContent = '-';
            orderLink.href = '#';
        }
    }

    setText(
        'customerName',
        order?.customer?.name || '-'
    );

    setText(
        'carrierName',
        shipment.carrier?.name || '-'
    );

    setText(
        'carrierService',
        shipment.carrierService?.name || '-'
    );

    setText(
        'trackingNumber',
        shipment.trackingNumber || 'Not available'
    );

    setText(
        'dispatchAddress',
        shipment.dispatchAddress || '-'
    );

    setText(
        'destinationAddress',
        shipment.destinationAddress || '-'
    );

    const tracking =
        document.getElementById('trackingLink');

    if (tracking) {

        if (shipment.trackingUrl) {

            tracking.href =
                shipment.trackingUrl;

            tracking.style.display =
                'inline-block';

        } else {

            tracking.style.display =
                'none';

            tracking.href = '#';
        }
    }

    displayPackages(
        shipment.packages || []
    );

    const delivered =
        String(status).toUpperCase() === 'DELIVERED';

    const deliveredButton =
        document.getElementById(
            'markDeliveredButton'
        );

    if (deliveredButton) {

        deliveredButton.disabled =
            delivered;

        deliveredButton.textContent =
            delivered
                ? 'Already Delivered'
                : 'Mark as Delivered';
    }
}


// =========================
// DISPLAY PACKAGES
// =========================

function displayPackages(packages) {

    const tbody =
        document.getElementById(
            'packagesTableBody'
        );

    if (!tbody) return;

    tbody.replaceChildren();

    if (!packages.length) {

        tbody.innerHTML =
            '<tr><td colspan="4" class="empty-message">No packages found.</td></tr>';

        return;
    }

    packages.forEach(pkg => {

        const row =
            document.createElement('tr');

        const dimensions =
            `${formatNumber(pkg.length)} × ` +
            `${formatNumber(pkg.width)} × ` +
            `${formatNumber(pkg.height)} cm`;

        row.innerHTML = `
            <td class="package-number">
                ${escapeHtml(pkg.packageNumber || '-')}
            </td>

            <td>
                ${formatNumber(pkg.weight)} kg
            </td>

            <td>
                ${dimensions}
            </td>

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

    if (!currentShipment) {
        return;
    }

    const s =
        currentShipment;

    putValue(
        'editShipmentDate',
        toDateInput(s.shipmentDate)
    );

    putValue(
        'editShipmentStatus',
        s.status || 'CREATED'
    );

    putValue(
        'editShippingMethod',
        s.shippingMethod || ''
    );

    putValue(
        'editShippingCharge',
        s.shippingCharge ?? 0
    );

    putValue(
        'editTrackingNumber',
        s.trackingNumber || ''
    );

    putValue(
        'editTrackingUrl',
        s.trackingUrl || ''
    );

    putValue(
        'editEstimatedDeliveryDate',
        toDateInput(s.estimatedDeliveryDate)
    );

    putValue(
        'editActualDeliveryDate',
        toDateInput(s.actualDeliveryDate)
    );

    putValue(
        'editDispatchAddress',
        s.dispatchAddress || ''
    );

    putValue(
        'editDestinationAddress',
        s.destinationAddress || ''
    );

    putValue(
        'editShipmentNotes',
        s.notes || ''
    );


    const delivered =
        String(
            s.status || ''
        ).toUpperCase() === 'DELIVERED';


    const checkbox =
        document.getElementById(
            'markAsDeliveredCheckbox'
        );

    if (checkbox) {
        checkbox.checked =
            delivered;
    }


    toggle(
        'editShipmentSection',
        true
    );


    showMessage(
        'editShipmentMessage',
        ''
    );


    const carrierId =
        s.carrier?.id ?? null;

    const serviceId =
        s.carrierService?.id ?? null;


    loadEditCarriers(
        carrierId
    ).then(() =>
        loadEditCarrierServices(
            carrierId,
            serviceId
        )
    );


    document
        .getElementById(
            'editShipmentSection'
        )
        ?.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });
}


// =========================
// LOAD CARRIERS
// =========================

async function loadEditCarriers(
    selectedCarrierId = null
) {

    const select =
        document.getElementById(
            'editCarrier'
        );

    if (!select) {
        return;
    }

    try {

        const carriers =
            await api(
                '/erpflow/api/carriers'
            );


        select.innerHTML =
            '<option value="">Select Carrier</option>';


        (
            Array.isArray(carriers)
                ? carriers
                : []
        ).forEach(carrier => {

            const option =
                document.createElement(
                    'option'
                );

            option.value =
                carrier.id;

            option.textContent =
                carrier.name ||
                `Carrier #${carrier.id}`;

            select.appendChild(
                option
            );
        });


        select.value =
            selectedCarrierId != null
                ? String(selectedCarrierId)
                : '';

    } catch (e) {

        console.error(e);

        select.innerHTML =
            '<option value="">Failed to load carriers</option>';
    }
}


// =========================
// LOAD CARRIER SERVICES
// =========================

async function loadEditCarrierServices(
    carrierId,
    selectedServiceId = null
) {

    const select =
        document.getElementById(
            'editCarrierService'
        );

    if (!select) {
        return;
    }


    select.innerHTML =
        '<option value="">Select Carrier Service</option>';


    select.disabled =
        !carrierId;


    if (!carrierId) {
        return;
    }


    try {

        const services =
            await api(
                `/erpflow/api/carriers/${encodeURIComponent(carrierId)}/services`
            );


        (
            Array.isArray(services)
                ? services
                : []
        ).forEach(service => {

            const option =
                document.createElement(
                    'option'
                );

            option.value =
                service.id;

            option.textContent =
                service.name ||
                `Service #${service.id}`;

            select.appendChild(
                option
            );
        });


        select.value =
            selectedServiceId != null
                ? String(selectedServiceId)
                : '';

    } catch (e) {

        console.error(e);

        select.innerHTML =
            '<option value="">Failed to load services</option>';
    }
}


// =========================
// SAVE SHIPMENT
// =========================

async function saveShipment(event) {

    event.preventDefault();


    if (!currentShipment) {
        return;
    }


    const s =
        currentShipment;


    const currentStatus =
        String(
            s.status || ''
        ).toUpperCase();


    // Delivered shipments are final.

    if (
        currentStatus === 'DELIVERED'
    ) {

        alert(
            'Delivered shipments cannot be edited.'
        );

        return;
    }


    // =========================
    // SHIPPING CHARGE
    // =========================

    const charge =
        Number(
            value(
                'editShippingCharge'
            )
        );


    if (
        !Number.isFinite(charge) ||
        charge < 0
    ) {

        showMessage(
            'editShipmentMessage',
            'Enter a valid non-negative shipping charge.'
        );

        return;
    }


    // =========================
    // DATES
    // =========================

    const shipmentDate =
        value(
            'editShipmentDate'
        );


    const estimatedDeliveryDate =
        value(
            'editEstimatedDeliveryDate'
        ) || null;


    const actualDeliveryDate =
        value(
            'editActualDeliveryDate'
        ) || null;


    // =========================
    // MARK AS DELIVERED
    // =========================

    const markAsDelivered =
        document
            .getElementById(
                'markAsDeliveredCheckbox'
            )
            ?.checked === true;


    // =========================
    // STATUS
    // =========================

    let selectedStatus =
        String(
            value(
                'editShipmentStatus'
            ) || ''
        ).toUpperCase();


    // =========================
    // SHIPMENT DATE REQUIRED
    // =========================

    if (!shipmentDate) {

        showMessage(
            'editShipmentMessage',
            'Shipment date is required.'
        );

        document
            .getElementById(
                'editShipmentDate'
            )
            ?.focus();

        return;
    }


    // =========================
    // SALES ORDER DATE
    // =========================

    const salesOrderDate =
        s.salesOrder?.orderDate ||
        s.salesOrderDate ||
        null;


    const normalizedSalesOrderDate =
        salesOrderDate
            ? String(
                salesOrderDate
            ).slice(0, 10)
            : null;


    // Shipment cannot happen
    // before sales order.

    if (
        normalizedSalesOrderDate &&
        shipmentDate <
        normalizedSalesOrderDate
    ) {

        showMessage(
            'editShipmentMessage',
            `Shipment date cannot be before sales order date (${normalizedSalesOrderDate}).`
        );

        document
            .getElementById(
                'editShipmentDate'
            )
            ?.focus();

        return;
    }


    // =========================
    // ESTIMATED DELIVERY DATE
    // =========================

    if (
        estimatedDeliveryDate &&
        estimatedDeliveryDate <
        shipmentDate
    ) {

        showMessage(
            'editShipmentMessage',
            'Estimated delivery date cannot be before shipment date.'
        );

        document
            .getElementById(
                'editEstimatedDeliveryDate'
            )
            ?.focus();

        return;
    }


    // =========================
    // ACTUAL DELIVERY DATE
    // =========================

    if (
        actualDeliveryDate &&
        actualDeliveryDate <
        shipmentDate
    ) {

        showMessage(
            'editShipmentMessage',
            'Actual delivery date cannot be before shipment date.'
        );

        document
            .getElementById(
                'editActualDeliveryDate'
            )
            ?.focus();

        return;
    }


    // =========================
    // MARK AS DELIVERED
    //
    // CREATED -> DELIVERED
    // SHIPPED -> DELIVERED
    // IN_TRANSIT -> DELIVERED
    //
    // ALL ALLOWED.
    // =========================

    if (markAsDelivered) {

        if (!actualDeliveryDate) {

            showMessage(
                'editShipmentMessage',
                'Actual delivery date is required when marking the shipment as delivered.'
            );

            document
                .getElementById(
                    'editActualDeliveryDate'
                )
                ?.focus();

            return;
        }


        selectedStatus =
            'DELIVERED';
    }


    // =========================
    // DROPDOWN STATUS = DELIVERED
    // =========================

    if (
        selectedStatus ===
        'DELIVERED'
    ) {

        if (!actualDeliveryDate) {

            showMessage(
                'editShipmentMessage',
                'Actual delivery date is required when status is DELIVERED.'
            );

            document
                .getElementById(
                    'editActualDeliveryDate'
                )
                ?.focus();

            return;
        }
    }


    // =========================
    // CARRIER
    // =========================

    const carrierId =
        value(
            'editCarrier'
        ) || null;


    const carrierServiceId =
        value(
            'editCarrierService'
        ) || null;


    if (
        carrierId &&
        !carrierServiceId
    ) {

        showMessage(
            'editShipmentMessage',
            'Please select a carrier service.'
        );

        document
            .getElementById(
                'editCarrierService'
            )
            ?.focus();

        return;
    }


    // =========================
    // PAYLOAD
    // =========================

    const payload = {

        id:
            s.id,

        shipmentNumber:
            s.shipmentNumber,

        shipmentDate:
            shipmentDate,

        status:
            selectedStatus,

        shippingMethod:
            value(
                'editShippingMethod'
            ),

        carrier:
            carrierId
                ? {
                    id:
                        Number(
                            carrierId
                        )
                }
                : null,

        carrierService:
            carrierServiceId
                ? {
                    id:
                        Number(
                            carrierServiceId
                        )
                }
                : null,

        trackingNumber:
            value(
                'editTrackingNumber'
            ),

        trackingUrl:
            value(
                'editTrackingUrl'
            ),

        shippingCharge:
            charge,

        dispatchAddress:
            value(
                'editDispatchAddress'
            ),

        destinationAddress:
            value(
                'editDestinationAddress'
            ),

        estimatedDeliveryDate:
            estimatedDeliveryDate,

        actualDeliveryDate:
            actualDeliveryDate,

        notes:
            value(
                'editShipmentNotes'
            ),

        packages:
            s.packages || []
    };


    // =========================
    // SAVE
    // =========================

    try {

        disable(
            'saveShipmentButton',
            true
        );


        await api(
            `/erpflow/api/shipments/${encodeURIComponent(shipmentId)}`,
            {
                method: 'PUT',

                body:
                    JSON.stringify(
                        payload
                    )
            }
        );


        toggle(
            'editShipmentSection',
            false
        );


        await loadShipment();


    } catch (e) {

        console.error(
            'Save Shipment Error:',
            e
        );


        showMessage(
            'editShipmentMessage',
            e.message ||
            'Failed to save shipment.'
        );


    } finally {

        disable(
            'saveShipmentButton',
            false
        );
    }
}


// =========================
// MARK AS DELIVERED
// =========================

async function markDelivered() {

    if (
        !currentShipment ||
        String(
            currentShipment.status || ''
        ).toUpperCase() ===
        'DELIVERED'
    ) {

        return;
    }


    const actualDeliveryDate =
        value(
            'editActualDeliveryDate'
        ) ||
        value(
            'actualDeliveryDate'
        );


    if (!actualDeliveryDate) {

        alert(
            'Please enter the actual delivery date.'
        );

        return;
    }


    const shipmentDate =
        toDateInput(
            currentShipment.shipmentDate
        );


    if (
        shipmentDate &&
        actualDeliveryDate <
        shipmentDate
    ) {

        alert(
            'Actual delivery date cannot be before shipment date.'
        );

        return;
    }


    if (
        !confirm(
            `Mark this shipment as delivered on ${formatDate(actualDeliveryDate)}?`
        )
    ) {

        return;
    }


    try {

        disable(
            'markDeliveredButton',
            true
        );


        await api(
            `/erpflow/api/shipments/${encodeURIComponent(shipmentId)}/deliver`,
            {
                method: 'POST',

                body:
                    JSON.stringify({
                        actualDeliveryDate
                    })
            }
        );


        await loadShipment();


    } catch (e) {

        alert(
            `Could not mark shipment delivered: ${e.message}`
        );


    } finally {

        disable(
            'markDeliveredButton',
            false
        );
    }
}


// =========================
// PACKAGE EDITOR
// =========================

async function openPackageEditor() {

    if (!currentShipment) {
        return;
    }


    if (
        String(
            currentShipment.status || ''
        ).toUpperCase() ===
        'DELIVERED'
    ) {

        return;
    }


    toggle(
        'editPackagesSection',
        true
    );


    showMessage(
        'packageEditMessage',
        'Loading packages…'
    );


    try {

        const result =
            await api(
                '/erpflow/api/packages'
            );


        allPackages =
            Array.isArray(result)
                ? result
                : (
                    result?.packages ||
                    result?.data ||
                    []
                );


        renderPackageChoices(
            currentShipment.packages ||
            []
        );


        showMessage(
            'packageEditMessage',
            'Select the packages for this shipment.'
        );


    } catch (e) {

        showMessage(
            'packageEditMessage',
            `Could not load packages: ${e.message}. Check that GET /api/packages returns the package list.`
        );


        const container =
            document.getElementById(
                'availablePackagesContainer'
            );


        if (container) {
            container.textContent = '';
        }
    }
}


// =========================
// RENDER PACKAGE CHOICES
// =========================

function renderPackageChoices(
    assigned
) {

    const container =
        document.getElementById(
            'availablePackagesContainer'
        );


    if (!container) {
        return;
    }


    const assignedIds =
        new Set(
            assigned.map(
                p => Number(
                    p.id
                )
            )
        );


    const eligible =
        allPackages.filter(
            pkg => {

                const items =
                    pkg.packageItems ||
                    pkg.items ||
                    [];


                const containsIneligibleItem =
                    items.some(
                        packageItem => {

                            const item =
                                packageItem.item ||
                                packageItem;


                            return (
                                item.itemType ===
                                'SERVICE'
                            ) ||
                            (
                                item.trackInventory ===
                                false
                            );
                        }
                    );


                const linkedToOtherShipment =
                    pkg.shipmentId &&
                    Number(
                        pkg.shipmentId
                    ) !==
                    Number(
                        shipmentId
                    );


                const packageStatus =
                    String(
                        pkg.status ||
                        ''
                    ).toUpperCase();


                // A package that is already
                // shipped must not be selected
                // again for another shipment.

                const alreadyShipped =
                    packageStatus ===
                    'SHIPPED' ||
                    packageStatus ===
                    'IN_TRANSIT' ||
                    packageStatus ===
                    'DELIVERED';


                return (
                    !containsIneligibleItem &&
                    !linkedToOtherShipment &&
                    (
                        assignedIds.has(
                            Number(
                                pkg.id
                            )
                        ) ||
                        !alreadyShipped
                    )
                );
            }
        );


    container.replaceChildren();


    if (!eligible.length) {

        container.textContent =
            'No eligible packages are available.';

        return;
    }


    eligible.forEach(
        pkg => {

            const label =
                document.createElement(
                    'label'
                );


            label.className =
                'package-choice';


            const checkbox =
                document.createElement(
                    'input'
                );


            checkbox.type =
                'checkbox';


            checkbox.value =
                pkg.id;


            checkbox.checked =
                assignedIds.has(
                    Number(
                        pkg.id
                    )
                );


            checkbox.dataset.packageId =
                pkg.id;


            const text =
                document.createElement(
                    'span'
                );


            text.textContent =
                `${pkg.packageNumber || `Package #${pkg.id}`} — ${Number(pkg.weight || 0).toFixed(2)} kg`;


            label.append(
                checkbox,
                text
            );


            container.appendChild(
                label
            );
        }
    );
}


// =========================
// SAVE PACKAGES
// =========================

async function savePackages() {

    const selected = [

        ...document.querySelectorAll(
            '#availablePackagesContainer input[type="checkbox"]:checked'
        )

    ].map(
        el =>
            Number(
                el.dataset.packageId
            )
    );


    try {

        disable(
            'savePackagesButton',
            true
        );


        await api(
            `/erpflow/api/shipments/${encodeURIComponent(shipmentId)}/packages`,
            {
                method: 'PUT',

                body:
                    JSON.stringify({
                        packageIds:
                            selected
                    })
            }
        );


        toggle(
            'editPackagesSection',
            false
        );


        await loadShipment();


    } catch (e) {

        showMessage(
            'packageEditMessage',
            e.message
        );


    } finally {

        disable(
            'savePackagesButton',
            false
        );
    }
}


// =========================
// HELPERS
// =========================

function value(id) {

    return document
        .getElementById(id)
        ?.value ?? '';
}


function putValue(id, v) {

    const element =
        document.getElementById(id);


    if (element) {

        element.value =
            v ?? '';
    }
}


function toggle(
    id,
    visible
) {

    const element =
        document.getElementById(id);


    if (element) {

        element.style.display =
            visible
                ? 'block'
                : 'none';
    }
}


function disable(
    id,
    state
) {

    const element =
        document.getElementById(id);


    if (element) {

        element.disabled =
            state;
    }
}


function showMessage(
    id,
    message
) {

    const element =
        document.getElementById(id);


    if (element) {

        element.textContent =
            message;
    }
}


function setText(
    id,
    v
) {

    const element =
        document.getElementById(id);


    if (element) {

        element.textContent =
            v == null ||
            v === ''
                ? '-'
                : v;
    }
}


function formatMoney(v) {

    const n =
        Number(v);


    return `₹${(
        Number.isFinite(n)
            ? n
            : 0
    ).toFixed(2)}`;
}


function formatNumber(v) {

    const n =
        Number(v);


    return Number.isFinite(n)
        ? n.toFixed(2)
        : '-';
}


// =========================
// DATE FORMATTERS
// =========================

function formatDate(v) {

    if (!v) {
        return '-';
    }


    if (Array.isArray(v)) {

        if (
            v.length >= 3 &&
            v[0] &&
            v[1] &&
            v[2]
        ) {

            return (
                `${pad(v[2])}/` +
                `${pad(v[1])}/` +
                `${v[0]}`
            );
        }


        return '-';
    }


    const value =
        String(v);


    const match =
        value.match(
            /^(\d{4})-(\d{2})-(\d{2})/
        );


    if (match) {

        return (
            `${match[3]}/` +
            `${match[2]}/` +
            `${match[1]}`
        );
    }


    const d =
        new Date(value);


    return Number.isNaN(
        d.getTime()
    )
        ? value
        : d.toLocaleDateString();
}


// =========================
// DATE INPUT
// =========================

function toDateInput(v) {

    if (!v) {
        return '';
    }


    if (Array.isArray(v)) {

        if (v.length < 3) {
            return '';
        }


        return (
            `${v[0]}-` +
            `${pad(v[1])}-` +
            `${pad(v[2])}`
        );
    }


    const value =
        String(v);


    const match =
        value.match(
            /^(\d{4})-(\d{2})-(\d{2})/
        );


    if (match) {

        return (
            `${match[1]}-` +
            `${match[2]}-` +
            `${match[3]}`
        );
    }


    return value.slice(
        0,
        10
    );
}


// =========================
// OPTIONAL DATETIME FORMAT
// =========================

function formatDateTime(v) {

    if (!v) {
        return '-';
    }


    if (Array.isArray(v)) {

        return (
            `${pad(v[2])}/` +
            `${pad(v[1])}/` +
            `${v[0]} ` +
            `${pad(v[3] || 0)}:` +
            `${pad(v[4] || 0)}`
        );
    }


    const d =
        new Date(v);


    return Number.isNaN(
        d.getTime()
    )
        ? String(v)
        : d.toLocaleString();
}


function pad(v) {

    return String(v)
        .padStart(
            2,
            '0'
        );
}


// =========================
// STATUS
// =========================

function getStatusClass(
    status
) {

    switch (
        String(
            status || ''
        ).toUpperCase()
    ) {

        case 'CREATED':
            return 'status-created';

        case 'SHIPPED':
            return 'status-shipped';

        case 'DELIVERED':
            return 'status-delivered';

        case 'IN_TRANSIT':
            return 'status-in-transit';

        default:
            return 'status-default';
    }
}


// =========================
// HTML ESCAPE
// =========================

function escapeHtml(v) {

    return String(
        v ?? ''
    )
        .replace(
            /&/g,
            '&amp;'
        )
        .replace(
            /</g,
            '&lt;'
        )
        .replace(
            />/g,
            '&gt;'
        )
        .replace(
            /"/g,
            '&quot;'
        )
        .replace(
            /'/g,
            '&#039;'
        );
}


// =========================
// NAVIGATION
// =========================

function goBack() {

    location.href =
        '/erpflow/shipments.jsp';
}


// =========================
// EDITABILITY
// =========================

function applyShipmentEditability(
    shipment
) {

    const isDelivered =
        String(
            shipment.status || ""
        ).toUpperCase() ===
        "DELIVERED";


    const editShipmentButton =
        document.getElementById(
            "editShipmentButton"
        );


    const editPackagesButton =
        document.getElementById(
            "editPackagesButton"
        );


    const markDeliveredButton =
        document.getElementById(
            "markDeliveredButton"
        );


    const editShipmentSection =
        document.getElementById(
            "editShipmentSection"
        );


    const editPackagesSection =
        document.getElementById(
            "editPackagesSection"
        );


    if (editShipmentButton) {

        editShipmentButton.disabled =
            isDelivered;

        editShipmentButton.hidden =
            isDelivered;
    }


    if (editPackagesButton) {

        editPackagesButton.disabled =
            isDelivered;

        editPackagesButton.hidden =
            isDelivered;
    }


    if (markDeliveredButton) {

        markDeliveredButton.disabled =
            isDelivered;

        markDeliveredButton.hidden =
            isDelivered;
    }


    if (isDelivered) {

        if (editShipmentSection) {

            editShipmentSection.hidden =
                true;
        }


        if (editPackagesSection) {

            editPackagesSection.hidden =
                true;
        }
    }
}