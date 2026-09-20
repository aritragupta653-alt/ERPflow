
<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>ERPFlow - Reports Center</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/app.css">

    <style>
        .reports-wrap {
            max-width: 1300px;
            margin: 32px auto;
            padding: 0 22px;
        }

        .reports-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 16px;
            margin-bottom: 24px;
        }

        .reports-header h1 {
            margin: 0 0 6px;
        }

        .muted {
            color: #6b7280;
        }

        .report-tabs {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            margin-bottom: 22px;
        }

        .report-tab {
            border: 1px solid #dbe1ea;
            border-radius: 9px;
            background: white;
            color: #334155;
            padding: 11px 16px;
            cursor: pointer;
            font: inherit;
            font-weight: 600;
        }

        .report-tab.active {
            color: white;
            background: #2563eb;
            border-color: #2563eb;
        }

        .report-panel {
            background: white;
            border: 1px solid #e5e7eb;
            border-radius: 14px;
            padding: 22px;
            box-shadow: 0 2px 8px #00000008;
        }

        .report-panel h2 {
            margin: 0 0 6px;
        }

        .report-controls {
            display: flex;
            flex-wrap: wrap;
            align-items: center;
            gap: 12px;
            margin: 20px 0;
        }

        .report-controls input,
        .report-controls select {
            min-width: 175px;
            padding: 11px 12px;
            border: 1px solid #cbd5e1;
            border-radius: 8px;
            background: white;
            color: #1e293b;
            font: inherit;
        }

        .report-btn {
            border: 0;
            border-radius: 8px;
            background: #2563eb;
            color: white;
            padding: 11px 17px;
            font-weight: 600;
            cursor: pointer;
        }

        .report-btn.secondary {
            background: #eef2ff;
            color: #1d4ed8;
        }

        .report-table-wrap {
            width: 100%;
            overflow-x: auto;
        }

        .report-table {
            width: 100%;
            min-width: 760px;
            border-collapse: collapse;
        }

        .report-table th,
        .report-table td {
            padding: 13px 14px;
            border-bottom: 1px solid #e5e7eb;
            text-align: left;
            white-space: nowrap;
        }

        .report-table th {
            background: #f8fafc;
            color: #475569;
            font-size: 13px;
        }

        .report-table tbody tr:hover {
            background: #f8fafc;
        }

        .empty-state {
            padding: 26px;
            text-align: center !important;
            color: #64748b;
        }

        .error-state {
            color: #b91c1c;
        }

        .status-pill {
            display: inline-block;
            border-radius: 20px;
            padding: 4px 9px;
            background: #dcfce7;
            color: #166534;
            font-size: 12px;
            font-weight: 600;
        }

        .status-pill.low {
            background: #fef3c7;
            color: #92400e;
        }

        .status-pill.out {
            background: #fee2e2;
            color: #991b1b;
        }

        .report-note {
            margin-top: 15px;
            color: #6b7280;
            font-size: 13px;
            line-height: 1.5;
        }

        @media (max-width: 650px) {
            .reports-wrap {
                padding: 0 12px;
            }

            .report-panel {
                padding: 15px;
            }

            .report-controls input,
            .report-controls select {
                width: 100%;
                min-width: 0;
            }
        }
    </style>
</head>

<body>

<header class="navbar">
    <a href="${pageContext.request.contextPath}/home.jsp" class="logo">
        <img src="${pageContext.request.contextPath}/images/erpflow-logo.png"
             alt="ERPFlow Logo">
        <span>ERPFlow</span>
    </a>
</header>

