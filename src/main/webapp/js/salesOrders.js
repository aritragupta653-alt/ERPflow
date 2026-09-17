let allSalesOrders = [];
let allCustomers = [];
let allItems = [];


document.addEventListener("DOMContentLoaded", () => {

    loadCustomers();

    loadItems();

    loadSalesOrders();

    setupSalesOrderForm();

    setupAddItemButton();

    setupSearch();

});


// ========================================
// LOAD CUSTOMERS
// ========================================

async function loadCustomers() {

    try {

        const response =
            await fetch(
                contextPath + "/api/customers"
            );


        if (!response.ok) {
            throw new Error(
                "Failed to load customers"
            );
        }


        allCustomers =
            await response.json();


        populateCustomers();


    } catch (error) {

        console.error(error);

        showMessage(
            "Failed to load customers.",
            "error"
        );
    }
}


// ========================================
// POPULATE CUSTOMERS
// ========================================

function populateCustomers() {

    const select =
        document.getElementById(
            "customerSelect"
        );


    select.innerHTML = "";


    const defaultOption =
        document.createElement("option");

    defaultOption.value = "";

    defaultOption.textContent =
        "Select Customer";

    defaultOption.disabled = true;

    defaultOption.selected = true;

    select.appendChild(
        defaultOption
    );


    allCustomers.forEach(customer => {

        if (customer.status &&
            customer.status !== "ACTIVE") {
            return;
        }


        const option =
            document.createElement("option");


        option.value =
            customer.id;


        option.textContent =
            customer.name;


        select.appendChild(option);

    });

}


// ========================================
// LOAD ITEMS
// ========================================

async function loadItems() {

    try {

        const response =
            await fetch(
                contextPath + "/api/items"
            );


        if (!response.ok) {
            throw new Error(
                "Failed to load items"
            );
        }


        allItems =
            await response.json();


        populateAllItemSelects();


    } catch (error) {

        console.error(error);

        showMessage(
            "Failed to load items.",
            "error"
        );
    }
}


// ========================================
// CREATE ITEM SELECT
// ========================================

function createItemSelect() {

    const select =
        document.createElement("select");


    select.className =
        "item-select";


    select.required = true;


    const defaultOption =
        document.createElement("option");


    defaultOption.value = "";

    defaultOption.textContent =
        "Select Item";

    defaultOption.disabled = true;

    defaultOption.selected = true;


    select.appendChild(
        defaultOption
    );


    allItems.forEach(item => {

        if (item.status &&
            item.status !== "ACTIVE") {
            return;
        }


        const option =
            document.createElement("option");


        option.value =
            item.id;


        option.textContent =
            `${item.name} - ${item.sku}`;


        option.dataset.price =
            item.sellingPrice || 0;


        select.appendChild(option);

    });


    // Automatically fill selling price

    select.addEventListener(
        "change",
        () => {

            const selectedOption =
                select.options[
                    select.selectedIndex
                ];


            const price =
                selectedOption.dataset.price;


            const row =
                select.closest(
                    ".sales-item-row"
                );


            const priceInput =
                row.querySelector(
                    ".price-input"
                );


            if (price &&
                Number(price) > 0) {

                priceInput.value =
                    price;
            }

        }
    );


    return select;
}


// ========================================
// POPULATE ITEM SELECTS
// ========================================

function populateAllItemSelects() {

    document
        .querySelectorAll(
            ".item-select"
        )
        .forEach(select => {

            const currentValue =
                select.value;


            const newSelect =
                createItemSelect();


            newSelect.value =
                currentValue;


            select.replaceWith(
                newSelect
            );

        });

}


// ========================================
// ADD ITEM ROW
// ========================================

function setupAddItemButton() {

    const button =
        document.getElementById(
            "addItemButton"
        );


    button.addEventListener(
        "click",
        () => {

            const container =
                document.getElementById(
                    "itemContainer"
                );


            const row =
                document.createElement(
                    "div"
                );


            row.className =
                "sales-item-row";


            const itemSelect =
                createItemSelect();


            const quantity =
                document.createElement(
                    "input"
                );


            quantity.type =
                "number";

            quantity.className =
                "quantity-input";

            quantity.placeholder =
                "Quantity";

            quantity.min = "1";

            quantity.required = true;


            const price =
                document.createElement(
                    "input"
                );


            price.type =
                "number";

            price.className =
                "price-input";

            price.placeholder =
                "Selling Price";

            price.min = "0";

            price.step = "0.01";

            price.required = true;


            const removeButton =
                document.createElement(
                    "button"
                );


            removeButton.type =
                "button";

            removeButton.className =
                "btn small-btn danger-btn";

            removeButton.textContent =
                "Remove";


            removeButton.addEventListener(
                "click",
                () => {

                    row.remove();

                }
            );


            row.appendChild(
                itemSelect
            );

            row.appendChild(
                quantity
            );

            row.appendChild(
                price
            );

            row.appendChild(
                removeButton
            );


            container.appendChild(
                row
            );

        }
    );
}


