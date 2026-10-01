"use strict";

// =====================================================
// SALES ORDER DETAILS
// =====================================================

const params =
    new URLSearchParams(
        window.location.search
    );

const salesOrderId =
    params.get("id");


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


let currentSalesOrder = null;


// =====================================================
// PAGE LOAD
// =====================================================

document.addEventListener(
    "DOMContentLoaded",
    () => {

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

        initializeShipmentCreation();



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


        setTodayIfEmpty(
            "manualShipmentDate"
        );

        setTodayIfEmpty(
            "autoShipmentDate"
        );


        setupDateValidation();

    }
);


// =====================================================
// DATE HELPERS
// =====================================================

function getTodayLocalDate() {

    const now =
        new Date();


    const year =
        now.getFullYear();


    const month =
        String(
            now.getMonth() + 1
        ).padStart(
            2,
            "0"
        );


    const day =
        String(
            now.getDate()
        ).padStart(
            2,
            "0"
        );


    return `${year}-${month}-${day}`;
}


function setTodayIfEmpty(
    elementId
) {

    const input =
        document.getElementById(
            elementId
        );


    if (
        !input ||
        input.value
    ) {

        return;
    }


    input.value =
        getTodayLocalDate();
}


function setupDateValidation() {

    const manualDate =
        document.getElementById(
            "manualShipmentDate"
        );


    const autoDate =
        document.getElementById(
            "autoShipmentDate"
        );


    if (manualDate) {

        manualDate.type =
            "date";


        if (!manualDate.value) {

            manualDate.value =
                getTodayLocalDate();
        }

    }


    if (autoDate) {

        autoDate.type =
            "date";


        if (!autoDate.value) {

            autoDate.value =
                getTodayLocalDate();
        }

    }

}


// =====================================================
// LOAD SALES ORDER
// =====================================================

async function loadSalesOrder() {

    try {

        const response =
            await fetch(
                salesOrderApi
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load Sales Order."
            );
        }


        const order =
            await response.json();


        currentSalesOrder =
            order;


        displaySalesOrder(
            order
        );


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

function displaySalesOrder(
    order
) {

    setValue(
        "orderId",
        order.id != null
            ? `#${order.id}`
            : "-"
    );


    setValue(
        "orderDate",
        formatDate(
            order.orderDate
        )
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
        formatCurrency(
            order.subtotal
        )
    );


    setValue(
        "taxRate",
        `${order.taxRate ?? 0}%`
    );


    setValue(
        "taxAmount",
        formatCurrency(
            order.taxAmount
        )
    );


    setValue(
        "totalAmount",
        formatCurrency(
            order.totalAmount
        )
    );


    const destinationInput =
        document.getElementById(
            "destinationAddress"
        );


    if (
        destinationInput &&
        !destinationInput.value &&
        customer.address
    ) {

        destinationInput.value =
            customer.address;

    }


    const tableBody =
        document.getElementById(
            "orderItemsTableBody"
        );


    if (!tableBody) {

        return;
    }


    tableBody.replaceChildren();


    const items =
        Array.isArray(
            order.items
        )
            ? order.items
            : [];


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


    items.forEach(
        orderItem => {

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
                document.createElement(
                    "tr"
                );


            [
                item.id ?? "-",
                item.name || "-",
                item.sku || "-",
                quantity,
                formatCurrency(price),
                formatCurrency(
                    quantity * price
                )

            ].forEach(
                value => {

                    const cell =
                        document.createElement(
                            "td"
                        );


                    cell.textContent =
                        String(value);


                    row.appendChild(
                        cell
                    );

                }
            );


            tableBody.appendChild(
                row
            );

        }
    );

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

        return;
    }


    try {

        const response =
            await fetch(
                packagesApi
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load packages."
            );
        }


        const packages =
            await response.json();


        renderPackages(
            packages
        );


    } catch (error) {

        console.error(
            "Packages Error:",
            error
        );


        tableBody.innerHTML = `
            <tr>
                <td colspan="10">
                    Failed to load packages.
                </td>
            </tr>
        `;

    }

}


