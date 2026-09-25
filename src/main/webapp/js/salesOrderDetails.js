"use strict";

// =====================================================
// SALES ORDER DETAILS
// =====================================================

const params = new URLSearchParams(window.location.search);
const salesOrderId = params.get("id");

// =====================================================
// API URLS
// =====================================================

const salesOrderApi =
    `/erpflow/api/sales-orders/${salesOrderId}`;

const packagesApi =
    `/erpflow/api/packages?salesOrderId=${salesOrderId}`;

const packagesBaseApi =
    "/erpflow/api/packages";

const carriersApi =
    "/erpflow/api/carriers";

const shippingApi =
    "/erpflow/api/shipping";

const autoPackShipApi =
    `/erpflow/api/auto-pack-ship/${salesOrderId}`;

// Current Sales Order object
let currentSalesOrder = null;

// =====================================================
// PAGE LOAD
// =====================================================

document.addEventListener("DOMContentLoaded", () => {

    if (!salesOrderId) {
        showMessage(
            "Sales Order ID is missing.",
            "error"
        );
        return;
    }

    loadSalesOrder();
    loadPackages();

    createPackageButton();

    const rateButton =
        document.getElementById(
            "calculateRateButton"
        );

    if (rateButton) {
        rateButton.addEventListener(
            "click",
            calculateShippingRate
        );
    }

    const createShipmentButton =
        document.getElementById(
            "createShipmentButton"
        );

    if (createShipmentButton) {
        createShipmentButton.addEventListener(
            "click",
            createShipment
        );
    }

    const packShipButton =
        document.getElementById(
            "packShipButton"
        );

    if (packShipButton) {
        packShipButton.addEventListener(
            "click",
            packAndShip
        );
    }

    // Default shipment date
    const shipmentDateInput =
        document.getElementById(
            "shipmentDate"
        );

    if (
        shipmentDateInput &&
        !shipmentDateInput.value
    ) {
        const now = new Date();

        const localDate =
            new Date(
                now.getTime() -
                now.getTimezoneOffset() * 60000
            )
                .toISOString()
                .slice(0, 10);

        shipmentDateInput.value =
            localDate;
    }
});

// =====================================================
// LOAD SALES ORDER
// =====================================================

async function loadSalesOrder() {

    try {

        const response =
            await fetch(salesOrderApi);

        if (!response.ok) {
            throw new Error(
                "Failed to load Sales Order."
            );
        }

        const order =
            await response.json();

        currentSalesOrder = order;

        console.log(
            "Sales Order:",
            order
        );

        displaySalesOrder(order);

    } catch (error) {

        console.error(
            "Sales Order Error:",
            error
        );

        showMessage(
            error.message ||
            "Failed to load Sales Order.",
            "error"
        );
    }
}

// =====================================================
// DISPLAY SALES ORDER
// =====================================================

function displaySalesOrder(order) {

    setValue(
        "orderId",
        order.id != null
            ? `#${order.id}`
            : "-"
    );

    setValue(
        "orderDate",
        formatDate(order.orderDate)
    );
    setValue(
        "expectedDeliveryDate",
        formatDate(
            order.expectedDeliveryDate
        )
    );

    setValue(
        "orderStatus",
        order.status || "-"
    );

    const customer =
        order.customer || {};

    setValue(
        "customerId",
        customer.id ?? "-"
    );

    setValue(
        "customerName",
        customer.name || "-"
    );

    setValue(
        "customerEmail",
        customer.email || "-"
    );

    setValue(
        "customerPhone",
        customer.phone || "-"
    );

    setValue(
        "subtotal",
        formatCurrency(order.subtotal)
    );

    setValue(
        "taxRate",
        `${order.taxRate ?? 0}%`
    );

    setValue(
        "taxAmount",
        formatCurrency(order.taxAmount)
    );

    setValue(
        "totalAmount",
        formatCurrency(order.totalAmount)
    );

    const customerAddress =
        customer.address || "";

    const destinationInput =
        document.getElementById(
            "destinationAddress"
        );

    if (
        destinationInput &&
        !destinationInput.value
    ) {
        destinationInput.value =
            customerAddress;
    }

    const tableBody =
        document.getElementById(
            "orderItemsTableBody"
        );

    if (!tableBody) {
        console.error(
            "orderItemsTableBody not found."
        );
        return;
    }

    tableBody.replaceChildren();

    const items =
        Array.isArray(order.items)
            ? order.items
            : [];

    if (items.length === 0) {

        tableBody.innerHTML = `
            <tr>
                <td
                    colspan="6"
                    class="empty-message"
                >
                    No items found.
                </td>
            </tr>
        `;

        return;
    }

    items.forEach(orderItem => {

        const item =
            orderItem.item || {};

        const quantity =
            Number(
                orderItem.quantity || 0
            );

        const price =
            Number(
                orderItem.sellingPrice || 0
            );

        const row =
            document.createElement("tr");

        [
            item.id ?? "-",
            item.name || "-",
            item.sku || "-",
            quantity,
            formatCurrency(price),
            formatCurrency(
                quantity * price
            )
        ].forEach(value => {

            const cell =
                document.createElement("td");

            cell.textContent =
                String(value);

            row.appendChild(cell);
        });

        tableBody.appendChild(row);
    });
}

// =====================================================
// CREATE PACKAGE BUTTON
// =====================================================