// ========================================
// CREATE SALES ORDER
// ========================================

function setupSalesOrderForm() {

    const form =
        document.getElementById(
            "salesOrderForm"
        );


    form.addEventListener(
        "submit",
        async event => {

            event.preventDefault();


            const customerId =
                document.getElementById(
                    "customerSelect"
                ).value;


            if (!customerId) {

                showMessage(
                    "Please select a customer.",
                    "error"
                );

                return;
            }


            const rows =
                document.querySelectorAll(
                    ".sales-item-row"
                );


            const items = [];


            for (const row of rows) {

                const itemId =
                    row.querySelector(
                        ".item-select"
                    ).value;


                const quantity =
                    Number(
                        row.querySelector(
                            ".quantity-input"
                        ).value
                    );


                const sellingPrice =
                    Number(
                        row.querySelector(
                            ".price-input"
                        ).value
                    );


                if (!itemId) {

                    showMessage(
                        "Please select an item for every row.",
                        "error"
                    );

                    return;
                }


                if (quantity <= 0) {

                    showMessage(
                        "Quantity must be greater than zero.",
                        "error"
                    );

                    return;
                }


                if (sellingPrice < 0) {

                    showMessage(
                        "Selling price cannot be negative.",
                        "error"
                    );

                    return;
                }


                items.push({

                    itemId:
                        Number(itemId),

                    quantity:
                        quantity,

                    sellingPrice:
                        sellingPrice

                });

            }


            if (items.length === 0) {

                showMessage(
                    "Add at least one item.",
                    "error"
                );

                return;
            }


            const requestBody = {

                customerId:
                    Number(customerId),

                items:
                    items

            };


            try {

                const response =
                    await fetch(
                        contextPath +
                        "/api/sales-orders",
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


                let data = {};

                try {

                    data =
                        await response.json();

                } catch (_) {
                    // Empty response
                }


                if (!response.ok) {

                    throw new Error(
                        data.error ||
                        "Failed to create sales order"
                    );

                }


                showMessage(
                    "Sales Order created successfully.",
                    "success"
                );


                form.reset();


                // Restore one item row

                const container =
                    document.getElementById(
                        "itemContainer"
                    );


                container.innerHTML = "";


                addInitialItemRow();


                await loadSalesOrders();


            } catch (error) {

                console.error(error);

                showMessage(
                    error.message,
                    "error"
                );
            }

        }
    );
}


// ========================================
// INITIAL ITEM ROW
// ========================================

function addInitialItemRow() {

    const container =
        document.getElementById(
            "itemContainer"
        );


    const row =
        document.createElement(
            "div"
        );


    row.className =
        "sales-item-row";


    const itemSelect =
        createItemSelect();


    const quantity =
        document.createElement(
            "input"
        );


    quantity.type =
        "number";

    quantity.className =
        "quantity-input";

    quantity.placeholder =
        "Quantity";

    quantity.min = "1";

    quantity.required = true;


    const price =
        document.createElement(
            "input"
        );


    price.type =
        "number";

    price.className =
        "price-input";

    price.placeholder =
        "Selling Price";

    price.min = "0";

    price.step = "0.01";

    price.required = true;


    row.appendChild(
        itemSelect
    );

    row.appendChild(
        quantity
    );

    row.appendChild(
        price
    );


    container.appendChild(
        row
    );

}


// ========================================
// LOAD SALES ORDERS
// ========================================

async function loadSalesOrders() {

    try {

        const response =
            await fetch(
                contextPath +
                "/api/sales-orders"
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load sales orders"
            );

        }


        allSalesOrders =
            await response.json();


        renderSalesOrders(
            allSalesOrders
        );


    } catch (error) {

        console.error(error);

        showMessage(
            "Failed to load sales orders.",
            "error"
        );
    }
}


// ========================================
// RENDER SALES ORDERS
// ========================================