// =====================================================
// RENDER PACKAGES
// =====================================================

function renderPackages(
    packages
) {

    const tableBody =
        document.getElementById(
            "packagesTableBody"
        );


    if (!tableBody) {

        return;
    }


    tableBody.replaceChildren();


    if (
        !Array.isArray(packages) ||
        packages.length === 0
    ) {

        tableBody.innerHTML = `
            <tr>
                <td colspan="10">
                    No packages created for this Sales Order.
                </td>
            </tr>
        `;

        updateShipmentButtons();

        return;
    }


    packages.forEach(
        pkg => {

            const row =
                document.createElement(
                    "tr"
                );


            const status =
                pkg.status || "-";


            const packageDate =
                formatDate(
                    pkg.packageDate
                );


            const items =
                Array.isArray(
                    pkg.items
                )
                    ? pkg.items
                    : [];


            const itemText =
                items.length > 0
                    ? items
                        .map(
                            item =>
                                `${escapeHtml(
                                    item.item?.name ||
                                    item.itemName ||
                                    "-"
                                )} × ${escapeHtml(item.quantity)}`
                        )
                        .join("<br>")
                    : "-";

            const isShipped =
    pkg.shipmentId != null ||
    pkg.shipment != null ||
    pkg.status === "SHIPPED";
            row.innerHTML = `

                <td>

                    <input
                        type="checkbox"
                        class="package-checkbox"
                        value="${pkg.id}" ${isShipped ? "disabled" : ""}
                        data-status="${escapeHtml(
                status
            )}"
                    >

                </td>


                <td>

                    <strong>
                        ${escapeHtml(
                pkg.packageNumber ||
                `#${pkg.id}`
            )}
                    </strong>

                </td>


                <td>
                    ${itemText}
                </td>


                <td>
                    ${pkg.weight ?? "-"}
                </td>


                <td>
                    ${pkg.length ?? "-"}
                </td>


                <td>
                    ${pkg.width ?? "-"}
                </td>


                <td>
                    ${pkg.height ?? "-"}
                </td>


                <td>
                    ${packageDate}
                </td>


                <td>

                    <span class="status">
                        ${escapeHtml(
                status
            )}
                    </span>

                </td>


                <td>

                    <button
                        type="button"
                        class="action-button"
                        onclick="editPackage(${pkg.id})"
                    >
                        Edit
                    </button>

                </td>

            `;


            tableBody.appendChild(
                row
            );

        }
    );


    document
        .querySelectorAll(
            ".package-checkbox"
        )
        .forEach(
            checkbox => {

                checkbox.addEventListener(
                    "change",
                    updateShipmentButtons
                );

            }
        );


    updateShipmentButtons();

}


// =====================================================
// PACKAGE SELECTION
// =====================================================

function getSelectedPackageIds() {

    return Array.from(
        document.querySelectorAll(
            ".package-checkbox:checked"
        )
    )
        .map(
            checkbox =>
                Number(
                    checkbox.value
                )
        )
        .filter(
            id => id > 0
        );

}


// =====================================================
// RATE RESET
// =====================================================

/*
 * Any change to the selected packages, carrier or service
 * makes a previously calculated rate invalid.
 */

function invalidateShippingRate() {

    const createButton =
        document.getElementById(
            "createShipmentButton"
        );

    if (createButton) {

        createButton.disabled =
            true;

        createButton.dataset.rateCalculated =
            "false";

    }

    [
        "actualWeight",
        "dimensionalWeight",
        "chargeableWeight",
        "baseCharge",
        "chargePerKg",
        "totalShippingCharge",
        "shippingCharge"
    ].forEach(
        id => {

            const field =
                document.getElementById(id);

            if (!field) {
                return;
            }

            if ("value" in field) {

                field.value = "";

            } else {

                field.textContent = "";

            }

        }
    );

    const display =
        document.getElementById(
            "shippingRateDisplay"
        );

    if (display) {

        display.textContent = "";

    }

}


// =====================================================
// UPDATE SHIPMENT BUTTONS
// =====================================================

function updateShipmentButtons() {

    const selected =
        getSelectedPackageIds();

    const section =
        document.getElementById(
            "shippingSection"
        );

    // Selection changed, so any earlier rate is stale
    invalidateShippingRate();

    /*
     * No separate "Ship Selected Packages"
     * button is required anymore.
     *
     * Once the user selects packed packages,
     * open the manual shipment section.
     */

    if (
        selected.length > 0 &&
        section
    ) {

        const isHidden =
            window.getComputedStyle(
                section
            ).display === "none";

        if (isHidden) {

            openShipmentCreation();

        } else {

            updateSelectedPackagesDisplay();

        }
    }

    if (
        selected.length === 0 &&
        section
    ) {

        section.style.display =
            "none";
    }
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


    let packageSection =
        tableBody.closest(
            "section"
        );


    if (!packageSection) {

        packageSection =
            tableBody.parentElement
                ?.parentElement;
    }


    if (!packageSection) {

        return;
    }


    const button =
        document.createElement(
            "button"
        );


    button.type =
        "button";


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
// CREATE PACKAGE MODAL
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
        document.createElement(
            "div"
        );


    modal.id =
        "salesOrderCreatePackageModal";


    modal.style.display =
        "none";


    modal.style.position =
        "fixed";


    modal.style.inset =
        "0";


    modal.style.background =
        "rgba(0,0,0,0.5)";


    modal.style.zIndex =
        "9999";


    modal.style.alignItems =
        "center";


    modal.style.justifyContent =
        "center";


    modal.style.padding =
        "20px";


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

                <h2 style="margin:0;">
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
                id="salesOrderPackageItems"
            ></div>


            <div
                style="
                    display:flex;
                    gap:10px;
                    justify-content:flex-end;
                    margin-top:20px;
                "
            >

                <button
                    type="button"
                    id="cancelSalesOrderPackage"
                    class="action-button"
                >
                    Cancel
                </button>


                <button
                    type="button"
                    id="saveSalesOrderPackage"
                    class="action-button"
                >
                    Create Package
                </button>

            </div>

        </div>

    `;


    document.body.appendChild(
        modal
    );


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
            "saveSalesOrderPackage"
        )
        ?.addEventListener(
            "click",
            saveCreatePackage
        );

}


