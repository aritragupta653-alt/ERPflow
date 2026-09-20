const apiBaseUrl = "/erpflow/api/sales-orders";
const customersApiUrl = "/erpflow/api/customers";
const itemsApiUrl = "/erpflow/api/items";


document.addEventListener("DOMContentLoaded", function () {

    loadCustomers();

    loadItems();

    loadSalesOrders();

    setupEventListeners();

    calculateOrderTotals();

});



/* =========================================================
   EVENT LISTENERS
   ========================================================= */

function setupEventListeners() {

    const addItemButton =
        document.getElementById("addItemButton");

    const createSalesOrderButton =
        document.getElementById("createSalesOrderButton");

    const taxRate =
        document.getElementById("taxRate");

    const searchInput =
        document.getElementById("searchInput");


    if (addItemButton) {

        addItemButton.addEventListener(
            "click",
            addItemRow
        );

    }


    if (createSalesOrderButton) {

        createSalesOrderButton.addEventListener(
            "click",
            createSalesOrder
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


    /*
     * Recalculate totals whenever quantity
     * or selling price changes.
     */

    document.addEventListener(
        "input",
        function (event) {

            if (
                event.target.name === "quantity" ||
                event.target.name === "sellingPrice"
            ) {

                calculateOrderTotals();

                updateRowTotal(event.target.closest("tr"));

            }

        }
    );

}


/* =========================================================
   LOAD CUSTOMERS
   ========================================================= */

async function loadCustomers() {

    try {

        const response =
            await fetch(customersApiUrl);

        if (!response.ok) {

            throw new Error(
                "Failed to load customers"
            );

        }

        const customers =
            await response.json();

        const select =
            document.getElementById("customerSelect");


        if (!select) {
            return;
        }


        select.innerHTML =
            '<option value="">Select Customer</option>';


        customers.forEach(function (customer) {

            const option =
                document.createElement("option");

            option.value = customer.id;

            option.textContent =
                `${customer.name} (${customer.email || "No email"})`;

            select.appendChild(option);

        });

    } catch (error) {

        console.error(
            "Error loading customers:",
            error
        );

        showMessage(
            "Unable to load customers.",
            "error"
        );

    }

}


/* =========================================================
   LOAD ITEMS
   ========================================================= */

async function loadItems() {

    try {

        const response =
            await fetch(itemsApiUrl);

        if (!response.ok) {

            throw new Error(
                "Failed to load items"
            );

        }

        const items =
            await response.json();

        window.erpflowItems = items || [];

    } catch (error) {

        console.error(
            "Error loading items:",
            error
        );

        window.erpflowItems = [];

        showMessage(
            "Unable to load items.",
            "error"
        );

    }

}


/* =========================================================
   ADD ITEM ROW
   ========================================================= */

function addItemRow() {

    const tbody =
        document.getElementById(
            "salesOrderItemsTableBody"
        );

    if (!tbody) {
        return;
    }


    const row =
        document.createElement("tr");

    row.className =
        "sales-item-row";


    row.innerHTML = `

        <td>

            <select
                name="itemId"
                class="item-select"
                required
            >

                <option value="">
                    Select Item
                </option>

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


        <td>

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

            <span class="line-total">
                ₹0.00
            </span>

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


    populateItemSelect(
        row.querySelector(".item-select")
    );


    const itemSelect =
        row.querySelector(".item-select");


    itemSelect.addEventListener(
        "change",
        function () {

            populateItemDetails(
                row,
                itemSelect.value
            );

        }
    );


    const removeButton =
        row.querySelector(
            ".remove-item-button"
        );


    removeButton.addEventListener(
        "click",
        function () {

            row.remove();

            calculateOrderTotals();

        }
    );


    calculateOrderTotals();

}


/* =========================================================
   POPULATE ITEM SELECT
   ========================================================= */

function populateItemSelect(select) {

    if (!select) {
        return;
    }


    const items =
        window.erpflowItems || [];


    items.forEach(function (item) {

        const option =
            document.createElement("option");

        option.value = item.id;

        option.textContent =
            `${item.name} - ${item.sku}`;

        select.appendChild(option);

    });

}


/* =========================================================
   POPULATE ITEM DETAILS
   ========================================================= */

function populateItemDetails(row, itemId) {

    if (!row || !itemId) {
        return;
    }


    const items =
        window.erpflowItems || [];


    const item =
        items.find(function (currentItem) {

            return String(currentItem.id) ===
                String(itemId);

        });


    if (!item) {
        return;
    }


    const skuInput =
        row.querySelector(
            'input[name="sku"]'
        );


    const stockInput =
        row.querySelector(
            'input[name="availableStock"]'
        );


    const priceInput =
        row.querySelector(
            'input[name="sellingPrice"]'
        );


    if (skuInput) {

        skuInput.value =
            item.sku || "";

    }


    /*
     * Different backend versions may use
     * different stock field names.
     */

    if (stockInput) {

        const stock =
            item.stockQuantity ??
            item.quantity ??
            item.availableStock ??
            0;

        stockInput.value = stock;

    }


    if (priceInput) {

        priceInput.value =
            item.sellingPrice ??
            0;

    }


    updateRowTotal(row);

    calculateOrderTotals();

}


/* =========================================================
   UPDATE LINE TOTAL
   ========================================================= */

function updateRowTotal(row) {

    if (!row) {
        return;
    }


    const quantityInput =
        row.querySelector(
            'input[name="quantity"]'
        );


    const priceInput =
        row.querySelector(
            'input[name="sellingPrice"]'
        );


    const lineTotalElement =
        row.querySelector(
            ".line-total"
        );


    if (
        !quantityInput ||
        !priceInput ||
        !lineTotalElement
    ) {

        return;

    }


    const quantity =
        parseFloat(quantityInput.value) || 0;


    const price =
        parseFloat(priceInput.value) || 0;


    const lineTotal =
        quantity * price;


    lineTotalElement.textContent =
        formatCurrency(lineTotal);

}


/* =========================================================
   CALCULATE ORDER TOTALS
   ========================================================= */

function calculateOrderTotals() {

    let subtotal = 0;


    const rows =
        document.querySelectorAll(
            ".sales-item-row"
        );


    rows.forEach(function (row) {

        const quantityInput =
            row.querySelector(
                'input[name="quantity"]'
            );


        const priceInput =
            row.querySelector(
                'input[name="sellingPrice"]'
            );


        if (
            !quantityInput ||
            !priceInput
        ) {

            return;

        }


        const quantity =
            parseFloat(quantityInput.value) || 0;


        const price =
            parseFloat(priceInput.value) || 0;


        subtotal +=
            quantity * price;


        updateRowTotal(row);

    });


    const taxRateInput =
        document.getElementById(
            "taxRate"
        );


    const taxRate =
        parseFloat(
            taxRateInput?.value || 0
        );


    const taxAmount =
        subtotal * taxRate / 100;


    const totalAmount =
        subtotal + taxAmount;


    const subtotalDisplay =
        document.getElementById(
            "subtotalDisplay"
        );


    const taxRateDisplay =
        document.getElementById(
            "taxRateDisplay"
        );


    const taxAmountDisplay =
        document.getElementById(
            "taxAmountDisplay"
        );


    const totalAmountDisplay =
        document.getElementById(
            "totalAmountDisplay"
        );


    if (subtotalDisplay) {

        subtotalDisplay.textContent =
            formatCurrency(subtotal);

    }


    if (taxRateDisplay) {

        taxRateDisplay.textContent =
            taxRate.toFixed(2);

    }


    if (taxAmountDisplay) {

        taxAmountDisplay.textContent =
            formatCurrency(taxAmount);

    }


    if (totalAmountDisplay) {

        totalAmountDisplay.textContent =
            formatCurrency(totalAmount);

    }

}


/* =========================================================
   CREATE SALES ORDER
   ========================================================= */

/* =========================================================
   CREATE SALES ORDER
   ========================================================= */

async function createSalesOrder() {

    const customerSelect =
        document.getElementById("customerSelect");

    const taxRateInput =
        document.getElementById("taxRate");

    const rows =
        document.querySelectorAll(".sales-item-row");

    // Customer validation
    if (!customerSelect || !customerSelect.value) {
        showMessage("Please select a customer.", "error");
        return;
    }

    // Item validation
    if (rows.length === 0) {
        showMessage("Please add at least one item.", "error");
        return;
    }

    const items = [];
    let hasValidationError = false;

    rows.forEach(function (row) {

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
        const quantity = parseInt(quantityInput.value, 10);
        const sellingPrice = parseFloat(priceInput.value);

        // Item selection validation
        if (!itemId) {
            showMessage(
                "Please select an item in every row.",
                "error"
            );

            hasValidationError = true;
            return;
        }

        // Quantity validation
        if (!Number.isInteger(quantity) || quantity <= 0) {
            showMessage(
                "Quantity must be greater than 0.",
                "error"
            );

            hasValidationError = true;
            return;
        }

        // Selling price validation
        if (
            Number.isNaN(sellingPrice) ||
            sellingPrice < 0
        ) {
            showMessage(
                "Selling price cannot be negative.",
                "error"
            );

            hasValidationError = true;
            return;
        }

        /*
         * Duplicate item validation removed.
         *
         * The same item can now be included in multiple
         * rows with separate quantities and selling prices.
         */

        items.push({
            itemId: itemId,
            quantity: quantity,
            sellingPrice: sellingPrice
        });

    });

    if (hasValidationError) {
        return;
    }

    // Tax validation
    const taxRate =
        parseFloat(taxRateInput?.value || 0);

    if (
        Number.isNaN(taxRate) ||
        taxRate < 0 ||
        taxRate > 100
    ) {
        showMessage(
            "Tax rate must be between 0% and 100%.",
            "error"
        );

        return;
    }

    // Build request
    const orderData = {
        customerId: parseInt(customerSelect.value, 10),
        taxRate: taxRate,
        items: items
    };

    try {

        const createButton =
            document.getElementById("createSalesOrderButton");

        if (createButton) {
            createButton.disabled = true;
            createButton.textContent = "Creating...";
        }

        const response = await fetch(apiBaseUrl, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(orderData)
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(
                result.message ||
                "Failed to create sales order"
            );
        }

        // Success message
        let successMessage =
            "Sales order created successfully.";

        if (result.totalAmount !== undefined) {
            successMessage +=
                ` Total: ${formatCurrency(result.totalAmount)}`;
        }

        showMessage(successMessage, "success");

        // Reset form
        resetSalesOrderForm();

        // Reload orders
        await loadSalesOrders();

    } catch (error) {

        console.error("Error creating sales order:", error);

        showMessage(
            error.message || "Failed to create sales order.",
            "error"
        );

    } finally {

        const createButton =
            document.getElementById("createSalesOrderButton");

        if (createButton) {
            createButton.disabled = false;
            createButton.textContent = "Create Sales Order";
        }

    }
}

/* =========================================================
   RESET FORM
   ========================================================= */

function resetSalesOrderForm() {

    const customerSelect =
        document.getElementById(
            "customerSelect"
        );


    const taxRate =
        document.getElementById(
            "taxRate"
        );


    const tbody =
        document.getElementById(
            "salesOrderItemsTableBody"
        );


    if (customerSelect) {

        customerSelect.value = "";

    }


    if (taxRate) {

        taxRate.value = "0";

    }


    if (tbody) {

        tbody.innerHTML = "";

    }


    calculateOrderTotals();

}


/* =========================================================
   LOAD SALES ORDERS
   ========================================================= */

async function loadSalesOrders() {

    try {

        const response =
            await fetch(apiBaseUrl);


        if (!response.ok) {

            throw new Error(
                "Failed to load sales orders"
            );

        }


        const orders =
            await response.json();


        window.erpflowSalesOrders =
            orders || [];


        displaySalesOrders(
            window.erpflowSalesOrders
        );


    } catch (error) {

        console.error(
            "Error loading sales orders:",
            error
        );


        showMessage(
            "Unable to load sales orders.",
            "error"
        );

    }

}


/* =========================================================
   DISPLAY SALES ORDERS
   ========================================================= */

function displaySalesOrders(orders) {

    const tbody =
        document.getElementById(
            "salesOrdersTableBody"
        );


    if (!tbody) {
        return;
    }


    tbody.innerHTML = "";


    if (
        !orders ||
        orders.length === 0
    ) {

        tbody.innerHTML = `

            <tr>

                <td
                    colspan="8"
                    style="text-align:center;"
                >
                    No sales orders found.
                </td>

            </tr>

        `;

        return;

    }


    orders.forEach(function (order) {

        const row =
            document.createElement("tr");


        const customer =
            order.customer ||
            {};


        const subtotal =
            Number(
                order.subtotal || 0
            );


        const taxRate =
            Number(
                order.taxRate || 0
            );


        const taxAmount =
            Number(
                order.taxAmount || 0
            );


        const totalAmount =
            Number(
                order.totalAmount || 0
            );


        row.innerHTML = `

            <td>
                ${escapeHtml(order.id)}
            </td>


            <td>
                ${escapeHtml(
                    customer.name || "-"
                )}
            </td>


            <td>
                ${formatDate(
                    order.orderDate
                )}
            </td>


            <td>
                ${formatCurrency(
                    subtotal
                )}
            </td>


            <td>
                ${taxRate.toFixed(2)}%
                <br>
                <small>
                    ${formatCurrency(
                        taxAmount
                    )}
                </small>
            </td>


            <td>
                <strong>
                    ${formatCurrency(
                        totalAmount
                    )}
                </strong>
            </td>


            <td>
                ${escapeHtml(
                    order.status || "-"
                )}
            </td>


            <td>

                <a
                    class="action-button"
                    href="/erpflow/salesOrderDetails.jsp?id=${encodeURIComponent(order.id)}"
                >
                    View
                </a>

            </td>

        `;


        tbody.appendChild(row);

    });

}


/* =========================================================
   SEARCH / FILTER
   ========================================================= */

function filterSalesOrders() {

    const searchInput =
        document.getElementById(
            "searchInput"
        );


    if (!searchInput) {
        return;
    }


    const searchText =
        searchInput.value
            .trim()
            .toLowerCase();


    const orders =
        window.erpflowSalesOrders ||
        [];


    if (!searchText) {

        displaySalesOrders(
            orders
        );

        return;

    }


    const filteredOrders =
        orders.filter(
            function (order) {

                const customer =
                    order.customer ||
                    {};


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


                return searchableText
                    .includes(searchText);

            }
        );


    displaySalesOrders(
        filteredOrders
    );

}


/* =========================================================
   DATE FORMATTER
   ========================================================= */

function formatDate(dateValue) {

    if (!dateValue) {

        return "-";

    }


    let date;


    /*
     * Jackson LocalDateTime format:
     *
     * [2026,9,18,12,46,59,954292000]
     */

    if (Array.isArray(dateValue)) {

        const year =
            dateValue[0];


        const month =
            (dateValue[1] || 1) - 1;


        const day =
            dateValue[2] || 1;


        const hour =
            dateValue[3] || 0;


        const minute =
            dateValue[4] || 0;


        const second =
            dateValue[5] || 0;


        date =
            new Date(
                year,
                month,
                day,
                hour,
                minute,
                second
            );

    } else {

        date =
            new Date(dateValue);

    }


    if (
        Number.isNaN(
            date.getTime()
        )
    ) {

        return "-";

    }


    return date.toLocaleString(
        "en-IN",
        {
            day: "2-digit",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        }
    );

}


/* =========================================================
   CURRENCY FORMATTER
   ========================================================= */

function formatCurrency(amount) {

    return new Intl.NumberFormat(
        "en-IN",
        {
            style: "currency",
            currency: "INR",
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }
    ).format(
        Number(amount) || 0
    );

}


/* =========================================================
   MESSAGE
   ========================================================= */

function showMessage(
    message,
    type = "info"
) {

    const messageBox =
        document.getElementById(
            "messageBox"
        );


    if (!messageBox) {
        return;
    }


    messageBox.innerHTML = `

        <div class="message ${type}">

            ${escapeHtml(message)}

        </div>

    `;


    setTimeout(
        function () {

            messageBox.innerHTML = "";

        },
        5000
    );

}


/* =========================================================
   HTML ESCAPE
   ========================================================= */

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