
"use strict";

const API_BASE = "/erpflow/api/sales-returns";

let eligibleLines = [];
let currentReturnDetails = null;
let returnsCache = [];

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("returnDate").value =
        new Date().toISOString().slice(0, 10);

    document.getElementById("showCreateButton")
        .addEventListener("click", showCreatePanel);

    document.getElementById("cancelCreateButton")
        .addEventListener("click", hideCreatePanel);

    document.getElementById("loadLinesButton")
        .addEventListener("click", loadEligibleLines);

    document.getElementById("createReturnButton")
        .addEventListener("click", createReturn);

    document.getElementById("refreshButton")
        .addEventListener("click", loadReturns);

    document.getElementById("statusFilter")
        .addEventListener("change", renderReturns);

    document.getElementById("closeDetailsButton")
        .addEventListener("click", () => {
            document.getElementById("detailPanel").classList.add("return-hidden");
        });

    document.getElementById("receiveReturnButton")
        .addEventListener("click", receiveCurrentReturn);

    loadReturns();
});

function showMessage(message, type = "info") {
    const box = document.getElementById("messageBox");
    box.textContent = message;
    box.className = `return-message ${type}`;
    box.scrollIntoView({ behavior: "smooth", block: "nearest" });
}

function clearMessage() {
    const box = document.getElementById("messageBox");
    box.textContent = "";
    box.className = "return-message";
}

function showCreatePanel() {
    clearMessage();
    document.getElementById("createPanel").classList.remove("return-hidden");
    document.getElementById("salesOrderId").focus();
}

function hideCreatePanel() {
    document.getElementById("createPanel").classList.add("return-hidden");
}

async function requestJson(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        }
    });

    const text = await response.text();
    let data = {};

    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = { message: text };
        }
    }

    if (!response.ok) {
        throw new Error(
            data.message ||
            data.error ||
            `Request failed (${response.status})`
        );
    }

    return data;
}

function asArray(data) {
    if (Array.isArray(data)) return data;
    if (Array.isArray(data.returns)) return data.returns;
    if (Array.isArray(data.items)) return data.items;
    if (Array.isArray(data.data)) return data.data;
    return [];
}

function firstValue(object, keys, fallback = "") {
    for (const key of keys) {
        if (object && object[key] !== undefined && object[key] !== null) {
            return object[key];
        }
    }
    return fallback;
}

function numberValue(value) {
    const number = Number(value);
    return Number.isFinite(number) ? number : 0;
}

function money(value) {
    return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        minimumFractionDigits: 2
    }).format(numberValue(value));
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[character]);
}

function normalizeStatus(status) {
    const value = String(status || "CREATED").toUpperCase();
    return value === "RECEIVED" ? "RECEIVED" : "CREATED";
}

function statusBadge(status) {
    const normalized = normalizeStatus(status);
    return `<span class="return-status status-${normalized.toLowerCase()}">
        ${normalized}
    </span>`;
}

async function loadReturns() {
    const body = document.getElementById("returnsBody");
    body.innerHTML = `<tr><td colspan="8" class="empty-message">
        Loading returns...
    </td></tr>`;

    try {
        const data = await requestJson(API_BASE);
        returnsCache = asArray(data);
        renderReturns();
    } catch (error) {
        body.innerHTML = `<tr><td colspan="8" class="empty-message">
            Could not load returns.
        </td></tr>`;
        showMessage(error.message, "error");
    }
}

