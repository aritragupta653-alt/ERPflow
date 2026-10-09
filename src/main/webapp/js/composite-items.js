
let compositeItems = [];
let eligibleComponents = [];
let newCompositeComponents = [];
let assemblyComponents = [];
let selectedAssemblyComposite = null;
let compositePriceManuallyEdited = false;

const contextPath = window.contextPath || "";

// ============================================================
// INITIALIZATION
// ============================================================

document.addEventListener("DOMContentLoaded", () => {
    createFeatureModals();
    loadCompositeItems();

    document.getElementById("searchInput")
        ?.addEventListener("input", renderCompositeItems);

    document.getElementById("closeComponentsButton")
        ?.addEventListener("click", closeComponentsModal);

    document.getElementById("componentsModal")
        ?.addEventListener("click", event => {
            if (event.target.id === "componentsModal") {
                closeComponentsModal();
            }
        });

    document.getElementById("createCompositeButton")
        ?.addEventListener("click", openCreateCompositeModal);

    document.getElementById("createAssemblyButton")
        ?.addEventListener("click", openCreateAssemblyModal);
});

// ============================================================
// API HELPERS
// ============================================================

async function apiRequest(url, options = {}) {
    const response = await fetch(`${contextPath}${url}`, {
        ...options,
        headers: {
            ...(options.body
                ? { "Content-Type": "application/json" }
                : {}),
            ...options.headers
        }
    });

    if (!response.ok) {
        let message = `Request failed (${response.status}).`;

        try {
            const data = await response.json();
            message = data.message || data.error || message;
        } catch {
            // Response was not JSON.
        }

        throw new Error(message);
    }

    if (response.status === 204) {
        return null;
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

function getArray(data) {
    if (Array.isArray(data)) return data;
    if (Array.isArray(data?.components)) return data.components;
    if (Array.isArray(data?.items)) return data.items;
    if (Array.isArray(data?.data)) return data.data;
    return [];
}

function itemId(item) {
    return item?.id ?? item?.itemId ?? item?.componentItemId;
}

function itemPrice(item) {
    return Number(
        item?.sellingPrice ??
        item?.componentSellingPrice ??
        item?.price ??
        0
    );
}

function itemStock(item) {
    return Number(
        item?.availableQuantity ??
        item?.availableStock ??
        item?.inHandQuantity ??
        item?.stockQuantity ??
        0
    );
}

function money(value) {
    return `₹${Number(value ?? 0).toFixed(2)}`;
}

function itemName(item) {
    return item?.name ?? item?.itemName ?? item?.componentName ?? "";
}

function itemSku(item) {
    return item?.sku ?? item?.itemSku ?? item?.componentSku ?? "";
}

function itemType(item) {
    return String(
        item?.itemType ?? item?.componentItemType ?? ""
    ).toUpperCase();
}

function isTracked(item) {
    return item?.trackInventory !== false;
}

function showMessage(message, type = "info") {
    const box = document.getElementById("messageBox");

    if (!box) {
        console.log(message);
        return;
    }

    box.textContent = message;
    box.className = `message-box ${type}`;
    box.hidden = false;
}

function setText(id, value) {
    const element = document.getElementById(id);
    if (element) element.textContent = value;
}

// ============================================================
// LOAD AND DISPLAY COMPOSITE ITEMS
// ============================================================

async function loadCompositeItems() {
    const body = document.getElementById("compositeItemsTableBody");

    try {
        compositeItems = getArray(
            await apiRequest("/api/composite-items")
        );

        renderCompositeItems();
    } catch (error) {
        console.error("Load composite items:", error);
        showMessage(error.message, "error");

        if (body) {
            body.replaceChildren();

            const row = body.insertRow();
            const cell = row.insertCell();

            cell.colSpan = 7;
            cell.className = "empty-state";
            cell.textContent = "Unable to load composite items.";
        }
    }
}

function renderCompositeItems() {
    const body = document.getElementById("compositeItemsTableBody");
    const count = document.getElementById("compositeItemCount");

    const search = (
        document.getElementById("searchInput")?.value || ""
    ).trim().toLowerCase();

    if (!body) return;

    const filtered = compositeItems.filter(item =>
        `${item.name || item.itemName || ""} ${item.sku || ""}`
            .toLowerCase()
            .includes(search)
    );

    body.replaceChildren();

    if (count) {
        count.textContent = filtered.length;
    }

    if (filtered.length === 0) {
        const row = body.insertRow();
        const cell = row.insertCell();

        cell.colSpan = 7;
        cell.className = "empty-state";
        cell.textContent = "No composite items found.";
        return;
    }

    filtered.forEach(item => {
        const row = body.insertRow();

        [
            itemId(item),
            itemName(item),
            itemSku(item),
            money(item.sellingPrice),
            isTracked(item) ? "Yes" : "No",
            isTracked(item) ? itemStock(item) : "Not tracked"
        ].forEach(value => {
            row.insertCell().textContent = value ?? "";
        });

        const actions = row.insertCell();
        actions.className = "action-buttons";

        const viewButton = document.createElement("button");
        viewButton.type = "button";
        viewButton.className = "action-button edit-button";
        viewButton.textContent = "View Components";

        viewButton.addEventListener("click", () => {
            openCompositeComponents(item);
        });

        actions.appendChild(viewButton);
    });
}

// ============================================================
// VIEW COMPOSITE COMPONENTS
// ============================================================

async function openCompositeComponents(item) {
    try {
        const components = getArray(
            await apiRequest(
                `/api/composite-items/${encodeURIComponent(itemId(item))}/components`
            )
        );

        renderComponents(item, components);
        showModal("componentsModal");
    } catch (error) {
        console.error("Load components:", error);
        showMessage(error.message, "error");
    }
}

function renderComponents(composite, components) {
    setText(
        "componentsTitle",
        `${itemName(composite)} — Components`
    );

    const body = document.getElementById("componentsTableBody");
    if (!body) return;

    body.replaceChildren();

    if (components.length === 0) {
        const row = body.insertRow();
        const cell = row.insertCell();

        cell.colSpan = 4;
        cell.className = "empty-state";
        cell.textContent = "No components configured.";
        return;
    }

    components.forEach(component => {
        const row = body.insertRow();

        row.insertCell().textContent = itemName(component);
        row.insertCell().textContent = itemSku(component);
        row.insertCell().textContent = Number(component.quantity ?? 0);

        row.insertCell().textContent = money(
            component.componentSellingPrice ??
            component.sellingPrice
        );
    });
}

function closeComponentsModal() {
    hideModal("componentsModal");
}

// ============================================================
// MODAL HTML
// ============================================================

function createFeatureModals() {
    if (!document.getElementById("createCompositeModal")) {
        const modal = document.createElement("div");

        modal.id = "createCompositeModal";
        modal.className = "modal";
        modal.hidden = true;
        modal.setAttribute("aria-hidden", "true");

        modal.innerHTML = `
            <div class="modal-content"
                 role="dialog"
                 aria-modal="true"
                 aria-labelledby="createCompositeTitle">

                <div class="modal-header">
                    <div>
                        <h2 id="createCompositeTitle">Create Composite Item</h2>
                        <p>Enter item details and configure its components.</p>
                    </div>

                    <button type="button"
                            class="modal-close"
                            data-close-modal="createCompositeModal"
                            aria-label="Close">&times;</button>
                </div>

                <form id="createCompositeForm">

                    <div class="form-group">
                        <label for="compositeName">Item Name *</label>
                        <input type="text"
                               id="compositeName"
                               name="name"
                               class="form-control"
                               required>
                    </div>

                    <div class="form-group">
                        <label for="compositeSku">SKU *</label>
                        <input type="text"
                               id="compositeSku"
                               name="sku"
                               class="form-control"
                               required>
                    </div>

                    <div class="form-group">
                        <label for="compositeDescription">Description</label>
                        <textarea id="compositeDescription"
                                  name="description"
                                  class="form-control"
                                  rows="3"></textarea>
                    </div>

                    <div class="form-group">
                        <label for="compositeSellingPrice">Selling Price *</label>
                        <input type="number"
                               id="compositeSellingPrice"
                               name="sellingPrice"
                               class="form-control"
                               min="0"
                               step="0.01"
                               value="0.00"
                               required>
                        <small>
                            Calculated from component prices. You can edit this value.
                        </small>
                    </div>

                    <div class="form-group">
                        <label for="compositePurchasePrice">Purchase Price</label>
                        <input type="number"
                               id="compositePurchasePrice"
                               name="purchasePrice"
                               class="form-control"
                               min="0"
                               step="0.01"
                               value="0.00">
                    </div>

                    <div class="form-group">
                        <label for="compositeReorderLevel">Reorder Level</label>
                        <input type="number"
                               id="compositeReorderLevel"
                               name="reorderLevel"
                               class="form-control"
                               min="0"
                               step="1"
                               value="0">
                    </div>

                    <div class="form-group">
                        <label for="compositeTrackInventory">
                            <input type="checkbox"
                                   id="compositeTrackInventory">
                            Track inventory for this composite
                        </label>
                    </div>

                    <input type="hidden"
                           id="compositeItemType"
                           value="COMPOSITE">

                    <hr>

                    <h3>Components</h3>

                    <div class="form-group">
                        <label for="componentSelector">Component Item *</label>
                        <select id="componentSelector"
                                class="form-control">
                            <option value="">Select a component</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="newComponentQuantity">Quantity per Composite *</label>
                        <input type="number"
                               id="newComponentQuantity"
                               class="form-control"
                               min="1"
                               step="1"
                               value="1">
                    </div>

                    <button type="button"
                            id="addComponentButton"
                            class="button button-secondary">
                        Add Component
                    </button>

                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Component</th>
                                    <th>Unit Price</th>
                                    <th>Quantity</th>
                                    <th>Action</th>
                                </tr>
                            </thead>

                            <tbody id="newCompositeComponentsBody">
                                <tr>
                                    <td colspan="4" class="empty-state">
                                        Add components to continue.
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="form-group">
                        <p>
                            Calculated Selling Price:
                            <strong id="calculatedCompositePrice">₹0.00</strong>
                        </p>
                    </div>

                    <div class="modal-footer">
                        <button type="button"
                                class="button button-secondary"
                                data-close-modal="createCompositeModal">
                            Cancel
                        </button>

                        <button type="submit"
                                class="button button-primary"
                                id="saveCompositeButton">
                            Create Composite Item
                        </button>
                    </div>
                </form>
            </div>`;

        document.body.appendChild(modal);
    }

    if (!document.getElementById("createAssemblyModal")) {
        const modal = document.createElement("div");

        modal.id = "createAssemblyModal";
        modal.className = "modal";
        modal.hidden = true;
        modal.setAttribute("aria-hidden", "true");

        modal.innerHTML = `
            <div class="modal-content"
                 role="dialog"
                 aria-modal="true"
                 aria-labelledby="createAssemblyTitle">

                <div class="modal-header">
                    <div>
                        <h2 id="createAssemblyTitle">Create Assembly</h2>
                        <p>Review component requirements before assembling.</p>
                    </div>

                    <button type="button"
                            class="modal-close"
                            data-close-modal="createAssemblyModal"
                            aria-label="Close">&times;</button>
                </div>

                <form id="createAssemblyForm">
                    <div class="form-group">
                        <label for="assemblyCompositeSelector">Composite Item *</label>
                        <select id="assemblyCompositeSelector"
                                class="form-control"
                                required>
                            <option value="">Select a composite item</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="productionQuantity">Production Quantity *</label>
                        <input type="number"
                               id="productionQuantity"
                               class="form-control"
                               min="1"
                               step="1"
                               value="1"
                               required>
                    </div>

                    <p id="assemblyInventoryNotice" role="status"></p>

                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Component</th>
                                    <th>Per Assembly</th>
                                    <th>Required</th>
                                    <th>Available Stock</th>
                                    <th>Status</th>
                                </tr>
                            </thead>

                            <tbody id="assemblyComponentsBody">
                                <tr>
                                    <td colspan="5" class="empty-state">
                                        Select a composite item.
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="modal-footer">
                        <button type="button"
                                class="button button-secondary"
                                data-close-modal="createAssemblyModal">
                            Cancel
                        </button>

                        <button type="submit"
                                class="button button-primary"
                                id="saveAssemblyButton">
                            Create Assembly
                        </button>
                    </div>
                </form>
            </div>`;

        document.body.appendChild(modal);
    }

    document.querySelectorAll("[data-close-modal]").forEach(button => {
        if (button.dataset.listenerAttached) return;

        button.dataset.listenerAttached = "true";

        button.addEventListener("click", () => {
            hideModal(button.dataset.closeModal);
        });
    });

    ["createCompositeModal", "createAssemblyModal"].forEach(id => {
        const modal = document.getElementById(id);

        if (!modal || modal.dataset.listenerAttached) return;

        modal.dataset.listenerAttached = "true";

        modal.addEventListener("click", event => {
            if (event.target.id === id) {
                hideModal(id);
            }
        });
    });

    document.getElementById("addComponentButton")
        ?.addEventListener("click", addSelectedComponent);

    document.getElementById("createCompositeForm")
        ?.addEventListener("submit", saveCompositeItem);

    document.getElementById("compositeSellingPrice")
        ?.addEventListener("input", () => {
            compositePriceManuallyEdited = true;
        });

    document.getElementById("assemblyCompositeSelector")
        ?.addEventListener("change", loadAssemblyPreview);

    document.getElementById("productionQuantity")
        ?.addEventListener("input", renderAssemblyPreview);

    document.getElementById("createAssemblyForm")
        ?.addEventListener("submit", saveAssembly);
}

function showModal(id) {
    const modal = document.getElementById(id);
    if (!modal) return;

    modal.hidden = false;
    modal.classList.add("show");
    modal.setAttribute("aria-hidden", "false");
}

function hideModal(id) {
    const modal = document.getElementById(id);
    if (!modal) return;

    modal.classList.remove("show");
    modal.hidden = true;
    modal.setAttribute("aria-hidden", "true");
}

// ============================================================
// CREATE COMPOSITE ITEM
// ============================================================

async function openCreateCompositeModal() {
    const form = document.getElementById("createCompositeForm");
    form?.reset();

    newCompositeComponents = [];
    eligibleComponents = [];
    compositePriceManuallyEdited = false;

    const priceInput = document.getElementById("compositeSellingPrice");
    if (priceInput) priceInput.value = "0.00";

    const purchasePriceInput =
        document.getElementById("compositePurchasePrice");

    if (purchasePriceInput) purchasePriceInput.value = "0.00";

    setText("calculatedCompositePrice", money(0));
    renderNewCompositeComponents();

    const selector = document.getElementById("componentSelector");
    selector.replaceChildren(new Option("Loading components...", ""));

    showModal("createCompositeModal");

    try {
        eligibleComponents = getArray(
            await apiRequest("/api/composite-items/components")
        );

        selector.replaceChildren();
        selector.add(new Option("Select a component", ""));

        eligibleComponents.forEach(component => {
            const name = itemName(component) || "Unnamed item";
            const sku = itemSku(component);
            const price = money(itemPrice(component));

            // Names appear first, not just prices.
            const label = sku
                ? `${name} (${sku}) — ${price}`
                : `${name} — ${price}`;

            selector.add(
                new Option(label, String(itemId(component)))
            );
        });

        if (eligibleComponents.length === 0) {
            selector.replaceChildren(
                new Option("No eligible components available", "")
            );
        }
    } catch (error) {
        console.error("Load eligible components:", error);
        showMessage(error.message, "error");
    }
}

function addSelectedComponent() {
    const selector = document.getElementById("componentSelector");
    const quantityInput = document.getElementById("newComponentQuantity");

    const id = Number(selector?.value);
    const quantity = Number(quantityInput?.value);

    if (!id) {
        showMessage("Select a component first.", "error");
        return;
    }

    if (!Number.isSafeInteger(quantity) || quantity <= 0) {
        showMessage(
            "Component quantity must be a positive whole number.",
            "error"
        );
        return;
    }

    const component = eligibleComponents.find(
        item => Number(itemId(item)) === id
    );

    if (!component) {
        showMessage("Selected component was not found.", "error");
        return;
    }

    if (newCompositeComponents.some(
        entry => Number(itemId(entry.component)) === id
    )) {
        showMessage(
            "This component is already added. Remove it first to change its quantity.",
            "error"
        );
        return;
    }

    newCompositeComponents.push({ component, quantity });

    renderNewCompositeComponents();

    selector.value = "";
    quantityInput.value = "1";
}

function calculateCompositeSellingPrice() {
    return newCompositeComponents.reduce(
        (total, entry) =>
            total + itemPrice(entry.component) * entry.quantity,
        0
    );
}

function renderNewCompositeComponents() {
    const body = document.getElementById("newCompositeComponentsBody");
    if (!body) return;

    body.replaceChildren();

    if (newCompositeComponents.length === 0) {
        const row = body.insertRow();
        const cell = row.insertCell();

        cell.colSpan = 4;
        cell.className = "empty-state";
        cell.textContent = "Add components to continue.";
    } else {
        newCompositeComponents.forEach(({ component, quantity }, index) => {
            const row = body.insertRow();

            row.insertCell().textContent =
                `${itemName(component)}${itemSku(component) ? ` (${itemSku(component)})` : ""}`;

            row.insertCell().textContent = money(itemPrice(component));
            row.insertCell().textContent = quantity;

            const actionCell = row.insertCell();
            const removeButton = document.createElement("button");

            removeButton.type = "button";
            removeButton.className = "action-button";
            removeButton.textContent = "Remove";

            removeButton.addEventListener("click", () => {
                newCompositeComponents.splice(index, 1);
                renderNewCompositeComponents();
            });

            actionCell.appendChild(removeButton);
        });
    }

    const calculatedPrice = calculateCompositeSellingPrice();

    setText("calculatedCompositePrice", money(calculatedPrice));

    const priceInput = document.getElementById("compositeSellingPrice");

    // Recalculate automatically until the user edits the price.
    if (priceInput && !compositePriceManuallyEdited) {
        priceInput.value = calculatedPrice.toFixed(2);
    }
}

async function saveCompositeItem(event) {
    event.preventDefault();

    const name = document.getElementById("compositeName").value.trim();
    const sku = document.getElementById("compositeSku").value.trim();
    const description =
        document.getElementById("compositeDescription").value.trim();

    const sellingPriceInput =
        document.getElementById("compositeSellingPrice");

    const sellingPrice = Number(sellingPriceInput.value);

    const purchasePrice = Number(
        document.getElementById("compositePurchasePrice").value || 0
    );

    const reorderLevel = Number(
        document.getElementById("compositeReorderLevel").value || 0
    );


    if (!name || !sku) {
        showMessage("Item name and SKU are required.", "error");
        return;
    }

    if (newCompositeComponents.length === 0) {
        showMessage("Add at least one component.", "error");
        return;
    }

    if (!Number.isFinite(sellingPrice) || sellingPrice < 0) {
        showMessage("Enter a valid selling price.", "error");
        return;
    }

    if (!Number.isFinite(purchasePrice) || purchasePrice < 0) {
        showMessage("Enter a valid purchase price.", "error");
        return;
    }

    if (!Number.isSafeInteger(reorderLevel) || reorderLevel < 0) {
        showMessage("Reorder level must be a non-negative whole number.", "error");
        return;
    }

    const button = document.getElementById("saveCompositeButton");
    button.disabled = true;

    try {
        const payload = {
            item: {
                name,
                sku,
                description,
                itemType: "COMPOSITE",
                sellingPrice,
                purchasePrice,
                reorderLevel,
                trackInventory:
                    document.getElementById("compositeTrackInventory").checked
            },
            components: newCompositeComponents.map(
                ({ component, quantity }) => ({
                    componentItemId: Number(itemId(component)),
                    quantity
                })
            )
        };

        await apiRequest("/api/composite-items", {
            method: "POST",
            body: JSON.stringify(payload)
        });

        hideModal("createCompositeModal");

        newCompositeComponents = [];
        compositePriceManuallyEdited = false;

        showMessage("Composite item created successfully.", "success");
        await loadCompositeItems();
    } catch (error) {
        console.error("Create composite item:", error);
        showMessage(error.message, "error");
    } finally {
        button.disabled = false;
    }
}

// ============================================================
// CREATE ASSEMBLY
// ============================================================

async function openCreateAssemblyModal() {
    selectedAssemblyComposite = null;
    assemblyComponents = [];

    document.getElementById("createAssemblyForm")?.reset();

    setText("assemblyInventoryNotice", "");
    renderAssemblyPreview();

    const selector = document.getElementById("assemblyCompositeSelector");

    selector.replaceChildren(
        new Option("Loading composite items...", "")
    );

    showModal("createAssemblyModal");

    try {
        await loadCompositeItems();

        selector.replaceChildren();
        selector.add(new Option("Select a composite item", ""));

        compositeItems.forEach(item => {
            selector.add(
                new Option(
                    `${itemName(item)}${itemSku(item) ? ` (${itemSku(item)})` : ""}`,
                    String(itemId(item))
                )
            );
        });
    } catch (error) {
        showMessage(error.message, "error");
    }
}

async function loadAssemblyPreview() {
    const id = Number(
        document.getElementById("assemblyCompositeSelector")?.value
    );

    selectedAssemblyComposite = compositeItems.find(
        item => Number(itemId(item)) === id
    ) || null;

    assemblyComponents = [];
    renderAssemblyPreview();

    if (!selectedAssemblyComposite) return;

    try {
        assemblyComponents = getArray(
            await apiRequest(
                `/api/composite-items/${encodeURIComponent(id)}/components`
            )
        );

        renderAssemblyPreview();
    } catch (error) {
        console.error("Load assembly components:", error);
        showMessage(error.message, "error");
    }
}

function renderAssemblyPreview() {
    const body = document.getElementById("assemblyComponentsBody");
    const notice = document.getElementById("assemblyInventoryNotice");

    if (!body) return;

    body.replaceChildren();

    if (!selectedAssemblyComposite) {
        const row = body.insertRow();
        const cell = row.insertCell();

        cell.colSpan = 5;
        cell.className = "empty-state";
        cell.textContent = "Select a composite item.";

        if (notice) notice.textContent = "";
        return;
    }

    if (assemblyComponents.length === 0) {
        const row = body.insertRow();
        const cell = row.insertCell();

        cell.colSpan = 5;
        cell.className = "empty-state";
        cell.textContent = "No components configured.";

        if (notice) notice.textContent = "";
        return;
    }

    const productionQuantity = Number(
        document.getElementById("productionQuantity")?.value || 1
    );

    let insufficientStock = false;

    assemblyComponents.forEach(component => {
        const perAssembly = Number(component.quantity ?? 0);
        const required = perAssembly * productionQuantity;

        const service = itemType(component) === "SERVICE";
        const tracked = !service && isTracked(component);
        const available = itemStock(component);

        let status;

        if (service) {
            status = "Service — no stock deduction";
        } else if (!tracked) {
            status = "Inventory not tracked";
        } else if (available < required) {
            status = "Insufficient stock";
            insufficientStock = true;
        } else {
            status = "Stock available";
        }

        const row = body.insertRow();

        row.insertCell().textContent = itemName(component);
        row.insertCell().textContent = perAssembly;
        row.insertCell().textContent = required;
        row.insertCell().textContent = tracked ? available : "—";
        row.insertCell().textContent = status;
    });

    if (notice) {
        notice.textContent = insufficientStock
            ? "Warning: some components may have insufficient stock. The server will validate availability before assembly."
            : "Stock shown is a preview. The server performs final inventory validation.";
    }
}

async function saveAssembly(event) {
    event.preventDefault();

    const compositeId = Number(
        document.getElementById("assemblyCompositeSelector")?.value
    );

    const productionQuantity = Number(
        document.getElementById("productionQuantity")?.value
    );

    if (!compositeId) {
        showMessage("Select a composite item.", "error");
        return;
    }

    if (!Number.isSafeInteger(productionQuantity) || productionQuantity <= 0) {
        showMessage(
            "Production quantity must be a positive whole number.",
            "error"
        );
        return;
    }

    const button = document.getElementById("saveAssemblyButton");
    button.disabled = true;

    try {
        await apiRequest("/api/composite-items/assembly", {
            method: "POST",
            body: JSON.stringify({
                compositeItemId: compositeId,
                productionQuantity
            })
        });

        hideModal("createAssemblyModal");

        showMessage("Assembly created successfully.", "success");
        await loadCompositeItems();
    } catch (error) {
        console.error("Create assembly:", error);
        showMessage(error.message, "error");
    } finally {
        button.disabled = false;
    }
}
