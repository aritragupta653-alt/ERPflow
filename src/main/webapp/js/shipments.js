const apiUrl = "/erpflow/api/shipments";

document.addEventListener("DOMContentLoaded", loadShipments);

async function loadShipments() {

    const tableBody = document.getElementById("shipmentsTableBody");

    try {
        const response = await fetch(apiUrl);

        if (!response.ok) {
            throw new Error("Failed to load shipments");
        }

        const shipments = await response.json();

        tableBody.innerHTML = "";

        if (shipments.length === 0) {
            tableBody.innerHTML =
                `<tr>
                    <td colspan="6">No shipments found.</td>
                 </tr>`;
            return;
        }

        shipments.forEach(shipment => {

            const row = document.createElement("tr");

            row.innerHTML = `
                <td>#${shipment.id}</td>
                <td>#${shipment.packageId ?? "-"}</td>
                <td>#${shipment.salesOrderId ?? "-"}</td>
                <td>${formatDate(shipment.shipmentDate)}</td>
                <td>
                    <span class="status">${shipment.status}</span>
                </td>
                <td>
                  <a class="action-button"
   href="/erpflow/shipmentDetails.jsp?id=${shipment.id}">
    View
</a>
                </td>
            `;

            tableBody.appendChild(row);
        });

    } catch (error) {

        console.error(error);

        document.getElementById("messageBox").innerHTML =
            `<p>Failed to load shipments.</p>`;
    }
}

function formatDate(dateString) {

    if (!dateString) {
        return "-";
    }

    return new Date(dateString).toLocaleString();
}