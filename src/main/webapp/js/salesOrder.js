document.addEventListener("DOMContentLoaded", () => {
    loadCustomers();
    loadItems();
    loadSalesOrders();

    document.getElementById("addItemButton")
        .addEventListener("click", addItemRow);

    document.getElementById("salesOrderForm")
        .addEventListener("submit", createSalesOrder);

    document.getElementById("searchInput")
        .addEventListener("input", searchOrders);
});


async function loadSalesOrders() {

    try {

        const response =
            await fetch("/erpflow/api/sales-orders");

        if (!response.ok) {
            throw new Error("Failed to load sales orders");
        }

        const orders = await response.json();

        displaySalesOrders(orders);

    } catch (error) {

        console.error(error);

        showMessage(
            "Failed to load sales orders.",
            "error"
        );
    }
}


function displaySalesOrders(orders) {

    const tableBody =
        document.getElementById("salesOrdersTableBody");

    tableBody.innerHTML = "";

    document.getElementById("salesOrderCount")
        .textContent = orders.length;

    orders.forEach(order => {

        const row =
            document.createElement("tr");

        row.innerHTML = `
            <td>#${order.id}</td>

            <td>
                ${order.customer?.name || "-"}
            </td>

            <td>
                ${formatDate(order.orderDate)}
            </td>

            <td>
                <span class="status">
                    ${order.status}
                </span>
            </td>

            <td>
                <a class="action-button"
                   href="/erpflow/salesOrderDetails.jsp?id=${order.id}">
                    View
                </a>
            </td>
        `;

        tableBody.appendChild(row);
    });
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


async function loadCustomers() {

    const select =
        document.getElementById("customerSelect");

    try {

        const response =
            await fetch("/erpflow/api/customers");

        const customers = await response.json();

        select.innerHTML =
            `<option value="">Select Customer</option>`;

        customers.forEach(customer => {

            select.innerHTML += `
                <option value="${customer.id}">
                    ${customer.name}
                </option>
            `;
        });

    } catch (error) {

        console.error(error);

        select.innerHTML =
            `<option value="">Failed to load customers</option>`;
    }
}


async function loadItems() {

    const response =
        await fetch("/erpflow/api/items");

    const items =
        await response.json();

    document.querySelectorAll(".item-select")
        .forEach(select => populateItemSelect(select, items));
}


function populateItemSelect(select, items) {

    select.innerHTML =
        `<option value="">Select Item</option>`;

    items.forEach(item => {

        select.innerHTML += `
            <option value="${item.id}">
                ${item.name}
            </option>
        `;
    });
}


function addItemRow() {

    const container =
        document.getElementById("itemContainer");

    const row =
        document.createElement("div");

    row.className = "sales-item-row";

    row.innerHTML = `
        <select class="item-select" required>
            <option value="">Loading items...</option>
        </select>

        <input type="number"
               class="quantity-input"
               placeholder="Quantity"
               min="1"
               required>

        <input type="number"
               class="price-input"
               placeholder="Selling Price"
               min="0"
               step="0.01"
               required>

        <button type="button"
                class="btn small-btn danger-btn remove-item-btn">
            Remove
        </button>
    `;

    container.appendChild(row);

    loadItems();
}


document.addEventListener("click", event => {

    if (event.target.classList.contains("remove-item-btn")) {

        const rows =
            document.querySelectorAll(".sales-item-row");

        if (rows.length > 1) {
            event.target.parentElement.remove();
        }
    }
});


async function createSalesOrder(event) {

    event.preventDefault();

    const customerId =
        document.getElementById("customerSelect").value;

    const rows =
        document.querySelectorAll(".sales-item-row");

    const items = [];

    rows.forEach(row => {

        const itemId =
            row.querySelector(".item-select").value;

        const quantity =
            row.querySelector(".quantity-input").value;

        const sellingPrice =
            row.querySelector(".price-input").value;

        if (itemId && quantity && sellingPrice) {

            items.push({
                itemId: parseInt(itemId),
                quantity: parseInt(quantity),
                sellingPrice: parseFloat(sellingPrice)
            });
        }
    });

    try {

        const response =
            await fetch("/erpflow/api/sales-orders", {

                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({
                    customerId: parseInt(customerId),
                    items: items
                })
            });

        const result =
            await response.json();

        if (!response.ok) {
            throw new Error(
                result.error || "Failed to create sales order"
            );
        }

        showMessage(
            "Sales order created successfully!",
            "success"
        );

        document.getElementById("salesOrderForm").reset();

        loadSalesOrders();

    } catch (error) {

        console.error(error);

        showMessage(
            error.message,
            "error"
        );
    }
}


function searchOrders() {

    const search =
        document.getElementById("searchInput")
            .value
            .toLowerCase();

    const rows =
        document.querySelectorAll(
            "#salesOrdersTableBody tr"
        );

    rows.forEach(row => {

        row.style.display =
            row.textContent
                .toLowerCase()
                .includes(search)
                ? ""
                : "none";
    });
}


function showMessage(message, type) {

    const box =
        document.getElementById("messageBox");

    box.style.display = "block";

    box.textContent = message;
}