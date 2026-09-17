const apiBaseUrl = "/erpflow/api/sales-orders";

document.addEventListener("DOMContentLoaded", loadSalesOrder);

async function loadSalesOrder() {

    const params = new URLSearchParams(window.location.search);
    const orderId = params.get("id");

    if (!orderId) {
        showError("Sales Order ID is missing.");
        return;
    }

    try {

        const response = await fetch(`${apiBaseUrl}/${orderId}`);

        if (!response.ok) {
            throw new Error("Failed to load sales order");
        }

        const order = await response.json();

        console.log("Sales Order:", order);

        displaySalesOrder(order);

    } catch (error) {

        console.error(error);

        showError("Failed to load sales order details.");
    }
}


function displaySalesOrder(order) {

    document.getElementById("orderId").value =
        order.id ?? "-";

    document.getElementById("orderDate").value =
        formatDate(order.orderDate);

    document.getElementById("orderStatus").value =
        order.status ?? "-";


    // Customer
    if (order.customer) {

        document.getElementById("customerId").value =
            order.customer.id ?? "-";

        document.getElementById("customerName").value =
            order.customer.name ?? "-";

        document.getElementById("customerEmail").value =
            order.customer.email ?? "-";

        document.getElementById("customerPhone").value =
            order.customer.phone ?? "-";
    }


    // Items
    const tableBody =
        document.getElementById("orderItemsTableBody");

    tableBody.innerHTML = "";

    const items = order.items || order.salesOrderItems || [];

    if (items.length === 0) {

        tableBody.innerHTML = `
            <tr>
                <td colspan="6">No items found.</td>
            </tr>
        `;

        return;
    }


    items.forEach(orderItem => {

        const item = orderItem.item || {};

        const quantity = orderItem.quantity ?? 0;
        const price = orderItem.sellingPrice ?? 0;

        const total = quantity * price;

        const row = document.createElement("tr");

        row.innerHTML = `
            <td>#${item.id ?? orderItem.itemId ?? "-"}</td>

            <td>${item.name ?? "-"}</td>

            <td>${item.sku ?? "-"}</td>

            <td>${quantity}</td>

            <td>₹${Number(price).toFixed(2)}</td>

            <td>₹${total.toFixed(2)}</td>
        `;

        tableBody.appendChild(row);
    });
}


function formatDate(dateValue) {

    if (!dateValue) return "-";

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

        const date = new Date(
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


    // ISO date string
    const date = new Date(dateValue);

    if (isNaN(date.getTime())) {
        return "-";
    }

    return date.toLocaleString();
}


function showError(message) {

    document.getElementById("messageBox").innerHTML = `
        <p>${message}</p>
    `;
}