
"use strict";

// =========================================================
// API URLS
// =========================================================

const apiBaseUrl = "/erpflow/api/sales-orders";
const customersApiUrl = "/erpflow/api/customers";
const itemsApiUrl = "/erpflow/api/items?status=ACTIVE";

// The ID of the sales order currently being edited.
// Null means the form is in Create mode.
let editingSalesOrderId = null;

// =========================================================
// PAGE INITIALIZATION
// =========================================================

document.addEventListener("DOMContentLoaded", async function () {
    setupEventListeners();

    await Promise.all([
        loadCustomers(),
        loadItems(),
        loadSalesOrders()
    ]);

    calculateOrderTotals();
});

// =========================================================
// EVENT LISTENERS
// =========================================================

function setupEventListeners() {
    const addItemButton =
        document.getElementById("addItemButton");

    const createSalesOrderButton =
        document.getElementById("createSalesOrderButton");

    const cancelEditButton =
        document.getElementById("cancelEditButton");

    const taxRate =
        document.getElementById("taxRate");

    const searchInput =
        document.getElementById("searchInput");
    
    const statusFilter = document.getElementById("statusFilter");
    if (statusFilter) {
    statusFilter.addEventListener("change", loadSalesOrders);
}


    if (addItemButton) {
        addItemButton.addEventListener("click", addItemRow);
    }

    if (createSalesOrderButton) {
        createSalesOrderButton.addEventListener(
            "click",
            submitSalesOrder
        );
    }

    if (cancelEditButton) {
        cancelEditButton.addEventListener(
            "click",
            cancelSalesOrderEdit
        );
    }

    if (taxRate) {
        taxRate.addEventListener(
            "change",
            calculateOrderTotals
        );
    }

    if (searchInput) {
        searchInput.addEventListener(
            "input",
            filterSalesOrders
        );
    }

    // Handle edits through the dynamically rendered order table.
    const salesOrdersTableBody =
        document.getElementById("salesOrdersTableBody");

    if (salesOrdersTableBody) {
        salesOrdersTableBody.addEventListener("click", function (event) {
            const editButton = event.target.closest("[data-edit-order]");

            if (!editButton) {
                return;
            }

            const orderId = editButton.dataset.editOrder;

            if (orderId) {
                editSalesOrder(orderId);
            }
        });
    }

    // Recalculate totals when quantity or selling price changes.
    document.addEventListener("input", function (event) {
        if (
            event.target.name === "quantity" ||
            event.target.name === "sellingPrice"
        ) {
            calculateOrderTotals();
            updateRowTotal(event.target.closest("tr"));
        }
    });
}

// =========================================================
// LOAD CUSTOMERS
// =========================================================

async function loadCustomers() {
    try {
        const response = await fetch(customersApiUrl);

        if (!response.ok) {
            throw new Error("Failed to load customers.");
        }

        const customers = await response.json();
        const select = document.getElementById("customerSelect");

        if (!select) {
            return;
        }

        select.replaceChildren();

        const placeholder = document.createElement("option");
        placeholder.value = "";
        placeholder.textContent = "Select Customer";
        select.appendChild(placeholder);

        (Array.isArray(customers) ? customers : []).forEach(customer => {
            const option = document.createElement("option");

            option.value = customer.id;
            option.textContent =
                `${customer.name} (${customer.email || "No email"})`;

            select.appendChild(option);
        });
    } catch (error) {
        console.error("Error loading customers:", error);
        showMessage(error.message || "Unable to load customers.", "error");
    }
}

// =========================================================
// LOAD ITEMS
// =========================================================

async function loadItems() {
    try {
        const response = await fetch(itemsApiUrl);

        if (!response.ok) {
            throw new Error("Failed to load items.");
        }

        const items = await response.json();

        window.erpflowItems = Array.isArray(items) ? items : [];
    } catch (error) {
        console.error("Error loading items:", error);

        window.erpflowItems = [];

        showMessage(error.message || "Unable to load items.", "error");
    }
}

// =========================================================
// ADD ITEM ROW
// =========================================================

