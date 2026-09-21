
"use strict";

// =====================================================
// SALES ORDER DETAILS
// =====================================================

const params = new URLSearchParams(window.location.search);
const salesOrderId = params.get("id");

// =====================================================
// API URLS
// =====================================================

const salesOrderApi = `/erpflow/api/sales-orders/${salesOrderId}`;
const packagesApi = `/erpflow/api/packages?salesOrderId=${salesOrderId}`;
const carriersApi = "/erpflow/api/carriers";
const shippingApi = "/erpflow/api/shipping";

// =====================================================
// PAGE LOAD
// =====================================================

document.addEventListener("DOMContentLoaded", () => {
    if (!salesOrderId) {
        showMessage("Sales Order ID is missing.", "error");
        return;
    }

    loadSalesOrder();
    loadPackages();

    const rateButton = document.getElementById("calculateRateButton");
    if (rateButton) {
        rateButton.addEventListener("click", calculateShippingRate);
    }

    const createShipmentButton = document.getElementById("createShipmentButton");
    if (createShipmentButton) {
        createShipmentButton.addEventListener("click", createShipment);
    }
});

// =====================================================
// LOAD SALES ORDER
// =====================================================

async function loadSalesOrder() {
    try {
        const response = await fetch(salesOrderApi);

        if (!response.ok) {
            throw new Error("Failed to load Sales Order.");
        }

        const order = await response.json();

        console.log("Sales Order:", order);

        displaySalesOrder(order);
    } catch (error) {
        console.error("Sales Order Error:", error);
        showMessage(error.message || "Failed to load Sales Order.", "error");
    }
}

// =====================================================
// DISPLAY SALES ORDER
// =====================================================

function displaySalesOrder(order) {
    // Order information
    setValue("orderId", order.id != null ? `#${order.id}` : "-");
    setValue("orderDate", formatDate(order.orderDate));
    setValue("orderStatus", order.status || "-");

    // Customer information
    const customer = order.customer || {};

    setValue("customerId", customer.id ?? "-");
    setValue("customerName", customer.name || "-");
    setValue("customerEmail", customer.email || "-");
    setValue("customerPhone", customer.phone || "-");

    // Order totals
    setValue("subtotal", formatCurrency(order.subtotal));
    setValue("taxRate", `${order.taxRate ?? 0}%`);
    setValue("taxAmount", formatCurrency(order.taxAmount));
    setValue("totalAmount", formatCurrency(order.totalAmount));

    // Order items
    const tableBody = document.getElementById("orderItemsTableBody");

    if (!tableBody) {
        console.error("orderItemsTableBody not found.");
        return;
    }

    tableBody.replaceChildren();

    const items = Array.isArray(order.items) ? order.items : [];

    if (items.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="6" class="empty-message">
                    No items found.
                </td>
            </tr>
        `;
        return;
    }

    items.forEach(orderItem => {
        const item = orderItem.item || {};
        const quantity = Number(orderItem.quantity || 0);
        const price = Number(orderItem.sellingPrice || 0);
        const total = quantity * price;

        const row = document.createElement("tr");

        [
            item.id ?? "-",
            item.name || "-",
            item.sku || "-",
            quantity,
            formatCurrency(price),
            formatCurrency(total)
        ].forEach(value => {
            const cell = document.createElement("td");
            cell.textContent = String(value);
            row.appendChild(cell);
        });

        tableBody.appendChild(row);
    });
}

// =====================================================
// LOAD PACKAGES
// =====================================================

async function loadPackages() {
    const tableBody = document.getElementById("packagesTableBody");

    if (!tableBody) {
        console.error("packagesTableBody not found.");
        return;
    }

    try {
        const response = await fetch(packagesApi);

        if (!response.ok) {
            throw new Error("Failed to load packages.");
        }

        const packages = await response.json();

        console.log("Packages:", packages);

        tableBody.replaceChildren();

        if (!Array.isArray(packages) || packages.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="7" class="empty-message">
                        No packages created for this Sales Order.
                    </td>
                </tr>
            `;
            updateShipButton();
            return;
        }

        packages.forEach(pkg => {
            const row = document.createElement("tr");
            const canShip = pkg.status === "PACKED";

            // Selection checkbox
            const selectCell = document.createElement("td");
            const checkbox = document.createElement("input");

            checkbox.type = "checkbox";
            checkbox.className = "package-checkbox";
            checkbox.value = pkg.id;
            checkbox.disabled = !canShip;
            checkbox.addEventListener("change", updateShipButton);

            selectCell.appendChild(checkbox);
            row.appendChild(selectCell);

            // Package number
            const packageCell = document.createElement("td");
            const packageStrong = document.createElement("strong");

            packageStrong.textContent = pkg.packageNumber || `#${pkg.id}`;
            packageCell.appendChild(packageStrong);
            row.appendChild(packageCell);

            // Status
            const statusCell = document.createElement("td");
            const statusBadge = document.createElement("span");

            statusBadge.className = "status-badge";
            statusBadge.textContent = pkg.status || "-";
            statusCell.appendChild(statusBadge);
            row.appendChild(statusCell);

            // Weight
            const weightCell = document.createElement("td");
            weightCell.textContent = `${Number(pkg.weight || 0).toFixed(2)} kg`;
            row.appendChild(weightCell);

            // Dimensions
            const dimensionsCell = document.createElement("td");
            dimensionsCell.textContent =
                `${Number(pkg.length || 0)} × ` +
                `${Number(pkg.width || 0)} × ` +
                `${Number(pkg.height || 0)} cm`;

            row.appendChild(dimensionsCell);

            // Package date
            const dateCell = document.createElement("td");
            dateCell.textContent = formatDate(pkg.packageDate);
            row.appendChild(dateCell);

            // View action
            const actionCell = document.createElement("td");
            const viewLink = document.createElement("a");

            viewLink.className = "action-button";
            viewLink.href = `/erpflow/packageDetails.jsp?id=${encodeURIComponent(pkg.id)}`;
            viewLink.textContent = "View";

            actionCell.appendChild(viewLink);
            row.appendChild(actionCell);

            tableBody.appendChild(row);
        });

        updateShipButton();
    } catch (error) {
        console.error("Package Error:", error);

        tableBody.innerHTML = `
            <tr>
                <td colspan="7" class="empty-message">
                    Failed to load packages.
                </td>
            </tr>
        `;

        showMessage(error.message || "Failed to load packages.", "error");
    }
}

