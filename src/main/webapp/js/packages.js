
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

    const salesOrderSelect = document.getElementById("salesOrderId");

    if (salesOrderSelect) {
        salesOrderSelect.addEventListener("change", loadSalesOrderItems);
    }
});


// ===============================
// LOAD PACKAGES
// ===============================

async function loadPackages() {
    const tableBody = document.getElementById("packagesTableBody");

    if (!tableBody) return;

    try {
        const response = await fetch(packagesApiUrl);

        if (!response.ok) {
            throw new Error("Failed to load packages.");
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
                        <strong>
                            ${escapeHtml(pkg.packageNumber || "PKG-" + pkg.id)}
                        </strong>
                    </td>

                    <td>
                        <a href="/erpflow/salesOrderDetails.jsp?id=${encodeURIComponent(salesOrder.id || "")}">
                            SO #${escapeHtml(salesOrder.id || "-")}
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
                            type="button"
                            class="btn btn-secondary"
                            onclick="viewPackage(${Number(pkg.id)})">
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

    if (!select) return;

    try {
        const response = await fetch(salesOrdersApiUrl);

        if (!response.ok) {
            throw new Error("Failed to load sales orders.");
        }

        salesOrders = await response.json();

        select.innerHTML = `
            <option value="">Select Sales Order</option>
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

                const option = document.createElement("option");
                option.value = order.id;
                option.textContent =
                    `SO #${order.id} - ${customerName}`;

                select.appendChild(option);
            });

    } catch (error) {
        console.error(error);

        select.innerHTML = `
            <option value="">Failed to load sales orders</option>
        `;
    }
}


// ===============================
// LOAD SALES ORDER ITEMS
// Each sales order line is unique.
// Match package quantities by line ID.
// ===============================