function addItemRow(itemData = null) {
    const tbody =
        document.getElementById("salesOrderItemsTableBody");

    if (!tbody) {
        return null;
    }

    const row = document.createElement("tr");
    row.className = "sales-item-row";

    row.innerHTML = `
        <td>
            <select name="itemId" class="item-select" required>
                <option value="">Select Item</option>
            </select>
        </td>

        <td>
            <input
                type="text"
                name="sku"
                class="sku-display"
                readonly
            >
        </td>

        <td>
            <input
                type="text"
                name="availableStock"
                class="stock-display"
                readonly
            >
        </td>

        <td class="quantity-cell">

            <input
                type="number"
                name="quantity"
                min="1"
                step="1"
                value="1"
                required
            >
        </td>

        <td>
            <input
                type="number"
                name="sellingPrice"
                min="0"
                step="0.01"
                value="0"
                required
            >
        </td>

        <td>
            <span class="line-total">₹0.00</span>
        </td>

        <td>
            <button
                type="button"
                class="danger-button remove-item-button"
            >
                Remove
            </button>
        </td>
    `;

    tbody.appendChild(row);

    const itemSelect = row.querySelector(".item-select");

    populateItemSelect(itemSelect);

    itemSelect.addEventListener("change", function () {
        populateItemDetails(row, itemSelect.value);
    });

    const removeButton =
        row.querySelector(".remove-item-button");

    removeButton.addEventListener("click", function () {
        row.remove();
        calculateOrderTotals();
    });

    // When loading an existing order, populate its item data.
    if (itemData) {
        const selectedItemId =
            itemData.itemId ??
            itemData.item?.id ??
            "";

        itemSelect.value = String(selectedItemId);

        populateItemDetails(row, itemSelect.value);

        const quantityInput =
            row.querySelector('input[name="quantity"]');

        const priceInput =
            row.querySelector('input[name="sellingPrice"]');

        if (quantityInput) {
            const selectedItem = (window.erpflowItems || []).find(
                item => String(item.id) === String(selectedItemId)
            );

            const isService =
                String(selectedItem?.itemType || "").toUpperCase() === "SERVICE";

            quantityInput.value = isService
                ? "1"
                : String(itemData.quantity ?? 1);

            quantityInput.readOnly = isService;
            quantityInput.required = !isService;
        }

        if (priceInput) {
            priceInput.value = String(itemData.sellingPrice ?? 0);
        }

        updateRowTotal(row);
    }

    calculateOrderTotals();

    return row;
}

// =========================================================
// POPULATE ITEM SELECT
// =========================================================

function populateItemSelect(select) {
    if (!select) {
        return;
    }

    const items = window.erpflowItems || [];

    items.forEach(item => {
        const option = document.createElement("option");

        option.value = item.id;
        option.textContent = `${item.name} - ${item.sku || ""}`;

        select.appendChild(option);
    });
}

// =========================================================
// POPULATE ITEM DETAILS
// =========================================================

function populateItemDetails(row, itemId) {
    if (!row) {
        return;
    }

    const items = window.erpflowItems || [];

    const item = items.find(currentItem =>
        String(currentItem.id) === String(itemId)
    );

    const quantityInput =
        row.querySelector('input[name="quantity"]');

    const quantityCell =
        row.querySelector(".quantity-cell");

    const skuInput =
        row.querySelector('input[name="sku"]');

    const stockInput =
        row.querySelector('input[name="availableStock"]');

    const priceInput =
        row.querySelector('input[name="sellingPrice"]');

    if (!item) {
        if (skuInput) skuInput.value = "";
        if (stockInput) stockInput.value = "";
        if (priceInput) priceInput.value = "0";

        if (quantityInput) {
            quantityInput.readOnly = false;
            quantityInput.required = true;
            quantityInput.value = "1";
        }

        if (quantityCell) {
            quantityCell.style.display = "";
        }

        updateRowTotal(row);
        calculateOrderTotals();
        return;
    }

    const isService =
        String(item.itemType || "").toUpperCase() === "SERVICE";

    if (quantityInput && quantityCell) {
        // Keep the quantity field visible for both item types.
        quantityCell.style.display = "";

        if (isService) {
            quantityInput.value = "1";
            quantityInput.readOnly = true;
            quantityInput.required = false;
        } else {
            quantityInput.readOnly = false;
            quantityInput.required = true;
        }
    }

    if (skuInput) {
        skuInput.value = item.sku || "";
    }

    if (stockInput) {
        const stock =
            item.stockQuantity ??
            item.quantity ??
            item.availableStock ??
            0;

        stockInput.value = stock;
    }

    if (priceInput) {
        priceInput.value = item.sellingPrice ?? 0;
    }

    updateRowTotal(row);
    calculateOrderTotals();
}