// =====================================================
// PACKAGE SELECTION
// =====================================================

function setupPackageSelection() {
    document.querySelectorAll(".package-checkbox").forEach(checkbox => {
        checkbox.removeEventListener("change", updateShipButton);
        checkbox.addEventListener("change", updateShipButton);
    });

    updateShipButton();
}

function getSelectedPackageIds() {
    return Array.from(
        document.querySelectorAll(".package-checkbox:checked")
    ).map(checkbox => Number(checkbox.value));
}

function updateShipButton() {
    const selectedIds = getSelectedPackageIds();
    const shipButton = document.getElementById("shipSelectedButton");

    if (!shipButton) {
        return;
    }

    shipButton.disabled = selectedIds.length === 0;

    shipButton.textContent = selectedIds.length > 0
        ? `Ship Selected Packages (${selectedIds.length})`
        : "Ship Selected Packages";

    if (shipButton.dataset.listenerAttached !== "true") {
        shipButton.addEventListener("click", openShippingSection);
        shipButton.dataset.listenerAttached = "true";
    }
}

// =====================================================
// OPEN SHIPPING SECTION
// =====================================================

function openShippingSection() {
    const selectedIds = getSelectedPackageIds();

    if (selectedIds.length === 0) {
        showMessage("Select at least one package.", "error");
        return;
    }

    const shippingSection = document.getElementById("shippingSection");

    if (!shippingSection) {
        console.error("shippingSection not found.");
        return;
    }

    shippingSection.style.display = "block";

    displaySelectedPackages(selectedIds);
    loadCarriers();

    shippingSection.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}

// =====================================================
// DISPLAY SELECTED PACKAGES
// =====================================================

function displaySelectedPackages(selectedIds) {
    const container = document.getElementById("selectedPackages");

    if (!container) {
        return;
    }

    container.replaceChildren();

    selectedIds.forEach(id => {
        const badge = document.createElement("span");

        badge.style.display = "inline-block";
        badge.style.padding = "6px 10px";
        badge.style.margin = "4px";
        badge.style.background = "#e5e7eb";
        badge.style.borderRadius = "5px";
        badge.textContent = `Package #${id}`;

        container.appendChild(badge);
    });
}

// =====================================================
// LOAD CARRIERS
// =====================================================

async function loadCarriers() {
    const carrierSelect = document.getElementById("carrierSelect");

    if (!carrierSelect) {
        return;
    }

    try {
        const response = await fetch(carriersApi);

        if (!response.ok) {
            throw new Error("Failed to load carriers.");
        }

        const carriers = await response.json();

        carrierSelect.replaceChildren();

        const placeholder = document.createElement("option");
        placeholder.value = "";
        placeholder.textContent = "Select Carrier";
        carrierSelect.appendChild(placeholder);

        (Array.isArray(carriers) ? carriers : []).forEach(carrier => {
            // Ignore inactive carriers.
            if (carrier.status && carrier.status !== "ACTIVE") {
                return;
            }

            const option = document.createElement("option");
            option.value = carrier.id;
            option.textContent = `${carrier.name} (${carrier.code})`;

            carrierSelect.appendChild(option);
        });

        if (carrierSelect.dataset.listenerAttached !== "true") {
            carrierSelect.addEventListener("change", loadCarrierServices);
            carrierSelect.dataset.listenerAttached = "true";
        }
    } catch (error) {
        console.error("Carrier Error:", error);
        showMessage(error.message || "Failed to load carriers.", "error");
    }
}

