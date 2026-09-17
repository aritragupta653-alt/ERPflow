const purchaseOrderApi =
    "/erpflow/api/purchase-orders";

const supplierApi =
    "/erpflow/api/suppliers";

const itemApi =
    "/erpflow/api/items";


let allPurchaseOrders = [];
let allSuppliers = [];
let allItems = [];


// ========================================
// PAGE LOAD
// ========================================

document.addEventListener(
    "DOMContentLoaded",
    () => {

        loadSuppliers();

        loadItems();

        loadPurchaseOrders();

        setupForm();

        setupAddItemButton();

        setupSearch();

    }
);


// ========================================
// LOAD SUPPLIERS
// ========================================

async function loadSuppliers() {

    try {

        const response =
            await fetch(
                supplierApi
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load suppliers"
            );
        }


        allSuppliers =
            await response.json();


        populateSuppliers();


    } catch (error) {

        console.error(error);

        showMessage(
            "Failed to load suppliers.",
            "error"
        );
    }
}


// ========================================
// POPULATE SUPPLIERS
// ========================================

function populateSuppliers() {

    const select =
        document.getElementById(
            "supplierSelect"
        );


    select.innerHTML = `
        <option value="">
            Select Supplier
        </option>
    `;


    allSuppliers.forEach(
        supplier => {

            const option =
                document.createElement(
                    "option"
                );


            option.value =
                supplier.id;


            option.textContent =
                supplier.name;


            select.appendChild(
                option
            );

        }
    );
}


// ========================================
// LOAD ITEMS
// ========================================

async function loadItems() {

    try {

        const response =
            await fetch(
                itemApi
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load items"
            );
        }


        allItems =
            await response.json();


        addInitialItemRow();


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
        document.createElement(
            "select"
        );


    select.className =
        "purchase-item-select";


    select.required = true;


    const defaultOption =
        document.createElement(
            "option"
        );


    defaultOption.value = "";

    defaultOption.textContent =
        "Select Item";

    defaultOption.disabled = true;

    defaultOption.selected = true;


    select.appendChild(
        defaultOption
    );


    allItems.forEach(
        item => {

            if (
                item.status &&
                item.status !== "ACTIVE"
            ) {
                return;
            }


            const option =
                document.createElement(
                    "option"
                );


            option.value =
                item.id;


            option.textContent =
                `${item.name} - ${item.sku}`;


            option.dataset.price =
                item.purchasePrice || 0;


            select.appendChild(
                option
            );

        }
    );


    return select;
}


// ========================================
// CREATE ITEM ROW
// ========================================

function createItemRow() {

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

    quantity.min =
        "1";

    quantity.required =
        true;


    const price =
        document.createElement(
            "input"
        );


    price.type =
        "number";

    price.className =
        "price-input";

    price.placeholder =
        "Purchase Price";

    price.min =
        "0";

    price.step =
        "0.01";

    price.required =
        true;


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


    itemSelect.addEventListener(
        "change",
        () => {

            const selected =
                itemSelect.options[
                    itemSelect.selectedIndex
                ];


            price.value =
                selected.dataset.price || "";

        }
    );


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


    return row;
}


// ========================================
// INITIAL ITEM ROW
// ========================================

function addInitialItemRow() {

    const container =
        document.getElementById(
            "itemContainer"
        );


    if (
        container.children.length === 0
    ) {

        container.appendChild(
            createItemRow()
        );

    }
}


// ========================================
// ADD ITEM BUTTON
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


            container.appendChild(
                createItemRow()
            );

        }
    );
}


// ========================================
// CREATE PURCHASE ORDER
// ========================================

