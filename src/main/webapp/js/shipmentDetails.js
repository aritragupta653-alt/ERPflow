document.addEventListener("DOMContentLoaded", () => {

    const params = new URLSearchParams(window.location.search);
    const shipmentId = params.get("id");

    if (!shipmentId) {
        alert("Shipment ID is missing.");
        return;
    }

    loadShipment(shipmentId);
});


async function loadShipment(shipmentId) {

    try {

        const response = await fetch(
            `/erpflow/api/shipments/${shipmentId}`
        );

        if (!response.ok) {
            throw new Error(
                `Failed to load shipment: ${response.status}`
            );
        }

        const shipment = await response.json();

        displayShipment(shipment);

    } catch (error) {

        console.error(error);

        alert("Unable to load shipment details.");
    }
}


function displayShipment(shipment) {

    // --------------------------------
    // Shipment header
    // --------------------------------

    setText(
        "shipmentNumber",
        shipment.shipmentNumber || `Shipment #${shipment.id}`
    );

    setText(
        "shipmentDate",
        formatDateTime(shipment.shipmentDate)
    );

    setText(
        "shippingCharge",
        formatMoney(shipment.shippingCharge)
    );

    setText(
        "estimatedDeliveryDate",
        formatDate(shipment.estimatedDeliveryDate)
    );


    // --------------------------------
    // Status
    // --------------------------------

    const statusElement =
        document.getElementById("shipmentStatus");

    if (statusElement) {

        const status =
            shipment.status || "UNKNOWN";

        statusElement.textContent = status;

        statusElement.className =
            "status-badge " +
            getStatusClass(status);
    }


    // --------------------------------
    // Sales Order + Customer
    // --------------------------------

    const pkg =
        shipment.packages &&
        shipment.packages.length > 0
            ? shipment.packages[0]
            : null;

    const salesOrder =
        pkg && pkg.salesOrder
            ? pkg.salesOrder
            : null;


    if (salesOrder) {

        const salesOrderId =
            salesOrder.id;

        const salesOrderLink =
            document.getElementById("salesOrderLink");

        if (salesOrderLink) {

            salesOrderLink.textContent =
                `#${salesOrderId}`;

            salesOrderLink.href =
                `/erpflow/salesOrderDetails.jsp?id=${encodeURIComponent(salesOrderId)}`;
        }


        if (salesOrder.customer) {

            setText(
                "customerName",
                salesOrder.customer.name || "-"
            );
        }

    } else {

        setText("customerName", "-");
    }


    // --------------------------------
    // Carrier
    // --------------------------------

    if (shipment.carrier) {

        setText(
            "carrierName",
            shipment.carrier.name || "-"
        );

    } else {

        setText("carrierName", "-");
    }


    // --------------------------------
    // Carrier Service
    // --------------------------------

    if (shipment.carrierService) {

        setText(
            "carrierService",
            shipment.carrierService.name || "-"
        );

    } else {

        setText("carrierService", "-");
    }


    // --------------------------------
    // Tracking
    // --------------------------------

    setText(
        "trackingNumber",
        shipment.trackingNumber || "Not available"
    );


    const trackingLink =
        document.getElementById("trackingLink");

    if (
        trackingLink &&
        shipment.trackingUrl
    ) {

        trackingLink.href =
            shipment.trackingUrl;

        trackingLink.style.display =
            "inline-block";

    } else if (trackingLink) {

        trackingLink.style.display =
            "none";
    }


    // --------------------------------
    // Addresses
    // --------------------------------

    setText(
        "dispatchAddress",
        shipment.dispatchAddress || "-"
    );

    setText(
        "destinationAddress",
        shipment.destinationAddress || "-"
    );


    // --------------------------------
    // Packages
    // --------------------------------

    displayPackages(
        shipment.packages || []
    );
}


// ========================================
// Packages
// ========================================

function displayPackages(packages) {

    const tbody =
        document.getElementById(
            "packagesTableBody"
        );

    if (!tbody) {
        return;
    }

    tbody.innerHTML = "";


    if (packages.length === 0) {

        tbody.innerHTML = `
            <tr>
                <td colspan="4" class="empty-message">
                    No packages found.
                </td>
            </tr>
        `;

        return;
    }


    packages.forEach(pkg => {

        const row =
            document.createElement("tr");


        const dimensions =
            `${formatNumber(pkg.length)} × ` +
            `${formatNumber(pkg.width)} × ` +
            `${formatNumber(pkg.height)} cm`;


        row.innerHTML = `

            <td class="package-number">
                ${escapeHtml(
                    pkg.packageNumber || "-"
                )}
            </td>

            <td>
                ${formatNumber(pkg.weight)} kg
            </td>

            <td>
                ${dimensions}
            </td>

            <td>
                <span class="status-badge ${getStatusClass(pkg.status)}">
                    ${escapeHtml(pkg.status || "-")}
                </span>
            </td>
        `;


        tbody.appendChild(row);
    });
}


// ========================================
// Helpers
// ========================================

function setText(id, value) {

    const element =
        document.getElementById(id);

    if (!element) {
        return;
    }

    element.textContent =
        value === null ||
        value === undefined ||
        value === ""
            ? "-"
            : value;
}


function formatMoney(value) {

    const number =
        Number(value);

    if (isNaN(number)) {
        return "₹0.00";
    }

    return `₹${number.toFixed(2)}`;
}


function formatNumber(value) {

    const number =
        Number(value);

    if (isNaN(number)) {
        return "-";
    }

    return number.toFixed(2);
}


function formatDateTime(value) {

    if (!value) {
        return "-";
    }


    // Jackson LocalDateTime array
    if (Array.isArray(value)) {

        const year = value[0];
        const month = value[1];
        const day = value[2];

        const hour = value[3] || 0;
        const minute = value[4] || 0;

        return (
            `${pad(day)}/${pad(month)}/${year} ` +
            `${pad(hour)}:${pad(minute)}`
        );
    }


    const date =
        new Date(value);

    if (isNaN(date.getTime())) {
        return "-";
    }

    return date.toLocaleString();
}


function formatDate(value) {

    if (!value) {
        return "-";
    }


    // Jackson LocalDate array
    if (Array.isArray(value)) {

        const year = value[0];
        const month = value[1];
        const day = value[2];

        return (
            `${pad(day)}/` +
            `${pad(month)}/` +
            `${year}`
        );
    }


    const date =
        new Date(value);

    if (isNaN(date.getTime())) {
        return "-";
    }

    return date.toLocaleDateString();
}


function pad(value) {

    return String(value)
        .padStart(2, "0");
}


function getStatusClass(status) {

    if (!status) {
        return "status-default";
    }

    switch (status.toUpperCase()) {

        case "CREATED":
            return "status-created";

        case "SHIPPED":
            return "status-shipped";

        case "DELIVERED":
            return "status-delivered";

        default:
            return "status-default";
    }
}


function escapeHtml(value) {

    if (
        value === null ||
        value === undefined
    ) {
        return "";
    }

    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}


function goBack() {

    window.location.href =
        "/erpflow/shipments.jsp";
}