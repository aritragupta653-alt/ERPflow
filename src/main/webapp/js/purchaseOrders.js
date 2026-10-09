
const purchaseOrderApi = "/erpflow/api/purchase-orders";
const supplierApi = "/erpflow/api/suppliers";
const itemApi = "/erpflow/api/items";

let allPurchaseOrders = [];
let allSuppliers = [];
let allItems = [];
let editingOrderId = null;

document.addEventListener("DOMContentLoaded", async () => {
    setupForm();
    setupAddItemButton();
    setupSearch();

    await Promise.all([
        loadSuppliers(),
        loadItems(),
        loadPurchaseOrders()
    ]);
});

// ---------------------------------------------------------
// HELPERS
// ---------------------------------------------------------

function isService(item) {
    return String(item?.itemType || "").toUpperCase() === "SERVICE"
        || item?.trackInventory === false;
}

function showMessage(message, type = "success") {
    const box = document.getElementById("messageBox");
    if (!box) return;

    box.textContent = message;
    box.className = type === "error" ? "error-message" : "success-message";
    box.style.display = "block";
}

async function readJson(response) {
    const text = await response.text();

    let data = {};
    if (text) {
        try {
            data = JSON.parse(text);
        } catch {
            data = { error: text };
        }
    }

    if (!response.ok) {
        throw new Error(data.error || data.message || "Request failed");
    }

    return data;
}

function setFormMode(editing) {
    const heading = document.querySelector(".form-section h2");
    const submit = document.querySelector("#purchaseOrderForm button[type='submit']");

    if (heading) {
        heading.textContent = editing ? "Edit Purchase Order" : "Create Purchase Order";
    }

    if (submit) {
        submit.textContent = editing ? "Save Changes" : "Create Purchase Order";
    }

    let cancelButton = document.getElementById("cancelEditButton");

    if (editing && !cancelButton) {
        cancelButton = document.createElement("button");
        cancelButton.type = "button";
        cancelButton.id = "cancelEditButton";
        cancelButton.className = "btn";
        cancelButton.textContent = "Cancel Edit";
        cancelButton.style.marginLeft = "10px";
        submit?.parentElement?.appendChild(cancelButton);
        cancelButton.addEventListener("click", resetForm);
    }

    if (cancelButton) {
        cancelButton.style.display = editing ? "inline-block" : "none";
    }
}

function resetForm() {
    editingOrderId = null;

    const form = document.getElementById("purchaseOrderForm");
    form?.reset();

    const container = document.getElementById("itemContainer");
    if (container) {
        container.innerHTML = "";
        container.appendChild(createItemRow());
    }

    setFormMode(false);
}

// ---------------------------------------------------------
// LOAD SUPPLIERS
// ---------------------------------------------------------

async function loadSuppliers() {
    try {
        const response = await fetch(supplierApi);
        allSuppliers = await readJson(response);
        populateSuppliers();
    } catch (error) {
        console.error(error);
        showMessage("Failed to load suppliers: " + error.message, "error");
    }
}

function populateSuppliers() {
    const select = document.getElementById("supplierSelect");
    if (!select) return;

    select.innerHTML = '<option value="">Select Supplier</option>';

    allSuppliers.forEach(supplier => {
        const option = document.createElement("option");
        option.value = supplier.id;
        option.textContent = supplier.name;
        select.appendChild(option);
    });
}

// ---------------------------------------------------------
// LOAD ITEMS
// ---------------------------------------------------------

async function loadItems() {
    try {
        const response = await fetch(itemApi);
        allItems = await readJson(response);

        const container = document.getElementById("itemContainer");
        if (container && container.children.length === 0) {
            container.appendChild(createItemRow());
        }
    } catch (error) {
        console.error(error);
        showMessage("Failed to load items: " + error.message, "error");
    }
}

// ---------------------------------------------------------
// ITEM ROWS
// ---------------------------------------------------------

function createItemSelect(selectedItemId = "") {
    const select = document.createElement("select");
    select.className = "purchase-item-select";
    select.required = true;

    const defaultOption = document.createElement("option");
    defaultOption.value = "";
    defaultOption.textContent = "Select Item";
    select.appendChild(defaultOption);

    allItems.forEach(item => {
        if (item.status && String(item.status).toUpperCase() !== "ACTIVE") {
            return;
        }

        const option = document.createElement("option");
        option.value = item.id;
        option.textContent = `${item.name} - ${item.sku || ""}`;
        option.dataset.price = item.purchasePrice ?? 0;
        option.dataset.service = isService(item) ? "true" : "false";

        if (String(item.id) === String(selectedItemId)) {
            option.selected = true;
        }

        select.appendChild(option);
    });

    return select;
}