<main class="reports-wrap">

    <div class="reports-header">
        <div>
            <h1>Reports Center</h1>
            <p class="muted">
                Analyze inventory, shipped sales, and customer activity.
            </p>
        </div>

        <a class="report-btn secondary"
           href="${pageContext.request.contextPath}/home.jsp"
           style="text-decoration:none">
            ← Dashboard
        </a>
    </div>

    <div class="report-tabs">
        <button class="report-tab active" data-report="inventory">
            Inventory Stock Summary
        </button>

        <button class="report-tab" data-report="itemSales">
            Sales per Item
        </button>

        <button class="report-tab" data-report="customerSales">
            Sales per Customer
        </button>
    </div>

    <!-- INVENTORY REPORT -->

    <section class="report-panel" id="inventoryPanel">
        <h2>Inventory Stock Summary</h2>
        <p class="muted">
            View current stock and stock movements during a selected date range.
        </p>

        <div class="report-controls">
            <input id="inventorySearch"
                   type="search"
                   placeholder="Search item name or SKU">

            <select id="inventoryStock">
                <option value="all">All stock statuses</option>
                <option value="low">Low stock</option>
                <option value="out">Out of stock</option>
            </select>

            <label>
                From
                <input id="inventoryFrom" type="date">
            </label>

            <label>
                To
                <input id="inventoryTo" type="date">
            </label>

            <button class="report-btn" id="inventoryRun">
                Run report
            </button>

            <button class="report-btn secondary" id="inventoryExport">
                Export CSV
            </button>
        </div>

        <div class="report-table-wrap">
            <table class="report-table">
                <thead>
                <tr>
                    <th>SKU</th>
                    <th>Item</th>
                    <th>On hand</th>
                    <th>Reserved</th>
                    <th>Available</th>
                    <th>Reorder level</th>
                    <th>Status</th>
                    <th>Stock in (period)</th>
                    <th>Stock out (period)</th>
                    <th>Stock value</th>
                </tr>
                </thead>
                <tbody id="inventoryRows">
                <tr><td colspan="10" class="empty-state">Loading report…</td></tr>
                </tbody>
            </table>
        </div>

        <p class="report-note">
            On hand, reserved, and available quantities represent current inventory.
            Stock-in and stock-out columns are movement totals between the selected
            dates, inclusive. Leaving a date blank means no boundary for that side.
        </p>
    </section>

    <!-- SALES PER ITEM REPORT -->

    <section class="report-panel" id="itemSalesPanel" hidden>
        <h2>Sales per Item</h2>
        <p class="muted">
            Item sales are counted only when the associated sales order is shipped.
        </p>

        <div class="report-controls">
            <input id="itemSearch"
                   type="search"
                   placeholder="Search item name or SKU">

            <label>
                From
                <input id="itemFrom" type="date">
            </label>

            <label>
                To
                <input id="itemTo" type="date">
            </label>

            <button class="report-btn" id="itemRun">Run report</button>
            <button class="report-btn secondary" id="itemExport">Export CSV</button>
        </div>

        <div class="report-table-wrap">
            <table class="report-table">
                <thead>
                <tr>
                    <th>SKU</th>
                    <th>Item</th>
                    <th>Quantity sold</th>
                    <th>Shipped orders</th>
                    <th>Sales revenue</th>
                </tr>
                </thead>
                <tbody id="itemRows">
                <tr><td colspan="5" class="empty-state">Run the report to view results.</td></tr>
                </tbody>
            </table>
        </div>

        <p class="report-note">
            Revenue is calculated from each sales order line's quantity multiplied
            by its saved selling price. Multiple lines for the same item are summed.
        </p>
    </section>

    <!-- SALES PER CUSTOMER REPORT -->

    <section class="report-panel" id="customerSalesPanel" hidden>
        <h2>Sales per Customer</h2>
        <p class="muted">
            Customer sales include shipped orders only.
        </p>

        <div class="report-controls">
            <input id="customerSearch"
                   type="search"
                   placeholder="Search customer name or ID">

            <label>
                From
                <input id="customerFrom" type="date">
            </label>

            <label>
                To
                <input id="customerTo" type="date">
            </label>

            <button class="report-btn" id="customerRun">Run report</button>
            <button class="report-btn secondary" id="customerExport">Export CSV</button>
        </div>

        <div class="report-table-wrap">
            <table class="report-table">
                <thead>
                <tr>
                    <th>Customer ID</th>
                    <th>Customer</th>
                    <th>Shipped orders</th>
                    <th>Quantity sold</th>
                    <th>Sales revenue</th>
                </tr>
                </thead>
                <tbody id="customerRows">
                <tr><td colspan="5" class="empty-state">Run the report to view results.</td></tr>
                </tbody>
            </table>
        </div>

        <p class="report-note">
            Revenue is the sum of shipped sales order line amounts for each customer.
        </p>
    </section>

</main>

