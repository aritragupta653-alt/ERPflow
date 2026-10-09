let allItems = [];
let compositeItems = [];
let currentAssemblyComponents = [];
let currentAssemblyComposite = null;

document.addEventListener("DOMContentLoaded", () => {
    loadItems();
    setupAddItemForm();
    setupEditForm();
    setupSearch();
    setupModal();
    setupCompositeUI();

    document.getElementById("statusFilter")
        ?.addEventListener("change", loadItems);
});

function getValue(id) {
    const element = document.getElementById(id);
    return element ? element.value.trim() : "";
}

function getCheckboxValue(id, defaultValue = true) {
    const element = document.getElementById(id);
    return element ? element.checked : defaultValue;
}

function getItemType(item) {
    return String(item?.itemType || "GOODS").toUpperCase();
}

async function loadItems() {
    try {
        const status =
            document.getElementById("statusFilter")?.value || "ALL";

        const url =
            contextPath +
            "/api/items" +
            (
                status === "ALL"
                    ? ""
                    : "?status=" + encodeURIComponent(status)
            );

        const response = await fetch(url);

        if (!response.ok) {
            throw new Error("Failed to load items");
        }

        allItems = await response.json();

        renderItems(allItems);
        renderCompositeItems(allItems);

    } catch (error) {
        console.error("Load Items Error:", error);
        showMessage("Unable to load items.", "error");
    }
}

function renderItems(items) {
    const tableBody =
        document.getElementById("itemsTableBody");

    const itemCount =
        document.getElementById("itemCount");

    if (!tableBody) return;

    tableBody.innerHTML = "";

    if (itemCount) {
        itemCount.textContent = items.length;
    }

    if (items.length === 0) {
        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 10;
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

        const idCell = document.createElement("td");
        const idBadge = document.createElement("span");

        idBadge.className = "id-badge";
        idBadge.textContent = item.id;

        idCell.appendChild(idBadge);

        const nameCell = document.createElement("td");
        const name = document.createElement("div");

        name.className = "item-name";
        name.textContent = item.name || "";

        nameCell.appendChild(name);

        const skuCell = document.createElement("td");
        const sku = document.createElement("span");

        sku.className = "sku-badge";
        sku.textContent = item.sku || "";

        skuCell.appendChild(sku);

        const descriptionCell = document.createElement("td");
        const description = document.createElement("span");

        description.className = "description-cell";
        description.textContent =
            item.description || "No description";

        descriptionCell.appendChild(description);

        const purchaseCell = document.createElement("td");

        purchaseCell.textContent =
            "₹" +
            Number(item.purchasePrice ?? 0).toFixed(2);

        const sellingCell = document.createElement("td");

        sellingCell.textContent =
            "₹" +
            Number(item.sellingPrice ?? 0).toFixed(2);

        const reorderCell = document.createElement("td");
        const reorderBadge = document.createElement("span");

        reorderBadge.className = "reorder-badge";
        reorderBadge.textContent =
            item.reorderLevel ?? 0;

        reorderCell.appendChild(reorderBadge);

        const typeCell = document.createElement("td");
        const typeBadge = document.createElement("div");

        typeBadge.className = "type-badge";
        typeBadge.textContent =
            item.itemType || "";

        typeCell.appendChild(typeBadge);

        const statusCell = document.createElement("td");
        const statusBadge = document.createElement("span");

        const itemStatus =
            String(item.status || "ACTIVE").toUpperCase();

        statusBadge.className = "status-badge";
        statusBadge.textContent = itemStatus;

        if (itemStatus === "INACTIVE") {
            statusBadge.classList.add("inactive");
        }

        statusCell.appendChild(statusBadge);

        const actionCell = document.createElement("td");
        const actionButtons = document.createElement("div");

        actionButtons.className = "action-buttons";

        const editButton = document.createElement("button");

        editButton.type = "button";
        editButton.className =
            "action-button edit-button";
        editButton.textContent = "Edit";

        editButton.addEventListener(
            "click",
            () => openEditModal(item)
        );

        actionButtons.appendChild(editButton);

        if (getItemType(item) === "COMPOSITE") {
            const assemblyButton =
                document.createElement("button");

            assemblyButton.type = "button";
            assemblyButton.className =
                "action-button edit-button";
            assemblyButton.textContent =
                "Create Assembly";

            assemblyButton.addEventListener(
                "click",
                () => openCreateAssembly(item)
            );

            actionButtons.appendChild(
                assemblyButton
            );
        }

        const statusButton =
            document.createElement("button");

        statusButton.type = "button";

        if (itemStatus === "INACTIVE") {
            statusButton.className =
                "action-button activate-button";

            statusButton.textContent =
                "Activate";

            statusButton.addEventListener(
                "click",
                () => activateItem(item)
            );

        } else {
            statusButton.className =
                "action-button delete-button";

            statusButton.textContent =
                "Inactive";

            statusButton.addEventListener(
                "click",
                () => deleteItem(item)
            );
        }

        actionButtons.appendChild(statusButton);

        actionCell.appendChild(actionButtons);

        row.appendChild(idCell);
        row.appendChild(nameCell);
        row.appendChild(skuCell);
        row.appendChild(descriptionCell);
        row.appendChild(purchaseCell);
        row.appendChild(sellingCell);
        row.appendChild(reorderCell);
        row.appendChild(typeCell);
        row.appendChild(statusCell);
        row.appendChild(actionCell);

        tableBody.appendChild(row);
    });
}