// =====================================================
// OPEN CREATE PACKAGE MODAL
// =====================================================

function openCreatePackageModal() {

    const modal =
        document.getElementById(
            "salesOrderCreatePackageModal"
        );


    if (!modal) {

        return;
    }


    const container =
        document.getElementById(
            "salesOrderPackageItems"
        );


    if (!container) {

        return;
    }


    const items =
        Array.isArray(
            currentSalesOrder?.items
        )
            ? currentSalesOrder.items
            : [];


    container.innerHTML =
        "";


    items.forEach(
        orderItem => {

            const item =
                orderItem.item || {};


            const quantity =
                Number(
                    orderItem.quantity || 0
                );


            if (quantity <= 0) {

                return;
            }


            const wrapper =
                document.createElement(
                    "div"
                );


            wrapper.style.display =
                "grid";


            wrapper.style.gridTemplateColumns =
                "1fr 150px";


            wrapper.style.gap =
                "15px";


            wrapper.style.alignItems =
                "center";


            wrapper.style.padding =
                "12px 0";


            wrapper.innerHTML = `

                <div>

                    <strong>
                        ${escapeHtml(
                item.name || "-"
            )}
                    </strong>


                    <div
                        style="
                            color:#6b7280;
                            font-size:13px;
                        "
                    >

                        SKU:
                        ${escapeHtml(
                item.sku || "-"
            )}

                        · Ordered:
                        ${quantity}

                    </div>

                </div>


                <input
                    type="number"
                    min="0"
                    max="${quantity}"
                    value="${quantity}"
                    class="create-package-quantity"
                    data-sales-order-item-id="${orderItem.id}"
                >

            `;


            container.appendChild(
                wrapper
            );

        }
    );


    modal.style.display =
        "flex";

}