// =========================================================
// UPDATE LINE TOTAL
// =========================================================

function updateRowTotal(row) {
    if (!row) {
        return;
    }

    const quantityInput =
        row.querySelector('input[name="quantity"]');

    const priceInput =
        row.querySelector('input[name="sellingPrice"]');

    const lineTotalElement =
        row.querySelector(".line-total");

    if (!quantityInput || !priceInput || !lineTotalElement) {
        return;
    }

    const quantity = parseFloat(quantityInput.value) || 0;
    const price = parseFloat(priceInput.value) || 0;

    lineTotalElement.textContent =
        formatCurrency(quantity * price);
}

// =========================================================
// CALCULATE ORDER TOTALS
// =========================================================

function calculateOrderTotals() {
    let subtotal = 0;

    const rows =
        document.querySelectorAll(".sales-item-row");

    rows.forEach(row => {
        const quantityInput =
            row.querySelector('input[name="quantity"]');

        const priceInput =
            row.querySelector('input[name="sellingPrice"]');

        if (!quantityInput || !priceInput) {
            return;
        }

        const quantity = parseFloat(quantityInput.value) || 0;
        const price = parseFloat(priceInput.value) || 0;

        subtotal += quantity * price;

        updateRowTotal(row);
    });

    const taxRateInput = document.getElementById("taxRate");
    const taxRate = parseFloat(taxRateInput?.value || 0);

    const taxAmount = subtotal * taxRate / 100;
    const totalAmount = subtotal + taxAmount;

    const subtotalDisplay =
        document.getElementById("subtotalDisplay");

    const taxRateDisplay =
        document.getElementById("taxRateDisplay");

    const taxAmountDisplay =
        document.getElementById("taxAmountDisplay");

    const totalAmountDisplay =
        document.getElementById("totalAmountDisplay");

    if (subtotalDisplay) {
        subtotalDisplay.textContent = formatCurrency(subtotal);
    }

    if (taxRateDisplay) {
        taxRateDisplay.textContent = taxRate.toFixed(2);
    }

    if (taxAmountDisplay) {
        taxAmountDisplay.textContent = formatCurrency(taxAmount);
    }

    if (totalAmountDisplay) {
        totalAmountDisplay.textContent = formatCurrency(totalAmount);
    }
}

// =========================================================
// CREATE / UPDATE SALES ORDER
// =========================================================