function setupAddItemForm() {
    const form =
        document.getElementById("addItemForm");

    if (!form) return;

    form.addEventListener(
        "submit",
        async event => {
            event.preventDefault();

            const itemType =
                getValue("itemType").toUpperCase();

            const trackInventory =
                getCheckboxValue(
                    "trackInventory",
                    true
                );

            const item = {
                name:
                    getValue("name"),

                sku:
                    getValue("sku"),

                description:
                    getValue("description"),

                purchasePrice:
                    Number(
                        getValue("purchasePrice")
                    ),

                sellingPrice:
                    Number(
                        getValue("sellingPrice")
                    ),

                reorderLevel:
                    Number(
                        getValue("reorderLevel")
                    ),

                length:
                    Number(
                        getValue("length")
                    ),

                width:
                    Number(
                        getValue("width")
                    ),

                height:
                    Number(
                        getValue("height")
                    ),

                weight:
                    Number(
                        getValue("weight")
                    ),

                itemType:
                    itemType,

                trackInventory:
                    itemType === "SERVICE"
                        ? false
                        : trackInventory,

                inHandQuantity:
                    Number(
                        getValue("openingStock")
                    ),

                maxStockQuantity:
                    Number(
                        getValue("maxStockQuantity")
                    )
            };

            try {
                const response =
                    await fetch(
                        contextPath + "/api/items",
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
                    throw new Error(
                        await getErrorMessage(
                            response,
                            "Failed to create item"
                        )
                    );
                }

                showMessage(
                    "Item created successfully.",
                    "success"
                );

                form.reset();

                const typeSelect =
                    document.getElementById(
                        "itemType"
                    );

                if (typeSelect) {
                    typeSelect.value = "GOODS";
                }

                const tracking =
                    document.getElementById(
                        "trackInventory"
                    );

                if (tracking) {
                    tracking.checked = true;
                }

                await loadItems();

            } catch (error) {
                console.error(
                    "Create Item Error:",
                    error
                );

                showMessage(
                    error.message,
                    "error"
                );
            }
        }
    );
}