function setupForm() {

    const form =
        document.getElementById(
            "purchaseOrderForm"
        );


    form.addEventListener(
        "submit",
        async event => {

            event.preventDefault();


            const supplierId =
                document.getElementById(
                    "supplierSelect"
                ).value;


            if (!supplierId) {

                showMessage(
                    "Please select a supplier.",
                    "error"
                );

                return;
            }


            const rows =
                document.querySelectorAll(
                    "#itemContainer > div"
                );


            const items = [];


            for (
                const row of rows
            ) {

                const itemId =
                    row.querySelector(
                        ".purchase-item-select"
                    ).value;


                const quantity =
                    Number(
                        row.querySelector(
                            ".quantity-input"
                        ).value
                    );


                const purchasePrice =
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


                if (purchasePrice < 0) {

                    showMessage(
                        "Purchase price cannot be negative.",
                        "error"
                    );

                    return;
                }


                items.push({

                    itemId:
                        Number(itemId),

                    quantity:
                        quantity,

                    purchasePrice:
                        purchasePrice

                });

            }


            if (
                items.length === 0
            ) {

                showMessage(
                    "Add at least one item.",
                    "error"
                );

                return;
            }


            const requestBody = {

                supplierId:
                    Number(supplierId),

                items:
                    items

            };


            try {

                const response =
                    await fetch(
                        purchaseOrderApi,
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


                const data =
                    await response.json();


                if (!response.ok) {

                    throw new Error(
                        data.error ||
                        "Failed to create purchase order"
                    );
                }


                showMessage(
                    "Purchase Order created successfully.",
                    "success"
                );


                form.reset();


                document.getElementById(
                    "itemContainer"
                ).innerHTML = "";


                addInitialItemRow();


                await loadPurchaseOrders();


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
// LOAD PURCHASE ORDERS
// ========================================

async function loadPurchaseOrders() {

    try {

        const response =
            await fetch(
                purchaseOrderApi
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load purchase orders"
            );
        }


        allPurchaseOrders =
            await response.json();


        renderPurchaseOrders(
            allPurchaseOrders
        );


    } catch (error) {

        console.error(error);

        showMessage(
            "Failed to load purchase orders.",
            "error"
        );
    }
}


// ========================================
// RENDER PURCHASE ORDERS
// ========================================

function renderPurchaseOrders(
    orders
) {

    const tableBody =
        document.getElementById(
            "purchaseOrdersTableBody"
        );


    const count =
        document.getElementById(
            "purchaseOrderCount"
        );


    tableBody.innerHTML = "";

    count.textContent =
        orders.length;


    if (orders.length === 0) {

        tableBody.innerHTML = `
            <tr>
                <td
                    colspan="5"
                    style="text-align:center;"
                >
                    No purchase orders found.
                </td>
            </tr>
        `;

        return;
    }


    orders.forEach(
        order => {

            const row =
                document.createElement(
                    "tr"
                );


            const supplierName =
                order.supplier
                    ? order.supplier.name
                    : "-";


            row.innerHTML = `

                <td>
                    #${order.id}
                </td>

                <td>
                    ${supplierName}
                </td>

                <td>
                    ${formatDate(
                        order.orderDate
                    )}
                </td>

                <td>

                    <span
                        class="status-badge
                        ${getStatusClass(
                            order.status
                        )}"
                    >
                        ${order.status || "-"}
                    </span>

                </td>

                <td>

                    <button
                        class="btn small-btn"
                        onclick="viewPurchaseOrder(${order.id})"
                    >
                        View
                    </button>

                    ${
                        order.status !== "RECEIVED"
                        ?

                        `
                        <button
                            class="btn small-btn"
                            onclick="receivePurchaseOrder(${order.id})"
                        >
                            Receive
                        </button>
                        `

                        :

                        ""
                    }

                </td>

            `;


            tableBody.appendChild(
                row
            );

        }
    );
}


// ========================================
// VIEW PURCHASE ORDER
// ========================================

function viewPurchaseOrder(id) {

    window.location.href =
        "/erpflow/purchaseOrderDetails.jsp?id=" +
        id;
}


// ========================================
// RECEIVE PURCHASE ORDER
// ========================================

async function receivePurchaseOrder(
    id
) {

    if (
        !confirm(
            "Are you sure you want to receive this purchase order?"
        )
    ) {

        return;
    }


    try {

        const response =
            await fetch(
                `${purchaseOrderApi}/${id}/receive`,
                {
                    method: "POST"
                }
            );


        const data =
            await response.json();


        if (!response.ok) {

            throw new Error(
                data.error ||
                "Failed to receive purchase order"
            );
        }


        showMessage(
            "Purchase Order received successfully.",
            "success"
        );


        await loadPurchaseOrders();


    } catch (error) {

        console.error(error);

        showMessage(
            error.message,
            "error"
        );
    }
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

                renderPurchaseOrders(
                    allPurchaseOrders
                );

                return;
            }


            const filtered =
                allPurchaseOrders.filter(
                    order => {

                        const id =
                            String(
                                order.id || ""
                            );


                        const supplier =
                            order.supplier
                                ? order.supplier.name
                                : "";


                        const status =
                            order.status || "";


                        return (

                            id.includes(
                                term
                            )

                            ||

                            supplier
                                .toLowerCase()
                                .includes(
                                    term
                                )

                            ||

                            status
                                .toLowerCase()
                                .includes(
                                    term
                                )

                        );

                    }
                );


            renderPurchaseOrders(
                filtered
            );

        }
    );
}


// ========================================
// FORMAT DATE
// ========================================

function formatDate(
    dateValue
) {

    if (!dateValue) {
        return "-";
    }


    // Jackson LocalDateTime array

    if (
        Array.isArray(dateValue)
    ) {

        const [
            year,
            month,
            day,
            hour = 0,
            minute = 0,
            second = 0
        ] = dateValue;


        const date =
            new Date(
                year,
                month - 1,
                day,
                hour,
                minute,
                second
            );


        if (
            isNaN(
                date.getTime()
            )
        ) {

            return "-";
        }


        return date.toLocaleString();
    }


    // ISO string fallback

    const date =
        new Date(
            dateValue
        );


    if (
        isNaN(
            date.getTime()
        )
    ) {

        return "-";
    }


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
        value === "created"
    ) {

        return "active";
    }


    if (
        value === "received"
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