// =========================================================
// ERPFlow - Items UI
// REST API frontend
// =========================================================

let allItems = [];


// =========================================================
// INITIAL LOAD
// =========================================================

document.addEventListener("DOMContentLoaded", () => {

    loadItems();

    setupAddItemForm();

    setupEditForm();

    setupSearch();

    setupModal();

});


// =========================================================
// HELPER: GET ELEMENT VALUE SAFELY
// =========================================================

function getValue(id) {

    const element = document.getElementById(id);

    return element ? element.value.trim() : "";
}


// =========================================================
// HELPER: GET CHECKBOX VALUE
// Defaults to true when the checkbox is absent.
// =========================================================

function getCheckboxValue(id, defaultValue = true) {

    const element = document.getElementById(id);

    return element ? element.checked : defaultValue;
}


// =========================================================
// GET ALL ITEMS
// GET /api/items
// =========================================================

async function loadItems() {

    try {

        const response = await fetch(
            contextPath + "/api/items"
        );

        if (!response.ok) {
            throw new Error("Failed to load items");
        }

        allItems = await response.json();

        renderItems(allItems);

    } catch (error) {

        console.error(error);

        showMessage(
            "Unable to load items.",
            "error"
        );
    }
}


// =========================================================
// RENDER ITEMS
// =========================================================

function renderItems(items) {

    const tableBody =
        document.getElementById("itemsTableBody");

    const itemCount =
        document.getElementById("itemCount");

    tableBody.innerHTML = "";

    itemCount.textContent = items.length;

    if (items.length === 0) {

        const row = document.createElement("tr");

        const cell = document.createElement("td");

        cell.colSpan = 9;
        cell.className = "empty-state";

        cell.innerHTML = `
            <div class="empty-icon">📦</div>
            <h3>No Items Found</h3>
            <p>Add an item or change your search.</p>
        `;

        row.appendChild(cell);
        tableBody.appendChild(row);

        return;
    }

    items.forEach(item => {

        const row = document.createElement("tr");


        // =================================================
        // ID
        // =================================================

        const idCell = document.createElement("td");

        const idBadge = document.createElement("span");

        idBadge.className = "id-badge";
        idBadge.textContent = item.id;

        idCell.appendChild(idBadge);


        // =================================================
        // NAME
        // =================================================

        const nameCell = document.createElement("td");

        const name = document.createElement("div");

        name.className = "item-name";
        name.textContent = item.name || "";

        nameCell.appendChild(name);


        // =================================================
        // SKU
        // =================================================

        const skuCell = document.createElement("td");

        const sku = document.createElement("span");

        sku.className = "sku-badge";
        sku.textContent = item.sku || "";

        skuCell.appendChild(sku);


        // =================================================
        // DESCRIPTION
        // =================================================

        const descriptionCell = document.createElement("td");

        const description = document.createElement("span");

        description.className = "description-cell";
        description.textContent =
            item.description || "No description";

        descriptionCell.appendChild(description);


        // =================================================
        // PURCHASE PRICE
        // =================================================

        const purchaseCell = document.createElement("td");

        purchaseCell.textContent =
            "₹" + Number(item.purchasePrice ?? 0).toFixed(2);


        // =================================================
        // SELLING PRICE
        // =================================================

        const sellingCell = document.createElement("td");

        sellingCell.textContent =
            "₹" + Number(item.sellingPrice ?? 0).toFixed(2);


        // =================================================
        // REORDER LEVEL
        // =================================================

        const reorderCell = document.createElement("td");

        const reorderBadge = document.createElement("span");

        reorderBadge.className = "reorder-badge";
        reorderBadge.textContent = item.reorderLevel ?? 0;

        reorderCell.appendChild(reorderBadge);


        // =================================================
        // STATUS
        // =================================================

        const statusCell = document.createElement("td");

        const statusBadge = document.createElement("span");

        statusBadge.className = "status-badge";
        statusBadge.textContent = item.status || "ACTIVE";

        if (
            item.status &&
            item.status.toUpperCase() === "INACTIVE"
        ) {
            statusBadge.classList.add("inactive");
        }

        statusCell.appendChild(statusBadge);


        // =================================================
        // ACTIONS
        // =================================================

        const actionCell = document.createElement("td");

        const actionButtons = document.createElement("div");

        actionButtons.className = "action-buttons";


        // EDIT BUTTON

        const editButton = document.createElement("button");

        editButton.type = "button";
        editButton.className =
            "action-button edit-button";

        editButton.textContent = "Edit";

        editButton.addEventListener(
            "click",
            () => openEditModal(item)
        );


        // INACTIVE BUTTON

        const deleteButton = document.createElement("button");

        deleteButton.type = "button";
        deleteButton.className =
            "action-button delete-button";

        deleteButton.textContent = "Inactive";

        deleteButton.disabled =
            item.status &&
            item.status.toUpperCase() === "INACTIVE";

        deleteButton.addEventListener(
            "click",
            () => deleteItem(item)
        );


        actionButtons.appendChild(editButton);
        actionButtons.appendChild(deleteButton);

        actionCell.appendChild(actionButtons);


        // =================================================
        // ADD CELLS TO ROW
        // =================================================

        row.appendChild(idCell);
        row.appendChild(nameCell);
        row.appendChild(skuCell);
        row.appendChild(descriptionCell);
        row.appendChild(purchaseCell);
        row.appendChild(sellingCell);
        row.appendChild(reorderCell);
        row.appendChild(statusCell);
        row.appendChild(actionCell);

        tableBody.appendChild(row);

    });
}


