let salesOrders = [];
let orderItems = [];


// =========================
// LOAD SALES ORDERS
// =========================

async function loadSalesOrders() {

    const salesOrderSelect =
        document.getElementById("salesOrderSelect");

    try {

        const response = await fetch(
            `${contextPath}/api/sales-orders`
        );

        if (!response.ok) {
            throw new Error("Failed to load sales orders");
        }

        salesOrders = await response.json();

        salesOrderSelect.innerHTML =
            '<option value="">Select Sales Order</option>';

        salesOrders.forEach(order => {

            const option =
                document.createElement("option");

            option.value = order.id;

            const customerName =
                order.customer
                    ? order.customer.name
                    : "Unknown Customer";

            option.textContent =
                `Order #${order.id} - ${customerName}`;

            salesOrderSelect.appendChild(option);
        });

    } catch (error) {

        console.error(error);

        salesOrderSelect.innerHTML =
            '<option value="">Failed to load orders</option>';
    }
}


// =========================
// LOAD ITEMS FOR SELECTED ORDER
// =========================

async function handleSalesOrderChange() {

    const salesOrderSelect =
        document.getElementById("salesOrderSelect");

    const salesOrderId =
        salesOrderSelect.value;

    // Get all package item rows
    const rows =
        document.querySelectorAll(".package-item-row");

    // Clear every item dropdown
    rows.forEach(row => {

        const itemSelect =
            row.querySelector(".item-select");

        const quantityInput =
            row.querySelector(".quantity-input");

        itemSelect.innerHTML =
            '<option value="">Select Item</option>';

        quantityInput.value = "";
        quantityInput.removeAttribute("max");
    });


    if (!salesOrderId) {
        return;
    }


    try {

        // IMPORTANT:
        // Our API expects ?id=, not /{id}/items
        const response = await fetch(
            `${contextPath}/api/sales-orders?id=${salesOrderId}`
        );

        if (!response.ok) {
            throw new Error(
                "Failed to load sales order items"
            );
        }

        const result =
            await response.json();

        // API returns:
        // {
        //     order: {...},
        //     items: [...]
        // }

        orderItems =
            result.items || [];


        if (orderItems.length === 0) {

            alert(
                "This sales order contains no items."
            );

            return;
        }


        // Populate every item dropdown
        rows.forEach(row => {

            const itemSelect =
                row.querySelector(".item-select");

            orderItems.forEach(orderItem => {

                const option =
                    document.createElement("option");


                /*
                 * SalesOrderItem contains:
                 *
                 * orderItem.item.id
                 * orderItem.item.name
                 * orderItem.item.sku
                 * orderItem.quantity
                 */

                const item =
                    orderItem.item;


                option.value =
                    item.id;

                option.textContent =
                    `${item.name} - ${item.sku} (Ordered: ${orderItem.quantity})`;

                option.dataset.quantity =
                    orderItem.quantity;

                itemSelect.appendChild(option);
            });
        });

    } catch (error) {

        console.error(error);

        alert(
            "Failed to load items for this sales order."
        );
    }
}


// =========================
// ITEM CHANGE
// =========================

function handleItemChange(event) {

    const itemSelect =
        event.target;

    const row =
        itemSelect.closest(".package-item-row");

    if (!row) {
        return;
    }

    const quantityInput =
        row.querySelector(".quantity-input");

    const selectedOption =
        itemSelect.options[
            itemSelect.selectedIndex
        ];


    if (!selectedOption ||
        !selectedOption.dataset.quantity) {

        quantityInput.value = "";

        quantityInput.removeAttribute("max");

        return;
    }


    const maxQuantity =
        parseInt(
            selectedOption.dataset.quantity
        );


    quantityInput.max =
        maxQuantity;

    quantityInput.value = "";
}


// =========================
// QUANTITY VALIDATION
// =========================

function validateQuantity(event) {

    const quantityInput =
        event.target;

    const max =
        parseInt(quantityInput.max);

    const value =
        parseInt(quantityInput.value);


    if (max && value > max) {

        quantityInput.value = max;

        alert(
            `Maximum quantity allowed is ${max}`
        );
    }
}


// =========================
// ADD ITEM ROW
// =========================

