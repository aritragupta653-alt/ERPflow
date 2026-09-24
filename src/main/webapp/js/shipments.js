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


    // Jackson LocalDateTime array
    if (Array.isArray(dateValue)) {

        const [
            year,
            month,
            day,
            hour = 0,
            minute = 0,
            second = 0
        ] = dateValue;


        const date =
            new Date(
                year,
                month - 1,
                day,
                hour,
                minute,
                second
            );


        if (isNaN(date.getTime())) {
            return "-";
        }


        return date.toLocaleString();
    }


    const date =
        new Date(dateValue);


    if (isNaN(date.getTime())) {
        return "-";
    }


    return date.toLocaleString();
}