function createPackageButton() {

    if (
        document.getElementById(
            "salesOrderCreatePackageButton"
        )
    ) {
        return;
    }

    const tableBody =
        document.getElementById(
            "packagesTableBody"
        );

    if (!tableBody) {
        return;
    }

    /*
     * Find the section containing the package table.
     */
    let packageSection =
        tableBody.closest(
            "section"
        );

    if (!packageSection) {
        packageSection =
            tableBody.parentElement?.parentElement;
    }

    if (!packageSection) {
        return;
    }

    const button =
        document.createElement("button");

    button.type = "button";
    button.id =
        "salesOrderCreatePackageButton";

    button.className =
        "action-button";

    button.textContent =
        "Create Package";

    button.style.marginBottom =
        "15px";

    button.addEventListener(
        "click",
        openCreatePackageModal
    );

    packageSection.insertBefore(
        button,
        packageSection.firstChild
    );

    createPackageModal();
}

// =====================================================
// CREATE PACKAGE MODAL HTML
// =====================================================

function createPackageModal() {

    if (
        document.getElementById(
            "salesOrderCreatePackageModal"
        )
    ) {
        return;
    }

    const modal =
        document.createElement("div");

    modal.id =
        "salesOrderCreatePackageModal";

    modal.style.display = "none";
    modal.style.position = "fixed";
    modal.style.inset = "0";
    modal.style.background =
        "rgba(0,0,0,0.5)";
    modal.style.zIndex = "9999";
    modal.style.alignItems = "center";
    modal.style.justifyContent = "center";
    modal.style.padding = "20px";

    modal.innerHTML = `

        <div
            style="
                background:white;
                width:100%;
                max-width:800px;
                max-height:90vh;
                overflow-y:auto;
                border-radius:10px;
                padding:24px;
                box-sizing:border-box;
            "
        >

            <div
                style="
                    display:flex;
                    justify-content:space-between;
                    align-items:center;
                    margin-bottom:20px;
                "
            >

                <h2
                    style="
                        margin:0;
                    "
                >
                    Create Package
                </h2>

                <button
                    type="button"
                    id="closeSalesOrderPackageModal"
                    style="
                        border:none;
                        background:none;
                        font-size:24px;
                        cursor:pointer;
                    "
                >
                    ×
                </button>

            </div>

            <div
                id="salesOrderPackageError"
                style="
                    display:none;
                    background:#fee2e2;
                    color:#991b1b;
                    padding:10px;
                    border-radius:6px;
                    margin-bottom:15px;
                "
            ></div>

            <div
                style="
                    background:#f3f4f6;
                    padding:10px;
                    border-radius:6px;
                    margin-bottom:20px;
                "
            >
                <strong>
                    Sales Order:
                </strong>

                #${escapeHtml(
        String(salesOrderId)
    )}
            </div>

            <form
                id="salesOrderCreatePackageForm"
            >

                <h3>
                    Items
                </h3>

                <div
                    id="salesOrderPackageItems"
                    style="
                        margin-bottom:20px;
                    "
                >
                    Loading items...
                </div>

                <h3>
                    Package Details
                </h3>

                <div
                    style="
                        display:grid;
                        grid-template-columns:
                            repeat(2, 1fr);
                        gap:15px;
                        margin-bottom:20px;
                    "
                >

                    <div>

                        <label>
                            Weight (kg)
                        </label>

                        <input
                            id="salesOrderPackageWeight"
                            type="number"
                            step="0.01"
                            min="0.01"
                            required
                            style="
                                width:100%;
                                box-sizing:border-box;
                            "
                        >

                    </div>

                    <div>

                        <label>
                            Length (cm)
                        </label>

                        <input
                            id="salesOrderPackageLength"
                            type="number"
                            step="0.01"
                            min="0.01"
                            required
                            style="
                                width:100%;
                                box-sizing:border-box;
                            "
                        >

                    </div>

                    <div>

                        <label>
                            Width (cm)
                        </label>

                        <input
                            id="salesOrderPackageWidth"
                            type="number"
                            step="0.01"
                            min="0.01"
                            required
                            style="
                                width:100%;
                                box-sizing:border-box;
                            "
                        >

                    </div>

                    <div>

                        <label>
                            Height (cm)
                        </label>

                        <input
                            id="salesOrderPackageHeight"
                            type="number"
                            step="0.01"
                            min="0.01"
                            required
                            style="
                                width:100%;
                                box-sizing:border-box;
                            "
                        >

                    </div>

                </div>

                <div
                    style="
                        display:flex;
                        justify-content:flex-end;
                        gap:10px;
                    "
                >

                    <button
                        type="button"
                        id="cancelSalesOrderPackage"
                    >
                        Cancel
                    </button>

                    <button
                        type="submit"
                        id="submitSalesOrderPackage"
                    >
                        Create Package
                    </button>

                </div>

            </form>

        </div>
    `;

    document.body.appendChild(modal);

    document
        .getElementById(
            "closeSalesOrderPackageModal"
        )
        ?.addEventListener(
            "click",
            closeCreatePackageModal
        );

    document
        .getElementById(
            "cancelSalesOrderPackage"
        )
        ?.addEventListener(
            "click",
            closeCreatePackageModal
        );

    document
        .getElementById(
            "salesOrderCreatePackageForm"
        )
        ?.addEventListener(
            "submit",
            submitSalesOrderPackage
        );
}

// =====================================================
// OPEN CREATE PACKAGE MODAL
// =====================================================