async function submitSalesOrder() {
    const customerSelect =
        document.getElementById("customerSelect");

    const taxRateInput =
        document.getElementById("taxRate");

    const rows =
        document.querySelectorAll(".sales-item-row");

    if (!customerSelect || !customerSelect.value) {
        showMessage("Please select a customer.", "error");
        return;
    }

    if (rows.length === 0) {
        showMessage("Please add at least one item.", "error");
        return;
    }

    const items = [];
    let hasValidationError = false;

    rows.forEach(row => {
        const itemSelect =
            row.querySelector('select[name="itemId"]');

        const quantityInput =
            row.querySelector('input[name="quantity"]');

        const priceInput =
            row.querySelector('input[name="sellingPrice"]');

        if (!itemSelect || !quantityInput || !priceInput) {
            hasValidationError = true;
            return;
        }

        const itemId = parseInt(itemSelect.value, 10);

        const selectedItem =
            (window.erpflowItems || []).find(item =>
                Number(item.id) === Number(itemSelect.value)
            );

        if (!itemId || !selectedItem) {
            showMessage(
                "Please select a valid item in every row.",
                "error"
            );

            hasValidationError = true;
            return;
        }

        const isService =
            String(selectedItem.itemType || "").toUpperCase() === "SERVICE";

        const quantity = isService
            ? 1
            : parseInt(quantityInput.value, 10);

        const sellingPrice = parseFloat(priceInput.value);

        if (!Number.isInteger(quantity) || quantity <= 0) {
            showMessage("Quantity must be greater than 0.", "error");
            hasValidationError = true;
            return;
        }

        if (Number.isNaN(sellingPrice) || sellingPrice < 0) {
            showMessage(
                "Selling price must be a valid non-negative number.",
                "error"
            );

            hasValidationError = true;
            return;
        }

        items.push({
            itemId: itemId,
            quantity: quantity,
            sellingPrice: sellingPrice
        });
    });

    if (hasValidationError) {
        return;
    }

    const taxRate = parseFloat(taxRateInput?.value || 0);

    if (Number.isNaN(taxRate) || taxRate < 0 || taxRate > 100) {
        showMessage(
            "Tax rate must be between 0% and 100%.",
            "error"
        );
        return;
    }

    const orderData = {
        customerId: parseInt(customerSelect.value, 10),
        taxRate: taxRate,
        items: items
    };

    const button =
        document.getElementById("createSalesOrderButton");

    const isEditing = editingSalesOrderId !== null;

    const requestUrl = isEditing
        ? `${apiBaseUrl}/${encodeURIComponent(editingSalesOrderId)}`
        : apiBaseUrl;

    const requestMethod = isEditing ? "PUT" : "POST";

    try {
        if (button) {
            button.disabled = true;
            button.textContent = isEditing ? "Updating..." : "Creating...";
        }

        const response = await fetch(requestUrl, {
            method: requestMethod,
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(orderData)
        });

        const result = await response.json().catch(() => ({}));

        if (!response.ok) {
            throw new Error(
                result.message ||
                result.error ||
                (isEditing
                    ? "Failed to update sales order."
                    : "Failed to create sales order.")
            );
        }

        let successMessage = isEditing
            ? "Sales order updated successfully."
            : "Sales order created successfully.";

        if (result.totalAmount !== undefined) {
            successMessage += ` Total: ${formatCurrency(result.totalAmount)}`;
        }

        showMessage(successMessage, "success");

        resetSalesOrderForm();
        await loadSalesOrders();

    } catch (error) {
        console.error(
            isEditing ? "Error updating sales order:" : "Error creating sales order:",
            error
        );

        showMessage(
            error.message ||
            (isEditing
                ? "Failed to update sales order."
                : "Failed to create sales order."),
            "error"
        );
    } finally {
        if (button) {
            button.disabled = false;
            button.textContent = editingSalesOrderId !== null
                ? "Update Sales Order"
                : "Create Sales Order";
        }
    }
}

// =========================================================
// EDIT SALES ORDER
// =========================================================