function renderReturns() {
    const body = document.getElementById("returnsBody");
    const filter = document.getElementById("statusFilter").value;

    const visible = returnsCache.filter(returnRow => {
        const status = normalizeStatus(
            firstValue(returnRow, ["status"], "CREATED")
        );
        return !filter || status === filter;
    });

    document.getElementById("totalReturns").textContent =
        returnsCache.length;

    document.getElementById("createdReturns").textContent =
        returnsCache.filter(row =>
            normalizeStatus(row.status) === "CREATED"
        ).length;

    document.getElementById("receivedReturns").textContent =
        returnsCache.filter(row =>
            normalizeStatus(row.status) === "RECEIVED"
        ).length;

    if (!visible.length) {
        body.innerHTML = `<tr><td colspan="8" class="empty-message">
            No sales returns found.
        </td></tr>`;
        return;
    }

    body.innerHTML = visible.map(row => {
        const id = numberValue(firstValue(
            row, ["salesReturnId", "sales_return_id", "id"]
        ));
        const returnNumber = firstValue(row, ["returnNumber", "return_number"], `SR-${id}`);
        const orderId = firstValue(row, ["salesOrderId", "sales_order_id"], "-");
        const customerId = firstValue(row, ["customerId", "customer_id"], "-");
        const returnDate = firstValue(row, ["returnDate", "return_date"], "-");
        const itemCount = firstValue(row, ["itemCount", "item_count", "lineCount"], "-");
        const total = firstValue(row, ["totalAmount", "total_amount"], 0);
        const status = normalizeStatus(row.status);

        return `
            <tr>
                <td><strong>${escapeHtml(returnNumber)}</strong></td>
                <td>#${escapeHtml(orderId)}</td>
                <td>${escapeHtml(customerId)}</td>
                <td>${escapeHtml(String(returnDate).slice(0, 10))}</td>
                <td>${escapeHtml(itemCount)}</td>
                <td>${money(total)}</td>
                <td>${statusBadge(status)}</td>
                <td>
                    <div class="return-actions">
                        <button type="button" class="btn btn-secondary"
                                data-view-return="${id}">View</button>
                    </div>
                </td>
            </tr>`;
    }).join("");

    body.querySelectorAll("[data-view-return]").forEach(button => {
        button.addEventListener("click", () => {
            viewReturn(Number(button.dataset.viewReturn));
        });
    });
}

async function loadEligibleLines() {
    clearMessage();

    const orderId = Number(document.getElementById("salesOrderId").value);
    const body = document.getElementById("eligibleLinesBody");
    const createButton = document.getElementById("createReturnButton");

    if (!Number.isInteger(orderId) || orderId <= 0) {
        showMessage("Enter a valid Sales Order ID.", "error");
        return;
    }

    body.innerHTML = `<tr><td colspan="8" class="empty-message">
        Loading returnable items...
    </td></tr>`;
    createButton.disabled = true;
    eligibleLines = [];

    try {
        const data = await requestJson(
            `${API_BASE}/eligible-lines?salesOrderId=${encodeURIComponent(orderId)}`
        );

        eligibleLines = asArray(data).map(line => ({
            raw: line,
            lineId: numberValue(firstValue(line, [
                "salesOrderItemId", "sales_order_item_id", "orderLineId", "id"
            ])),
            itemId: numberValue(firstValue(line, ["itemId", "item_id"])),
            name: firstValue(line, ["itemName", "name", "item_name"], "Item"),
            sku: firstValue(line, ["sku"], "-"),
            shipped: numberValue(firstValue(line, [
                "shippedQuantity", "shipped_quantity", "shippedQty"
            ])),
            available: numberValue(firstValue(line, [
                "returnableQuantity", "returnable_quantity",
                "availableToReturn", "availableQuantity", "available"
            ])),
            unitPrice: numberValue(firstValue(line, [
                "unitPrice", "sellingPrice", "selling_price", "unit_price"
            ]))
        })).filter(line => line.lineId > 0 && line.available > 0);

        if (!eligibleLines.length) {
            body.innerHTML = `<tr><td colspan="8" class="empty-message">
                No shipped quantities are available for return on this order.
            </td></tr>`;
            return;
        }

        renderEligibleLines();
        showMessage("Returnable items loaded. Enter quantities and create the return.", "success");
    } catch (error) {
        body.innerHTML = `<tr><td colspan="8" class="empty-message">
            Failed to load returnable items.
        </td></tr>`;
        showMessage(error.message, "error");
    }
}