function addItemRow() {

    const container =
        document.getElementById("packageItems");

    const row =
        document.createElement("div");

    row.className =
        "package-item-row";


    row.innerHTML = `

        <select class="item-select" required>

            <option value="">
                Select Item
            </option>

        </select>


        <input
            type="number"
            class="quantity-input"
            min="1"
            placeholder="Quantity"
            required
        >


        <button
            type="button"
            class="btn small-btn danger-btn remove-btn">
            Remove
        </button>

       

    `;


    container.appendChild(row);


    // If a sales order is already selected,
    // populate the new row immediately.
    if (document.getElementById("salesOrderSelect").value) {

        const itemSelect =
            row.querySelector(".item-select");

        orderItems.forEach(orderItem => {

            const option =
                document.createElement("option");

            const item =
                orderItem.item;

            option.value =
                item.id;

            option.textContent =
                `${item.name} - ${item.sku} (Ordered: ${orderItem.quantity})`;

            option.dataset.quantity =
                orderItem.quantity;

            itemSelect.appendChild(option);
        });
    }
}


// =========================
// REMOVE ITEM ROW
// =========================

function removeItemRow(button) {

    const rows =
        document.querySelectorAll(
            ".package-item-row"
        );


    // Keep at least one row
    if (rows.length <= 1) {

        alert(
            "At least one package item is required."
        );

        return;
    }


    button
        .closest(".package-item-row")
        .remove();
}


// =========================
// CREATE PACKAGE
// =========================

async function createPackage(event) {

    event.preventDefault();


    const salesOrderId =
        document.getElementById(
            "salesOrderSelect"
        ).value;


    const weight =
        document.getElementById(
            "weightInput"
        ).value;


    if (!salesOrderId) {

        alert(
            "Please select a sales order."
        );

        return;
    }


    const rows =
        document.querySelectorAll(
            ".package-item-row"
        );


    const items = [];


    for (const row of rows) {

        const itemSelect =
            row.querySelector(
                ".item-select"
            );

        const quantityInput =
            row.querySelector(
                ".quantity-input"
            );


        const itemId =
            itemSelect.value;

        const quantity =
            parseInt(
                quantityInput.value
            );


        if (!itemId) {

            alert(
                "Please select an item."
            );

            return;
        }


        if (!quantity || quantity < 1) {

            alert(
                "Please enter a valid quantity."
            );

            return;
        }


        const maxQuantity =
            parseInt(
                itemSelect
                    .options[
                        itemSelect.selectedIndex
                    ]
                    .dataset.quantity
            );


        if (quantity > maxQuantity) {

            alert(
                `Maximum quantity for this item is ${maxQuantity}`
            );

            return;
        }


        items.push({

            itemId:
                parseInt(itemId),

            quantity:
                quantity

        });
    }


    const packageData = {

        salesOrderId:
            parseInt(salesOrderId),

        weight:
            parseFloat(weight),

        items:
            items
    };


    try {

        const response =
            await fetch(
                `${contextPath}/api/packages`,
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(
                            packageData
                        )
                }
            );


        const result =
            await response.json();


        if (!response.ok) {

            throw new Error(
                result.error ||
                "Failed to create package"
            );
        }


        showMessage(
            "Package created successfully!",
            "success"
        );


        document.getElementById(
            "packageForm"
        ).reset();


        document.getElementById(
            "packageItems"
        ).innerHTML = `

            <div class="package-item-row">

                <select
                    class="item-select"
                    required>

                    <option value="">
                        Select Item
                    </option>

                </select>


                <input
                    type="number"
                    class="quantity-input"
                    min="1"
                    placeholder="Quantity"
                    required
                >


                <button
                    type="button"
                    class="btn small-btn danger-btn remove-btn">
                    Remove
                </button>

            </div>

        `;


        loadPackages();

    } catch (error) {

        console.error(error);

        showMessage(
            error.message,
            "error"
        );
    }
}


// =========================
// LOAD PACKAGES
// =========================

