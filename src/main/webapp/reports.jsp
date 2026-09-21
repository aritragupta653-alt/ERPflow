
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ERPFlow | Reports</title>

    <style>
        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f5f7fb;
            color: #1f2937;
        }

        .page {
            max-width: 1400px;
            margin: auto;
            padding: 28px;
        }

        .header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 16px;
            margin-bottom: 24px;
        }

        h1 {
            margin: 0 0 8px;
            font-size: 28px;
        }

        .subtitle {
            color: #6b7280;
            margin: 0;
        }

        .panel {
            background: white;
            border: 1px solid #e5e7eb;
            border-radius: 12px;
            padding: 22px;
            margin-bottom: 22px;
            box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
        }

        .filters {
            display: grid;
            grid-template-columns: repeat(5, minmax(130px, 1fr));
            gap: 14px;
            align-items: end;
        }

        label {
            display: block;
            font-size: 13px;
            font-weight: 600;
            margin-bottom: 7px;
        }

        input, select, button {
            width: 100%;
            padding: 11px 12px;
            border: 1px solid #d1d5db;
            border-radius: 7px;
            background: white;
            font-size: 14px;
        }

        button {
            cursor: pointer;
            border: none;
            background: #2563eb;
            color: white;
            font-weight: 600;
        }

        button:hover {
            background: #1d4ed8;
        }

        .secondary {
            background: #e5e7eb;
            color: #111827;
        }

        .secondary:hover {
            background: #d1d5db;
        }

        .table-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 12px;
            margin-bottom: 16px;
        }

        .table-header h2 {
            margin: 0;
            font-size: 20px;
        }

        .table-wrap {
            overflow-x: auto;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            white-space: nowrap;
        }

        th, td {
            padding: 13px 15px;
            text-align: left;
            border-bottom: 1px solid #e5e7eb;
            font-size: 14px;
        }

        th {
            background: #f9fafb;
            color: #4b5563;
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: .04em;
        }

        tbody tr:hover {
            background: #f9fafb;
        }

        .message {
            padding: 14px;
            border-radius: 8px;
            margin-bottom: 15px;
            display: none;
        }

        .error {
            display: block;
            color: #991b1b;
            background: #fee2e2;
        }

        .empty {
            padding: 30px;
            text-align: center;
            color: #6b7280;
        }

        .status {
            display: inline-block;
            padding: 5px 9px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 600;
        }

        .IN_STOCK {
            background: #dcfce7;
            color: #166534;
        }

        .LOW_STOCK {
            background: #fef3c7;
            color: #92400e;
        }

        .OUT_OF_STOCK {
            background: #fee2e2;
            color: #991b1b;
        }

        @media (max-width: 900px) {
            .filters {
                grid-template-columns: repeat(2, minmax(130px, 1fr));
            }

            .page {
                padding: 16px;
            }
        }

        @media (max-width: 520px) {
            .filters {
                grid-template-columns: 1fr;
            }

            .header {
                align-items: flex-start;
                flex-direction: column;
            }
        }
    </style>
</head>

<body>
<div class="page">

    <div class="header">
        <div>
            <h1>Reports</h1>
            <p class="subtitle">
                View inventory, item sales, customer sales and order summaries.
            </p>
        </div>
    </div>

    <section class="panel">
        <div class="filters">

            <div>
                <label for="reportType">Report</label>
                <select id="reportType">
                    <option value="inventory">Inventory Stock Summary</option>
                    <option value="sales-by-item">Sales per Item</option>
                    <option value="sales-by-customer">Sales per Customer</option>
                    <option value="sales-order-summary">Sales Order Summary</option>
                </select>
            </div>

            <div>
                <label for="search">Search</label>
                <input id="search" type="text"
                       placeholder="Search report...">
            </div>

            <div>
                <label for="from">From Date</label>
                <input id="from" type="date">
            </div>

            <div>
                <label for="to">To Date</label>
                <input id="to" type="date">
            </div>

            <div id="stockFilterContainer">
                <label for="stock">Stock Status</label>
                <select id="stock">
                    <option value="all">All</option>
                    <option value="IN_STOCK">In Stock</option>
                    <option value="LOW_STOCK">Low Stock</option>
                    <option value="OUT_OF_STOCK">Out of Stock</option>
                </select>
            </div>

            <div>
                <button id="applyButton" type="button">Apply Filters</button>
            </div>

            <div>
                <button id="resetButton" class="secondary" type="button">
                    Reset
                </button>
            </div>

        </div>
    </section>

    <section class="panel">
        <div class="table-header">
            <h2 id="reportTitle">Inventory Stock Summary</h2>
            <span id="rowCount">0 rows</span>
        </div>

        <div id="message" class="message"></div>

        <div class="table-wrap">
            <table>
                <thead id="tableHead"></thead>
                <tbody id="tableBody"></tbody>
            </table>
        </div>
    </section>

</div>