// =====================================================
// CLOSE PACKAGE MODAL
// =====================================================

function closeCreatePackageModal() {

    const modal =
        document.getElementById(
            "salesOrderCreatePackageModal"
        );


    if (modal) {

        modal.style.display =
            "none";

    }

}


// =====================================================
// SAVE PACKAGE
// =====================================================

async function saveCreatePackage() {

    const quantityInputs =
        Array.from(
            document.querySelectorAll(
                ".create-package-quantity"
            )
        );


    const items =
        [];


    for (
        const input
        of quantityInputs
    ) {

        const quantity =
            Number(
                input.value || 0
            );


        const maxQuantity =
            Number(
                input.max || 0
            );


        const salesOrderItemId =
            Number(
                input.dataset
                    .salesOrderItemId
            );


        if (
            quantity < 0 ||
            !Number.isInteger(
                quantity
            )
        ) {

            showMessage(
                "Package quantities must be whole numbers.",
                "error"
            );

            return;
        }


        if (quantity > maxQuantity) {

            showMessage(
                "Package quantity cannot exceed the ordered quantity.",
                "error"
            );

            return;
        }


        if (quantity > 0) {

            items.push({

                salesOrderItemId,

                quantity

            });

        }

    }


    if (items.length === 0) {

        showMessage(
            "Enter quantity for at least one item.",
            "error"
        );

        return;
    }


    const saveButton =
        document.getElementById(
            "saveSalesOrderPackage"
        );


    try {

        if (saveButton) {

            saveButton.disabled =
                true;

            saveButton.textContent =
                "Creating...";

        }


        const response =
            await fetch(
                packagesBaseApi,
                {
                    method:
                        "POST",

                    headers: {

                        "Content-Type":
                            "application/json"

                    },

                    body:
                        JSON.stringify({

                            salesOrderId:
                                Number(
                                    salesOrderId
                                ),

                            packageDate:
                                getTodayLocalDate(),

                            items

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
                "Failed to create package."
            );

        }


        closeCreatePackageModal();


        showMessage(
            "Package created successfully.",
            "success"
        );


        await loadPackages();


    } catch (error) {

        console.error(
            "Create Package Error:",
            error
        );


        showMessage(
            error.message ||
            "Failed to create package.",
            "error"
        );


    } finally {

        if (saveButton) {

            saveButton.disabled =
                false;

            saveButton.textContent =
                "Create Package";

        }

    }

}


// =====================================================
// SHIP SELECTED PACKAGES
// =====================================================

function openShipmentCreation() {

    const packageIds =
        getSelectedPackageIds();


    if (packageIds.length === 0) {

        showMessage(
            "Select at least one package.",
            "error"
        );

        return;
    }


    const section =
        document.getElementById(
            "shippingSection"
        );


    if (!section) {

        showMessage(
            "Shipment creation section is not available.",
            "error"
        );

        return;
    }


    section.style.display =
        "block";


    setTodayIfEmpty(
        "manualShipmentDate"
    );


    updateSelectedPackagesDisplay();


    section.scrollIntoView({

        behavior:
            "smooth",

        block:
            "start"

    });


    loadCarriers();

}


// =====================================================
// SELECTED PACKAGE DISPLAY
// =====================================================

function updateSelectedPackagesDisplay() {

    const container =
        document.getElementById(
            "selectedPackages"
        );


    if (!container) {

        return;
    }


    const selectedCheckboxes =
        Array.from(
            document.querySelectorAll(
                ".package-checkbox:checked"
            )
        );


    if (
        selectedCheckboxes.length === 0
    ) {

        container.textContent =
            "No packages selected.";

        return;
    }


    container.innerHTML =
        selectedCheckboxes
            .map(
                checkbox => {

                    const row =
                        checkbox.closest(
                            "tr"
                        );


                    const packageCell =
                        row?.children?.[1];


                    return escapeHtml(
                        packageCell
                            ? packageCell.textContent.trim()
                            : `#${checkbox.value}`
                    );

                }
            )
            .join(", ");

}


// =====================================================
// SHIP BUTTON
// =====================================================

document.addEventListener(
    "click",
    event => {

        if (
            event.target.closest(
                "#shipSelectedPackagesButton"
            )
        ) {

            openShipmentCreation();

        }

    }
);


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


    const serviceSelect =
        document.getElementById(
            "serviceSelect"
        );


    try {

        const response =
            await fetch(
                carriersApi
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load carriers."
            );

        }


        const carriers =
            await response.json();


        carrierSelect.innerHTML =
            `
                <option value="">
                    Select Carrier
                </option>
            `;


        // Clear services left over from a previous carrier
        if (serviceSelect) {

            serviceSelect.innerHTML =
                `
                    <option value="">
                        Select Service
                    </option>
                `;

            serviceSelect.onchange =
                invalidateShippingRate;

        }


        carriers.forEach(
            carrier => {

                const option =
                    document.createElement(
                        "option"
                    );


                option.value =
                    carrier.id;


                option.textContent =
                    carrier.name;


                carrierSelect.appendChild(
                    option
                );

            }
        );


        carrierSelect.onchange =
            loadCarrierServices;


    } catch (error) {

        console.error(
            "Carrier Error:",
            error
        );


        showMessage(
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


    if (
        !carrierSelect ||
        !serviceSelect
    ) {

        return;
    }


    const carrierId =
        Number(
            carrierSelect.value
        );


    serviceSelect.innerHTML =
        `
            <option value="">
                Select Service
            </option>
        `;


    // Carrier changed, so any earlier rate is stale
    invalidateShippingRate();


    if (!carrierId) {

        return;
    }


    try {

        const response =
            await fetch(
                `${carriersApi}/${carrierId}/services`
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load carrier services."
            );

        }


        const services =
            await response.json();


        services.forEach(
            service => {

                const option =
                    document.createElement(
                        "option"
                    );


                option.value =
                    service.id;


                option.textContent =
                    service.name;


                serviceSelect.appendChild(
                    option
                );

            }
        );


    } catch (error) {

        console.error(
            "Carrier Service Error:",
            error
        );


        showMessage(
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


    if (packageIds.length === 0) {

        showMessage(
            "Select at least one package.",
            "error"
        );

        return;
    }


    const serviceSelect =
        document.getElementById(
            "serviceSelect"
        );


    const carrierServiceId =
        Number(
            serviceSelect?.value || 0
        );


    if (!carrierServiceId) {

        showMessage(
            "Select a carrier service.",
            "error"
        );

        return;
    }


    const rateButton =
        document.getElementById(
            "calculateRateButton"
        );


    // FIX: this was used below but never declared
    const createButton =
        document.getElementById(
            "createShipmentButton"
        );


    try {

        if (rateButton) {

            rateButton.disabled =
                true;

            rateButton.textContent =
                "Calculating...";

        }


        const response =
            await fetch(
                `${shippingApi}/rate`,
                {
                    method:
                        "POST",

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

        const shippingCharge =
            Number(
                rate.totalCharge ??
                rate.shippingCharge ??
                rate.total ??
                rate.amount ??
                0
            );

        const setRateField =
            (id, value) => {

                const field =
                    document.getElementById(id);

                if (!field) {
                    return;
                }

                if (
                    value === null ||
                    value === undefined ||
                    value === ""
                ) {
                    field.value = "-";
                    return;
                }

                field.value =
                    Number(value).toFixed(2);
            };


        /*
         * Populate complete shipping-rate information
         */

        setRateField(
            "actualWeight",
            rate.actualWeight
        );

        setRateField(
            "dimensionalWeight",
            rate.dimensionalWeight
        );

        setRateField(
            "chargeableWeight",
            rate.chargeableWeight
        );

        setRateField(
            "baseCharge",
            rate.baseCharge
        );

        setRateField(
            "chargePerKg",
            rate.chargePerKg
        );

        setRateField(
            "totalShippingCharge",
            shippingCharge
        );


        /*
         * Hidden field used when creating shipment
         */

        setRateField(
            "shippingCharge",
            shippingCharge
        );


        /*
         * Optional display element
         */

        const display =
            document.getElementById(
                "shippingRateDisplay"
            );

        if (display) {

            display.textContent =
                formatCurrency(
                    shippingCharge
                );
        }


        /*
         * Rate has now been successfully calculated.
         * Enable Create Shipment.
         */

        if (createButton) {

            createButton.disabled =
                false;

            createButton.dataset.rateCalculated =
                "true";
        }


    } catch (error) {

        console.error(
            "Shipping Rate Error:",
            error
        );


        showMessage(
            error.message ||
            "Failed to calculate shipping rate.",
            "error"
        );


    } finally {

        if (rateButton) {

            rateButton.disabled =
                false;

            rateButton.textContent =
                "Calculate Shipping Rate";

        }

    }

}


// =====================================================
// CREATE MANUAL SHIPMENT
// =====================================================

async function createShipment() {

    const createButton =
        document.getElementById("createShipmentButton");

    if (!createButton) {
        showMessage(
            "Create Shipment button not found.",
            "error"
        );
        return;
    }

    if (
        createButton.dataset.rateCalculated !== "true"
    ) {
        showMessage(
            "Please calculate the shipping rate before creating the shipment.",
            "error"
        );
        return;
    }

    const packageIds =
        getSelectedPackageIds();


    if (packageIds.length === 0) {

        showMessage(
            "Select at least one package.",
            "error"
        );

        return;
    }


    const shipmentDateInput =
        document.getElementById(
            "manualShipmentDate"
        );


    const shipmentDate =
        shipmentDateInput?.value?.trim() ||
        "";


    if (!shipmentDate) {

        showMessage(
            "Please select a shipment date.",
            "error"
        );


        shipmentDateInput?.focus();


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


    const shippingMethod =
        document.getElementById(
            "manualShippingMethod"
        )?.value ||
        "CARRIER";


    const trackingNumber =
        document.getElementById(
            "trackingNumber"
        )?.value.trim() ||
        "";


    const trackingUrl =
        document.getElementById(
            "trackingUrl"
        )?.value.trim() ||
        "";


    const dispatchAddress =
        document.getElementById(
            "dispatchAddress"
        )?.value.trim() ||
        "";


    const destinationAddress =
        document.getElementById(
            "destinationAddress"
        )?.value.trim() ||
        "";


    const notes =
        document.getElementById(
            "shipmentNotes"
        )?.value.trim() ||
        "";


    const shippingChargeInput =
        document.getElementById(
            "shippingCharge"
        ) ||
        document.getElementById(
            "totalShippingCharge"
        );

    const shippingCharge =
        shippingChargeInput &&
            shippingChargeInput.value !== ""
            ? Number(
                shippingChargeInput.value
            )
            : 0;

    if (
        Number.isNaN(
            shippingCharge
        ) ||
        shippingCharge < 0
    ) {

        showMessage(
            "Shipping charge cannot be negative.",
            "error"
        );

        return;

    }


    const shipment = {

        salesOrderId:
            Number(
                salesOrderId
            ),

        packageIds,

        carrierServiceId,

        shipmentDate,

        shippingMethod,

        trackingNumber,

        trackingUrl,

        shippingCharge,

        dispatchAddress,

        destinationAddress,

        notes,

        status:
            "CREATED"

    };


    console.log(
        "Creating Manual Shipment:",
        shipment
    );


    try {

        createButton.disabled =
            true;

        createButton.textContent =
            "Creating Shipment...";


        const response =
            await fetch(
                shippingApi,
                {

                    method:
                        "POST",

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


        console.log(
            "Shipment Created:",
            result
        );


        showMessage(
            "Shipment created successfully.",
            "success"
        );


        await loadPackages();

        await loadSalesOrder();


        const shippingSection =
            document.getElementById(
                "shippingSection"
            );


        if (shippingSection) {

            shippingSection.style.display =
                "none";

        }


        document
            .querySelectorAll(
                ".package-checkbox:checked"
            )
            .forEach(
                checkbox => {

                    checkbox.checked =
                        false;

                }
            );


        // Also resets the calculated rate
        updateShipmentButtons();


        updateSelectedPackagesDisplay();


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


    } finally {

        createButton.textContent =
            "Create Shipment";

        // Stay enabled only while a valid rate is still calculated
        createButton.disabled =
            createButton.dataset.rateCalculated !== "true";

    }

}


// =====================================================
// AUTO PACK & SHIP
// =====================================================

async function packAndShip() {

    const shipmentDateInput =
        document.getElementById(
            "autoShipmentDate"
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
        shipmentDateInput?.value?.trim() ||
        "";


    const deliveryStatus =
        deliveryStatusInput?.value?.trim() ||
        "";


    if (!shipmentDate) {

        showMessage(
            "Please select a shipment date.",
            "error"
        );


        shipmentDateInput?.focus();


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


    if (!deliveryStatus) {

        showMessage(
            "Please select a shipment status.",
            "error"
        );


        deliveryStatusInput?.focus();


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

                    method:
                        "POST",

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
            "Remaining items packed and shipped successfully.",
            "success"
        );


        await loadSalesOrder();

        await loadPackages();


        setTimeout(
            () => {

                window.location.reload();

            },
            700
        );


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


    } finally {

        if (packShipButton) {

            packShipButton.disabled =
                false;

            packShipButton.textContent =
                "Auto Pack & Ship";

        }

    }

}


// =====================================================
// EDIT PACKAGE
// =====================================================

function editPackage(
    packageId
) {

    if (!packageId) {

        return;
    }


    window.location.href =
        `/erpflow/packageDetails.jsp?id=${encodeURIComponent(
            packageId
        )}`;

}


// =====================================================
// SAFE VALUE SETTER
// =====================================================

function setValue(
    id,
    value
) {

    const element =
        document.getElementById(
            id
        );


    if (!element) {

        return;
    }


    const safeValue =
        value ?? "-";


    if (
        "value" in element
    ) {

        element.value =
            String(
                safeValue
            );

    } else {

        element.textContent =
            String(
                safeValue
            );

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


    if (
        Array.isArray(
            value
        )
    ) {

        const year =
            value[0];


        const month =
            String(
                value[1] ?? 1
            ).padStart(
                2,
                "0"
            );


        const day =
            String(
                value[2] ?? 1
            ).padStart(
                2,
                "0"
            );


        return `${year}-${month}-${day}`;

    }


    if (
        typeof value ===
        "string"
    ) {

        return value
            .replace(
                "T",
                " "
            )
            .slice(
                0,
                10
            );

    }


    return String(
        value
    );

}


// =====================================================
// CURRENCY
// =====================================================

function formatCurrency(
    value
) {

    const amount =
        Number(
            value || 0
        );


    return amount.toLocaleString(
        "en-IN",
        {

            style:
                "currency",

            currency:
                "INR"

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
        "12px 16px";


    messageElement.style.marginBottom =
        "20px";


    messageElement.style.borderRadius =
        "8px";


    messageElement.style.fontWeight =
        "500";


    if (
        type === "error"
    ) {

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


    setTimeout(
        () => {

            if (
                messageBox.contains(
                    messageElement
                )
            ) {

                messageBox.removeChild(
                    messageElement
                );

            }

        },
        4000
    );

}

function initializeShipmentCreation() {

    const createShipmentButton =
        document.getElementById("createShipmentButton");

    if (!createShipmentButton) {
        console.warn(
            "Create Shipment button not found."
        );
        return;
    }

    // Initially disabled until shipping rate is calculated
    createShipmentButton.disabled = true;

    createShipmentButton.dataset.rateCalculated =
        "false";

    createShipmentButton.addEventListener(
        "click",
        createShipment
    );
}