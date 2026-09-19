const packagesApiUrl = "/erpflow/api/packages";
const salesOrdersApiUrl = "/erpflow/api/sales-orders";

let salesOrders = [];
let selectedOrderItems = [];

// ===============================
// PAGE LOAD
// ===============================

document.addEventListener("DOMContentLoaded", () => {
    loadPackages();
    loadSalesOrders();
    const form = document.getElementById("createPackageForm");

    if (form) {
        form.addEventListener("submit", createPackage);
    }

    document
        .getElementById("createPackageForm")
        .addEventListener("submit", createPackage);
});


// ===============================
// LOAD PACKAGES
// ===============================

async function loadPackages() {

    const tableBody = document.getElementById("packagesTableBody");

    try {

        const response = await fetch(packagesApiUrl);

        if (!response.ok) {
            throw new Error("Failed to load packages");
        }

        const packages = await response.json();

        if (!packages || packages.length === 0) {

            tableBody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align:center;">
                        No packages found
                    </td>
                </tr>
            `;

            return;
        }

        tableBody.innerHTML = packages.map(pkg => {

            const salesOrder = pkg.salesOrder || {};
            const customer = salesOrder.customer || {};

            return `
                <tr>

                    <td>
                        <strong>${escapeHtml(pkg.packageNumber || "PKG-" + pkg.id)}</strong>
                    </td>

                    <td>
                        <a href="/erpflow/salesOrderDetails.jsp?id=${pkg.salesOrder?.id || ""}">
                            SO #${pkg.salesOrder?.id || "-"}
                        </a>
                    </td>

                    <td>
                        ${escapeHtml(customer.name || "-")}
                    </td>

                    <td>
                        ${formatNumber(pkg.weight)} kg
                    </td>

                    <td>
                        ${formatNumber(pkg.length)}
                        ×
                        ${formatNumber(pkg.width)}
                        ×
                        ${formatNumber(pkg.height)} cm
                    </td>

                    <td>
                        <span class="status-badge ${getStatusClass(pkg.status)}">
                            ${escapeHtml(pkg.status || "-")}
                        </span>
                    </td>

                    <td>

                        <button
                            class="btn btn-secondary"
                            onclick="viewPackage(${pkg.id})">
                            View
                        </button>

                    </td>

                </tr>
            `;

        }).join("");

    } catch (error) {

        console.error(error);

        tableBody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align:center;">
                    Failed to load packages
                </td>
            </tr>
        `;
    }
}


// ===============================
// LOAD SALES ORDERS
// ===============================

async function loadSalesOrders() {

    const select = document.getElementById("salesOrderId");

    try {

        const response = await fetch(salesOrdersApiUrl);

        if (!response.ok) {
            throw new Error("Failed to load sales orders");
        }

        salesOrders = await response.json();

        select.innerHTML = `
            <option value="">
                Select Sales Order
            </option>
        `;

        salesOrders
            .filter(order => {

                const status = (order.status || "").toUpperCase();

                return status !== "SHIPPED" &&
                       status !== "DELIVERED" &&
                       status !== "CANCELLED";

            })
            .forEach(order => {

                const customerName =
                    order.customer?.name || "Unknown Customer";

                select.innerHTML += `
                    <option value="${order.id}">
                        SO #${order.id} - ${escapeHtml(customerName)}
                    </option>
                `;

            });

    } catch (error) {

        console.error(error);

        select.innerHTML = `
            <option value="">
                Failed to load sales orders
            </option>
        `;
    }
}


// ===============================
// LOAD SALES ORDER ITEMS
// ===============================

async function loadSalesOrderItems() {

    const salesOrderId =
        document.getElementById("salesOrderId").value;

    const container =
        document.getElementById("orderItemsContainer");

    selectedOrderItems = [];

    if (!salesOrderId) {

        container.innerHTML = `
            <p class="muted">
                Select a sales order first.
            </p>
        `;

        return;
    }

    try {

        const response =
            await fetch(`${salesOrdersApiUrl}/${salesOrderId}`);

        if (!response.ok) {
            throw new Error("Failed to load sales order");
        }

        const order = await response.json();

        selectedOrderItems = order.items || [];

        if (selectedOrderItems.length === 0) {

            container.innerHTML = `
                <p class="muted">
                    This sales order has no items.
                </p>
            `;

            return;
        }

        container.innerHTML = selectedOrderItems.map((orderItem, index) => {

            const item = orderItem.item || {};

            return `
                <div class="package-item-row"
                     style="
                        display:grid;
                        grid-template-columns:2fr 1fr 1fr;
                        gap:12px;
                        align-items:center;
                        margin-bottom:10px;
                     ">

                    <div>

                        <strong>
                            ${escapeHtml(item.name || "Unknown Item")}
                        </strong>

                        <div class="muted">
                            SKU: ${escapeHtml(item.sku || "-")}
                        </div>

                    </div>

                    <div>
                        Ordered:
                        <strong>
                            ${orderItem.quantity}
                        </strong>
                    </div>

                    <div>

                        <label
                            for="quantity_${index}">
                            Package Qty
                        </label>

                        <input
                            type="number"
                            id="quantity_${index}"
                            class="package-quantity"
                            data-item-id="${item.id}"
                            data-max-quantity="${orderItem.quantity}"
                            min="0"
                            max="${orderItem.quantity}"
                            value="0"
                            step="1"
                            style="width:100%;"
                        >

                    </div>

                </div>
            `;

        }).join("");

    } catch (error) {

        console.error(error);

        container.innerHTML = `
            <p class="error-message">
                Failed to load sales order items.
            </p>
        `;
    }
}


// ===============================
// CREATE PACKAGE
// ===============================

async function createPackage(event) {

    event.preventDefault();

    hideError();

    const salesOrderId =
        Number(document.getElementById("salesOrderId").value);

    const weight =
        Number(document.getElementById("weight").value);

    const length =
        Number(document.getElementById("length").value);

    const width =
        Number(document.getElementById("width").value);

    const height =
        Number(document.getElementById("height").value);


    // -------------------------------
    // Basic validation
    // -------------------------------

    if (!salesOrderId) {
        showError("Please select a sales order.");
        return;
    }

    if (!weight || weight <= 0) {
        showError("Weight must be greater than 0.");
        return;
    }

    if (!length || length <= 0 ||
        !width || width <= 0 ||
        !height || height <= 0) {

        showError("All dimensions must be greater than 0.");
        return;
    }


    // -------------------------------
    // Collect item quantities
    // -------------------------------

    const quantityInputs =
        document.querySelectorAll(".package-quantity");

    const items = [];

    quantityInputs.forEach(input => {

        const quantity = Number(input.value);

        const itemId =
            Number(input.dataset.itemId);

        const maxQuantity =
            Number(input.dataset.maxQuantity);

        if (quantity < 0) {
            throw new Error("Quantity cannot be negative.");
        }

        if (quantity > maxQuantity) {
            throw new Error(
                `Package quantity cannot exceed ordered quantity (${maxQuantity}).`
            );
        }

        if (quantity > 0) {

            items.push({
                itemId: itemId,
                quantity: quantity
            });

        }

    });


    if (items.length === 0) {

        showError(
            "Please enter a quantity for at least one item."
        );

        return;
    }


    // -------------------------------
    // Request body
    // -------------------------------

    const requestBody = {

        salesOrderId: salesOrderId,

        weight: weight,

        length: length,

        width: width,

        height: height,

        items: items

    };


    try {

        const response = await fetch(packagesApiUrl, {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(requestBody)

        });


        const result = await response.json();


        if (!response.ok) {

            throw new Error(
                result.message ||
                result.error ||
                "Failed to create package."
            );

        }


        alert(
            `Package ${result.packageNumber || ""} created successfully!`
        );


        closeCreatePackage();

        await loadPackages();


    } catch (error) {

        console.error(error);

        showError(error.message);

    }

}


// ===============================
// OPEN CREATE PACKAGE
// ===============================

function openCreatePackage() {

    document.getElementById("createPackageModal")
        .style.display = "flex";

    document.getElementById("createPackageForm")
        .reset();

    document.getElementById("orderItemsContainer")
        .innerHTML = `
            <p class="muted">
                Select a sales order first.
            </p>
        `;

    hideError();

}


// ===============================
// CLOSE CREATE PACKAGE
// ===============================

function closeCreatePackage() {

    document.getElementById("createPackageModal")
        .style.display = "none";

}


// ===============================
// VIEW PACKAGE
// ===============================

function viewPackage(id) {

    window.location.href =
        `/erpflow/packageDetails.jsp?id=${id}`;

}


// ===============================
// STATUS CLASS
// ===============================

function getStatusClass(status) {

    if (!status) {
        return "";
    }

    switch (status.toUpperCase()) {

        case "PACKED":
            return "status-success";

        case "SHIPPED":
            return "status-info";

        case "DELIVERED":
            return "status-success";

        case "PACKING":
            return "status-warning";

        default:
            return "";

    }

}


// ===============================
// NUMBER FORMAT
// ===============================

function formatNumber(value) {

    const number = Number(value);

    if (Number.isNaN(number)) {
        return "-";
    }

    return number.toFixed(2);

}


// ===============================
// ERROR HANDLING
// ===============================

function showError(message) {

    const errorElement =
        document.getElementById("createPackageError");

    errorElement.textContent = message;

    errorElement.style.display = "block";

}


function hideError() {

    const errorElement =
        document.getElementById("createPackageError");

    errorElement.textContent = "";

    errorElement.style.display = "none";

}


// ===============================
// HTML ESCAPING
// ===============================

function escapeHtml(value) {

    if (value === null || value === undefined) {
        return "";
    }

    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");

}