<script>
    const contextPath = '<%= request.getContextPath() %>';

    const reportType = document.getElementById("reportType");
    const searchInput = document.getElementById("search");
    const fromInput = document.getElementById("from");
    const toInput = document.getElementById("to");
    const stockInput = document.getElementById("stock");

    const stockContainer =
        document.getElementById("stockFilterContainer");

    const tableHead = document.getElementById("tableHead");
    const tableBody = document.getElementById("tableBody");
    const message = document.getElementById("message");
    const rowCount = document.getElementById("rowCount");
    const reportTitle = document.getElementById("reportTitle");

    const reportConfig = {
        "inventory": {
            title: "Inventory Stock Summary",
            endpoint: "/api/reports/inventory",
            columns: [
                ["sku", "SKU"],
                ["name", "Item"],
                ["quantity", "On Hand"],
                ["reserved", "Reserved"],
                ["available", "Available"],
                ["reorderLevel", "Reorder Level"],
                ["stockIn", "Stock In"],
                ["stockOut", "Stock Out"],
                ["status", "Status"]
            ]
        },

        "sales-by-item": {
            title: "Sales per Item",
            endpoint: "/api/reports/sales-by-item",
            columns: [
                ["sku", "SKU"],
                ["name", "Item"],
                ["quantitySold", "Quantity Sold"],
                ["orderCount", "Orders"],
                ["revenue", "Revenue"]
            ]
        },

        "sales-by-customer": {
            title: "Sales per Customer",
            endpoint: "/api/reports/sales-by-customer",
            columns: [
                ["customerName", "Customer"],
                ["orderCount", "Orders"],
                ["quantitySold", "Quantity Sold"],
                ["revenue", "Revenue"]
            ]
        },

        "sales-order-summary": {
            title: "Sales Order Summary",
            endpoint: "/api/reports/sales-order-summary",
            columns: [
                ["orderId", "Order ID"],
                ["orderDate", "Order Date"],
                ["customerName", "Customer"],
                ["status", "Status"],
                ["itemQuantity", "Item Quantity"],
                ["subtotal", "Subtotal"]
            ]
        }
    };

    function showError(text) {
        message.textContent = text;
        message.className = "message error";
    }

    function clearError() {
        message.textContent = "";
        message.className = "message";
    }

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function formatValue(key, value) {
        if (value === null || value === undefined) {
            return "—";
        }

        if (key === "status") {
            const safe = escapeHtml(value);
            return '<span class="status ' + safe + '">' + safe + '</span>';
        }

        if (typeof value === "number") {
            return Number(value).toLocaleString(undefined, {
                maximumFractionDigits: 2
            });
        }

        return escapeHtml(value);
    }

    function renderTable(rows, config) {
        tableHead.innerHTML = "";
        tableBody.innerHTML = "";

        const headerRow = document.createElement("tr");

        config.columns.forEach(column => {
            const th = document.createElement("th");
            th.textContent = column[1];
            headerRow.appendChild(th);
        });

        tableHead.appendChild(headerRow);

        if (!rows.length) {
            const tr = document.createElement("tr");
            const td = document.createElement("td");

            td.colSpan = config.columns.length;
            td.className = "empty";
            td.textContent = "No records found for the selected filters.";

            tr.appendChild(td);
            tableBody.appendChild(tr);
            rowCount.textContent = "0 rows";
            return;
        }

        rows.forEach(row => {
            const tr = document.createElement("tr");

            config.columns.forEach(column => {
                const td = document.createElement("td");
                td.innerHTML = formatValue(column[0], row[column[0]]);
                tr.appendChild(td);
            });

            tableBody.appendChild(tr);
        });

        rowCount.textContent =
            rows.length + (rows.length === 1 ? " row" : " rows");
    }

    async function loadReport() {
        clearError();

        const selected = reportType.value;
        const config = reportConfig[selected];

        reportTitle.textContent = config.title;
        stockContainer.style.display =
            selected === "inventory" ? "block" : "none";

        const params = new URLSearchParams();

        if (searchInput.value.trim()) {
            params.set("search", searchInput.value.trim());
        }

        if (fromInput.value) {
            params.set("from", fromInput.value);
        }

        if (toInput.value) {
            params.set("to", toInput.value);
        }

        if (selected === "inventory") {
            params.set("stock", stockInput.value);
        }

        tableBody.innerHTML =
            '<tr><td class="empty" colspan="' +
            config.columns.length +
            '">Loading report...</td></tr>';

        try {
            const response = await fetch(
                contextPath + config.endpoint + "?" + params.toString(),
                {
                    method: "GET",
                    headers: {
                        "Accept": "application/json"
                    }
                }
            );

            const result = await response.json();

            if (!response.ok) {
                throw new Error(
                    result.message || "Unable to load report."
                );
            }

            if (!Array.isArray(result)) {
                throw new Error("Unexpected response received from server.");
            }

            renderTable(result, config);

        } catch (error) {
            tableHead.innerHTML = "";
            tableBody.innerHTML = "";
            rowCount.textContent = "—";
            showError(error.message || "An unexpected error occurred.");
        }
    }

    document.getElementById("applyButton")
        .addEventListener("click", loadReport);

    document.getElementById("resetButton")
        .addEventListener("click", () => {
            searchInput.value = "";
            fromInput.value = "";
            toInput.value = "";
            stockInput.value = "all";
            loadReport();
        });

    reportType.addEventListener("change", loadReport);

    searchInput.addEventListener("keydown", event => {
        if (event.key === "Enter") {
            loadReport();
        }
    });

    loadReport();
</script>
</body>
</html>