// =========================================================
// CREATE ITEM
// POST /api/items
// =========================================================

function setupAddItemForm() {

    const form = document.getElementById("addItemForm");

    if (!form) return;

    form.addEventListener("submit", async (event) => {

        event.preventDefault();

        const itemType = getValue("itemType").toUpperCase();

        const trackInventory =
            getCheckboxValue("trackInventory", true);

        const item = {

            name: getValue("name"),

            sku: getValue("sku"),

            description: getValue("description"),

            purchasePrice:
                Number(getValue("purchasePrice")),

            sellingPrice:
                Number(getValue("sellingPrice")),

            reorderLevel:
                Number(getValue("reorderLevel")),

            itemType: itemType,

            // Services must not track inventory.
            trackInventory:
                itemType === "SERVICE" ? false : trackInventory

        };

        try {

            const response = await fetch(
                contextPath + "/api/items",
                {
                    method: "POST",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify(item)
                }
            );

            if (!response.ok) {

                let errorMessage = "Failed to create item";

                try {
                    const error = await response.json();
                    errorMessage = error.error || errorMessage;
                } catch (e) {
                    // Ignore response parsing errors.
                }

                throw new Error(errorMessage);
            }

            await response.json();

            showMessage(
                "Item created successfully.",
                "success"
            );

            form.reset();

            await loadItems();

        } catch (error) {

            console.error(error);

            showMessage(
                error.message,
                "error"
            );
        }
    });
}


// =========================================================
// OPEN EDIT MODAL
// =========================================================

function openEditModal(item) {

    document.getElementById("editId").value = item.id;

    document.getElementById("editName").value =
        item.name || "";

    document.getElementById("editSku").value =
        item.sku || "";

    document.getElementById("editDescription").value =
        item.description || "";

    document.getElementById("editPurchasePrice").value =
        item.purchasePrice ?? "";

    document.getElementById("editSellingPrice").value =
        item.sellingPrice ?? "";

    document.getElementById("editReorderLevel").value =
        item.reorderLevel ?? "";


    // Populate item type if the edit field exists.
    const editItemType =
        document.getElementById("editItemType");

    if (editItemType) {
        editItemType.value = item.itemType || "GOODS";
    }


    // Populate inventory tracking checkbox if it exists.
    const editTrackInventory =
        document.getElementById("editTrackInventory");

    if (editTrackInventory) {

        editTrackInventory.checked =
            item.itemType &&
            item.itemType.toUpperCase() === "SERVICE"
                ? false
                : Boolean(item.trackInventory);
    }

    document.getElementById("editModal").hidden = false;
}


// =========================================================
// CLOSE EDIT MODAL
// =========================================================

function closeEditModal() {

    document.getElementById("editModal").hidden = true;
}


// =========================================================
// EDIT ITEM
// PUT /api/items/{id}
// =========================================================