async function editSalesOrder(orderId) {
    try {
        const response = await fetch(
            `${apiBaseUrl}/${encodeURIComponent(orderId)}`
        );

        const order = await response.json().catch(() => ({}));

        if (!response.ok) {
            throw new Error(
                order.message ||
                order.error ||
                "Failed to load sales order."
            );
        }

        if (String(order.status || "").toUpperCase() !== "CREATED") {
            showMessage(
                "Only CREATED sales orders can be edited.",
                "error"
            );
            return;
        }

        const customerSelect =
            document.getElementById("customerSelect");

        const taxRateSelect =
            document.getElementById("taxRate");

        const tbody =
            document.getElementById("salesOrderItemsTableBody");

        if (!customerSelect || !taxRateSelect || !tbody) {
            throw new Error("Sales order form elements were not found.");
        }

        // Set the current edit ID.
        editingSalesOrderId = Number(order.id);

        // Populate customer and tax fields.
        customerSelect.value = String(order.customer?.id ?? "");
        taxRateSelect.value = String(order.taxRate ?? 0);

        // Clear any existing item rows.
        tbody.replaceChildren();

        const orderItems = Array.isArray(order.items) ? order.items : [];

        if (orderItems.length === 0) {
            showMessage(
                "This sales order has no items to edit.",
                "error"
            );
            cancelSalesOrderEdit();
            return;
        }

        // Recreate each existing order line in the form.
        orderItems.forEach(orderItem => {
            addItemRow(orderItem);
        });

        // Switch the form to Edit mode.
        const title =
            document.getElementById("salesOrderFormTitle");

        const button =
            document.getElementById("createSalesOrderButton");

        const cancelButton =
            document.getElementById("cancelEditButton");

        const editNote =
            document.getElementById("editModeNote");

        if (title) {
            title.textContent = `Edit Sales Order #${order.id}`;
        }

        if (button) {
            button.textContent = "Update Sales Order";
        }

        if (cancelButton) {
            cancelButton.style.display = "inline-block";
        }

        if (editNote) {
            editNote.style.display = "block";
        }

        calculateOrderTotals();

        showMessage(
            `Sales Order #${order.id} loaded for editing.`,
            "info"
        );

        // Bring the edit form into view.
        title?.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });

    } catch (error) {
        console.error("Error loading sales order for editing:", error);

        showMessage(
            error.message || "Unable to load sales order for editing.",
            "error"
        );
    }
}

// =========================================================
// CANCEL EDIT
// =========================================================

function cancelSalesOrderEdit() {
    resetSalesOrderForm();

    showMessage("Edit cancelled.", "info");
}

// =========================================================
// RESET FORM
// =========================================================

function resetSalesOrderForm() {
    editingSalesOrderId = null;

    const customerSelect =
        document.getElementById("customerSelect");

    const taxRate =
        document.getElementById("taxRate");

    const tbody =
        document.getElementById("salesOrderItemsTableBody");

    const title =
        document.getElementById("salesOrderFormTitle");

    const button =
        document.getElementById("createSalesOrderButton");

    const cancelButton =
        document.getElementById("cancelEditButton");

    const editNote =
        document.getElementById("editModeNote");

    if (customerSelect) {
        customerSelect.value = "";
    }

    if (taxRate) {
        taxRate.value = "0";
    }

    if (tbody) {
        tbody.replaceChildren();
    }

    if (title) {
        title.textContent = "Create Sales Order";
    }

    if (button) {
        button.textContent = "Create Sales Order";
        button.disabled = false;
    }

    if (cancelButton) {
        cancelButton.style.display = "none";
    }

    if (editNote) {
        editNote.style.display = "none";
    }

    calculateOrderTotals();
}

// =========================================================
// LOAD SALES ORDERS
// =========================================================

async function loadSalesOrders() {
    try {
        const statusFilter = document.getElementById("statusFilter");
        const selectedStatus = statusFilter?.value || "ALL";

        const requestUrl = selectedStatus === "ALL"
            ? apiBaseUrl
            : `${apiBaseUrl}?status=${encodeURIComponent(selectedStatus)}`;

        const response = await fetch(requestUrl);

        if (!response.ok) {
            throw new Error("Failed to load sales orders.");
        }

        const orders = await response.json();

        window.erpflowSalesOrders = Array.isArray(orders) ? orders : [];

        filterSalesOrders();

    } catch (error) {
        console.error("Error loading sales orders:", error);

        showMessage(
            error.message || "Unable to load sales orders.",
            "error"
        );
    }
}
// =========================================================
// DISPLAY SALES ORDERS
// =========================================================