function renderEligibleLines() {
    const body = document.getElementById("eligibleLinesBody");

    body.innerHTML = eligibleLines.map((line, index) => `
        <tr data-line-index="${index}">
            <td>
                <strong>${escapeHtml(line.name)}</strong>
                <div class="muted-text">Item ID: ${escapeHtml(line.itemId || "-")}</div>
            </td>
            <td>${escapeHtml(line.sku)}</td>
            <td>${line.shipped}</td>
            <td>${line.available}</td>
            <td>${money(line.unitPrice)}</td>
            <td>
                <input type="number" min="0" max="${line.available}"
                       step="1" value="0" class="return-quantity"
                       aria-label="Return quantity for ${escapeHtml(line.name)}">
            </td>
            <td>
                <select class="line-reason" aria-label="Reason for ${escapeHtml(line.name)}">
                    <option value="">Select reason</option>
                    <option value="DEFECTIVE_PRODUCT">Defective</option>
                    <option value="WRONG_ITEM">Wrong Item</option>
                    <option value="DAMAGED_IN_TRANSIT">Damaged</option>
                    <option value="OTHER">Other</option>
                </select>
            </td>
            <td class="line-amount">${money(0)}</td>
        </tr>
    `).join("");

    body.querySelectorAll(".return-quantity").forEach(input => {
        input.addEventListener("input", () => {
            const row = input.closest("tr");
            const index = Number(row.dataset.lineIndex);
            let quantity = numberValue(input.value);

            if (quantity < 0) quantity = 0;
            if (quantity > eligibleLines[index].available) {
                quantity = eligibleLines[index].available;
                input.value = String(quantity);
            }

            row.querySelector(".line-amount").textContent =
                money(quantity * eligibleLines[index].unitPrice);

            updateReturnTotal();
        });
    });

    updateReturnTotal();
}

function updateReturnTotal() {
    let total = 0;
    let hasQuantity = false;

    document.querySelectorAll("#eligibleLinesBody tr[data-line-index]")
        .forEach(row => {
            const index = Number(row.dataset.lineIndex);
            const quantity = numberValue(
                row.querySelector(".return-quantity").value
            );
            if (quantity > 0) hasQuantity = true;
            total += quantity * eligibleLines[index].unitPrice;
        });

    document.getElementById("returnTotal").textContent = money(total);
    document.getElementById("createReturnButton").disabled = !hasQuantity;
}

async function createReturn() {
    clearMessage();

    const salesOrderId = Number(document.getElementById("salesOrderId").value);
    const returnDate = document.getElementById("returnDate").value;

    if (!Number.isInteger(salesOrderId) || salesOrderId <= 0) {
        showMessage("Enter a valid Sales Order ID.", "error");
        return;
    }

    if (!returnDate) {
        showMessage("Select a return date.", "error");
        return;
    }

    const items = [];

    document.querySelectorAll("#eligibleLinesBody tr[data-line-index]")
        .forEach(row => {
            const index = Number(row.dataset.lineIndex);
            const quantity = numberValue(
                row.querySelector(".return-quantity").value
            );
            const reason = row.querySelector(".line-reason").value;

            if (quantity > 0) {
                items.push({
                    salesOrderItemId: eligibleLines[index].lineId,
                    quantity,
                    reason: reason || null
                });
            }
        });

    if (!items.length) {
        showMessage("Enter a return quantity for at least one item.", "error");
        return;
    }

    const button = document.getElementById("createReturnButton");
    button.disabled = true;
    button.textContent = "Creating...";

    try {
        const payload = {
            salesOrderId,
            returnDate,
            reason: document.getElementById("overallReason").value || null,
            notes: document.getElementById("returnNotes").value.trim() || null,
            items
        };

        const created = await requestJson(API_BASE, {
            method: "POST",
            body: JSON.stringify(payload)
        });

        showMessage(
            `Sales return ${firstValue(created, ["returnNumber", "return_number"], "")} created successfully.`,
            "success"
        );

        hideCreatePanel();
        document.getElementById("detailPanel").classList.add("return-hidden");
        await loadReturns();

        const newId = numberValue(firstValue(
            created, ["salesReturnId", "sales_return_id", "id"]
        ));
        if (newId > 0) await viewReturn(newId);

    } catch (error) {
        showMessage(error.message, "error");
    } finally {
        button.textContent = "Create Return";
        updateReturnTotal();
    }
}