function openEditModal(item) {
    const modal =
        document.getElementById("editModal");

    if (!modal) {
        showMessage(
            "Edit form could not be opened.",
            "error"
        );

        return;
    }

    document.getElementById("editId").value =
        item.id;

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

    document.getElementById("editLength").value =
        item.length ?? 0;

    document.getElementById("editWidth").value =
        item.width ?? 0;

    document.getElementById("editHeight").value =
        item.height ?? 0;

    document.getElementById("editWeight").value =
        item.weight ?? 0;

    const itemType =
        getItemType(item);

    const editItemType =
        document.getElementById(
            "editItemType"
        );

    if (editItemType) {
        editItemType.value = itemType;
    }

    const editTrackInventory =
        document.getElementById(
            "editTrackInventory"
        );

    if (editTrackInventory) {
        editTrackInventory.checked =
            itemType === "SERVICE"
                ? false
                : item.trackInventory !== false;
    }

    modal.hidden = false;
    modal.classList.add("show");

    modal.setAttribute(
        "aria-hidden",
        "false"
    );
}

function closeEditModal() {
    const modal =
        document.getElementById("editModal");

    if (!modal) return;

    modal.classList.remove("show");
    modal.hidden = true;

    modal.setAttribute(
        "aria-hidden",
        "true"
    );
}

function setupEditForm() {
    const form =
        document.getElementById(
            "editItemForm"
        );

    if (!form) return;

    form.addEventListener(
        "submit",
        async event => {
            event.preventDefault();

            const id =
                getValue("editId");

            const itemType =
                getValue("editItemType")
                    .toUpperCase();

            const trackInventory =
                getCheckboxValue(
                    "editTrackInventory",
                    true
                );

            const item = {
                name:
                    getValue("editName"),

                sku:
                    getValue("editSku"),

                description:
                    getValue("editDescription"),

                purchasePrice:
                    Number(
                        getValue(
                            "editPurchasePrice"
                        )
                    ),

                sellingPrice:
                    Number(
                        getValue(
                            "editSellingPrice"
                        )
                    ),

                reorderLevel:
                    Number(
                        getValue(
                            "editReorderLevel"
                        )
                    ),

                height:
                    Number(
                        getValue("editHeight")
                    ),

                length:
                    Number(
                        getValue("editLength")
                    ),

                width:
                    Number(
                        getValue("editWidth")
                    ),

                weight:
                    Number(
                        getValue("editWeight")
                    ),

                itemType:
                    itemType,

                trackInventory:
                    itemType === "SERVICE"
                        ? false
                        : trackInventory
            };

            try {
                const response =
                    await fetch(
                        contextPath +
                        "/api/items/" +
                        encodeURIComponent(id),
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
                    throw new Error(
                        await getErrorMessage(
                            response,
                            "Failed to update item"
                        )
                    );
                }

                if (
                    response.status !== 204 &&
                    response.headers
                        .get("content-type")
                        ?.includes(
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
                console.error(
                    "Edit Form Error:",
                    error
                );

                showMessage(
                    error.message,
                    "error"
                );
            }
        }
    );
}

async function activateItem(item) {
    const confirmed =
        confirm(
            `Are you sure you want to activate "${item.name}"?`
        );

    if (!confirmed) return;

    const itemType =
        getItemType(item);

    const updatedItem = {
        name:
            item.name || "",

        sku:
            item.sku || "",

        description:
            item.description || "",

        purchasePrice:
            Number(
                item.purchasePrice ?? 0
            ),

        sellingPrice:
            Number(
                item.sellingPrice ?? 0
            ),

        reorderLevel:
            Number(
                item.reorderLevel ?? 0
            ),

        height:
            Number(
                item.height ?? 0
            ),

        length:
            Number(
                item.length ?? 0
            ),

        width:
            Number(
                item.width ?? 0
            ),

        weight:
            Number(
                item.weight ?? 0
            ),

        itemType:
            itemType,

        trackInventory:
            itemType === "SERVICE"
                ? false
                : item.trackInventory !== false,

        status:
            "ACTIVE"
    };

    try {
        const response =
            await fetch(
                contextPath +
                "/api/items/" +
                encodeURIComponent(item.id),
                {
                    method: "PUT",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify(updatedItem)
                }
            );

        if (!response.ok) {
            throw new Error(
                await getErrorMessage(
                    response,
                    "Failed to activate item"
                )
            );
        }

        if (
            response.status !== 204 &&
            response.headers
                .get("content-type")
                ?.includes(
                    "application/json"
                )
        ) {
            await response.json();
        }

        showMessage(
            "Item activated successfully.",
            "success"
        );

        await loadItems();

    } catch (error) {
        console.error(
            "Activate Item Error:",
            error
        );

        showMessage(
            error.message,
            "error"
        );
    }
}

async function deleteItem(item) {
    const confirmed =
        confirm(
            `Are you sure you want to mark "${item.name}" as inactive?`
        );

    if (!confirmed) return;

    try {
        const response =
            await fetch(
                contextPath +
                "/api/items/" +
                encodeURIComponent(item.id),
                {
                    method: "DELETE"
                }
            );

        if (!response.ok) {
            throw new Error(
                await getErrorMessage(
                    response,
                    "Failed to deactivate item"
                )
            );
        }

        showMessage(
            "Item marked as inactive.",
            "success"
        );

        await loadItems();

    } catch (error) {
        console.error(
            "Deactivate Item Error:",
            error
        );

        showMessage(
            error.message,
            "error"
        );
    }
}

function setupSearch() {
    const searchInput =
        document.getElementById(
            "searchInput"
        );

    if (!searchInput) return;

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

                    const itemType =
                        (
                            item.itemType || ""
                        ).toLowerCase();

                    return (
                        name.includes(searchTerm) ||
                        sku.includes(searchTerm) ||
                        itemType.includes(searchTerm)
                    );
                });

            renderItems(filteredItems);
            renderCompositeItems(filteredItems);
        }
    );
}