function displaySalesOrders(orders) {
    const tbody =
        document.getElementById("salesOrdersTableBody");

    if (!tbody) {
        return;
    }

    tbody.replaceChildren();

    if (!orders || orders.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" style="text-align:center;">
                    No sales orders found.
                </td>
            </tr>
        `;
        return;
    }

    orders.forEach(order => {
        const row = document.createElement("tr");

        const customer = order.customer || {};

        const subtotal = Number(order.subtotal || 0);
        const taxRate = Number(order.taxRate || 0);
        const taxAmount = Number(order.taxAmount || 0);
        const totalAmount = Number(order.totalAmount || 0);

        const safeId = escapeHtml(order.id);
        const encodedId = encodeURIComponent(order.id);
        const status = String(order.status || "-");
        const canEdit = status.toUpperCase() === "CREATED";

        row.innerHTML = `
            <td>${safeId}</td>

            <td>${escapeHtml(customer.name || "-")}</td>

            <td>${escapeHtml(formatDate(order.orderDate))}</td>

            <td>${formatCurrency(subtotal)}</td>

            <td>
                ${taxRate.toFixed(2)}%
                <br>
                <small>${formatCurrency(taxAmount)}</small>
            </td>

            <td>
                <strong>${formatCurrency(totalAmount)}</strong>
            </td>

            <td>${escapeHtml(status)}</td>

            <td>
                <a
                    class="action-button"
                    href="/erpflow/salesOrderDetails.jsp?id=${encodedId}"
                >
                    View
                </a>

                ${canEdit
                ? `
                            <button
                                type="button"
                                class="action-button edit-button"
                                data-edit-order="${escapeHtml(order.id)}"
                            >
                                Edit
                            </button>
                        `
                : `
                            <span
                                class="action-button disabled-action"
                                title="Only CREATED orders can be edited"
                            >
                                Edit
                            </span>
                        `
            }
            </td>
        `;

        tbody.appendChild(row);
    });
}

// =========================================================
// SEARCH / FILTER
// =========================================================

function filterSalesOrders() {
    const searchInput =
        document.getElementById("searchInput");

    if (!searchInput) {
        return;
    }

    const searchText =
        searchInput.value.trim().toLowerCase();

    const orders = window.erpflowSalesOrders || [];

    if (!searchText) {
        displaySalesOrders(orders);
        return;
    }

    const filteredOrders = orders.filter(order => {
        const customer = order.customer || {};

        const searchableText = [
            order.id,
            order.status,
            customer.name,
            customer.email,
            customer.phone
        ]
            .filter(Boolean)
            .join(" ")
            .toLowerCase();

        return searchableText.includes(searchText);
    });

    displaySalesOrders(filteredOrders);
}

// =========================================================
// DATE FORMATTER
// =========================================================

function formatDate(dateValue) {
    if (!dateValue) {
        return "-";
    }

    let date;

    // Jackson LocalDateTime array:
    // [year, month, day, hour, minute, second, nanoseconds]
    if (Array.isArray(dateValue)) {
        const year = dateValue[0];
        const month = (dateValue[1] || 1) - 1;
        const day = dateValue[2] || 1;
        const hour = dateValue[3] || 0;
        const minute = dateValue[4] || 0;
        const second = dateValue[5] || 0;

        date = new Date(
            year,
            month,
            day,
            hour,
            minute,
            second
        );
    } else {
        date = new Date(dateValue);
    }

    if (Number.isNaN(date.getTime())) {
        return "-";
    }

    return date.toLocaleString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
}

// =========================================================
// CURRENCY FORMATTER
// =========================================================

function formatCurrency(amount) {
    return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(Number(amount) || 0);
}

// =========================================================
// MESSAGE
// =========================================================

function showMessage(message, type = "info") {
    const messageBox =
        document.getElementById("messageBox");

    if (!messageBox) {
        console.log(`[${type}] ${message}`);
        return;
    }

    const messageElement = document.createElement("div");

    messageElement.className = `message ${type}`;
    messageElement.textContent = message;

    messageBox.replaceChildren(messageElement);

    // Remove this message after five seconds,
    // without removing a newer message displayed in the meantime.
    setTimeout(function () {
        if (messageBox.contains(messageElement)) {
            messageBox.removeChild(messageElement);
        }
    }, 5000);
}

// =========================================================
// HTML ESCAPE
// =========================================================

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