async function openCreatePackageModal() {

    const modal =
        document.getElementById(
            "salesOrderCreatePackageModal"
        );

    if (!modal) {
        createPackageModal();
        return openCreatePackageModal();
    }

    modal.style.display = "flex";

    clearPackageError();

    const container =
        document.getElementById(
            "salesOrderPackageItems"
        );

    if (!container) {
        return;
    }

    container.innerHTML = `
        <p>
            Loading items...
        </p>
    `;

    try {

        /*
         * Always fetch the CURRENT Sales Order.
         */
        const orderResponse =
            await fetch(
                salesOrderApi
            );

        if (!orderResponse.ok) {
            throw new Error(
                "Failed to load Sales Order items."
            );
        }

        const order =
            await orderResponse.json();

        /*
         * IMPORTANT:
         *
         * Only packages belonging to
         * THIS Sales Order are fetched.
         */
        const packageResponse =
            await fetch(
                packagesApi
            );

        if (!packageResponse.ok) {
            throw new Error(
                "Failed to load existing packages."
            );
        }

        const packages =
            await packageResponse.json();

        renderCreatePackageItems(
            order,
            packages
        );

    } catch (error) {

        console.error(
            "Create Package Load Error:",
            error
        );

        container.innerHTML = `
            <p
                style="
                    color:#991b1b;
                "
            >
                ${escapeHtml(
            error.message ||
            "Failed to load package items."
        )}
            </p>
        `;
    }
}

// =====================================================
// CLOSE CREATE PACKAGE MODAL
// =====================================================

function closeCreatePackageModal() {

    const modal =
        document.getElementById(
            "salesOrderCreatePackageModal"
        );

    if (modal) {
        modal.style.display = "none";
    }
}

// =====================================================
// RENDER PACKAGE ITEMS
// =====================================================

function renderCreatePackageItems(
    order,
    packages
) {

    const container =
        document.getElementById(
            "salesOrderPackageItems"
        );

    if (!container) {
        return;
    }

    const orderItems =
        Array.isArray(order.items)
            ? order.items
            : [];

    /*
     * Only GOODS + inventory tracked.
     */
    const packageableItems =
        orderItems.filter(orderItem => {

            const item =
                orderItem.item || {};

            const itemType =
                String(
                    item.itemType || ""
                ).toUpperCase();

            return (
                itemType === "GOODS" &&
                (
                    item.trackInventory === true ||
                    item.trackInventory === 1 ||
                    item.trackInventory === "true"
                )
            );
        });

    if (
        packageableItems.length === 0
    ) {

        container.innerHTML = `
            <p>
                This Sales Order has no
                inventory-tracked goods to package.
            </p>
        `;

        return;
    }

    /*
     * Calculate packed quantities.
     *
     * PACKED + SHIPPED count.
     * CANCELLED does not count.
     */
    const packedByLine =
        {};

    const currentOrderPackages =
        (
            Array.isArray(packages)
                ? packages
                : []
        ).filter(pkg => {

            const packageOrderId =
                Number(
                    pkg.salesOrder?.id ??
                    pkg.salesOrderId
                );

            const status =
                String(
                    pkg.status || ""
                ).toUpperCase();

            return (
                packageOrderId ===
                Number(salesOrderId) &&
                status !== "CANCELLED"
            );
        });

    currentOrderPackages.forEach(pkg => {

        const packageItems =
            Array.isArray(pkg.items)
                ? pkg.items
                : [];

        packageItems.forEach(
            packageItem => {

                const lineId =
                    Number(
                        packageItem.salesOrderItemId ??
                        packageItem.salesOrderItem?.id
                    );

                if (
                    !Number.isFinite(lineId)
                ) {
                    return;
                }

                const quantity =
                    Number(
                        packageItem.quantity || 0
                    );

                packedByLine[lineId] =
                    (
                        packedByLine[lineId] || 0
                    ) + quantity;
            }
        );
    });

    container.innerHTML =
        packageableItems
            .map(
                (orderItem, index) => {

                    const item =
                        orderItem.item || {};

                    const lineId =
                        orderItem.id ??
                        orderItem.salesOrderItemId;

                    const itemId =
                        item.id ??
                        orderItem.itemId;

                    const ordered =
                        Number(
                            orderItem.quantity || 0
                        );

                    const packed =
                        Number(
                            packedByLine[
                            Number(lineId)
                            ] || 0
                        );

                    const remaining =
                        Math.max(
                            0,
                            ordered - packed
                        );

                    return `
                        <div
                            style="
                                display:grid;
                                grid-template-columns:
                                    2fr 1fr 1fr;
                                gap:15px;
                                align-items:center;
                                padding:15px;
                                margin-bottom:10px;
                                border:1px solid #ddd;
                                border-radius:8px;
                            "
                        >

                            <div>

                                <strong>
                                    ${escapeHtml(
                        item.name ||
                        "Unknown Item"
                    )}
                                </strong>

                                <div>
                                    SKU:
                                    ${escapeHtml(
                        item.sku ||
                        "-"
                    )}
                                </div>

                                <div>
                                    Ordered:
                                    ${ordered}
                                </div>

                                <div>
                                    Packed:
                                    ${packed}
                                </div>

                                <div>
                                    Remaining:
                                    <strong>
                                        ${remaining}
                                    </strong>
                                </div>

                            </div>

                            <div>

                                <label>
                                    Package Qty
                                </label>

                                <input
                                    type="number"
                                    class="sales-order-package-quantity"
                                    data-line-id="${escapeHtml(
                        lineId
                    )}"
                                    data-item-id="${escapeHtml(
                        itemId
                    )}"
                                    data-max-quantity="${remaining}"
                                    min="0"
                                    max="${remaining}"
                                    value="0"
                                    step="1"
                                    style="
                                        width:100%;
                                        box-sizing:border-box;
                                    "
                                    ${remaining === 0
                            ? "disabled"
                            : ""
                        }
                                >

                            </div>

                            <div>
                                ${remaining === 0
                            ? `
                                            <span
                                                style="
                                                    color:#991b1b;
                                                "
                                            >
                                                Fully Packed
                                            </span>
                                        `
                            : `
                                            <span
                                                style="
                                                    color:#166534;
                                                "
                                            >
                                                Available
                                            </span>
                                        `
                        }
                            </div>

                        </div>
                    `;
                }
            )
            .join("");
}