function setupModal() {
    const closeButton =
        document.getElementById(
            "closeModalButton"
        );

    const cancelButton =
        document.getElementById(
            "cancelEditButton"
        );

    const modal =
        document.getElementById(
            "editModal"
        );

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
        modal.addEventListener(
            "click",
            event => {
                if (event.target === modal) {
                    closeEditModal();
                }
            }
        );
    }

    document.addEventListener(
        "keydown",
        event => {
            if (
                event.key === "Escape" &&
                modal &&
                !modal.hidden
            ) {
                closeEditModal();
            }
        }
    );
}

/* =========================================================
   COMPOSITE ITEM UI
   ========================================================= */

function renderCompositeItems(items) {
    const tableBody =
        document.getElementById(
            "compositeItemsTableBody"
        );

    const countElement =
        document.getElementById(
            "compositeItemCount"
        );

    const selector =
        document.getElementById(
            "compositeItemSelector"
        );

    compositeItems =
        (items || []).filter(
            item =>
                getItemType(item) === "COMPOSITE"
        );

    if (countElement) {
        countElement.textContent =
            compositeItems.length;
    }

    if (selector) {
        const selectedValue =
            selector.value;

        selector.innerHTML =
            '<option value="">Select composite item</option>';

        compositeItems.forEach(item => {
            const option =
                document.createElement(
                    "option"
                );

            option.value = item.id;

            option.textContent =
                `${item.name || ""} (${item.sku || ""})`;

            selector.appendChild(option);
        });

        if (
            selectedValue &&
            compositeItems.some(
                item =>
                    String(item.id) ===
                    String(selectedValue)
            )
        ) {
            selector.value =
                selectedValue;
        }
    }

    if (!tableBody) return;

    tableBody.innerHTML = "";

    if (compositeItems.length === 0) {
        const row =
            document.createElement("tr");

        const cell =
            document.createElement("td");

        cell.colSpan = 6;
        cell.className = "empty-state";

        cell.textContent =
            "No composite items found.";

        row.appendChild(cell);
        tableBody.appendChild(row);

        return;
    }

    compositeItems.forEach(item => {
        const row =
            document.createElement("tr");

        const idCell =
            document.createElement("td");

        idCell.textContent =
            item.id;

        const nameCell =
            document.createElement("td");

        nameCell.textContent =
            item.name || "";

        const skuCell =
            document.createElement("td");

        skuCell.textContent =
            item.sku || "";

        const stockCell =
            document.createElement("td");

        stockCell.textContent =
            item.trackInventory === false
                ? "Not Tracked"
                : (item.inHandQuantity ?? 0);

        const trackingCell =
            document.createElement("td");

        trackingCell.textContent =
            item.trackInventory === false
                ? "No"
                : "Yes";

        const actionCell =
            document.createElement("td");

        const actions =
            document.createElement("div");

        actions.className =
            "action-buttons";

        const componentsButton =
            document.createElement("button");

        componentsButton.type =
            "button";

        componentsButton.className =
            "action-button edit-button";

        componentsButton.textContent =
            "View Components";

        componentsButton.addEventListener(
            "click",
            () =>
                openCompositeComponents(item)
        );

        const assemblyButton =
            document.createElement("button");

        assemblyButton.type =
            "button";

        assemblyButton.className =
            "action-button edit-button";

        assemblyButton.textContent =
            "Create Assembly";

        assemblyButton.addEventListener(
            "click",
            () =>
                openCreateAssembly(item)
        );

        actions.appendChild(
            componentsButton
        );

        actions.appendChild(
            assemblyButton
        );

        actionCell.appendChild(actions);

        row.appendChild(idCell);
        row.appendChild(nameCell);
        row.appendChild(skuCell);
        row.appendChild(stockCell);
        row.appendChild(trackingCell);
        row.appendChild(actionCell);

        tableBody.appendChild(row);
    });
}