async function viewReturn(id) {
    clearMessage();

    try {
        const data = await requestJson(`${API_BASE}/${id}`);
        currentReturnDetails = data;

        const header = data.salesReturn || data.return || data;
        const detailsId = numberValue(firstValue(
            header, ["salesReturnId", "sales_return_id", "id"], id
        ));
        const status = normalizeStatus(header.status);
        const returnNumber = firstValue(
            header, ["returnNumber", "return_number"], `SR-${detailsId}`
        );

        document.getElementById("detailTitle").textContent =
            `Sales Return ${returnNumber}`;

        document.getElementById("detailSubtitle").textContent =
            `Return ID: ${detailsId} · Status: ${status}`;

        const items = asArray(data.items ? data : { items: data.items })
            .length ? asArray(data.items ? data : { items: data.items })
            : asArray(header);

        const returnItems = Array.isArray(data.items) ? data.items :
            (Array.isArray(header.items) ? header.items : []);

        let html = `
            <div class="return-detail-row">
                <span>Sales Order</span>
                <span>#${escapeHtml(firstValue(header, ["salesOrderId", "sales_order_id"], "-"))}</span>
            </div>
            <div class="return-detail-row">
                <span>Customer ID</span>
                <span>${escapeHtml(firstValue(header, ["customerId", "customer_id"], "-"))}</span>
            </div>
            <div class="return-detail-row">
                <span>Return Date</span>
                <span>${escapeHtml(String(firstValue(header, ["returnDate", "return_date"], "-")).slice(0, 10))}</span>
            </div>
            <div class="return-detail-row">
                <span>Reason</span>
                <span>${escapeHtml(firstValue(header, ["reason"], "-") || "-")}</span>
            </div>
            <div class="return-detail-row">
                <span>Notes</span>
                <span>${escapeHtml(firstValue(header, ["notes"], "-") || "-")}</span>
            </div>
            <h3 style="margin-top:24px">Returned Items</h3>
            <div class="return-table-wrap">
            <table class="return-table">
                <thead><tr>
                    <th>Item ID</th><th>Order Line ID</th>
                    <th>Quantity</th><th>Unit Price</th><th>Amount</th>
                    <th>Condition</th>
                </tr></thead><tbody>`;

        if (!returnItems.length) {
            html += `<tr><td colspan="6" class="empty-message">No item details found.</td></tr>`;
        } else {
            html += returnItems.map(item => `
                <tr>
                    <td>${escapeHtml(firstValue(item, ["itemId", "item_id"], "-"))}</td>
                    <td>${escapeHtml(firstValue(item, ["salesOrderItemId", "sales_order_item_id"], "-"))}</td>
                    <td>${escapeHtml(firstValue(item, ["quantity"], 0))}</td>
                    <td>${money(firstValue(item, ["unitPrice", "unit_price"], 0))}</td>
                    <td>${money(firstValue(item, ["returnAmount", "return_amount"], 0))}</td>
                    <td>${escapeHtml(firstValue(item, ["itemCondition", "item_condition"], "PENDING"))}</td>
                </tr>
            `).join("");
        }

        html += `</tbody></table></div>
            <div class="return-total">
                <span>Total Return Amount</span>
                <strong>${money(firstValue(header, ["totalAmount", "total_amount"], 0))}</strong>
            </div>`;

        document.getElementById("returnDetails").innerHTML = html;

        const receiveButton = document.getElementById("receiveReturnButton");
        receiveButton.classList.toggle("return-hidden", status !== "CREATED");
        receiveButton.dataset.returnId = String(detailsId);

        document.getElementById("detailPanel").classList.remove("return-hidden");
        document.getElementById("detailPanel")
            .scrollIntoView({ behavior: "smooth", block: "start" });

    } catch (error) {
        showMessage(error.message, "error");
    }
}

async function receiveCurrentReturn() {
    const button = document.getElementById("receiveReturnButton");
    const id = Number(button.dataset.returnId);

    if (!id) {
        showMessage("No sales return selected.", "error");
        return;
    }

    const confirmed = window.confirm(
        "Receive this return and restock eligible returned goods?"
    );
    if (!confirmed) return;

    button.disabled = true;
    button.textContent = "Receiving...";

    try {
        await requestJson(`${API_BASE}/${id}/status`, {
            method: "PUT",
            body: JSON.stringify({ status: "RECEIVED" })
        });

        showMessage(
            "Return marked as RECEIVED. Inventory should now reflect the returned stock.",
            "success"
        );

        await loadReturns();
        await viewReturn(id);

    } catch (error) {
        showMessage(error.message, "error");
    } finally {
        button.disabled = false;
        button.textContent = "Receive Return";
    }
}