// =====================================================
// SUBMIT CREATE PACKAGE
// =====================================================

async function submitSalesOrderPackage(
    event
) {

    event.preventDefault();

    clearPackageError();

    const weight =
        Number(
            document.getElementById(
                "salesOrderPackageWeight"
            )?.value
        );

    const length =
        Number(
            document.getElementById(
                "salesOrderPackageLength"
            )?.value
        );

    const width =
        Number(
            document.getElementById(
                "salesOrderPackageWidth"
            )?.value
        );

    const height =
        Number(
            document.getElementById(
                "salesOrderPackageHeight"
            )?.value
        );

    if (
        !Number.isFinite(weight) ||
        weight <= 0
    ) {

        showPackageError(
            "Weight must be greater than 0."
        );

        return;
    }

    if (
        !Number.isFinite(length) ||
        length <= 0
    ) {

        showPackageError(
            "Length must be greater than 0."
        );

        return;
    }

    if (
        !Number.isFinite(width) ||
        width <= 0
    ) {

        showPackageError(
            "Width must be greater than 0."
        );

        return;
    }

    if (
        !Number.isFinite(height) ||
        height <= 0
    ) {

        showPackageError(
            "Height must be greater than 0."
        );

        return;
    }

    const quantityInputs =
        document.querySelectorAll(
            ".sales-order-package-quantity"
        );

    const items = [];

    for (
        const input of quantityInputs
    ) {

        const quantity =
            Number(input.value);

        const itemId =
            Number(
                input.dataset.itemId
            );

        const lineId =
            Number(
                input.dataset.lineId
            );

        const maxQuantity =
            Number(
                input.dataset.maxQuantity
            );

        if (
            !Number.isSafeInteger(
                quantity
            ) ||
            quantity < 0
        ) {

            showPackageError(
                "Package quantity must be a non-negative whole number."
            );

            return;
        }

        if (
            quantity > maxQuantity
        ) {

            showPackageError(
                `Quantity cannot exceed the remaining quantity (${maxQuantity}).`
            );

            return;
        }

        if (quantity === 0) {
            continue;
        }

        if (
            !Number.isSafeInteger(
                itemId
            ) ||
            itemId <= 0
        ) {

            showPackageError(
                "Invalid item ID."
            );

            return;
        }

        if (
            !Number.isSafeInteger(
                lineId
            ) ||
            lineId <= 0
        ) {

            showPackageError(
                "Invalid Sales Order line ID."
            );

            return;
        }

        items.push({

            itemId,

            salesOrderItemId:
                lineId,

            quantity

        });
    }

    if (items.length === 0) {

        showPackageError(
            "Enter a quantity for at least one item."
        );

        return;
    }

    /*
     * EXACT payload structure used
     * by the existing Packages API.
     */
    const requestBody = {

        salesOrderId:
            Number(salesOrderId),

        weight,

        length,

        width,

        height,

        items
    };

    const submitButton =
        document.getElementById(
            "submitSalesOrderPackage"
        );

    try {

        if (submitButton) {

            submitButton.disabled =
                true;

            submitButton.textContent =
                "Creating...";
        }

        console.log(
            "Creating package:",
            requestBody
        );

        const response =
            await fetch(
                packagesBaseApi,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(
                            requestBody
                        )
                }
            );

        const result =
            await response
                .json()
                .catch(
                    () => ({})
                );

        if (!response.ok) {

            throw new Error(
                result.error ||
                result.message ||
                "Failed to create package."
            );
        }

        showMessage(
            `Package ${result.packageNumber ||
            result.id ||
            ""
            } created successfully.`,
            "success"
        );

        closeCreatePackageModal();

        /*
         * IMPORTANT:
         *
         * Refresh only the packages
         * belonging to THIS Sales Order.
         */
        await loadPackages();

        await loadSalesOrder();

    } catch (error) {

        console.error(
            "Create Package Error:",
            error
        );

        showPackageError(
            error.message ||
            "Failed to create package."
        );

    } finally {

        if (submitButton) {

            submitButton.disabled =
                false;

            submitButton.textContent =
                "Create Package";
        }
    }
}

// =====================================================
// PACKAGE ERROR
// =====================================================

function showPackageError(
    message
) {

    const errorBox =
        document.getElementById(
            "salesOrderPackageError"
        );

    if (!errorBox) {
        return;
    }

    errorBox.textContent =
        message;

    errorBox.style.display =
        "block";
}