function setupCompositeUI() {
    const selector =
        document.getElementById(
            "compositeItemSelector"
        );

    const modal =
        document.getElementById(
            "createAssemblyModal"
        );

    const closeButton =
        document.getElementById(
            "closeAssemblyModalButton"
        );

    const cancelButton =
        document.getElementById(
            "cancelAssemblyButton"
        );

    const form =
        document.getElementById(
            "createAssemblyForm"
        );

    const quantityInput =
        document.getElementById(
            "assemblyQuantity"
        );

    if (selector) {
        selector.addEventListener(
            "change",
            () => {
                const item =
                    compositeItems.find(
                        candidate =>
                            String(candidate.id) ===
                            String(selector.value)
                    );

                if (item) {
                    openCreateAssembly(item);
                }
            }
        );
    }

    if (quantityInput) {
        quantityInput.addEventListener(
            "input",
            updateAssemblyRequirements
        );
    }

    if (closeButton) {
        closeButton.addEventListener(
            "click",
            closeAssemblyModal
        );
    }

    if (cancelButton) {
        cancelButton.addEventListener(
            "click",
            closeAssemblyModal
        );
    }

    if (modal) {
        modal.addEventListener(
            "click",
            event => {
                if (event.target === modal) {
                    closeAssemblyModal();
                }
            }
        );
    }

    if (form) {
        form.addEventListener(
            "submit",
            handleCreateAssembly
        );
    }

    document.addEventListener(
        "keydown",
        event => {
            if (event.key !== "Escape") return;

            const assemblyModal =
                document.getElementById(
                    "createAssemblyModal"
                );

            if (
                assemblyModal &&
                !assemblyModal.hidden
            ) {
                closeAssemblyModal();
            }
        }
    );
}

function openCompositeComponents(item) {
    openCreateAssembly(item);
}

