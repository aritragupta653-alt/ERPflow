"use strict";

(() => {

    // =====================================================
    // PACKAGE PAGE API
    // =====================================================

    const packagesPageApiUrl =
        "/erpflow/api/packages";

    const packagesPageSalesOrdersApiUrl =
        "/erpflow/api/sales-orders";

    let salesOrders = [];
    let selectedOrderItems = [];


    // =====================================================
    // PAGE LOAD
    // =====================================================

    document.addEventListener("DOMContentLoaded", () => {

        /*
         * Only initialize the global Packages page
         * when statusFilter exists.
         *
         * salesOrderDetails.jsp does NOT have statusFilter,
         * so its own loadPackages() remains untouched.
         */

        const statusFilter =
            document.getElementById("statusFilter");

        if (statusFilter) {

            loadPackages();

            statusFilter.addEventListener(
                "change",
                loadPackages
            );

            loadSalesOrders();
        }


        // Create Package form exists on both pages.

        const form =
            document.getElementById(
                "createPackageForm"
            );

        if (form) {

            form.addEventListener(
                "submit",
                createPackage
            );
        }


        const salesOrderSelect =
            document.getElementById(
                "salesOrderId"
            );

        if (salesOrderSelect) {

            salesOrderSelect.addEventListener(
                "change",
                loadSalesOrderItems
            );
        }

    });


    // =====================================================
    // LOAD ALL PACKAGES
    // Only used by packages.jsp
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

            const selectedStatus =
                document
                    .getElementById("statusFilter")
                    ?.value || "ALL";


            const url =
                selectedStatus === "ALL"
                    ? packagesPageApiUrl
                    : `${packagesPageApiUrl}?status=${encodeURIComponent(
                        selectedStatus
                    )}`;


            const response =
                await fetch(url);


            if (!response.ok) {

                throw new Error(
                    "Failed to load packages."
                );
            }


            const packages =
                await response.json();


            if (
                !Array.isArray(packages) ||
                packages.length === 0
            ) {

                tableBody.innerHTML = `
                    <tr>
                        <td
                            colspan="7"
                            style="text-align:center;"
                        >
                            No packages found
                        </td>
                    </tr>
                `;

                return;
            }


            tableBody.innerHTML =
                packages.map(pkg => {

                    const salesOrder =
                        pkg.salesOrder || {};

                    const customer =
                        salesOrder.customer || {};

                    const editable =
                        String(
                            pkg.status || ""
                        ).toUpperCase() === "PACKED";


                    return `
                        <tr>

                            <td>
                                <strong>
                                    ${escapeHtml(
                                        pkg.packageNumber ||
                                        "PKG-" + pkg.id
                                    )}
                                </strong>
                            </td>


                            <td>

                                <a
                                    href="/erpflow/salesOrderDetails.jsp?id=${encodeURIComponent(
                                        salesOrder.id || ""
                                    )}"
                                >
                                    SO #${escapeHtml(
                                        salesOrder.id || "-"
                                    )}
                                </a>

                            </td>


                            <td>
                                ${escapeHtml(
                                    customer.name || "-"
                                )}
                            </td>


                            <td>
                                ${formatNumber(
                                    pkg.weight
                                )} kg
                            </td>


                            <td>

                                ${formatNumber(
                                    pkg.length
                                )}

                                ×

                                ${formatNumber(
                                    pkg.width
                                )}

                                ×

                                ${formatNumber(
                                    pkg.height
                                )}

                                cm

                            </td>


                            <td>

                                <span
                                    class="status-badge ${getStatusClass(
                                        pkg.status
                                    )}"
                                >
                                    ${escapeHtml(
                                        pkg.status || "-"
                                    )}
                                </span>

                            </td>


                            <td>

                                <div
                                    style="
                                        display:flex;
                                        gap:8px;
                                        flex-wrap:wrap;
                                    "
                                >

                                    <button
                                        type="button"
                                        class="btn btn-secondary"
                                        onclick="viewPackage(${Number(
                                            pkg.id
                                        )})"
                                    >
                                        View
                                    </button>


                                    ${
                                        editable
                                            ? `
                                                <button
                                                    type="button"
                                                    class="btn btn-primary"
                                                    onclick="editPackage(${Number(
                                                        pkg.id
                                                    )})"
                                                >
                                                    Edit
                                                </button>
                                            `
                                            : ""
                                    }

                                </div>

                            </td>

                        </tr>
                    `;

                }).join("");


        } catch (error) {

            console.error(error);


            tableBody.innerHTML = `
                <tr>
                    <td
                        colspan="7"
                        style="text-align:center;"
                    >
                        Failed to load packages
                    </td>
                </tr>
            `;
        }
    }


    // =====================================================
    // LOAD SALES ORDERS
    // Only used by packages.jsp
    // =====================================================

    async function loadSalesOrders() {

        const select =
            document.getElementById(
                "salesOrderId"
            );

        if (!select) {
            return;
        }


        try {

            const response =
                await fetch(
                    packagesPageSalesOrdersApiUrl
                );


            if (!response.ok) {

                throw new Error(
                    "Failed to load sales orders."
                );
            }


            salesOrders =
                await response.json();


            select.innerHTML = `
                <option value="">
                    Select Sales Order
                </option>
            `;


            salesOrders
                .filter(order => {

                    const status =
                        (
                            order.status || ""
                        ).toUpperCase();


                    return (
                        status !== "SHIPPED" &&
                        status !== "DELIVERED" &&
                        status !== "CANCELLED"
                    );

                })
                .forEach(order => {

                    const customerName =
                        order.customer?.name ||
                        "Unknown Customer";


                    const option =
                        document.createElement(
                            "option"
                        );


                    option.value =
                        order.id;


                    option.textContent =
                        `SO #${order.id} - ${customerName}`;


                    select.appendChild(
                        option
                    );

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


    // =====================================================
    // LOAD SALES ORDER ITEMS
    // =====================================================

    async function loadSalesOrderItems() {

        const salesOrderId =
            document.getElementById(
                "salesOrderId"
            )?.value;


        const container =
            document.getElementById(
                "orderItemsContainer"
            );


        if (!container) {
            return;
        }


        selectedOrderItems = [];


        if (!salesOrderId) {

            container.innerHTML = `
                <p class="muted">
                    Select a sales order first.
                </p>
            `;

            return;
        }


        container.innerHTML = `
            <p class="muted">
                Loading sales order items...
            </p>
        `;


        try {

            const [
                orderResponse,
                packagesResponse
            ] = await Promise.all([

                fetch(
                    `${packagesPageSalesOrdersApiUrl}/${encodeURIComponent(
                        salesOrderId
                    )}`
                ),

                /*
                 * IMPORTANT:
                 * Only load packages belonging to
                 * this Sales Order.
                 */
                fetch(
                    `${packagesPageApiUrl}?salesOrderId=${encodeURIComponent(
                        salesOrderId
                    )}`
                )

            ]);


            if (!orderResponse.ok) {

                throw new Error(
                    "Failed to load sales order."
                );
            }


            if (!packagesResponse.ok) {

                throw new Error(
                    "Failed to load existing packages."
                );
            }


            const order =
                await orderResponse.json();


            const packages =
                await packagesResponse.json();


            const orderItems =
                order.items || [];


            if (orderItems.length === 0) {

                container.innerHTML = `
                    <p class="muted">
                        This sales order has no items.
                    </p>
                `;

                return;
            }


            // =================================================
            // ONLY GOODS + INVENTORY TRACKED
            // =================================================

            const packageableOrderItems =
                orderItems.filter(
                    orderItem => {

                        const item =
                            orderItem.item || {};


                        const itemType =
                            String(
                                item.itemType || ""
                            ).toUpperCase();


                        return (
                            itemType === "GOODS" &&
                            item.trackInventory === true
                        );
                    }
                );


            if (
                packageableOrderItems.length === 0
            ) {

                selectedOrderItems = [];


                container.innerHTML = `
                    <p class="muted">
                        This sales order has no
                        inventory-tracked goods to package.
                    </p>
                `;

                return;
            }


            // =================================================
            // BUILD ORDER LINES
            // =================================================

            selectedOrderItems =
                packageableOrderItems.map(
                    (orderItem, index) => {

                        const item =
                            orderItem.item || {};


                        return {

                            lineId:
                                orderItem.id ?? null,

                            lineIndex:
                                index,

                            itemId:
                                Number(
                                    item.id ??
                                    orderItem.itemId
                                ),

                            name:
                                item.name ||
                                "Unknown Item",

                            sku:
                                item.sku ||
                                "-",

                            ordered:
                                Number(
                                    orderItem.quantity || 0
                                ),

                            packed:
                                0,

                            remaining:
                                Number(
                                    orderItem.quantity || 0
                                )
                        };

                    }
                );


            // =================================================
            // CALCULATE PACKED
            // PACKED + SHIPPED COUNT
            // CANCELLED DOES NOT COUNT
            // =================================================

            const orderPackages =
                Array.isArray(packages)
                    ? packages.filter(pkg => {

                        const packageOrderId =
                            Number(
                                pkg.salesOrder?.id ??
                                pkg.salesOrderId
                            );


                        const status =
                            (
                                pkg.status || ""
                            ).toUpperCase();


                        return (
                            packageOrderId ===
                                Number(salesOrderId) &&
                            status !== "CANCELLED"
                        );

                    })
                    : [];


            orderPackages.forEach(pkg => {

                (
                    pkg.items || []
                ).forEach(packageItem => {

                    const rawLineId =
                        packageItem.salesOrderItemId ??
                        packageItem.salesOrderItem?.id;


                    if (
                        rawLineId === null ||
                        rawLineId === undefined
                    ) {
                        return;
                    }


                    const packageLineId =
                        Number(rawLineId);


                    if (
                        !Number.isFinite(
                            packageLineId
                        )
                    ) {
                        return;
                    }


                    const matchingLine =
                        selectedOrderItems.find(
                            line =>
                                line.lineId !== null &&
                                Number(
                                    line.lineId
                                ) === packageLineId
                        );


                    if (matchingLine) {

                        matchingLine.packed +=
                            Number(
                                packageItem.quantity || 0
                            );
                    }

                });

            });


            // =================================================
            // REMAINING
            // =================================================

            selectedOrderItems.forEach(
                line => {

                    line.remaining =
                        Math.max(
                            0,
                            line.ordered -
                            line.packed
                        );

                }
            );


            // =================================================
            // RENDER ITEMS
            // =================================================

            container.innerHTML =
                selectedOrderItems.map(
                    line => {

                        const displayLineId =
                            line.lineId ??
                            `Line ${
                                line.lineIndex + 1
                            }`;


                        return `
                            <div
                                class="package-item-row"
                                style="
                                    display:grid;
                                    grid-template-columns:
                                        2fr 1.5fr 1fr;
                                    gap:12px;
                                    align-items:center;
                                    margin-bottom:12px;
                                    padding:12px;
                                    border:1px solid #ddd;
                                    border-radius:8px;
                                "
                            >

                                <div>

                                    <strong>
                                        ${escapeHtml(
                                            line.name
                                        )}
                                    </strong>


                                    <div class="muted">
                                        SKU:
                                        ${escapeHtml(
                                            line.sku
                                        )}
                                    </div>


                                    <div class="muted">
                                        Sales Order Line ID:
                                        ${escapeHtml(
                                            displayLineId
                                        )}
                                    </div>

                                </div>


                                <div>

                                    <div>
                                        Ordered:
                                        <strong>
                                            ${line.ordered}
                                        </strong>
                                    </div>


                                    <div>
                                        Packed:
                                        <strong>
                                            ${line.packed}
                                        </strong>
                                    </div>


                                    <div>
                                        To Be Packed:
                                        <strong>
                                            ${line.remaining}
                                        </strong>
                                    </div>

                                </div>


                                <div>

                                    <label
                                        for="quantity_${line.lineIndex}"
                                    >
                                        Package Qty
                                    </label>


                                    <input
                                        type="number"
                                        id="quantity_${line.lineIndex}"
                                        class="package-quantity"
                                        data-line-id="${
                                            line.lineId ?? ""
                                        }"
                                        data-item-id="${
                                            line.itemId
                                        }"
                                        data-max-quantity="${
                                            line.remaining
                                        }"
                                        min="0"
                                        max="${
                                            line.remaining
                                        }"
                                        step="1"
                                        value="0"
                                        style="width:100%;"
                                        ${
                                            line.remaining === 0
                                                ? "disabled"
                                                : ""
                                        }
                                    >

                                </div>

                            </div>
                        `;

                    }
                ).join("");


        } catch (error) {

            console.error(error);


            container.innerHTML = `
                <p class="error-message">
                    ${escapeHtml(
                        error.message ||
                        "Failed to load sales order items."
                    )}
                </p>
            `;
        }
    }


    // =====================================================
    // CREATE PACKAGE
    // =====================================================

    async function createPackage(event) {

        event.preventDefault();

        hideError();


        const salesOrderId =
            Number(
                document.getElementById(
                    "salesOrderId"
                )?.value
            );


        const weight =
            Number(
                document.getElementById(
                    "weight"
                )?.value
            );


        const length =
            Number(
                document.getElementById(
                    "length"
                )?.value
            );


        const width =
            Number(
                document.getElementById(
                    "width"
                )?.value
            );


        const height =
            Number(
                document.getElementById(
                    "height"
                )?.value
            );


        // =================================================
        // VALIDATION
        // =================================================

        if (
            !Number.isSafeInteger(
                salesOrderId
            ) ||
            salesOrderId <= 0
        ) {

            showError(
                "Please select a valid Sales Order."
            );

            return;
        }


        if (
            !Number.isFinite(weight) ||
            weight <= 0
        ) {

            showError(
                "Weight must be greater than 0."
            );

            return;
        }


        if (
            !Number.isFinite(length) ||
            length <= 0 ||

            !Number.isFinite(width) ||
            width <= 0 ||

            !Number.isFinite(height) ||
            height <= 0
        ) {

            showError(
                "All dimensions must be greater than 0."
            );

            return;
        }


        // =================================================
        // ITEM QUANTITIES
        // =================================================

        const quantityInputs =
            document.querySelectorAll(
                ".package-quantity"
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
                input.dataset.lineId;


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

                showError(
                    "Quantity must be a non-negative whole number."
                );

                return;
            }


            if (
                quantity > maxQuantity
            ) {

                showError(
                    `Quantity cannot exceed the remaining quantity (${maxQuantity}).`
                );

                return;
            }


            if (quantity > 0) {

                if (!lineId) {

                    showError(
                        "Sales Order line ID is missing."
                    );

                    return;
                }


                if (
                    !Number.isSafeInteger(
                        itemId
                    ) ||
                    itemId <= 0
                ) {

                    showError(
                        "Invalid item ID."
                    );

                    return;
                }


                items.push({

                    itemId,

                    salesOrderItemId:
                        Number(lineId),

                    quantity

                });
            }
        }


        if (
            items.length === 0
        ) {

            showError(
                "Please enter a quantity for at least one item."
            );

            return;
        }


        // =================================================
        // REQUEST
        // =================================================

        const requestBody = {

            salesOrderId,

            weight,

            length,

            width,

            height,

            items
        };


        const submitButton =
            document.querySelector(
                "#createPackageForm button[type='submit']"
            );


        if (submitButton) {
            submitButton.disabled = true;
        }


        try {

            const response =
                await fetch(
                    packagesPageApiUrl,
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
                    .catch(() => ({}));


            if (!response.ok) {

                throw new Error(
                    result.error ||
                    result.message ||
                    "Failed to create package."
                );
            }


            alert(
                `Package ${
                    result.packageNumber || ""
                } created successfully!`
            );


            closeCreatePackage();


            /*
             * Only refresh the global Packages page.
             *
             * Sales Order Details has its own
             * loadPackages() function.
             */

            const statusFilter =
                document.getElementById(
                    "statusFilter"
                );


            if (statusFilter) {

                await loadPackages();
            }


        } catch (error) {

            console.error(error);


            showError(
                error.message ||
                "Failed to create package."
            );


        } finally {

            if (submitButton) {
                submitButton.disabled = false;
            }
        }
    }


    // =====================================================
    // OPEN CREATE PACKAGE
    // =====================================================

    function openCreatePackage() {

        const modal =
            document.getElementById(
                "createPackageModal"
            );


        const form =
            document.getElementById(
                "createPackageForm"
            );


        const container =
            document.getElementById(
                "orderItemsContainer"
            );


        const salesOrderSelect =
            document.getElementById(
                "salesOrderId"
            );


        if (modal) {
            modal.style.display = "flex";
        }


        if (form) {
            form.reset();
        }


        selectedOrderItems = [];


        /*
         * If this modal is opened from
         * Sales Order Details page, the URL is:
         *
         * salesOrderDetails.jsp?id=64
         *
         * Automatically select SO #64.
         */

        const currentPageSalesOrderId =
            new URLSearchParams(
                window.location.search
            ).get("id");


        if (
            currentPageSalesOrderId &&
            salesOrderSelect
        ) {

            salesOrderSelect.innerHTML = `
                <option value="${escapeHtml(
                    currentPageSalesOrderId
                )}">
                    SO #${escapeHtml(
                        currentPageSalesOrderId
                    )}
                </option>
            `;


            salesOrderSelect.value =
                String(
                    currentPageSalesOrderId
                );


            /*
             * Automatically load the items
             * and already-packed quantities.
             */

            loadSalesOrderItems();
        }


        if (
            container &&
            !currentPageSalesOrderId
        ) {

            container.innerHTML = `
                <p class="muted">
                    Select a sales order first.
                </p>
            `;
        }


        hideError();
    }


    // =====================================================
    // CLOSE CREATE PACKAGE
    // =====================================================

    function closeCreatePackage() {

        const modal =
            document.getElementById(
                "createPackageModal"
            );


        if (modal) {

            modal.style.display =
                "none";
        }
    }


    // =====================================================
    // VIEW PACKAGE
    // =====================================================

    function viewPackage(id) {

        window.location.href =
            `/erpflow/packageDetails.jsp?id=${
                encodeURIComponent(id)
            }`;
    }


    // =====================================================
    // EDIT PACKAGE
    // =====================================================

    function editPackage(id) {

        if (
            !Number.isSafeInteger(
                Number(id)
            ) ||
            Number(id) <= 0
        ) {

            alert(
                "Invalid package ID."
            );

            return;
        }


        window.location.href =
            `/erpflow/editPackage.jsp?id=${
                encodeURIComponent(id)
            }`;
    }


    // =====================================================
    // STATUS CLASS
    // =====================================================

    function getStatusClass(status) {

        if (!status) {
            return "";
        }


        switch (
            status.toUpperCase()
        ) {

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


    // =====================================================
    // NUMBER FORMAT
    // =====================================================

    function formatNumber(value) {

        const number =
            Number(value);


        if (
            !Number.isFinite(number)
        ) {

            return "-";
        }


        return number.toFixed(2);
    }


    // =====================================================
    // ERROR
    // =====================================================

    function showError(message) {

        const errorElement =
            document.getElementById(
                "createPackageError"
            );


        if (!errorElement) {

            alert(message);

            return;
        }


        errorElement.textContent =
            message;


        errorElement.style.display =
            "block";
    }


    function hideError() {

        const errorElement =
            document.getElementById(
                "createPackageError"
            );


        if (!errorElement) {
            return;
        }


        errorElement.textContent =
            "";


        errorElement.style.display =
            "none";
    }


    // =====================================================
    // HTML ESCAPE
    // =====================================================

    function escapeHtml(value) {

        if (
            value === null ||
            value === undefined
        ) {

            return "";
        }


        return String(value)

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
    // EXPOSE ONLY REQUIRED FUNCTIONS
    // =====================================================

    window.openCreatePackage =
        openCreatePackage;

    window.closeCreatePackage =
        closeCreatePackage;

    window.viewPackage =
        viewPackage;

    window.editPackage =
        editPackage;

})();