function clearPackageError() {

    const errorBox =
        document.getElementById(
            "salesOrderPackageError"
        );

    if (!errorBox) {
        return;
    }

    errorBox.textContent =
        "";

    errorBox.style.display =
        "none";
}

// =====================================================
// LOAD PACKAGES
// =====================================================

async function loadPackages() {

    const tableBody =
        document.getElementById(
            "packagesTableBody"
        );

    if (!tableBody) {
        console.error(
            "packagesTableBody not found."
        );
        return;
    }

    try {

        /*
         * This endpoint already contains:
         *
         * ?salesOrderId=${salesOrderId}
         *
         * so this page never asks for
         * every package in the system.
         */
        const response =
            await fetch(packagesApi);

        if (!response.ok) {

            throw new Error(
                "Failed to load packages."
            );
        }

        const responseData =
            await response.json();

        const allReturnedPackages =
            Array.isArray(responseData)
                ? responseData
                : [];

        /*
         * Extra safety:
         *
         * Even if backend returns more than
         * one Sales Order's packages, only
         * packages belonging to the current
         * Sales Order are displayed.
         */
        const packages =
            allReturnedPackages.filter(
                pkg => {

                    const packageOrderId =
                        Number(
                            pkg.salesOrder?.id ??
                            pkg.salesOrderId
                        );

                    return (
                        packageOrderId ===
                        Number(salesOrderId)
                    );
                }
            );

        console.log(
            "Current Sales Order Packages:",
            packages
        );

        tableBody.replaceChildren();

        if (
            packages.length === 0
        ) {

            tableBody.innerHTML = `
                <tr>
                    <td
                        colspan="7"
                        class="empty-message"
                    >
                        No packages created for this Sales Order.
                    </td>
                </tr>
            `;

            updateShipButton();

            return;
        }

        packages.forEach(pkg => {

            const row =
                document.createElement(
                    "tr"
                );

            const canShip =
                String(
                    pkg.status || ""
                ).toUpperCase() ===
                "PACKED";

            // Checkbox
            const selectCell =
                document.createElement(
                    "td"
                );

            const checkbox =
                document.createElement(
                    "input"
                );

            checkbox.type =
                "checkbox";

            checkbox.className =
                "package-checkbox";

            checkbox.value =
                pkg.id;

            checkbox.disabled =
                !canShip;

            checkbox.addEventListener(
                "change",
                updateShipButton
            );

            selectCell.appendChild(
                checkbox
            );

            row.appendChild(
                selectCell
            );

            // Package number
            const packageCell =
                document.createElement(
                    "td"
                );

            const packageStrong =
                document.createElement(
                    "strong"
                );

            packageStrong.textContent =
                pkg.packageNumber ||
                `#${pkg.id}`;

            packageCell.appendChild(
                packageStrong
            );

            row.appendChild(
                packageCell
            );

            // Status
            const statusCell =
                document.createElement(
                    "td"
                );

            const statusBadge =
                document.createElement(
                    "span"
                );

            statusBadge.className =
                "status-badge";

            statusBadge.textContent =
                pkg.status || "-";

            statusCell.appendChild(
                statusBadge
            );

            row.appendChild(
                statusCell
            );

            // Weight
            const weightCell =
                document.createElement(
                    "td"
                );

            weightCell.textContent =
                `${Number(
                    pkg.weight || 0
                ).toFixed(2)} kg`;

            row.appendChild(
                weightCell
            );

            // Dimensions
            const dimensionsCell =
                document.createElement(
                    "td"
                );

            dimensionsCell.textContent =
                `${Number(
                    pkg.length || 0
                )} × ` +
                `${Number(
                    pkg.width || 0
                )} × ` +
                `${Number(
                    pkg.height || 0
                )} cm`;

            row.appendChild(
                dimensionsCell
            );

            // Date
            const dateCell =
                document.createElement(
                    "td"
                );

            dateCell.textContent =
                formatDate(
                    pkg.packageDate
                );

            row.appendChild(
                dateCell
            );

            // View
            const actionCell =
                document.createElement(
                    "td"
                );

            const viewLink =
                document.createElement(
                    "a"
                );

            viewLink.className =
                "action-button";

            viewLink.href =
                `/erpflow/packageDetails.jsp?id=${encodeURIComponent(
                    pkg.id
                )}`;

            viewLink.textContent =
                "View";

            actionCell.appendChild(
                viewLink
            );

            row.appendChild(
                actionCell
            );

            tableBody.appendChild(
                row
            );
        });

        updateShipButton();

    } catch (error) {

        console.error(
            "Package Error:",
            error
        );

        tableBody.innerHTML = `
            <tr>
                <td
                    colspan="7"
                    class="empty-message"
                >
                    Failed to load packages.
                </td>
            </tr>
        `;

        showMessage(
            error.message ||
            "Failed to load packages.",
            "error"
        );
    }
}

// =====================================================
// PACKAGE SELECTION
// =====================================================

function getSelectedPackageIds() {

    return Array.from(
        document.querySelectorAll(
            ".package-checkbox:checked"
        )
    ).map(
        checkbox =>
            Number(checkbox.value)
    );
}

function updateShipButton() {

    const selectedIds =
        getSelectedPackageIds();

    const shipButton =
        document.getElementById(
            "shipSelectedButton"
        );

    if (!shipButton) {
        return;
    }

    shipButton.disabled =
        selectedIds.length === 0;

    shipButton.textContent =
        selectedIds.length > 0
            ? `Ship Selected Packages (${selectedIds.length})`
            : "Ship Selected Packages";

    if (
        shipButton.dataset
            .listenerAttached !==
        "true"
    ) {

        shipButton.addEventListener(
            "click",
            openShippingSection
        );

        shipButton.dataset
            .listenerAttached =
            "true";
    }
}