function createItemRow(line = {}) {
    const row = document.createElement("div");
    row.className = "sales-item-row";

    const itemSelect = createItemSelect(line.itemId ?? line.item?.id ?? "");

    const quantity = document.createElement("input");
    quantity.type = "number";
    quantity.className = "quantity-input";
    quantity.placeholder = "Quantity";
    quantity.min = "1";
    quantity.step = "1";
    quantity.value = line.quantity ?? "";

    const price = document.createElement("input");
    price.type = "number";
    price.className = "price-input";
    price.placeholder = "Purchase Price";
    price.min = "0";
    price.step = "0.01";
    price.required = true;
    price.value = line.purchasePrice ?? "";

    const removeButton = document.createElement("button");
    removeButton.type = "button";
    removeButton.className = "btn small-btn danger-btn";
    removeButton.textContent = "Remove";
    removeButton.addEventListener("click", () => row.remove());

    function updateQuantityField() {
        const selected = itemSelect.options[itemSelect.selectedIndex];
        const service = selected?.dataset.service === "true";

        if (service) {
            quantity.value = "";
            quantity.disabled = true;
            quantity.required = false;
            quantity.placeholder = "Not required for service";
        } else {
            quantity.disabled = false;
            quantity.required = true;
            quantity.placeholder = "Quantity";
            quantity.min = "1";
        }

        if (selected && selected.value && !price.value) {
            price.value = selected.dataset.price || "0";
        }
    }

    itemSelect.addEventListener("change", () => {
        price.value = itemSelect.options[itemSelect.selectedIndex]?.dataset.price || "0";
        updateQuantityField();
    });

    row.append(itemSelect, quantity, price, removeButton);
    updateQuantityField();

    return row;
}

function setupAddItemButton() {
    document.getElementById("addItemButton")?.addEventListener("click", () => {
        document.getElementById("itemContainer")?.appendChild(createItemRow());
    });
}

// ---------------------------------------------------------
// FORM SUBMIT: CREATE OR EDIT
// ---------------------------------------------------------

function setupForm() {
    const form = document.getElementById("purchaseOrderForm");
    if (!form) return;

    form.addEventListener("submit", async event => {
        event.preventDefault();

        const supplierId = document.getElementById("supplierSelect").value;

        if (!supplierId) {
            showMessage("Please select a supplier.", "error");
            return;
        }

        const rows = document.querySelectorAll("#itemContainer .sales-item-row");
        const items = [];

        for (const row of rows) {
            const itemId = row.querySelector(".purchase-item-select").value;
            const quantityInput = row.querySelector(".quantity-input");
            const priceInput = row.querySelector(".price-input");

            if (!itemId) {
                showMessage("Please select an item for every row.", "error");
                return;
            }

            const item = allItems.find(candidate => String(candidate.id) === String(itemId));
            if (!item) {
                showMessage("The selected item could not be found.", "error");
                return;
            }

            const service = isService(item);
            const quantity = service ? 1 : Number(quantityInput.value);
            const purchasePrice = Number(priceInput.value);

            if (!service && (!Number.isInteger(quantity) || quantity <= 0)) {
                showMessage("Enter a positive whole-number quantity for goods.", "error");
                return;
            }

            if (!Number.isFinite(purchasePrice) || purchasePrice < 0) {
                showMessage("Enter a valid purchase price.", "error");
                return;
            }

            items.push({
                itemId: Number(itemId),
                quantity,
                purchasePrice
            });
        }

        if (items.length === 0) {
            showMessage("Add at least one item.", "error");
            return;
        }

        const expectedDeliveryDate =
            document.getElementById("expectedDeliveryDate").value;
        const orderDate =document.getElementById("orderDate").value;

        if (!expectedDeliveryDate) {
            showMessage(
                "Please select an expected delivery date.",
                "error"
            );
            return;
        }

        const today = new Date().toISOString().split("T")[0];

        if (expectedDeliveryDate < today) {
            showMessage(
                "Expected delivery date cannot be before today.",
                "error"
            );
            return;
        }
        if (expectedDeliveryDate < orderDate) {
    alert("Expected delivery date cannot be before order date.");
    return;
}

        const body = {
            supplierId: Number(supplierId),
            orderDate,
            expectedDeliveryDate,
            items
        };

        const url = editingOrderId
            ? `${purchaseOrderApi}/${editingOrderId}`
            : purchaseOrderApi;

        try {
            const response = await fetch(url, {
                method: editingOrderId ? "PUT" : "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(body)
            });

            await readJson(response);

            showMessage(
                editingOrderId
                    ? "Purchase order updated successfully."
                    : "Purchase order created successfully."
            );

            resetForm();
            await loadPurchaseOrders();
        } catch (error) {
            console.error(error);
            showMessage(error.message, "error");
        }
    });
}

