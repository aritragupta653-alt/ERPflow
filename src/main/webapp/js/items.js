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
// GET ALL ITEMS
// GET /api/items
// =========================================================

async function loadItems() {

    try {

        const response = await fetch(
            contextPath + "/api/items"
        );


        if (!response.ok) {

            throw new Error(
                "Failed to load items"
            );

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
        document.getElementById(
            "itemsTableBody"
        );


    const itemCount =
        document.getElementById(
            "itemCount"
        );


    tableBody.innerHTML = "";


    itemCount.textContent =
        items.length;


    if (items.length === 0) {

        const row =
            document.createElement("tr");


        const cell =
            document.createElement("td");


        cell.colSpan = 9;

        cell.className =
            "empty-state";


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

        const row =
            document.createElement("tr");


        // =================================================
        // ID
        // =================================================

        const idCell =
            document.createElement("td");


        const idBadge =
            document.createElement("span");


        idBadge.className =
            "id-badge";


        idBadge.textContent =
            item.id;


        idCell.appendChild(idBadge);


        // =================================================
        // NAME
        // =================================================

        const nameCell =
            document.createElement("td");


        const name =
            document.createElement("div");


        name.className =
            "item-name";


        name.textContent =
            item.name;


        nameCell.appendChild(name);


        // =================================================
        // SKU
        // =================================================

        const skuCell =
            document.createElement("td");


        const sku =
            document.createElement("span");


        sku.className =
            "sku-badge";


        sku.textContent =
            item.sku;


        skuCell.appendChild(sku);


        // =================================================
        // DESCRIPTION
        // =================================================

        const descriptionCell =
            document.createElement("td");


        const description =
            document.createElement("span");


        description.className =
            "description-cell";


        description.textContent =
            item.description ||
            "No description";


        descriptionCell.appendChild(
            description
        );


        // =================================================
        // PURCHASE PRICE
        // =================================================

        const purchaseCell =
            document.createElement("td");


        purchaseCell.textContent =
            "₹" +
            Number(
                item.purchasePrice
            ).toFixed(2);


        // =================================================
        // SELLING PRICE
        // =================================================

        const sellingCell =
            document.createElement("td");


        sellingCell.textContent =
            "₹" +
            Number(
                item.sellingPrice
            ).toFixed(2);


        // =================================================
        // REORDER LEVEL
        // =================================================

        const reorderCell =
            document.createElement("td");


        const reorderBadge =
            document.createElement("span");


        reorderBadge.className =
            "reorder-badge";


        reorderBadge.textContent =
            item.reorderLevel;


        reorderCell.appendChild(
            reorderBadge
        );


        // =================================================
        // STATUS
        // =================================================

        const statusCell =
            document.createElement("td");


        const statusBadge =
            document.createElement("span");


        statusBadge.className =
            "status-badge";


        statusBadge.textContent =
            item.status || "ACTIVE";


        if (
            item.status &&
            item.status.toUpperCase() ===
            "INACTIVE"
        ) {

            statusBadge.classList.add(
                "inactive"
            );

        }


        statusCell.appendChild(
            statusBadge
        );


        // =================================================
        // ACTIONS
        // =================================================

        const actionCell =
            document.createElement("td");


        const actionButtons =
            document.createElement("div");


        actionButtons.className =
            "action-buttons";


        // EDIT BUTTON

        const editButton =
            document.createElement("button");


        editButton.type = "button";

        editButton.className =
            "action-button edit-button";


        editButton.textContent =
            "Edit";


        editButton.addEventListener(
            "click",
            () => openEditModal(item)
        );


        // DELETE BUTTON

        const deleteButton =
            document.createElement("button");


        deleteButton.type = "button";

        deleteButton.className =
            "action-button delete-button";


        deleteButton.textContent =
            "Inactive";


        deleteButton.addEventListener(
            "click",
            () => deleteItem(item)
        );


        actionButtons.appendChild(
            editButton
        );


        actionButtons.appendChild(
            deleteButton
        );


        actionCell.appendChild(
            actionButtons
        );


        // =================================================
        // ADD CELLS
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

    const form =
        document.getElementById(
            "addItemForm"
        );


    form.addEventListener(
        "submit",
        async (event) => {

            event.preventDefault();


            const item = {

                name:
                    document.getElementById(
                        "name"
                    ).value.trim(),

                sku:
                    document.getElementById(
                        "sku"
                    ).value.trim(),

                description:
                    document.getElementById(
                        "description"
                    ).value.trim(),

                purchasePrice:
                    Number(
                        document.getElementById(
                            "purchasePrice"
                        ).value
                    ),

                sellingPrice:
                    Number(
                        document.getElementById(
                            "sellingPrice"
                        ).value
                    ),

                reorderLevel:
                    Number(
                        document.getElementById(
                            "reorderLevel"
                        ).value
                    )

            };


            try {

                const response =
                    await fetch(
                        contextPath +
                        "/api/items",
                        {

                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body:
                                JSON.stringify(item)

                        }
                    );


                if (!response.ok) {

                    const error =
                        await response.json();

                    throw new Error(
                        error.error ||
                        "Failed to create item"
                    );

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

        }
    );

}


// =========================================================
// OPEN EDIT MODAL
// =========================================================

function openEditModal(item) {

    document.getElementById(
        "editId"
    ).value = item.id;


    document.getElementById(
        "editName"
    ).value = item.name || "";


    document.getElementById(
        "editSku"
    ).value = item.sku || "";


    document.getElementById(
        "editDescription"
    ).value =
        item.description || "";


    document.getElementById(
        "editPurchasePrice"
    ).value =
        item.purchasePrice ?? "";


    document.getElementById(
        "editSellingPrice"
    ).value =
        item.sellingPrice ?? "";


    document.getElementById(
        "editReorderLevel"
    ).value =
        item.reorderLevel ?? "";


    document.getElementById(
        "editModal"
    ).hidden = false;

}


// =========================================================
// CLOSE EDIT MODAL
// =========================================================

function closeEditModal() {

    document.getElementById(
        "editModal"
    ).hidden = true;

}


// =========================================================
// EDIT ITEM
// PUT /api/items/{id}
// =========================================================

function setupEditForm() {

    const form =
        document.getElementById(
            "editItemForm"
        );


    form.addEventListener(
        "submit",
        async (event) => {

            event.preventDefault();


            const id =
                document.getElementById(
                    "editId"
                ).value;


            const item = {

                name:
                    document.getElementById(
                        "editName"
                    ).value.trim(),

                sku:
                    document.getElementById(
                        "editSku"
                    ).value.trim(),

                description:
                    document.getElementById(
                        "editDescription"
                    ).value.trim(),

                purchasePrice:
                    Number(
                        document.getElementById(
                            "editPurchasePrice"
                        ).value
                    ),

                sellingPrice:
                    Number(
                        document.getElementById(
                            "editSellingPrice"
                        ).value
                    ),

                reorderLevel:
                    Number(
                        document.getElementById(
                            "editReorderLevel"
                        ).value
                    )

            };


            try {

                const response =
                    await fetch(
                        contextPath +
                        "/api/items/" +
                        id,
                        {

                            method: "PUT",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body:
                                JSON.stringify(item)

                        }
                    );


                if (!response.ok) {

                    const error =
                        await response.json();

                    throw new Error(
                        error.error ||
                        "Failed to update item"
                    );

                }


                await response.json();


                closeEditModal();


                showMessage(
                    "Item updated successfully.",
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
    );

}


// =========================================================
// DELETE / MARK INACTIVE
// DELETE /api/items/{id}
// =========================================================

async function deleteItem(item) {

    const confirmed =
        confirm(
            'Are you sure you want to mark "' +
            item.name +
            '" as inactive?'
        );


    if (!confirmed) {

        return;

    }


    try {

        const response =
            await fetch(
                contextPath +
                "/api/items/" +
                item.id,
                {
                    method: "DELETE"
                }
            );


        if (!response.ok) {

            let errorMessage =
                "Failed to mark item as inactive";


            try {

                const error =
                    await response.json();

                errorMessage =
                    error.error ||
                    errorMessage;

            } catch (e) {
                // Ignore JSON parsing failure
            }


            throw new Error(
                errorMessage
            );

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
// SEARCH
// =========================================================

function setupSearch() {

    const searchInput =
        document.getElementById(
            "searchInput"
        );


    searchInput.addEventListener(
        "input",
        () => {

            const searchTerm =
                searchInput.value
                    .trim()
                    .toLowerCase();


            const filteredItems =
                allItems.filter(item => {

                    const name =
                        (
                            item.name || ""
                        ).toLowerCase();


                    const sku =
                        (
                            item.sku || ""
                        ).toLowerCase();


                    return (
                        name.includes(
                            searchTerm
                        ) ||
                        sku.includes(
                            searchTerm
                        )
                    );

                });


            renderItems(
                filteredItems
            );

        }
    );

}


// =========================================================
// MODAL EVENTS
// =========================================================

function setupModal() {

    document.getElementById(
        "closeModalButton"
    ).addEventListener(
        "click",
        closeEditModal
    );


    document.getElementById(
        "cancelEditButton"
    ).addEventListener(
        "click",
        closeEditModal
    );


    document.getElementById(
        "editModal"
    ).addEventListener(
        "click",
        (event) => {

            if (
                event.target.id ===
                "editModal"
            ) {

                closeEditModal();

            }

        }
    );

}


// =========================================================
// MESSAGE
// =========================================================

function showMessage(
    message,
    type
) {

    const box =
        document.getElementById(
            "messageBox"
        );


    box.textContent =
        message;


    box.className =
        "message-box " +
        type;


    box.hidden = false;


    setTimeout(
        () => {

            box.hidden = true;

        },
        3000
    );

}