function openCreateAssembly(item) {
    const modal =
        document.getElementById(
            "createAssemblyModal"
        );

    if (!modal) {
        showMessage(
            "Create Assembly modal is missing from items.jsp.",
            "error"
        );

        return;
    }

    currentAssemblyComposite =
        item;

    currentAssemblyComponents =
        [];

    const compositeField =
        document.getElementById(
            "assemblyCompositeItem"
        );

    const stockField =
        document.getElementById(
            "assemblyCurrentStock"
        );

    const quantityField =
        document.getElementById(
            "assemblyQuantity"
        );

    if (compositeField) {
        compositeField.value =
            item.name || "";
    }

    if (stockField) {
        stockField.value =
            item.trackInventory === false
                ? "Not Tracked"
                : String(
                    item.inHandQuantity ?? 0
                );
    }

    if (quantityField) {
        quantityField.value = "1";
        quantityField.min = "1";
    }

    hideAssemblyError();

    modal.hidden = false;

    modal.classList.add("show");

    modal.setAttribute(
        "aria-hidden",
        "false"
    );

    loadAssemblyComponents(
        item.id
    );
}

function closeAssemblyModal() {
    const modal =
        document.getElementById(
            "createAssemblyModal"
        );

    if (!modal) return;

    modal.classList.remove("show");

    modal.hidden = true;

    modal.setAttribute(
        "aria-hidden",
        "true"
    );

    currentAssemblyComponents =
        [];

    currentAssemblyComposite =
        null;
}

async function loadAssemblyComponents(
    compositeItemId
) {
    const tableBody =
        document.getElementById(
            "assemblyComponentsTableBody"
        );

    if (tableBody) {
        tableBody.innerHTML = "";

        const loadingRow =
            document.createElement("tr");

        const loadingCell =
            document.createElement("td");

        loadingCell.colSpan = 5;

        loadingCell.className =
            "empty-state";

        loadingCell.textContent =
            "Loading components...";

        loadingRow.appendChild(
            loadingCell
        );

        tableBody.appendChild(
            loadingRow
        );
    }

    try {
        const response =
            await fetch(
                contextPath +
                "/api/item-components?compositeItemId=" +
                encodeURIComponent(
                    compositeItemId
                )
            );

        if (!response.ok) {
            throw new Error(
                await getErrorMessage(
                    response,
                    "Failed to load composite components"
                )
            );
        }

        currentAssemblyComponents =
            await response.json();

        renderAssemblyComponents();

    } catch (error) {
        console.error(
            "Load Assembly Components Error:",
            error
        );

        currentAssemblyComponents =
            [];

        renderAssemblyComponents();

        showAssemblyError(
            error.message
        );
    }
}

function renderAssemblyComponents() {
    const tableBody =
        document.getElementById(
            "assemblyComponentsTableBody"
        );

    if (!tableBody) return;

    tableBody.innerHTML = "";

    if (
        currentAssemblyComponents.length === 0
    ) {
        const row =
            document.createElement("tr");

        const cell =
            document.createElement("td");

        cell.colSpan = 5;

        cell.className =
            "empty-state";

        cell.textContent =
            "No components configured for this composite item.";

        row.appendChild(cell);

        tableBody.appendChild(row);

        updateAssemblyRequirements();

        return;
    }

    currentAssemblyComponents.forEach(
        component => {
            const item =
                component.componentItem ||
                component.item ||
                {};

            const row =
                document.createElement("tr");

            const nameCell =
                document.createElement("td");

            nameCell.textContent =
                item.name || "";

            const skuCell =
                document.createElement("td");

            skuCell.textContent =
                item.sku || "";

            const availableCell =
                document.createElement("td");

            const available =
                Number(
                    item.availableQuantity ??
                    (
                        Number(
                            item.inHandQuantity ?? 0
                        ) -
                        Number(
                            item.committedQuantity ?? 0
                        )
                    )
                );

            availableCell.textContent =
                Number.isFinite(
                    available
                )
                    ? available
                    : 0;

            const perAssemblyCell =
                document.createElement("td");

            perAssemblyCell.textContent =
                Number(
                    component.quantity ?? 0
                );

            const requiredCell =
                document.createElement("td");

            requiredCell.className =
                "assembly-required-quantity";

            requiredCell.dataset.perAssembly =
                Number(
                    component.quantity ?? 0
                );

            requiredCell.textContent =
                Number(
                    component.quantity ?? 0
                );

            row.appendChild(nameCell);
            row.appendChild(skuCell);
            row.appendChild(availableCell);
            row.appendChild(perAssemblyCell);
            row.appendChild(requiredCell);

            tableBody.appendChild(row);
        }
    );

    updateAssemblyRequirements();
}

