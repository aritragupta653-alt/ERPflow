<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Package - ERPFlow</title>
    <link rel="stylesheet" href="/erpflow/css/app.css">

    <style>
        .edit-package-container {
            max-width: 1000px;
            margin: 30px auto;
            padding: 0 20px;
        }

        .package-info {
            margin-bottom: 20px;
        }

        .form-row {
            display: grid;
            grid-template-columns: repeat(4, minmax(0, 1fr));
            gap: 16px;
        }

        .package-items-table {
            width: 100%;
            border-collapse: collapse;
        }

        .package-items-table th,
        .package-items-table td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }

        .package-items-table input {
            width: 100%;
            box-sizing: border-box;
        }

        .error-message {
            color: #b42318;
            margin-top: 12px;
        }

        .success-message {
            color: #067647;
            margin-top: 12px;
        }

        .form-actions {
            display: flex;
            gap: 12px;
            margin-top: 20px;
        }

        .muted {
            color: #667085;
        }

        @media (max-width: 700px) {
            .form-row {
                grid-template-columns: repeat(2, minmax(0, 1fr));
            }

            .table-container {
                overflow-x: auto;
            }
        }
    </style>
</head>

<body>
    <div class="edit-package-container">

        <div class="page-header">
            <div>
                <h1>Edit Package</h1>
                <p>Update package dimensions and quantities.</p>
            </div>

            <a class="btn btn-secondary" href="/erpflow/packages.jsp">
                Back to Packages
            </a>
        </div>

        <div class="card">
            <div class="card-header">
                <h2 id="packageHeading">Loading package...</h2>
            </div>

            <div class="package-info">
                <p>
                    <strong>Sales Order:</strong>
                    <span id="salesOrderNumber">—</span>
                </p>

                <p>
                    <strong>Status:</strong>
                    <span id="packageStatus">—</span>
                </p>
            </div>

            <form id="editPackageForm" novalidate>
                <div class="form-row">
                    <div class="form-group">
                        <label for="weight">Weight (kg)</label>
                        <input
                            type="number"
                            id="weight"
                            min="0.01"
                            step="0.01"
                            required
                        >
                    </div>

                    <div class="form-group">
                        <label for="length">Length (cm)</label>
                        <input
                            type="number"
                            id="length"
                            min="0.01"
                            step="0.01"
                            required
                        >
                    </div>

                    <div class="form-group">
                        <label for="width">Width (cm)</label>
                        <input
                            type="number"
                            id="width"
                            min="0.01"
                            step="0.01"
                            required
                        >
                    </div>

                    <div class="form-group">
                        <label for="height">Height (cm)</label>
                        <input
                            type="number"
                            id="height"
                            min="0.01"
                            step="0.01"
                            required
                        >
                    </div>
                </div>

                <h2>Package Items</h2>

                <p class="muted">
                    Change quantities below. Each item must remain associated
                    with its original Sales Order line.
                </p>

                <div class="table-container">
                    <table class="package-items-table">
                        <thead>
                            <tr>
                                <th>Item</th>
                                <th>SKU</th>
                                <th>Sales Order Line</th>
                                <th>Quantity</th>
                            </tr>
                        </thead>

                        <tbody id="packageItemsBody">
                            <tr>
                                <td colspan="4">Loading package items...</td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div
                    id="editPackageError"
                    class="error-message"
                    role="alert"
                    style="display: none;"
                ></div>

                <div
                    id="editPackageSuccess"
                    class="success-message"
                    role="status"
                    style="display: none;"
                ></div>

                <div class="form-actions">
                    <button
                        type="submit"
                        class="btn btn-primary"
                        id="savePackageButton"
                    >
                        Save Changes
                    </button>

                    <a class="btn btn-secondary" href="/erpflow/packages.jsp">
                        Cancel
                    </a>
                </div>
            </form>
        </div>
    </div>

    <script>
        const packageId = new URLSearchParams(window.location.search).get("id");

        const form = document.getElementById("editPackageForm");
        const saveButton = document.getElementById("savePackageButton");
        const itemsBody = document.getElementById("packageItemsBody");
        const errorBox = document.getElementById("editPackageError");
        const successBox = document.getElementById("editPackageSuccess");

        let loadedPackage = null;
        let packageItems = [];

        document.addEventListener("DOMContentLoaded", loadPackage);

        async function loadPackage() {
            hideMessages();

            if (!packageId || !/^[1-9]\d*$/.test(packageId)) {
                showError(
                    "A valid package ID is required in the URL. Example: editPackage.jsp?id=1"
                );

                form.style.display = "none";
                return;
            }

            try {
                const response = await fetch(
                    `/erpflow/api/packages/${encodeURIComponent(packageId)}`
                );

                const data = await readJsonResponse(response);

                if (!response.ok) {
                    throw new Error(data.error || "Unable to load package.");
                }

                if (!data.package) {
                    throw new Error("Package data was not returned by the server.");
                }

                loadedPackage = data.package;
                packageItems = Array.isArray(data.items) ? data.items : [];

                if (!isEditable(loadedPackage.status)) {
                    form.style.display = "none";
                    showError(
                        "This package cannot be edited because its status is not PACKED."
                    );
                    return;
                }

                document.getElementById("packageHeading").textContent =
                    loadedPackage.packageNumber || `Package ${packageId}`;

                const order = loadedPackage.salesOrder || {};

                document.getElementById("salesOrderNumber").textContent =
                    order.salesOrderNumber || order.orderNumber || order.id || "—";

                document.getElementById("packageStatus").textContent =
                    loadedPackage.status || "—";

                setInputValue("weight", loadedPackage.weight);
                setInputValue("length", loadedPackage.length);
                setInputValue("width", loadedPackage.width);
                setInputValue("height", loadedPackage.height);

                renderItems();
            } catch (error) {
                showError(error.message || "Unable to load package.");
                form.style.display = "none";
            }
        }

        function renderItems() {
            itemsBody.innerHTML = "";

            if (packageItems.length === 0) {
                itemsBody.innerHTML =
                    '<tr><td colspan="4">This package has no items to edit.</td></tr>';
                return;
            }

            packageItems.forEach((packageItem, index) => {
                const item = packageItem.item || {};
                const row = document.createElement("tr");

                row.innerHTML = `
                    <td>${escapeHtml(item.name || "Unnamed item")}</td>
                    <td>${escapeHtml(item.sku || "—")}</td>
                    <td>${escapeHtml(packageItem.salesOrderItemId ?? "—")}</td>
                    <td>
                        <input
                            type="number"
                            class="quantity-input"
                            data-index="${index}"
                            min="1"
                            step="1"
                            value="${Number(packageItem.quantity) || 1}"
                            required
                            aria-label="Quantity for ${escapeHtml(item.name || "item")}"
                        >
                    </td>
                `;

                itemsBody.appendChild(row);
            });
        }

        form.addEventListener("submit", async function (event) {
            event.preventDefault();
            hideMessages();

            if (!loadedPackage) {
                showError("Package details have not loaded.");
                return;
            }

            if (!isEditable(loadedPackage.status)) {
                showError("Only PACKED packages can be edited.");
                return;
            }

            const dimensions = {
                weight: readPositiveNumber("weight"),
                length: readPositiveNumber("length"),
                width: readPositiveNumber("width"),
                height: readPositiveNumber("height")
            };

            if (Object.values(dimensions).some(value => value === null)) {
                showError(
                    "Weight and all dimensions must be valid numbers greater than zero."
                );
                return;
            }

            const updatedItems = [];

            for (const input of itemsBody.querySelectorAll(".quantity-input")) {
                const index = Number(input.dataset.index);
                const original = packageItems[index];
                const quantity = Number(input.value);

                if (!Number.isSafeInteger(quantity) || quantity <= 0) {
                    showError("Every item quantity must be a positive whole number.");
                    input.focus();
                    return;
                }

                const itemId =
                    original && original.item ? Number(original.item.id) : NaN;

                const salesOrderItemId = Number(
                    original && original.salesOrderItemId
                );

                if (
                    !Number.isSafeInteger(itemId) ||
                    itemId <= 0 ||
                    !Number.isSafeInteger(salesOrderItemId) ||
                    salesOrderItemId <= 0
                ) {
                    showError(
                        "An item or Sales Order line has invalid data. Reload the package and try again."
                    );
                    return;
                }

                updatedItems.push({
                    itemId,
                    salesOrderItemId,
                    quantity
                });
            }

            if (updatedItems.length === 0) {
                showError("A package must contain at least one item.");
                return;
            }

            saveButton.disabled = true;
            saveButton.textContent = "Saving...";

            try {
                const response = await fetch(
                    `/erpflow/api/packages/${encodeURIComponent(packageId)}`,
                    {
                        method: "PUT",
                        headers: {
                            "Content-Type": "application/json"
                        },
                        body: JSON.stringify({
                            salesOrderId: Number(loadedPackage.salesOrder.id),
                            ...dimensions,
                            items: updatedItems
                        })
                    }
                );

                const data = await readJsonResponse(response);

                if (!response.ok) {
                    throw new Error(data.error || "Package update failed.");
                }

                showSuccess(data.message || "Package updated successfully.");

                // Refresh server values after a successful save.
                await loadPackage();
            } catch (error) {
                showError(error.message || "Package update failed.");
            } finally {
                saveButton.disabled = false;
                saveButton.textContent = "Save Changes";
            }
        });

        function isEditable(status) {
            return (
                typeof status === "string" &&
                status.toUpperCase() === "PACKED"
            );
        }

        function readPositiveNumber(id) {
            const input = document.getElementById(id);
            const value = Number(input.value);

            if (
                input.value.trim() === "" ||
                !Number.isFinite(value) ||
                value <= 0
            ) {
                return null;
            }

            return value;
        }

        function setInputValue(id, value) {
            const input = document.getElementById(id);
            input.value = value == null ? "" : value;
        }

        async function readJsonResponse(response) {
            const text = await response.text();

            if (!text) {
                return {};
            }

            try {
                return JSON.parse(text);
            } catch {
                throw new Error("The server returned an invalid response.");
            }
        }

        function showError(message) {
            errorBox.textContent = message;
            errorBox.style.display = "block";
            successBox.style.display = "none";
        }

        function showSuccess(message) {
            successBox.textContent = message;
            successBox.style.display = "block";
            errorBox.style.display = "none";
        }

        function hideMessages() {
            errorBox.style.display = "none";
            successBox.style.display = "none";
            errorBox.textContent = "";
            successBox.textContent = "";
        }

        function escapeHtml(value) {
            return String(value).replace(/[&<>"']/g, character => ({
                "&": "&amp;",
                "<": "&lt;",
                ">": "&gt;",
                '"': "&quot;",
                "'": "&#39;"
            })[character]);
        }
    </script>
</body>
</html>