// =====================================================
// OPEN SHIPPING SECTION
// =====================================================

function openShippingSection() {

    const selectedIds =
        getSelectedPackageIds();

    if (
        selectedIds.length === 0
    ) {

        showMessage(
            "Select at least one package.",
            "error"
        );

        return;
    }

    const shippingSection =
        document.getElementById(
            "shippingSection"
        );

    if (!shippingSection) {

        console.error(
            "shippingSection not found."
        );

        return;
    }

    shippingSection.style.display =
        "block";

    displaySelectedPackages(
        selectedIds
    );

    loadCarriers();

    shippingSection.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}

// =====================================================
// DISPLAY SELECTED PACKAGES
// =====================================================

function displaySelectedPackages(
    selectedIds
) {

    const container =
        document.getElementById(
            "selectedPackages"
        );

    if (!container) {
        return;
    }

    container.replaceChildren();

    selectedIds.forEach(id => {

        const badge =
            document.createElement(
                "span"
            );

        badge.style.display =
            "inline-block";

        badge.style.padding =
            "6px 10px";

        badge.style.margin =
            "4px";

        badge.style.background =
            "#e5e7eb";

        badge.style.borderRadius =
            "5px";

        badge.textContent =
            `Package #${id}`;

        container.appendChild(
            badge
        );
    });
}

// =====================================================
// LOAD CARRIERS
// =====================================================

async function loadCarriers() {

    const carrierSelect =
        document.getElementById(
            "carrierSelect"
        );

    if (!carrierSelect) {
        return;
    }

    try {

        const response =
            await fetch(carriersApi);

        if (!response.ok) {

            throw new Error(
                "Failed to load carriers."
            );
        }

        const carriers =
            await response.json();

        carrierSelect.replaceChildren();

        const placeholder =
            document.createElement(
                "option"
            );

        placeholder.value =
            "";

        placeholder.textContent =
            "Select Carrier";

        carrierSelect.appendChild(
            placeholder
        );

        (
            Array.isArray(carriers)
                ? carriers
                : []
        ).forEach(carrier => {

            if (
                carrier.status &&
                carrier.status !==
                "ACTIVE"
            ) {
                return;
            }

            const option =
                document.createElement(
                    "option"
                );

            option.value =
                carrier.id;

            option.textContent =
                `${carrier.name} (${carrier.code})`;

            carrierSelect.appendChild(
                option
            );
        });

        if (
            carrierSelect.dataset
                .listenerAttached !==
            "true"
        ) {

            carrierSelect.addEventListener(
                "change",
                loadCarrierServices
            );

            carrierSelect.dataset
                .listenerAttached =
                "true";
        }

    } catch (error) {

        console.error(
            "Carrier Error:",
            error
        );

        showMessage(
            error.message ||
            "Failed to load carriers.",
            "error"
        );
    }
}

// =====================================================
// LOAD CARRIER SERVICES
// =====================================================

async function loadCarrierServices() {

    const carrierSelect =
        document.getElementById(
            "carrierSelect"
        );

    const serviceSelect =
        document.getElementById(
            "serviceSelect"
        );

    const createButton =
        document.getElementById(
            "createShipmentButton"
        );

    if (
        !carrierSelect ||
        !serviceSelect
    ) {
        return;
    }

    const carrierId =
        carrierSelect.value;

    serviceSelect.replaceChildren();

    const placeholder =
        document.createElement(
            "option"
        );

    placeholder.value =
        "";

    placeholder.textContent =
        "Select Service";

    serviceSelect.appendChild(
        placeholder
    );

    clearShippingRate();

    if (createButton) {
        createButton.disabled =
            true;
    }

    if (!carrierId) {
        return;
    }

    try {

        const response =
            await fetch(
                `${carriersApi}/${encodeURIComponent(
                    carrierId
                )}/services`
            );

        if (!response.ok) {

            throw new Error(
                "Failed to load carrier services."
            );
        }

        const services =
            await response.json();

        (
            Array.isArray(services)
                ? services
                : []
        ).forEach(service => {

            const option =
                document.createElement(
                    "option"
                );

            option.value =
                service.id;

            option.textContent =
                `${service.name} - ${service.estimatedDays} days`;

            serviceSelect.appendChild(
                option
            );
        });

    } catch (error) {

        console.error(
            "Service Error:",
            error
        );

        showMessage(
            error.message ||
            "Failed to load carrier services.",
            "error"
        );
    }
}

// =====================================================
// CALCULATE SHIPPING RATE
// =====================================================

