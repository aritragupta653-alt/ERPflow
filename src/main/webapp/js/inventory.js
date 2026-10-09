
let items = [];
let inventory = [];

document.addEventListener("DOMContentLoaded", () => {
    loadData();

    document
        .getElementById("inventoryForm")
        .addEventListener("submit", adjustInventory);

    document
        .getElementById("searchInput")
        .addEventListener("input", searchInventory);
});


// Load items

async function loadData() {
    try {
        const response = await fetch("/erpflow/api/items");

        if (!response.ok) {
            throw new Error("Failed to load items");
        }

        items = await response.json();

        // Inventory is now represented by tracked goods in the Items table.
        inventory = items.filter(item =>
            item.status === "ACTIVE" &&
            String(item.itemType).toUpperCase() === "GOODS" &&
            item.trackInventory === true
        );

        populateItems();
        renderInventory(inventory);

    } catch (error) {
        console.error(error);

        showMessage(
            "Failed to load inventory.",
            "error"
        );
    }
}


// Populate item dropdown

function populateItems() {
    const select = document.getElementById("itemSelect");

    select.innerHTML =
        '<option value="">Select Item</option>';

    items.forEach(item => {
        if (
            item.status !== "ACTIVE" ||
            String(item.itemType).toUpperCase() !== "GOODS" ||
            item.trackInventory !== true
        ) {
            return;
        }

        const option = document.createElement("option");

        option.value = item.id;
        option.textContent = `${item.name} (${item.sku})`;

        select.appendChild(option);
    });
}


// Adjust inventory

async function adjustInventory(event) {
    event.preventDefault();

    const itemId =
        document.getElementById("itemSelect").value;

    const quantity =
        Number(document.getElementById("quantityInput").value);

    const operation =
        document.getElementById("operationSelect").value;

    if (!itemId || quantity <= 0) {
        showMessage(
            "Select an item and enter a valid quantity.",
            "error"
        );
        return;
    }

    const endpoint =
        operation === "stock-in"
            ? "/erpflow/api/inventory/stock-in"
            : "/erpflow/api/inventory/stock-out";

    try {
        const response = await fetch(endpoint, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                itemId: Number(itemId),
                quantity: quantity
            })
        });

        let data = {};

        try {
            data = await response.json();
        } catch (_) {
        }

        if (!response.ok) {
            throw new Error(
                data.error || "Inventory operation failed"
            );
        }

        showMessage(
            "Inventory updated successfully.",
            "success"
        );

        document.getElementById("inventoryForm").reset();

        await loadData();

    } catch (error) {
        console.error(error);

        showMessage(error.message, "error");
    }
}


// Render inventory table

function renderInventory(data) {
    const tbody =
        document.getElementById("inventoryTableBody");

    const count =
        document.getElementById("inventoryCount");

    tbody.innerHTML = "";
    count.textContent = data.length;

    if (data.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" style="text-align:center;">
                    No inventory found.
                </td>
            </tr>
        `;
        return;
    }

    data.forEach(item => {
        const quantity = Number(item.inHandQuantity || 0);
        const committed = Number(item.committedQuantity || 0);
        const available = quantity - committed;

        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${item.id ?? "-"}</td>
            <td>${item.name ?? "-"}</td>
            <td>${item.sku ?? "-"}</td>
            <td>${quantity}</td>
            <td>${committed}</td>
            <td>${available}</td>
        `;

        tbody.appendChild(row);
    });
}


// Search inventory

function searchInventory() {
    const search =
        document
            .getElementById("searchInput")
            .value
            .toLowerCase()
            .trim();

    if (!search) {
        renderInventory(inventory);
        return;
    }

    const filtered = inventory.filter(item =>
        String(item.id || "").includes(search) ||
        (item.name || "").toLowerCase().includes(search) ||
        (item.sku || "").toLowerCase().includes(search)
    );

    renderInventory(filtered);
}


// Display message

function showMessage(message, type) {
    const box = document.getElementById("messageBox");

    box.textContent = message;
    box.className = "message-box " + type;
    box.style.display = "block";

    setTimeout(() => {
        box.style.display = "none";
    }, 3000);
}