<script>
    const contextPath = '${pageContext.request.contextPath}';

    let inventoryData = [];
    let itemSalesData = [];
    let customerSalesData = [];

    function money(value) {
        if (value == null || value === '') {
            return '—';
        }

        const number = Number(value);

        if (!Number.isFinite(number)) {
            return '—';
        }

        return number.toLocaleString(undefined, {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    }

    function cell(row, value) {
        const td = document.createElement('td');
        td.textContent = value == null || value === '' ? '—' : String(value);
        row.appendChild(td);
        return td;
    }

    function statusCell(row, status) {
        const td = document.createElement('td');
        const pill = document.createElement('span');

        pill.className = 'status-pill';

        if (status === 'Low stock') {
            pill.classList.add('low');
        } else if (status === 'Out of stock') {
            pill.classList.add('out');
        }

        pill.textContent = status || '—';
        td.appendChild(pill);
        row.appendChild(td);
    }

    function messageRow(tbody, colspan, message, error) {
        const tr = document.createElement('tr');
        const td = document.createElement('td');

        td.colSpan = colspan;
        td.className = error ? 'empty-state error-state' : 'empty-state';
        td.textContent = message;

        tr.appendChild(td);
        tbody.replaceChildren(tr);
    }

    async function fetchReport(endpoint, parameters) {
        const query = new URLSearchParams();

        Object.keys(parameters).forEach(function (key) {
            query.set(key, parameters[key] || '');
        });

        const response = await fetch(
            contextPath + endpoint + '?' + query.toString(),
            { headers: { 'Accept': 'application/json' } }
        );

        const responseText = await response.text();

        let data;

        try {
            data = JSON.parse(responseText);
        } catch (error) {
            throw new Error(
                'The server returned a non-JSON response. Check the API URL and Tomcat logs.'
            );
        }

        if (!response.ok) {
            throw new Error(data.error || 'The report request failed.');
        }

        if (!Array.isArray(data)) {
            throw new Error(data.error || 'Unexpected report response.');
        }

        return data;
    }

    // INVENTORY

    async function loadInventory() {
        const tbody = document.getElementById('inventoryRows');

        messageRow(tbody, 10, 'Loading report…', false);

        try {
            inventoryData = await fetchReport('/api/reports/inventory', {
                search: document.getElementById('inventorySearch').value,
                stock: document.getElementById('inventoryStock').value,
                from: document.getElementById('inventoryFrom').value,
                to: document.getElementById('inventoryTo').value
            });

            if (!inventoryData.length) {
                messageRow(tbody, 10, 'No matching inventory records.', false);
                return;
            }

            const fragment = document.createDocumentFragment();

            inventoryData.forEach(function (item) {
                const tr = document.createElement('tr');

                cell(tr, item.sku);
                cell(tr, item.name);
                cell(tr, item.quantity);
                cell(tr, item.reserved);
                cell(tr, item.available);
                cell(tr, item.reorderLevel);
                statusCell(tr, item.status);
                cell(tr, item.stockIn);
                cell(tr, item.stockOut);
                cell(tr, money(item.stockValue));

                fragment.appendChild(tr);
            });

            tbody.replaceChildren(fragment);

        } catch (error) {
            inventoryData = [];
            messageRow(tbody, 10, error.message, true);
        }
    }

    // SALES PER ITEM

    async function loadItemSales() {
        const tbody = document.getElementById('itemRows');

        messageRow(tbody, 5, 'Loading report…', false);

        try {
            itemSalesData = await fetchReport('/api/reports/sales-by-item', {
                search: document.getElementById('itemSearch').value,
                from: document.getElementById('itemFrom').value,
                to: document.getElementById('itemTo').value
            });

            if (!itemSalesData.length) {
                messageRow(tbody, 5, 'No shipped sales found for these filters.', false);
                return;
            }

            const fragment = document.createDocumentFragment();

            itemSalesData.forEach(function (item) {
                const tr = document.createElement('tr');

                cell(tr, item.sku);
                cell(tr, item.itemName);
                cell(tr, item.quantitySold);
                cell(tr, item.shippedOrderCount);
                cell(tr, money(item.salesRevenue));

                fragment.appendChild(tr);
            });

            tbody.replaceChildren(fragment);

        } catch (error) {
            itemSalesData = [];
            messageRow(tbody, 5, error.message, true);
        }
    }

    // SALES PER CUSTOMER

    async function loadCustomerSales() {
        const tbody = document.getElementById('customerRows');

        messageRow(tbody, 5, 'Loading report…', false);

        try {
            customerSalesData = await fetchReport('/api/reports/sales-by-customer', {
                search: document.getElementById('customerSearch').value,
                from: document.getElementById('customerFrom').value,
                to: document.getElementById('customerTo').value
            });

            if (!customerSalesData.length) {
                messageRow(tbody, 5, 'No shipped sales found for these filters.', false);
                return;
            }

            const fragment = document.createDocumentFragment();

            customerSalesData.forEach(function (customer) {
                const tr = document.createElement('tr');

                cell(tr, customer.customerId);
                cell(tr, customer.customerName);
                cell(tr, customer.shippedOrderCount);
                cell(tr, customer.quantitySold);
                cell(tr, money(customer.salesRevenue));

                fragment.appendChild(tr);
            });

            tbody.replaceChildren(fragment);

        } catch (error) {
            customerSalesData = [];
            messageRow(tbody, 5, error.message, true);
        }
    }

    // CSV EXPORT

    function exportCsv(columns, rows, filename) {
        if (!rows.length) {
            alert('There are no report rows to export.');
            return;
        }

        function csvEscape(value) {
            const text = value == null ? '' : String(value);
            return '"' + text.replace(/"/g, '""') + '"';
        }

        const output = [];

        output.push(columns.map(function (column) {
            return csvEscape(column.label);
        }).join(','));

        rows.forEach(function (record) {
            output.push(columns.map(function (column) {
                return csvEscape(record[column.key]);
            }).join(','));
        });

        const blob = new Blob(
            ['\ufeff' + output.join('\r\n')],
            { type: 'text/csv;charset=utf-8;' }
        );

        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');

        link.href = url;
        link.download = filename;

        document.body.appendChild(link);
        link.click();
        link.remove();

        URL.revokeObjectURL(url);
    }

    document.getElementById('inventoryExport').addEventListener('click', function () {
        exportCsv([
            { label: 'SKU', key: 'sku' },
            { label: 'Item', key: 'name' },
            { label: 'On hand', key: 'quantity' },
            { label: 'Reserved', key: 'reserved' },
            { label: 'Available', key: 'available' },
            { label: 'Reorder level', key: 'reorderLevel' },
            { label: 'Status', key: 'status' },
            { label: 'Stock in (period)', key: 'stockIn' },
            { label: 'Stock out (period)', key: 'stockOut' },
            { label: 'Stock value', key: 'stockValue' }
        ], inventoryData, 'erpflow-inventory-report.csv');
    });

    document.getElementById('itemExport').addEventListener('click', function () {
        exportCsv([
            { label: 'SKU', key: 'sku' },
            { label: 'Item', key: 'itemName' },
            { label: 'Quantity sold', key: 'quantitySold' },
            { label: 'Shipped orders', key: 'shippedOrderCount' },
            { label: 'Sales revenue', key: 'salesRevenue' }
        ], itemSalesData, 'erpflow-sales-per-item.csv');
    });

    document.getElementById('customerExport').addEventListener('click', function () {
        exportCsv([
            { label: 'Customer ID', key: 'customerId' },
            { label: 'Customer', key: 'customerName' },
            { label: 'Shipped orders', key: 'shippedOrderCount' },
            { label: 'Quantity sold', key: 'quantitySold' },
            { label: 'Sales revenue', key: 'salesRevenue' }
        ], customerSalesData, 'erpflow-sales-per-customer.csv');
    });

    // REPORT NAVIGATION

    const panels = {
        inventory: document.getElementById('inventoryPanel'),
        itemSales: document.getElementById('itemSalesPanel'),
        customerSales: document.getElementById('customerSalesPanel')
    };

    function showReport(reportName) {
        Object.keys(panels).forEach(function (key) {
            panels[key].hidden = key !== reportName;
        });

        document.querySelectorAll('.report-tab').forEach(function (button) {
            button.classList.toggle(
                'active',
                button.dataset.report === reportName
            );
        });

        if (reportName === 'inventory') {
            loadInventory();
        } else if (reportName === 'itemSales') {
            loadItemSales();
        } else if (reportName === 'customerSales') {
            loadCustomerSales();
        }
    }

    document.querySelectorAll('.report-tab').forEach(function (button) {
        button.addEventListener('click', function () {
            showReport(button.dataset.report);
        });
    });

    document.getElementById('inventoryRun')
        .addEventListener('click', loadInventory);

    document.getElementById('itemRun')
        .addEventListener('click', loadItemSales);

    document.getElementById('customerRun')
        .addEventListener('click', loadCustomerSales);

    document.getElementById('inventoryStock')
        .addEventListener('change', loadInventory);

    // Load inventory when the report page opens.
    loadInventory();
</script>

</body>
</html>