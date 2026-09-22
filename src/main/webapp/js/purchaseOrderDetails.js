
const apiUrl = "/erpflow/api/purchase-orders";

let currentOrder = null;

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("receiveForm")
        ?.addEventListener("submit", receiveSelectedQuantities);

    loadPurchaseOrder();
});

async function readJson(response) {
    const text = await response.text();

    let data = {};
    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = { error: text };
        }
    }

    if (!response.ok) {
        throw new Error(data.error || data.message || "Request failed");
    }

    return data;
}

function getOrderId() {
    return new URLSearchParams(window.location.search).get("id");
}

async function loadPurchaseOrder() {
    const orderId = getOrderId();

    if (!orderId) {
        showError("Purchase order ID is missing.");
        return;
    }

    try {
        const response = await fetch(`${apiUrl}/${encodeURIComponent(orderId)}`);
        currentOrder = await readJson(response);
        displayPurchaseOrder(currentOrder);
    } catch (error) {
        console.error(error);
        showError("Failed to load purchase order: " + error.message);
    }
}

function displayPurchaseOrder(order) {
    setValue("orderId", order.id);
    setValue("orderDate", formatDate(order.orderDate));
    setValue("orderStatus", order.status);

    const supplier = order.supplier || {};

    setValue("supplierId", supplier.id);
    setValue("supplierName", supplier.name);
    setValue("contactPerson", supplier.contactPerson);
    setValue("supplierPhone", supplier.phone);
    setValue("supplierEmail", supplier.email);
    setValue("supplierAddress", supplier.address);

    renderOrderItems(order.items || []);
    renderReceivingItems(order.items || []);

    const status = String(order.status || "").toUpperCase();
    const receivingSection = document.getElementById("receivingSection");

    if (receivingSection) {
        receivingSection.style.display =
            status === "RECEIVED" ? "none" : "block";
    }
}

function renderOrderItems(items) {
    const tbody = document.getElementById("orderItemsTableBody");
    if (!tbody) return;

    tbody.innerHTML = "";

    items.forEach(line => {
        const item = line.item || {};
        const service = isService(item);
        const quantity = Number(line.quantity || 0);
        const received = Number(line.recievedQuantity || 0);
        const remaining = Number(
            line.remainingQuantity ?? Math.max(0, quantity - received)
        );
        const price = Number(line.purchasePrice || 0);

        const row = document.createElement("tr");

        appendCell(row, item.id ?? "—");
        appendCell(row, item.name || "—");
        appendCell(row, item.sku || "—");
        appendCell(row, service ? "Service" : "Goods");
        appendCell(row, service ? "N/A" : quantity);
        appendCell(row, service ? "N/A" : received);
        appendCell(row, service ? "N/A" : remaining);
        appendCell(row, formatMoney(price));
        appendCell(row, service ? formatMoney(price) : formatMoney(quantity * price));

        tbody.appendChild(row);
    });
}

function renderReceivingItems(items) {
    const tbody = document.getElementById("receiveItemsTableBody");
    if (!tbody) return;

    tbody.innerHTML = "";

    let receivableCount = 0;

    items.forEach(line => {
        const item = line.item || {};
        if (isService(item)) return;

        const quantity = Number(line.quantity || 0);
        const received = Number(line.recievedQuantity || 0);
        const remaining = Number(
            line.remainingQuantity ?? Math.max(0, quantity - received)
        );

        if (remaining <= 0) return;

        receivableCount++;

        const row = document.createElement("tr");

        const nameCell = document.createElement("td");
        nameCell.textContent = `${item.name || "Item"} (${item.sku || "No SKU"})`;

        const remainingCell = document.createElement("td");
        remainingCell.textContent = remaining;

        const inputCell = document.createElement("td");
        const input = document.createElement("input");

        input.type = "number";
        input.className = "receive-quantity-input";
        input.min = "0";
        input.max = String(remaining);
        input.step = "1";
        input.value = "0";
        input.dataset.lineId = String(line.id);
        input.dataset.remaining = String(remaining);
        input.setAttribute("aria-label", `Quantity to receive for ${item.name || "item"}`);

        inputCell.appendChild(input);

        row.append(nameCell, remainingCell, inputCell);
        tbody.appendChild(row);
    });

    const button = document.getElementById("receiveButton");

    if (button) {
        button.disabled = receivableCount === 0;
        button.textContent = receivableCount === 0
            ? "Nothing Remaining to Receive"
            : "Receive Selected Quantities";
    }

    if (receivableCount === 0 && currentOrder) {
        const status = String(currentOrder.status || "").toUpperCase();
        if (status !== "RECEIVED") {
            showMessage("No outstanding inventory quantities remain.");
        }
    }
}

async function receiveSelectedQuantities(event) {
    event.preventDefault();

    if (!currentOrder) {
        showError("Purchase order details have not loaded.");
        return;
    }

    const inputs = document.querySelectorAll(".receive-quantity-input");
    const items = [];

    for (const input of inputs) {
        const quantity = Number(input.value);
        const remaining = Number(input.dataset.remaining);

        if (!Number.isInteger(quantity) || quantity < 0) {
            showError("Enter a valid non-negative whole number.");
            return;
        }

        if (quantity > remaining) {
            showError("Receive quantity cannot exceed the outstanding quantity.");
            return;
        }

        if (quantity > 0) {
            items.push({
                purchaseOrderItemId: Number(input.dataset.lineId),
                quantity
            });
        }
    }

    if (items.length === 0) {
        showError("Enter a quantity greater than zero for at least one item.");
        return;
    }

    const orderId = getOrderId();

    try {
        const response = await fetch(
            `${apiUrl}/${encodeURIComponent(orderId)}/receive`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ items })
            }
        );

        const result = await readJson(response);

        showMessage(result.message || "Receiving processed successfully.");

        await loadPurchaseOrder();
    } catch (error) {
        console.error(error);
        showError("Receiving failed: " + error.message);
    }
}

function isService(item) {
    return String(item?.itemType || "").toUpperCase() === "SERVICE"
        || item?.trackInventory === false;
}

function appendCell(row, value) {
    const cell = document.createElement("td");
    cell.textContent = value == null ? "—" : String(value);
    row.appendChild(cell);
}

function setValue(id, value) {
    const element = document.getElementById(id);
    if (element) {
        element.value = value == null ? "" : String(value);
    }
}

function formatMoney(value) {
    return Number(value || 0).toFixed(2);
}

function formatDate(value) {
    if (!value) return "—";

    if (Array.isArray(value)) {
        const [year, month, day, hour = 0, minute = 0] = value;

        return `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")} `
            + `${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}`;
    }

    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString();
}

function showMessage(message) {
    const box = document.getElementById("messageBox");
    if (!box) return;

    box.textContent = message;
    box.className = "success-message";
    box.style.display = "block";
}

function showError(message) {
    const box = document.getElementById("messageBox");
    if (!box) return;

    box.textContent = message;
    box.className = "error-message";
    box.style.display = "block";
}