function updateAssemblyRequirements() {
    const quantityInput =
        document.getElementById(
            "assemblyQuantity"
        );

    const quantity =
        Math.max(
            1,
            Number(
                quantityInput?.value || 1
            )
        );

    document
        .querySelectorAll(
            "#assemblyComponentsTableBody .assembly-required-quantity"
        )
        .forEach(cell => {
            const perAssembly =
                Number(
                    cell.dataset.perAssembly || 0
                );

            cell.textContent =
                perAssembly * quantity;
        });
}

async function handleCreateAssembly(
    event
) {
    event.preventDefault();

    if (!currentAssemblyComposite) {
        showAssemblyError(
            "Please select a composite item."
        );

        return;
    }

    const quantity =
        Number(
            document.getElementById(
                "assemblyQuantity"
            )?.value || 0
        );

    if (
        !Number.isInteger(quantity) ||
        quantity <= 0
    ) {
        showAssemblyError(
            "Assembly quantity must be a positive whole number."
        );

        return;
    }

    if (
        currentAssemblyComponents.length === 0
    ) {
        showAssemblyError(
            "This composite item has no components configured."
        );

        return;
    }

    for (
        const component
        of currentAssemblyComponents
    ) {
        const item =
            component.componentItem ||
            component.item ||
            {};

        const available =
            Number(
                item.availableQuantity ??
                (
                    Number(
                        item.inHandQuantity ?? 0
                    ) -
                    Number(
                        item.committedQuantity ?? 0
                    )
                )
            );

        const required =
            Number(
                component.quantity || 0
            ) * quantity;

        if (available < required) {
            showAssemblyError(
                `Insufficient stock for ${item.name || "component"}. Required: ${required}, Available: ${available}.`
            );

            return;
        }
    }

    /*
     * Backend assembly API will be connected
     * after the ItemComponentServlet /
     * AssemblyServlet and transactional
     * InventoryService logic are implemented.
     */

    showAssemblyError(
        "Assembly API is not connected yet. The UI validation is ready."
    );
}

function showAssemblyError(
    message
) {
    const element =
        document.getElementById(
            "assemblyError"
        );

    if (!element) {
        showMessage(
            message,
            "error"
        );

        return;
    }

    element.textContent =
        message;

    element.hidden = false;
}

function hideAssemblyError() {
    const element =
        document.getElementById(
            "assemblyError"
        );

    if (!element) return;

    element.textContent = "";

    element.hidden = true;
}

async function getErrorMessage(
    response,
    fallbackMessage
) {
    try {
        const contentType =
            response.headers.get(
                "content-type"
            ) || "";

        if (
            contentType.includes(
                "application/json"
            )
        ) {
            const data =
                await response.json();

            return (
                data.error ||
                data.message ||
                fallbackMessage
            );
        }

        const text =
            await response.text();

        return (
            text ||
            fallbackMessage
        );

    } catch (error) {
        return fallbackMessage;
    }
}

function showMessage(
    message,
    type
) {
    const box =
        document.getElementById(
            "messageBox"
        );

    if (!box) {
        console.log(message);
        return;
    }

    box.textContent =
        message;

    box.className =
        "message-box " + type;

    box.hidden = false;

    setTimeout(
        () => {
            box.hidden = true;
        },
        3000
    );
}