function renderSalesOrders(
    orders
) {

    const tableBody =
        document.getElementById(
            "salesOrdersTableBody"
        );


    const count =
        document.getElementById(
            "salesOrderCount"
        );


    tableBody.innerHTML = "";

    count.textContent =
        orders.length;


    if (orders.length === 0) {

        const row =
            document.createElement("tr");


        const cell =
            document.createElement("td");


        cell.colSpan = 5;

        cell.textContent =
            "No sales orders found.";

        cell.style.textAlign =
            "center";


        row.appendChild(cell);

        tableBody.appendChild(row);

        return;
    }


    orders.forEach(order => {

        const row =
            document.createElement("tr");


        // ID

        const idCell =
            document.createElement("td");

        idCell.textContent =
            order.id;

        row.appendChild(idCell);


        // Customer

        const customerCell =
            document.createElement("td");


        customerCell.textContent =
            order.customer
                ? order.customer.name
                : "-";


        row.appendChild(
            customerCell
        );


        // Date

        const dateCell =
            document.createElement("td");


        dateCell.textContent =
            formatDate(
                order.orderDate
            );


        row.appendChild(
            dateCell
        );


        // Status

        const statusCell =
            document.createElement("td");


        const badge =
            document.createElement("span");


        badge.className =
            "status-badge " +
            getStatusClass(
                order.status
            );


        badge.textContent =
            order.status || "UNKNOWN";


        statusCell.appendChild(
            badge
        );


        row.appendChild(
            statusCell
        );


        // Action

        const actionCell =
            document.createElement("td");


        const viewButton =
            document.createElement(
                "button"
            );


        viewButton.type =
            "button";


        viewButton.className =
            "btn small-btn";


        viewButton.textContent =
            "View";


        viewButton.addEventListener(
            "click",
            () => viewSalesOrder(
                order.id
            )
        );


        actionCell.appendChild(
            viewButton
        );


        row.appendChild(
            actionCell
        );


        tableBody.appendChild(
            row
        );

    });

}


// ========================================
// VIEW SALES ORDER
// ========================================

async function viewSalesOrder(
    id
) {

    try {

        const response =
            await fetch(
                contextPath +
                "/api/sales-orders/" +
                id
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load sales order"
            );

        }


        const order =
            await response.json();


        showSalesOrderDetails(
            order
        );


    } catch (error) {

        console.error(error);

        showMessage(
            "Unable to load sales order details.",
            "error"
        );
    }
}


// ========================================
// SALES ORDER DETAILS
// ========================================

function showSalesOrderDetails(
    order
) {

    let message =
        `Sales Order #${order.id}\n\n`;


    message +=
        `Customer: ${
            order.customer
                ? order.customer.name
                : "-"
        }\n`;


    message +=
        `Order Date: ${
            formatDate(
                order.orderDate
            )
        }\n`;


    message +=
        `Status: ${
            order.status || "-"
        }\n`;


    // If API returns items

    if (
        Array.isArray(order.items)
    ) {

        message +=
            "\nItems:\n";


        order.items.forEach(
            item => {

                const itemName =
                    item.item
                        ? item.item.name
                        : item.itemName ||
                          "Item";


                message +=
                    `${itemName} - Qty: ${
                        item.quantity
                    } - Price: ${
                        item.sellingPrice
                    }\n`;

            }
        );

    }


    alert(message);

}


// ========================================
// SEARCH
// ========================================

function setupSearch() {

    const searchInput =
        document.getElementById(
            "searchInput"
        );


    searchInput.addEventListener(
        "input",
        () => {

            const term =
                searchInput.value
                    .toLowerCase()
                    .trim();


            if (!term) {

                renderSalesOrders(
                    allSalesOrders
                );

                return;
            }


            const filtered =
                allSalesOrders.filter(
                    order => {

                        const id =
                            String(
                                order.id ||
                                ""
                            );


                        const customer =
                            order.customer
                                ? order.customer.name
                                : "";


                        const status =
                            order.status ||
                            "";


                        return (

                            id.includes(term)

                            ||

                            customer
                                .toLowerCase()
                                .includes(term)

                            ||

                            status
                                .toLowerCase()
                                .includes(term)

                        );

                    }
                );


            renderSalesOrders(
                filtered
            );

        }
    );
}


// ========================================
// FORMAT DATE
// ========================================

function formatDate(dateValue) {
    if (!dateValue) return "-";

    // LocalDateTime returned by Jackson
    if (Array.isArray(dateValue)) {
        const [
            year,
            month,
            day,
            hour = 0,
            minute = 0,
            second = 0
        ] = dateValue;

        const date = new Date(
            year,
            month - 1,
            day,
            hour,
            minute,
            second
        );

        if (isNaN(date.getTime())) return "-";

        return date.toLocaleString();
    }

    // ISO string fallback
    const date = new Date(dateValue);

    if (isNaN(date.getTime())) return "-";

    return date.toLocaleString();
}



// ========================================
// STATUS CLASS
// ========================================

function getStatusClass(
    status
) {

    if (!status) {
        return "";
    }


    const value =
        status.toLowerCase();


    if (
        value === "created" ||
        value === "completed"
    ) {

        return "active";

    }


    if (
        value === "cancelled" ||
        value === "cancel"
    ) {

        return "inactive";

    }


    return "";
}


// ========================================
// MESSAGE
// ========================================

function showMessage(
    message,
    type
) {

    const messageBox =
        document.getElementById(
            "messageBox"
        );


    messageBox.textContent =
        message;


    messageBox.className =
        "message-box " + type;


    messageBox.style.display =
        "block";


    setTimeout(
        () => {

            messageBox.style.display =
                "none";

        },
        3000
    );
}