// =====================================================
// LOAD CARRIER SERVICES
// =====================================================

async function loadCarrierServices() {
    const carrierSelect = document.getElementById("carrierSelect");
    const serviceSelect = document.getElementById("serviceSelect");
    const createButton = document.getElementById("createShipmentButton");

    if (!carrierSelect || !serviceSelect) {
        return;
    }

    const carrierId = carrierSelect.value;

    serviceSelect.replaceChildren();

    const placeholder = document.createElement("option");
    placeholder.value = "";
    placeholder.textContent = "Select Service";
    serviceSelect.appendChild(placeholder);

    clearShippingRate();

    if (createButton) {
        createButton.disabled = true;
    }

    if (!carrierId) {
        return;
    }

    try {
        const response = await fetch(
            `${carriersApi}/${encodeURIComponent(carrierId)}/services`
        );

        if (!response.ok) {
            throw new Error("Failed to load carrier services.");
        }

        const services = await response.json();

        (Array.isArray(services) ? services : []).forEach(service => {
            const option = document.createElement("option");

            option.value = service.id;
            option.textContent = `${service.name} - ${service.estimatedDays} days`;

            serviceSelect.appendChild(option);
        });
    } catch (error) {
        console.error("Service Error:", error);
        showMessage(error.message || "Failed to load carrier services.", "error");
    }
}

// =====================================================
// CALCULATE SHIPPING RATE
// =====================================================

async function calculateShippingRate() {
    const packageIds = getSelectedPackageIds();
    const serviceSelect = document.getElementById("serviceSelect");
    const carrierServiceId = Number(serviceSelect?.value || 0);
    const createButton = document.getElementById("createShipmentButton");

    if (packageIds.length === 0) {
        showMessage("Select at least one package.", "error");
        return;
    }

    if (!carrierServiceId) {
        showMessage("Select a carrier service.", "error");
        return;
    }

    if (createButton) {
        createButton.disabled = true;
    }

    try {
        const response = await fetch(`${shippingApi}/rate`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                packageIds: packageIds,
                carrierServiceId: carrierServiceId
            })
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => null);

            throw new Error(
                errorData?.message ||
                errorData?.error ||
                "Failed to calculate shipping rate."
            );
        }

        const rate = await response.json();

        console.log("Shipping Rate:", rate);

        displayShippingRate(rate);

        if (createButton) {
            createButton.disabled = false;
        }

        showMessage("Shipping rate calculated successfully.", "success");
    } catch (error) {
        console.error("Shipping Rate Error:", error);

        clearShippingRate();

        if (createButton) {
            createButton.disabled = true;
        }

        showMessage(
            error.message || "Failed to calculate shipping rate.",
            "error"
        );
    }
}

// =====================================================
// DISPLAY SHIPPING RATE
// =====================================================

function displayShippingRate(rate) {
    setValue("actualWeight", `${Number(rate.actualWeight || 0).toFixed(2)} kg`);
    setValue(
        "dimensionalWeight",
        `${Number(rate.dimensionalWeight || 0).toFixed(2)} kg`
    );
    setValue(
        "chargeableWeight",
        `${Number(rate.chargeableWeight || 0).toFixed(2)} kg`
    );

    setValue("baseCharge", formatCurrency(rate.baseCharge));
    setValue("chargePerKg", formatCurrency(rate.chargePerKg));
    setValue("totalShippingCharge", formatCurrency(rate.totalCharge));
}

// =====================================================
// CLEAR SHIPPING RATE
// =====================================================

function clearShippingRate() {
    setValue("actualWeight", "-");
    setValue("dimensionalWeight", "-");
    setValue("chargeableWeight", "-");
    setValue("baseCharge", "-");
    setValue("chargePerKg", "-");
    setValue("totalShippingCharge", "-");
}

// =====================================================
// CREATE SHIPMENT
// =====================================================