function setupEditForm() {

    const form = document.getElementById("editItemForm");

    if (!form) return;

    form.addEventListener("submit", async (event) => {

        event.preventDefault();

        const id = document.getElementById("editId").value;

        const itemType =
            getValue("editItemType").toUpperCase();

        const trackInventory =
            getCheckboxValue("editTrackInventory", true);

        const item = {

            name: getValue("editName"),

            sku: getValue("editSku"),

            description: getValue("editDescription"),

            purchasePrice:
                Number(getValue("editPurchasePrice")),

            sellingPrice:
                Number(getValue("editSellingPrice")),

            reorderLevel:
                Number(getValue("editReorderLevel")),

            itemType: itemType,

            // Services must not track inventory.
            trackInventory:
                itemType === "SERVICE" ? false : trackInventory
        };

        try {

            const response = await fetch(
                contextPath + "/api/items/" + id,
                {
                    method: "PUT",

                    headers: {
                        "Content-Type": "application/json"
                    },

                    body: JSON.stringify(item)
                }
            );

            if (!response.ok) {

                let errorMessage = "Failed to update item";

                try {
                    const error = await response.json();
                    errorMessage = error.error || errorMessage;
                } catch (e) {
                    // Ignore response parsing errors.
                }

                throw new Error(errorMessage);
            }

            if (
                response.status !== 204 &&
                response.headers.get("content-type")?.includes(
                    "application/json"
                )
            ) {
                await response.json();
            }

            closeEditModal();

            showMessage(
                "Item updated successfully.",
                "success"
            );

            await loadItems();

        } catch (error) {

            console.error("Edit Form Error:", error);

            showMessage(
                error.message,
                "error"
            );
        }
    });
}


// =========================================================
// SEARCH
// =========================================================

function setupSearch() {

    const searchInput =
        document.getElementById("searchInput");

    if (!searchInput) return;

    searchInput.addEventListener("input", () => {

        const searchTerm =
            searchInput.value.trim().toLowerCase();

        const filteredItems = allItems.filter(item => {

            const name =
                (item.name || "").toLowerCase();

            const sku =
                (item.sku || "").toLowerCase();

            const itemType =
                (item.itemType || "").toLowerCase();

            return (
                name.includes(searchTerm) ||
                sku.includes(searchTerm) ||
                itemType.includes(searchTerm)
            );
        });

        renderItems(filteredItems);
    });
}


// =========================================================
// INACTIVE ITEM
// DELETE /api/items/{id}
// =========================================================

async function deleteItem(item) {

    const confirmed = confirm(
        `Are you sure you want to mark "${item.name}" as inactive?`
    );

    if (!confirmed) return;

    try {

        const response = await fetch(
            contextPath + "/api/items/" + item.id,
            {
                method: "DELETE"
            }
        );

        if (!response.ok) {

            let errorMessage = "Failed to deactivate item";

            try {
                const error = await response.json();
                errorMessage = error.error || errorMessage;
            } catch (e) {
                // Ignore response parsing errors.
            }

            throw new Error(errorMessage);
        }

        showMessage(
            "Item marked as inactive.",
            "success"
        );

        await loadItems();

    } catch (error) {

        console.error(error);

        showMessage(
            error.message,
            "error"
        );
    }
}


// =========================================================
// MODAL EVENTS
// =========================================================

function setupModal() {

    const closeButton =
        document.getElementById("closeModalButton");

    const cancelButton =
        document.getElementById("cancelEditButton");

    const modal =
        document.getElementById("editModal");

    if (closeButton) {
        closeButton.addEventListener(
            "click",
            closeEditModal
        );
    }

    if (cancelButton) {
        cancelButton.addEventListener(
            "click",
            closeEditModal
        );
    }

    if (modal) {

        modal.addEventListener("click", (event) => {

            if (event.target.id === "editModal") {
                closeEditModal();
            }
        });
    }
}


// =========================================================
// MESSAGE
// =========================================================

function showMessage(message, type) {

    const box = document.getElementById("messageBox");

    if (!box) return;

    box.textContent = message;

    box.className = "message-box " + type;

    box.hidden = false;

    setTimeout(() => {
        box.hidden = true;
    }, 3000);
}