// ---------------------------------------------------------
// LOAD AND RENDER PURCHASE ORDERS
// ---------------------------------------------------------

async function loadPurchaseOrders() {
    try {
        const response = await fetch(purchaseOrderApi);
        allPurchaseOrders = await readJson(response);
        renderPurchaseOrders(allPurchaseOrders);
    } catch (error) {
        console.error(error);
        showMessage("Failed to load purchase orders: " + error.message, "error");
    }
}

function renderPurchaseOrders(orders) {
    const tableBody = document.getElementById("purchaseOrdersTableBody");
    if (!tableBody) return;

    tableBody.innerHTML = "";

    document.getElementById("purchaseOrderCount").textContent = orders.length;

    orders.forEach(order => {
        const row = document.createElement("tr");

        const idCell = document.createElement("td");
        idCell.textContent = order.id;

        const supplierCell = document.createElement("td");
        supplierCell.textContent = order.supplier?.name || "—";

        const dateCell = document.createElement("td");
        dateCell.textContent = formatDate(order.orderDate);

        const expectedDateCell = document.createElement("td");
        expectedDateCell.textContent =
        order.expectedDeliveryDate || "—";

        const statusCell = document.createElement("td");
        statusCell.textContent = order.status || "—";

        const actionCell = document.createElement("td");

        const detailsLink = document.createElement("a");
        detailsLink.className = "btn small-btn";
        detailsLink.href = `/erpflow/purchaseOrderDetails.jsp?id=${order.id}`;
        detailsLink.textContent = "View";

        actionCell.appendChild(detailsLink);

        if (String(order.status).toUpperCase() === "CREATED") {
            const editButton = document.createElement("button");
            editButton.type = "button";
            editButton.className = "btn small-btn";
            editButton.textContent = "Edit";
            editButton.style.marginLeft = "6px";
            editButton.addEventListener("click", () => beginEdit(order.id));
            actionCell.appendChild(editButton);
        }

        row.append(idCell, supplierCell, dateCell, expectedDateCell, statusCell, actionCell);
        tableBody.appendChild(row);
    });
}

function formatDate(value) {
    if (!value) return "—";

    if (Array.isArray(value)) {
        const [year, month, day, hour = 0, minute = 0] = value;
        return `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")} `
            + `${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}`;
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return String(value);

    return date.toLocaleString();
}

// ---------------------------------------------------------
// EDIT EXISTING ORDER
// ---------------------------------------------------------

async function beginEdit(orderId) {
    try {
        const response = await fetch(`${purchaseOrderApi}/${orderId}`);
        const order = await readJson(response);

        if (String(order.status).toUpperCase() !== "CREATED") {
            showMessage("Only orders that have not been received can be edited.", "error");
            return;
        }

        editingOrderId = orderId;

        document.getElementById("supplierSelect").value = order.supplier?.id ?? "";
        document.getElementById("expectedDeliveryDate").value =
    order.expectedDeliveryDate || "";

        const container = document.getElementById("itemContainer");
        container.innerHTML = "";

        (order.items || []).forEach(line => {
            container.appendChild(createItemRow({
                itemId: line.item?.id,
                quantity: line.quantity,
                purchasePrice: line.purchasePrice
            }));
        });

        if (container.children.length === 0) {
            container.appendChild(createItemRow());
        }

        setFormMode(true);
        document.getElementById("purchaseOrderForm")
            ?.scrollIntoView({ behavior: "smooth", block: "start" });

        showMessage(`Editing purchase order #${orderId}.`);
    } catch (error) {
        console.error(error);
        showMessage("Could not load purchase order for editing: " + error.message, "error");
    }
}

// ---------------------------------------------------------
// SEARCH
// ---------------------------------------------------------

function setupSearch() {
    document.getElementById("searchInput")?.addEventListener("input", event => {
        const query = event.target.value.trim().toLowerCase();

        const filtered = allPurchaseOrders.filter(order =>
            String(order.id).includes(query)
            || String(order.supplier?.name || "").toLowerCase().includes(query)
            || String(order.status || "").toLowerCase().includes(query)
        );

        renderPurchaseOrders(filtered);
    });
}