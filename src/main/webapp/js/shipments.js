const apiUrl = "/erpflow/api/shipments";

document.addEventListener("DOMContentLoaded", ()=>{


    loadShipments();

document.getElementById("statusFilter")
    ?.addEventListener("change", loadShipments);
    
    
});


async function loadShipments() {

    const tableBody =
        document.getElementById("shipmentsTableBody");

const selectedStatus =
    document.getElementById("statusFilter")?.value || "ALL";



    try {
        const url = selectedStatus === "ALL"
    ? apiUrl
    : `${apiUrl}?status=${encodeURIComponent(selectedStatus)}`;


        const response =
            await fetch(url);

        if (!response.ok) {
            throw new Error("Failed to load shipments");
        }

        const shipments =
            await response.json();

        tableBody.innerHTML = "";


        if (!shipments || shipments.length === 0) {

            tableBody.innerHTML = `
                <tr>
                    <td colspan="9">
                        No shipments found.
                    </td>
                </tr>
            `;

            return;
        }


        shipments.forEach(shipment => {

            const row =
                document.createElement("tr");


            // -------------------------
            // PACKAGES
            // -------------------------

            const packages =
                shipment.packages || [];


            const packageNumbers =
                packages.length > 0
                    ? packages
                        .map(pkg => pkg.packageNumber || `#${pkg.id}`)
                        .join(", ")
                    : "-";


            // -------------------------
            // SALES ORDERS
            // -------------------------

            const salesOrderIds =
                packages
                    .filter(pkg => pkg.salesOrder)
                    .map(pkg => `#${pkg.salesOrder.id}`);


            const uniqueSalesOrders =
                [...new Set(salesOrderIds)];


            const salesOrders =
                uniqueSalesOrders.length > 0
                    ? uniqueSalesOrders.join(", ")
                    : "-";


            // -------------------------
            // CARRIER
            // -------------------------

            const carrier =
                shipment.carrier
                    ? shipment.carrier.name
                    : "-";


            // -------------------------
            // SERVICE
            // -------------------------

            const service =
                shipment.carrierService
                    ? shipment.carrierService.name
                    : "-";


            // -------------------------
            // SHIPPING CHARGE
            // -------------------------

            const shippingCharge =
                shipment.shippingCharge != null
                    ? `₹${Number(shipment.shippingCharge).toFixed(2)}`
                    : "-";


            row.innerHTML = `

                <td>
                    <strong>
                        ${shipment.shipmentNumber || "#" + shipment.id}
                    </strong>
                </td>

                <td>
                    ${packageNumbers}
                </td>

                <td>
                    ${salesOrders}
                </td>

                <td>
                    ${carrier}
                </td>

                <td>
                    ${service}
                </td>

                <td>
                    ${shippingCharge}
                </td>

                <td>
                    ${formatDate(shipment.shipmentDate)}
                </td>

                <td>
                    <span class="status">
                        ${shipment.status || "-"}
                    </span>
                </td>

                <td>

                    <a
                        class="action-button"
                        href="/erpflow/shipmentDetails.jsp?id=${shipment.id}"
                    >
                        View
                    </a>
                    

                </td>

            `;

            tableBody.appendChild(row);

        });


    } catch (error) {

        console.error(error);

        document.getElementById("messageBox").innerHTML = `
            <p>Failed to load shipments.</p>
        `;
    }
}


function formatDate(dateValue) {

    if (!dateValue) {
        return "-";
    }

    // Jackson LocalDate array: [year, month, day]
    if (Array.isArray(dateValue)) {

        const [year, month, day] = dateValue;

        if (!year || !month || !day) {
            return "-";
        }

        return `${String(day).padStart(2, "0")}/${
            String(month).padStart(2, "0")
        }/${year}`;
    }

    // LocalDate string: "2026-09-25"
    if (typeof dateValue === "string") {

        const match = dateValue.match(
            /^(\d{4})-(\d{2})-(\d{2})$/
        );

        if (match) {

            const [, year, month, day] = match;

            return `${day}/${month}/${year}`;
        }

        // If backend somehow sends an ISO datetime,
        // take only the date portion.
        if (dateValue.includes("T")) {

            const datePart = dateValue.split("T")[0];

            const [year, month, day] =
                datePart.split("-");

            if (year && month && day) {
                return `${day}/${month}/${year}`;
            }
        }
    }

    return "-";
}