async function calculateShippingRate() {

    const packageIds =
        getSelectedPackageIds();

    const serviceSelect =
        document.getElementById(
            "serviceSelect"
        );

    const carrierServiceId =
        Number(
            serviceSelect?.value || 0
        );

    const createButton =
        document.getElementById(
            "createShipmentButton"
        );

    if (
        packageIds.length === 0
    ) {

        showMessage(
            "Select at least one package.",
            "error"
        );

        return;
    }

    if (!carrierServiceId) {

        showMessage(
            "Select a carrier service.",
            "error"
        );

        return;
    }

    if (createButton) {
        createButton.disabled =
            true;
    }

    try {

        const response =
            await fetch(
                `${shippingApi}/rate`,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify({
                            packageIds,
                            carrierServiceId
                        })
                }
            );

        if (!response.ok) {

            const errorData =
                await response
                    .json()
                    .catch(
                        () => null
                    );

            throw new Error(
                errorData?.message ||
                errorData?.error ||
                "Failed to calculate shipping rate."
            );
        }

        const rate =
            await response.json();

        displayShippingRate(
            rate
        );

        if (createButton) {
            createButton.disabled =
                false;
        }

        showMessage(
            "Shipping rate calculated successfully.",
            "success"
        );

    } catch (error) {

        console.error(
            "Shipping Rate Error:",
            error
        );

        clearShippingRate();

        if (createButton) {
            createButton.disabled =
                true;
        }

        showMessage(
            error.message ||
            "Failed to calculate shipping rate.",
            "error"
        );
    }
}

// =====================================================
// DISPLAY SHIPPING RATE
// =====================================================

function displayShippingRate(
    rate
) {

    setValue(
        "actualWeight",
        `${Number(
            rate.actualWeight || 0
        ).toFixed(2)} kg`
    );

    setValue(
        "dimensionalWeight",
        `${Number(
            rate.dimensionalWeight || 0
        ).toFixed(2)} kg`
    );

    setValue(
        "chargeableWeight",
        `${Number(
            rate.chargeableWeight || 0
        ).toFixed(2)} kg`
    );

    setValue(
        "baseCharge",
        formatCurrency(
            rate.baseCharge
        )
    );

    setValue(
        "chargePerKg",
        formatCurrency(
            rate.chargePerKg
        )
    );

    setValue(
        "totalShippingCharge",
        formatCurrency(
            rate.totalCharge
        )
    );
}

// =====================================================
// CLEAR SHIPPING RATE
// =====================================================

function clearShippingRate() {

    setValue(
        "actualWeight",
        "-"
    );

    setValue(
        "dimensionalWeight",
        "-"
    );

    setValue(
        "chargeableWeight",
        "-"
    );

    setValue(
        "baseCharge",
        "-"
    );

    setValue(
        "chargePerKg",
        "-"
    );

    setValue(
        "totalShippingCharge",
        "-"
    );
}

// =====================================================
// CREATE MANUAL SHIPMENT
// =====================================================

async function createShipment() {

    const packageIds =
        getSelectedPackageIds();

    if (
        packageIds.length === 0
    ) {

        showMessage(
            "Select at least one package.",
            "error"
        );

        return;
    }
    const shipmentDate =
        document.getElementById("shipmentDate")?.value || "";

    if (!shipmentDate) {
        showMessage(
            "Please select a shipment date.",
            "error"
        );
        return;
    }

    if (!/^\d{4}-\d{2}-\d{2}$/.test(shipmentDate)) {
        showMessage(
            "Shipment date must be in YYYY-MM-DD format.",
            "error"
        );
        return;
    }

    const carrierServiceId =
        Number(
            document.getElementById(
                "serviceSelect"
            )?.value || 0
        );

    if (!carrierServiceId) {

        showMessage(
            "Select a carrier service.",
            "error"
        );

        return;
    }

    const trackingNumber =
        document.getElementById(
            "trackingNumber"
        )?.value.trim() || "";

    const trackingUrl =
        document.getElementById(
            "trackingUrl"
        )?.value.trim() || "";

    const dispatchAddress =
        document.getElementById(
            "dispatchAddress"
        )?.value.trim() || "";

    const destinationAddress =
        document.getElementById(
            "destinationAddress"
        )?.value.trim() || "";

    const notes =
        document.getElementById(
            "shipmentNotes"
        )?.value.trim() || "";

    const createButton =
        document.getElementById(
            "createShipmentButton"
        );

    

    const shipment = {

        salesOrderId:
            Number(salesOrderId),

        packageIds,

        carrierServiceId,
        shipmentDate : shipmentDate,

        shippingMethod:
            "CARRIER",

        trackingNumber,

        trackingUrl,

        dispatchAddress,

        destinationAddress,

        notes,
        
    };

    try {

        if (createButton) {

            createButton.disabled =
                true;

            createButton.textContent =
                "Creating Shipment...";
        }

        const response =
            await fetch(
                shippingApi,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(
                            shipment
                        )
                }
            );

        if (!response.ok) {

            const errorData =
                await response
                    .json()
                    .catch(
                        () => null
                    );

            throw new Error(
                errorData?.message ||
                errorData?.error ||
                "Failed to create shipment."
            );
        }

        const result =
            await response.json();

        showMessage(
            "Shipment created successfully.",
            "success"
        );

        await loadPackages();

        const shippingSection =
            document.getElementById(
                "shippingSection"
            );

        if (shippingSection) {
            shippingSection.style.display =
                "none";
        }

        const shipmentId =
            result.shipmentId ||
            result.id;

        if (shipmentId) {

            window.location.href =
                `/erpflow/salesOrderDetails.jsp?id=${encodeURIComponent(
                    salesOrderId
                )}`;

        } else if (createButton) {

            createButton.textContent =
                "Create Shipment";
        }

    } catch (error) {

        console.error(
            "Create Shipment Error:",
            error
        );

        showMessage(
            error.message ||
            "Failed to create shipment.",
            "error"
        );

        if (createButton) {

            createButton.disabled =
                false;

            createButton.textContent =
                "Create Shipment";
        }
    }
}