async function loadSalesOrderItems() {
    const salesOrderId =
        document.getElementById("salesOrderId")?.value;

    const container =
        document.getElementById("orderItemsContainer");

    if (!container) return;

    selectedOrderItems = [];

    if (!salesOrderId) {
        container.innerHTML = `
            <p class="muted">Select a sales order first.</p>
        `;
        return;
    }

    container.innerHTML = `
        <p class="muted">Loading sales order items...</p>
    `;

    try {
        const [orderResponse, packagesResponse] = await Promise.all([
            fetch(`${salesOrdersApiUrl}/${encodeURIComponent(salesOrderId)}`),
            fetch(packagesApiUrl)
        ]);

        if (!orderResponse.ok) {
            throw new Error("Failed to load sales order.");
        }

        if (!packagesResponse.ok) {
            throw new Error("Failed to load existing packages.");
        }

        const order = await orderResponse.json();
        const packages = await packagesResponse.json();

        const orderItems = order.items || [];

        if (orderItems.length === 0) {
            container.innerHTML = `
                <p class="muted">This sales order has no items.</p>
            `;
            return;
        }

        // Preserve each order line separately.
        selectedOrderItems = orderItems.map((orderItem, index) => {
            const item = orderItem.item || {};

            return {
                lineId: orderItem.id ?? null,
                lineIndex: index,
                itemId: Number(item.id ?? orderItem.itemId),
                name: item.name || "Unknown Item",
                sku: item.sku || "-",
                ordered: Number(orderItem.quantity || 0),
                packed: 0,
                remaining: Number(orderItem.quantity || 0)
            };
        });

        // Select only packages belonging to this sales order.
        // Exclude cancelled packages from packed totals.
        const orderPackages = packages.filter(pkg => {
            const packageOrderId =
                Number(pkg.salesOrder?.id ?? pkg.salesOrderId);

            const status = (pkg.status || "").toUpperCase();

            return packageOrderId === Number(salesOrderId) &&
                   status !== "CANCELLED";
        });

        // Match quantities using Sales Order LINE ID.
        // Do not aggregate by product/item ID.
        orderPackages.forEach(pkg => {
            (pkg.items || []).forEach(packageItem => {
                const rawLineId =
                    packageItem.salesOrderItemId ??
                    packageItem.salesOrderItem?.id;

                if (rawLineId === null || rawLineId === undefined) {
                    return;
                }

                const packageLineId = Number(rawLineId);

                if (!Number.isFinite(packageLineId)) {
                    return;
                }

                const matchingLine = selectedOrderItems.find(line =>
                    line.lineId !== null &&
                    Number(line.lineId) === packageLineId
                );

                if (matchingLine) {
                    matchingLine.packed +=
                        Number(packageItem.quantity || 0);
                }
            });
        });

        // Calculate remaining quantity for every order line.
        selectedOrderItems.forEach(line => {
            line.remaining = Math.max(
                0,
                line.ordered - line.packed
            );
        });

        // Render each sales order line independently.
        container.innerHTML = selectedOrderItems.map(line => {
            const displayLineId =
                line.lineId ?? `Line ${line.lineIndex + 1}`;

            return `
                <div
                    class="package-item-row"
                    style="
                        display:grid;
                        grid-template-columns:2fr 1.5fr 1fr;
                        gap:12px;
                        align-items:center;
                        margin-bottom:12px;
                        padding:12px;
                        border:1px solid #ddd;
                        border-radius:8px;
                    ">

                    <div>
                        <strong>
                            ${escapeHtml(line.name)}
                        </strong>

                        <div class="muted">
                            SKU: ${escapeHtml(line.sku)}
                        </div>

                        <div class="muted">
                            Sales Order Line ID: ${escapeHtml(displayLineId)}
                        </div>
                    </div>

                    <div>
                        <div>
                            Ordered:
                            <strong>${line.ordered}</strong>
                        </div>

                        <div>
                            Packed:
                            <strong>${line.packed}</strong>
                        </div>

                        <div>
                            To Be Packed:
                            <strong>${line.remaining}</strong>
                        </div>
                    </div>

                    <div>
                        <label for="quantity_${line.lineIndex}">
                            Package Qty
                        </label>

                        <input
                            type="number"
                            id="quantity_${line.lineIndex}"
                            class="package-quantity"
                            data-line-id="${line.lineId ?? ""}"
                            data-item-id="${line.itemId}"
                            data-max-quantity="${line.remaining}"
                            min="0"
                            max="${line.remaining}"
                            step="1"
                            value="0"
                            style="width:100%;"
                            ${line.remaining === 0 ? "disabled" : ""}
                        >
                    </div>
                </div>
            `;
        }).join("");

    } catch (error) {
        console.error(error);

        container.innerHTML = `
            <p class="error-message">
                ${escapeHtml(error.message || "Failed to load sales order items.")}
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
        Number(document.getElementById("salesOrderId")?.value);

    const weight =
        Number(document.getElementById("weight")?.value);

    const length =
        Number(document.getElementById("length")?.value);

    const width =
        Number(document.getElementById("width")?.value);

    const height =
        Number(document.getElementById("height")?.value);


    // -------------------------------
    // Basic validation
    // -------------------------------

    if (!salesOrderId) {
        showError("Please select a sales order.");
        return;
    }

    if (!Number.isFinite(weight) || weight <= 0) {
        showError("Weight must be greater than 0.");
        return;
    }

    if (
        !Number.isFinite(length) || length <= 0 ||
        !Number.isFinite(width) || width <= 0 ||
        !Number.isFinite(height) || height <= 0
    ) {
        showError("All dimensions must be greater than 0.");
        return;
    }


    // -------------------------------
    // Collect package item quantities
    // Keep every order line distinct.
    // -------------------------------

    const quantityInputs =
        document.querySelectorAll(".package-quantity");

    const items = [];

    for (const input of quantityInputs) {
        const quantity = Number(input.value);
        const itemId = Number(input.dataset.itemId);
        const lineId = input.dataset.lineId;
        const maxQuantity = Number(input.dataset.maxQuantity);

        if (!Number.isInteger(quantity) || quantity < 0) {
            showError("Quantity must be a non-negative whole number.");
            return;
        }

        if (quantity > maxQuantity) {
            showError(
                `Quantity cannot exceed the remaining quantity (${maxQuantity}).`
            );
            return;
        }

        if (quantity > 0) {
            if (!lineId) {
                showError(
                    "Sales order line ID is missing. The package cannot be created safely."
                );
                return;
            }

            if (!Number.isFinite(itemId)) {
                showError("Invalid item ID.");
                return;
            }

            items.push({
                itemId: itemId,
                salesOrderItemId: Number(lineId),
                quantity: quantity
            });
        }
    }

    if (items.length === 0) {
        showError("Please enter a quantity for at least one item.");
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


    // -------------------------------
    // Send request
    // -------------------------------

    try {
        const response = await fetch(packagesApiUrl, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(requestBody)
        });

        const result = await response.json().catch(() => ({}));

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
        showError(error.message || "Failed to create package.");
    }
}


// ===============================
// OPEN CREATE PACKAGE
// ===============================

function openCreatePackage() {
    const modal = document.getElementById("createPackageModal");
    const form = document.getElementById("createPackageForm");
    const container = document.getElementById("orderItemsContainer");

    if (modal) {
        modal.style.display = "flex";
    }

    if (form) {
        form.reset();
    }

    selectedOrderItems = [];

    if (container) {
        container.innerHTML = `
            <p class="muted">Select a sales order first.</p>
        `;
    }

    hideError();
}


// ===============================
// CLOSE CREATE PACKAGE
// ===============================

function closeCreatePackage() {
    const modal = document.getElementById("createPackageModal");

    if (modal) {
        modal.style.display = "none";
    }
}


// ===============================
// VIEW PACKAGE
// ===============================

function viewPackage(id) {
    window.location.href =
        `/erpflow/packageDetails.jsp?id=${encodeURIComponent(id)}`;
}


// ===============================
// STATUS CLASS
// ===============================

function getStatusClass(status) {
    if (!status) return "";

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

    if (!Number.isFinite(number)) {
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

    if (!errorElement) {
        alert(message);
        return;
    }

    errorElement.textContent = message;
    errorElement.style.display = "block";
}

function hideError() {
    const errorElement =
        document.getElementById("createPackageError");

    if (!errorElement) return;

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