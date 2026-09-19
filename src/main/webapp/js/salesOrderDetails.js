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

const carriersApi =
    `/erpflow/api/carriers`;

const shippingApi =
    `/erpflow/api/shipping`;


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

    }
);


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


        console.log(
            "Sales Order:",
            order
        );


        displaySalesOrder(
            order
        );


    }
    catch (error) {

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


    // -------------------------------------------------
    // ORDER INFORMATION
    // -------------------------------------------------

    setValue(
        "orderId",
        `#${order.id}`
    );


    setValue(
        "orderDate",
        formatDate(
            order.orderDate
        )
    );


    setValue(
        "orderStatus",
        order.status || "-"
    );



    // -------------------------------------------------
    // CUSTOMER
    // -------------------------------------------------

    const customer =
        order.customer || {};


    setValue(
        "customerId",
        customer.id || "-"
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



    // -------------------------------------------------
    // TAX / ORDER TOTALS
    // -------------------------------------------------

    setValue(
        "subtotal",
        formatCurrency(
            order.subtotal
        )
    );


    setValue(
        "taxRate",
        `${order.taxRate || 0}%`
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



    // -------------------------------------------------
    // ORDER ITEMS
    // -------------------------------------------------

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


    tableBody.innerHTML = "";


    const items =
        order.items || [];


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


    items.forEach(
        orderItem => {

            const row =
                document.createElement(
                    "tr"
                );


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


            const total =
                quantity * price;


            row.innerHTML = `

                <td>
                    ${item.id || "-"}
                </td>

                <td>
                    ${item.name || "-"}
                </td>

                <td>
                    ${item.sku || "-"}
                </td>

                <td>
                    ${quantity}
                </td>

                <td>
                    ${formatCurrency(price)}
                </td>

                <td>
                    ${formatCurrency(total)}
                </td>

            `;


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

        console.error(
            "packagesTableBody not found."
        );

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


        console.log(
            "Packages:",
            packages
        );


        tableBody.innerHTML = "";


        if (
            !packages ||
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

            return;
        }


        packages.forEach(
            pkg => {

                const row =
                    document.createElement(
                        "tr"
                    );


                /*
                 * Only PACKED packages can be
                 * selected for shipment.
                 */

                const canShip =
                    pkg.status === "PACKED";


                row.innerHTML = `

                    <!-- SELECT -->

                    <td>

                        <input
                            type="checkbox"
                            class="package-checkbox"
                            value="${pkg.id}"
                            ${canShip ? "" : "disabled"}
                        >

                    </td>


                    <!-- PACKAGE -->

                    <td>

                        <strong>
                            ${pkg.packageNumber || `#${pkg.id}`}
                        </strong>

                    </td>


                    <!-- STATUS -->

                    <td>

                        <span class="status-badge">

                            ${pkg.status || "-"}

                        </span>

                    </td>


                    <!-- WEIGHT -->

                    <td>

                        ${Number(
                            pkg.weight || 0
                        ).toFixed(2)}

                        kg

                    </td>


                    <!-- DIMENSIONS -->

                    <td>

                        ${Number(
                            pkg.length || 0
                        )}

                        ×

                        ${Number(
                            pkg.width || 0
                        )}

                        ×

                        ${Number(
                            pkg.height || 0
                        )}

                        cm

                    </td>


                    <!-- PACKAGE DATE -->

                    <td>

                        ${formatDate(
                            pkg.packageDate
                        )}

                    </td>


                    <!-- ACTION -->

                    <td>

                        <a
                            class="action-button"
                            href="/erpflow/packageDetails.jsp?id=${pkg.id}"
                        >
                            View
                        </a>

                    </td>

                `;


                tableBody.appendChild(
                    row
                );

            }
        );


        setupPackageSelection();


    }
    catch (error) {

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

    }

}


// =====================================================
// PACKAGE SELECTION
// =====================================================

function setupPackageSelection() {

    const checkboxes =
        document.querySelectorAll(
            ".package-checkbox"
        );


    const shipButton =
        document.getElementById(
            "shipSelectedButton"
        );


    if (!shipButton) {

        console.error(
            "shipSelectedButton not found."
        );

        return;
    }


    checkboxes.forEach(
        checkbox => {

            checkbox.addEventListener(
                "change",
                updateShipButton
            );

        }
    );


    updateShipButton();

}


// =====================================================
// UPDATE SHIP BUTTON
// =====================================================

function updateShipButton() {

    const selected =
        getSelectedPackageIds();


    const shipButton =
        document.getElementById(
            "shipSelectedButton"
        );


    if (!shipButton) {
        return;
    }


    shipButton.disabled =
        selected.length === 0;


    if (
        selected.length > 0
    ) {

        shipButton.textContent =
            `Ship Selected Packages (${selected.length})`;

    }
    else {

        shipButton.textContent =
            "Ship Selected Packages";

    }


    /*
     * Attach click handler only once.
     */

    if (
        !shipButton.dataset.listenerAttached
    ) {

        shipButton.addEventListener(
            "click",
            openShippingSection
        );


        shipButton.dataset.listenerAttached =
            "true";

    }

}


// =====================================================
// GET SELECTED PACKAGE IDS
// =====================================================

function getSelectedPackageIds() {

    const checkboxes =
        document.querySelectorAll(
            ".package-checkbox:checked"
        );


    return Array.from(
        checkboxes
    ).map(
        checkbox =>
            Number(
                checkbox.value
            )
    );

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


    /*
     * Scroll user to shipment section.
     */

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


    container.innerHTML = "";


    selectedIds.forEach(
        id => {

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

        }
    );

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


        carrierSelect.innerHTML = `

            <option value="">
                Select Carrier
            </option>

        `;


        carriers.forEach(
            carrier => {

                /*
                 * Ignore inactive carriers.
                 */

                if (
                    carrier.status &&
                    carrier.status !== "ACTIVE"
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

            }
        );


        /*
         * Attach carrier change listener.
         */

        if (
            !carrierSelect.dataset.listenerAttached
        ) {

            carrierSelect.addEventListener(
                "change",
                loadCarrierServices
            );


            carrierSelect.dataset.listenerAttached =
                "true";

        }


    }
    catch (error) {

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
        carrierSelect.value;


    serviceSelect.innerHTML = `

        <option value="">
            Select Service
        </option>

    `;


    clearShippingRate();


    const createButton =
        document.getElementById(
            "createShipmentButton"
        );


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
                    `${service.name} - ${service.estimatedDays} days`;


                serviceSelect.appendChild(
                    option
                );

            }
        );


    }
    catch (error) {

        console.error(
            "Service Error:",
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


    const serviceSelect =
        document.getElementById(
            "serviceSelect"
        );


    const carrierServiceId =
        Number(
            serviceSelect?.value || 0
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

                            packageIds:
                                packageIds,

                            carrierServiceId:
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


        console.log(
            "Shipping Rate:",
            rate
        );


        displayShippingRate(
            rate
        );


        const createButton =
            document.getElementById(
                "createShipmentButton"
            );


        if (createButton) {

            createButton.disabled =
                false;

        }


        showMessage(
            "Shipping rate calculated successfully.",
            "success"
        );


    }
    catch (error) {

        console.error(
            "Shipping Rate Error:",
            error
        );


        clearShippingRate();


        const createButton =
            document.getElementById(
                "createShipmentButton"
            );


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

function displayShippingRate(rate) {

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
// CREATE SHIPMENT
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


    try {

        if (createButton) {

            createButton.disabled =
                true;

            createButton.textContent =
                "Creating Shipment...";

        }


        /*
         * Shipment payload
         */

        const shipment =
            {

                salesOrderId:
                    Number(
                        salesOrderId
                    ),

                packageIds:
                    packageIds,

                carrierServiceId:
                    carrierServiceId,

                shippingMethod:
                    "CARRIER",

                trackingNumber:
                    trackingNumber,

                trackingUrl:
                    trackingUrl,

                dispatchAddress:
                    dispatchAddress,

                destinationAddress:
                    destinationAddress,

                notes:
                    notes

            };


        console.log(
            "Creating Shipment:",
            shipment
        );


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


        console.log(
            "Shipment Created:",
            result
        );


        showMessage(
            "Shipment created successfully.",
            "success"
        );


        /*
         * Reload packages so their
         * status becomes SHIPPED.
         */

        await loadPackages();


        /*
         * Hide shipment section.
         */

        const shippingSection =
            document.getElementById(
                "shippingSection"
            );


        if (shippingSection) {

            shippingSection.style.display =
                "none";

        }


        /*
         * Redirect to shipment details
         * if shipment ID is returned.
         */

        const shipmentId =
            result.shipmentId ||
            result.id;


        if (shipmentId) {

            setTimeout(
                () => {

                    window.location.href =
                        `/erpflow/salesOrderDetails.jsp?id=${salesOrderId}`;

                },
                800
            );

        }


    }
    catch (error) {

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
// BUTTON LISTENERS
// =====================================================

document.addEventListener(
    "DOMContentLoaded",
    () => {

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

    }
);


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

        console.error(
            `Element #${id} not found in JSP.`
        );

        return;

    }


    /*
     * All our fields are input/textarea.
     */

    if (
        "value" in element
    ) {

        element.value =
            value ?? "-";

    }
    else {

        element.textContent =
            value ?? "-";

    }

}


// =====================================================
// DATE FORMAT
// =====================================================

function formatDate(value) {

    if (!value) {
        return "-";
    }


    /*
     * Jackson LocalDateTime:
     *
     * [
     *   2026,
     *   9,
     *   18,
     *   17,
     *   19,
     *   0
     * ]
     */

    if (
        Array.isArray(value)
    ) {

        const year =
            value[0];


        const month =
            String(
                value[1]
            ).padStart(
                2,
                "0"
            );


        const day =
            String(
                value[2]
            ).padStart(
                2,
                "0"
            );


        let result =
            `${year}-${month}-${day}`;


        /*
         * LocalDateTime
         */

        if (
            value.length >= 5
        ) {

            const hour =
                String(
                    value[3]
                ).padStart(
                    2,
                    "0"
                );


            const minute =
                String(
                    value[4]
                ).padStart(
                    2,
                    "0"
                );


            result +=
                ` ${hour}:${minute}`;

        }


        return result;

    }


    return value;

}


// =====================================================
// CURRENCY
// =====================================================

function formatCurrency(value) {

    const amount =
        Number(
            value || 0
        );


    return amount.toLocaleString(
        "en-IN",
        {
            style: "currency",
            currency: "INR"
        }
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


    messageBox.innerHTML = `

        <div
            style="
                padding:12px;
                margin-bottom:20px;
                border-radius:6px;
                background:${
                    type === "error"
                        ? "#fee2e2"
                        : type === "success"
                            ? "#dcfce7"
                            : "#e0f2fe"
                };
                color:${
                    type === "error"
                        ? "#991b1b"
                        : type === "success"
                            ? "#166534"
                            : "#075985"
                };
            "
        >

            ${message}

        </div>

    `;


    /*
     * Automatically remove message
     * after a few seconds.
     */

    setTimeout(
        () => {

            if (
                messageBox
            ) {

                messageBox.innerHTML =
                    "";

            }

        },
        4000
    );

}