async function loadPackages() {

    const tableBody =
        document.getElementById(
            "packagesTableBody"
        );

    try {

        const response =
            await fetch(
                `${contextPath}/api/packages`
            );


        if (!response.ok) {
            throw new Error(
                "Failed to load packages"
            );
        }


        const packages =
            await response.json();


        tableBody.innerHTML = "";


        document.getElementById(
            "packageCount"
        ).textContent =
            packages.length;


        packages.forEach(pkg => {

            const row =
                document.createElement("tr");
                let action = `
        <a class="action-button"
           href="/erpflow/packageDetails.jsp?id=${pkg.id}">
            View
        </a>
    `;
        

    if (pkg.status === "PACKED") {
        action += `
            <button class="action-button"
                    onclick="shipPackage(${pkg.id})">
                Ship
            </button>
        `;
    }


            const customerName =
                pkg.salesOrder &&
                pkg.salesOrder.customer
                    ? pkg.salesOrder.customer.name
                    : "Unknown";


            row.innerHTML = `

                <td>#${pkg.id}</td>

                <td>
                    #${pkg.salesOrder
                        ? pkg.salesOrder.id
                        : "-"}
                </td>

                <td>
                    ${customerName}
                </td>

                <td>
                    ${pkg.packageDate || "-"}
                </td>

                <td>
                    ${pkg.weight || 0} kg
                </td>

                <td>
                    <span class="status">
                        ${pkg.status || "-"}
                    </span>
                </td>
                 <button class="action-button"
        onclick="shipPackage(${pkg.id})">
  ship
</button>


            `;


            tableBody.appendChild(row);
        });


    } catch (error) {

        console.error(error);

        tableBody.innerHTML = `
            <tr>
                <td colspan="6">
                    Failed to load packages
                </td>
            </tr>
        `;
    }
}


// =========================
// SEARCH
// =========================

function searchPackages() {

    const search =
        document.getElementById(
            "searchInput"
        ).value.toLowerCase();


    const rows =
        document.querySelectorAll(
            "#packagesTableBody tr"
        );


    rows.forEach(row => {

        row.style.display =
            row.innerText
                .toLowerCase()
                .includes(search)
                ? ""
                : "none";
    });
}


// =========================
// MESSAGE
// =========================

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
        `message-box ${type}`;


    messageBox.style.display =
        "block";


    setTimeout(() => {

        messageBox.style.display =
            "none";

    }, 3000);
}


// =========================
// INITIALIZE
// =========================

document.addEventListener(
    "DOMContentLoaded",
    function () {

        const salesOrderSelect =
            document.getElementById(
                "salesOrderSelect"
            );


        const packageForm =
            document.getElementById(
                "packageForm"
            );


        const addItemButton =
            document.getElementById(
                "addItemButton"
            );


        const searchInput =
            document.getElementById(
                "searchInput"
            );


        salesOrderSelect.addEventListener(
            "change",
            handleSalesOrderChange
        );


        packageForm.addEventListener(
            "submit",
            createPackage
        );


        addItemButton.addEventListener(
            "click",
            addItemRow
        );


        searchInput.addEventListener(
            "input",
            searchPackages
        );


        // Event delegation for item rows
        document
            .getElementById("packageItems")
            .addEventListener(
                "change",
                function (event) {

                    if (
                        event.target.classList
                            .contains("item-select")
                    ) {

                        handleItemChange(event);
                    }
                }
            );


        document
            .getElementById("packageItems")
            .addEventListener(
                "input",
                function (event) {

                    if (
                        event.target.classList
                            .contains("quantity-input")
                    ) {

                        validateQuantity(event);
                    }
                }
            );


        document
            .getElementById("packageItems")
            .addEventListener(
                "click",
                function (event) {

                    if (
                        event.target.classList
                            .contains("remove-btn")
                    ) {

                        removeItemRow(
                            event.target
                        );
                    }
                }
            );


        loadSalesOrders();

        loadPackages();
    }

    
    
);

async function shipPackage(packageId) {

    if (!confirm("Are you sure you want to ship this package?")) {
        return;
    }

    try {

        const response = await fetch(
            `/erpflow/api/shipping/${packageId}`,
            {
                method: "POST"
            }
        );

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.error || "Failed to ship package");
        }

        alert("Package shipped successfully!");

        loadPackages();

    } catch (error) {

        console.error(error);

        alert(error.message);
    }
}