async function createShipment() {
    const packageIds = getSelectedPackageIds();

    if (packageIds.length === 0) {
        showMessage("Select at least one package.", "error");
        return;
    }

    const carrierServiceId = Number(
        document.getElementById("serviceSelect")?.value || 0
    );

    if (!carrierServiceId) {
        showMessage("Select a carrier service.", "error");
        return;
    }

    const trackingNumber =
        document.getElementById("trackingNumber")?.value.trim() || "";

    const trackingUrl =
        document.getElementById("trackingUrl")?.value.trim() || "";

    const dispatchAddress =
        document.getElementById("dispatchAddress")?.value.trim() || "";

    const destinationAddress =
        document.getElementById("destinationAddress")?.value.trim() || "";

    const notes =
        document.getElementById("shipmentNotes")?.value.trim() || "";

    const createButton = document.getElementById("createShipmentButton");

    const shipment = {
        salesOrderId: Number(salesOrderId),
        packageIds: packageIds,
        carrierServiceId: carrierServiceId,
        shippingMethod: "CARRIER",
        trackingNumber: trackingNumber,
        trackingUrl: trackingUrl,
        dispatchAddress: dispatchAddress,
        destinationAddress: destinationAddress,
        notes: notes
    };

    try {
        if (createButton) {
            createButton.disabled = true;
            createButton.textContent = "Creating Shipment...";
        }

        console.log("Creating Shipment:", shipment);

        const response = await fetch(shippingApi, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(shipment)
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => null);

            throw new Error(
                errorData?.message ||
                errorData?.error ||
                "Failed to create shipment."
            );
        }

        const result = await response.json();

        console.log("Shipment Created:", result);

        showMessage("Shipment created successfully.", "success");

        // Reload packages so their statuses are refreshed.
        await loadPackages();

        // Hide the shipment section.
        const shippingSection = document.getElementById("shippingSection");

        if (shippingSection) {
            shippingSection.style.display = "none";
        }

        // Redirect back to the sales order details page.
        const shipmentId = result.shipmentId || result.id;

        if (shipmentId) {
            setTimeout(() => {
                window.location.href =
                    `/erpflow/salesOrderDetails.jsp?id=${encodeURIComponent(salesOrderId)}`;
            }, 800);
        } else if (createButton) {
            createButton.textContent = "Create Shipment";
        }
    } catch (error) {
        console.error("Create Shipment Error:", error);

        showMessage(error.message || "Failed to create shipment.", "error");

        if (createButton) {
            createButton.disabled = false;
            createButton.textContent = "Create Shipment";
        }
    }
}

// =====================================================
// SAFE VALUE SETTER
// =====================================================

function setValue(id, value) {
    const element = document.getElementById(id);

    if (!element) {
        console.error(`Element #${id} not found in JSP.`);
        return;
    }

    const safeValue = value ?? "-";

    if ("value" in element) {
        element.value = String(safeValue);
    } else {
        element.textContent = String(safeValue);
    }
}

// =====================================================
// DATE FORMAT
// =====================================================

function formatDate(value) {
    if (!value) {
        return "-";
    }

    // Jackson LocalDate / LocalDateTime array:
    // [year, month, day, hour, minute, second, ...]
    if (Array.isArray(value)) {
        const year = value[0];
        const month = String(value[1] ?? 1).padStart(2, "0");
        const day = String(value[2] ?? 1).padStart(2, "0");

        let result = `${year}-${month}-${day}`;

        if (value.length >= 5) {
            const hour = String(value[3] ?? 0).padStart(2, "0");
            const minute = String(value[4] ?? 0).padStart(2, "0");

            result += ` ${hour}:${minute}`;
        }

        return result;
    }

    // ISO date strings and other string representations.
    if (typeof value === "string") {
        return value.replace("T", " ").slice(0, 16);
    }

    return String(value);
}

// =====================================================
// CURRENCY
// =====================================================

function formatCurrency(value) {
    const amount = Number(value || 0);

    return amount.toLocaleString("en-IN", {
        style: "currency",
        currency: "INR"
    });
}

// =====================================================
// MESSAGE
// =====================================================

function showMessage(message, type = "info") {
    const messageBox = document.getElementById("messageBox");

    if (!messageBox) {
        console.log(`[${type}] ${message}`);
        return;
    }

    const messageElement = document.createElement("div");

    messageElement.style.padding = "12px";
    messageElement.style.marginBottom = "20px";
    messageElement.style.borderRadius = "6px";

    if (type === "error") {
        messageElement.style.background = "#fee2e2";
        messageElement.style.color = "#991b1b";
    } else if (type === "success") {
        messageElement.style.background = "#dcfce7";
        messageElement.style.color = "#166534";
    } else {
        messageElement.style.background = "#e0f2fe";
        messageElement.style.color = "#075985";
    }

    messageElement.textContent = message;
    messageBox.replaceChildren(messageElement);

    // Automatically remove the message after four seconds.
    setTimeout(() => {
        if (messageBox.contains(messageElement)) {
            messageBox.removeChild(messageElement);
        }
    }, 4000);
}