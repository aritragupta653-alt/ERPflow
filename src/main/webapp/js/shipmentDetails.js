document.addEventListener("DOMContentLoaded", loadShipment);

async function loadShipment() {

    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");

    if (!id) {
        showError("Shipment ID is missing.");
        return;
    }

    try {

        const response = await fetch(
            `/erpflow/api/shipments/${id}`
        );

        if (!response.ok) {
            throw new Error("Shipment not found");
        }

        const shipment = await response.json();

        document.getElementById("shipmentId").textContent =
            "#" + shipment.id;

        document.getElementById("packageId").textContent =
            shipment.packageId
                ? "#" + shipment.packageId
                : "-";

        document.getElementById("salesOrderId").textContent =
            shipment.salesOrderId
                ? "#" + shipment.salesOrderId
                : "-";

        document.getElementById("shipmentDate").textContent =
            formatDate(shipment.shipmentDate);

        document.getElementById("shipmentStatus").textContent =
            shipment.status || "-";

    } catch (error) {

        console.error(error);
        showError("Failed to load shipment details.");
    }
}

function formatDate(dateString) {

    if (!dateString) {
        return "-";
    }

    const date = new Date(dateString);

    if (isNaN(date.getTime())) {
        return "-";
    }

    return date.toLocaleString();
}

function showError(message) {

    document.getElementById("messageBox").innerHTML =
        `<p>${message}</p>`;
}