// =====================================================
// ONE-CLICK PACK & SHIP
// =====================================================

async function packAndShip() {

    const shipmentDateInput =
        document.getElementById(
            "shipmentDate"
        );

    const deliveryStatusInput =
        document.getElementById(
            "deliveryStatus"
        );

    const packShipButton =
        document.getElementById(
            "packShipButton"
        );

    const shipmentDate =
        shipmentDateInput?.value || "";

    const deliveryStatus =
        deliveryStatusInput?.value || "";

    if (!shipmentDate) {

        showMessage(
            "Please select a shipment date.",
            "error"
        );

        return;
    }

    if (!deliveryStatus) {

        showMessage(
            "Please select a delivery status.",
            "error"
        );

        return;
    }

    if (
        !/^\d{4}-\d{2}-\d{2}$/.test(
            shipmentDate
        )
    ) {

        showMessage(
            "Shipment date must be in YYYY-MM-DD format.",
            "error"
        );

        return;
    }

    try {

        if (packShipButton) {

            packShipButton.disabled =
                true;

            packShipButton.textContent =
                "Packing & Shipping...";
        }

        const response =
            await fetch(
                autoPackShipApi,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify({
                            shipmentDate,
                            deliveryStatus
                        })
                }
            );

        if (!response.ok) {

            const errorData =
                await response
                    .json()
                    .catch(
                        () => null
                    );

            throw new Error(
                errorData?.message ||
                errorData?.error ||
                "Pack & Ship operation failed."
            );
        }

        const result =
            await response.json();

        console.log(
            "Pack & Ship Result:",
            result
        );

        showMessage(
            "Package and shipment created successfully.",
            "success"
        );

        await loadSalesOrder();
        await loadPackages();

        window.location.href =
            `/erpflow/salesOrderDetails.jsp?id=${encodeURIComponent(
                salesOrderId
            )}`;

    } catch (error) {

        console.error(
            "Pack & Ship Error:",
            error
        );

        showMessage(
            error.message ||
            "Pack & Ship operation failed.",
            "error"
        );

        if (packShipButton) {

            packShipButton.disabled =
                false;

            packShipButton.textContent =
                "Pack & Ship";
        }
    }
}

// =====================================================
// SAFE VALUE SETTER
// =====================================================

function setValue(
    id,
    value
) {

    const element =
        document.getElementById(id);

    if (!element) {
        return;
    }

    const safeValue =
        value ?? "-";

    if ("value" in element) {

        element.value =
            String(safeValue);

    } else {

        element.textContent =
            String(safeValue);
    }
}

// =====================================================
// DATE FORMAT
// =====================================================

function formatDate(
    value
) {

    if (!value) {
        return "-";
    }

    /*
     * Jackson LocalDate /
     * LocalDateTime array.
     */
    if (Array.isArray(value)) {

        const year =
            value[0];

        const month =
            String(
                value[1] ?? 1
            ).padStart(2, "0");

        const day =
            String(
                value[2] ?? 1
            ).padStart(2, "0");

        let result =
            `${year}-${month}-${day}`;

        if (value.length >= 5) {

            const hour =
                String(
                    value[3] ?? 0
                ).padStart(2, "0");

            const minute =
                String(
                    value[4] ?? 0
                ).padStart(2, "0");

            result +=
                ` ${hour}:${minute}`;
        }

        return result;
    }

    if (
        typeof value ===
        "string"
    ) {

        return value
            .replace("T", " ")
            .slice(0, 16);
    }

    return String(value);
}

// =====================================================
// CURRENCY
// =====================================================

function formatCurrency(
    value
) {

    const amount =
        Number(value || 0);

    return amount.toLocaleString(
        "en-IN",
        {
            style: "currency",
            currency: "INR"
        }
    );
}

// =====================================================
// HTML ESCAPE
// =====================================================

function escapeHtml(
    value
) {

    return String(
        value ?? ""
    )
        .replace(
            /&/g,
            "&amp;"
        )
        .replace(
            /</g,
            "&lt;"
        )
        .replace(
            />/g,
            "&gt;"
        )
        .replace(
            /"/g,
            "&quot;"
        )
        .replace(
            /'/g,
            "&#039;"
        );
}

// =====================================================
// MESSAGE
// =====================================================

function showMessage(
    message,
    type = "info"
) {

    const messageBox =
        document.getElementById(
            "messageBox"
        );

    if (!messageBox) {

        console.log(
            `[${type}] ${message}`
        );

        return;
    }

    const messageElement =
        document.createElement(
            "div"
        );

    messageElement.style.padding =
        "12px";

    messageElement.style.marginBottom =
        "20px";

    messageElement.style.borderRadius =
        "6px";

    if (type === "error") {

        messageElement.style.background =
            "#fee2e2";

        messageElement.style.color =
            "#991b1b";

    } else if (
        type === "success"
    ) {

        messageElement.style.background =
            "#dcfce7";

        messageElement.style.color =
            "#166534";

    } else {

        messageElement.style.background =
            "#e0f2fe";

        messageElement.style.color =
            "#075985";
    }

    messageElement.textContent =
        message;

    messageBox.replaceChildren(
        messageElement
    );

    setTimeout(() => {

        if (
            messageBox.contains(
                messageElement
            )
        ) {

            messageBox.removeChild(
                messageElement
            );
        }

    }, 4000);
}