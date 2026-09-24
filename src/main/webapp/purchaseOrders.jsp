
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Purchase Orders - ERPFlow</title>

    <link rel="stylesheet" href="/erpflow/css/app.css">

    <style>
        /* Page layout and spacing */
        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 24px;
        }

        .page-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 20px;
            margin-bottom: 28px;
        }

        .page-header h1 {
            margin: 0 0 6px;
        }

        .page-header p {
            margin: 0;
        }

        /* Sections */
        .form-section,
        .table-section {
            margin-bottom: 28px;
        }

        .form-section h2,
        .table-section h2 {
            margin-top: 0;
            margin-bottom: 20px;
        }

        /* Form alignment */
        #purchaseOrderForm {
            display: flex;
            flex-direction: column;
            gap: 18px;
        }

        #purchaseOrderForm .form-group {
            margin: 0;
        }

        #purchaseOrderForm h3 {
            margin: 8px 0 0;
        }

        #itemContainer {
            display: flex;
            flex-direction: column;
            gap: 14px;
            width: 100%;
        }

        #purchaseOrderForm button {
            align-self: flex-start;
        }

        /* Remove excessive spacing caused by old line breaks */
        #purchaseOrderForm br {
            display: none;
        }

        /* Listing header */
        .section-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 20px;
            margin-bottom: 16px;
        }

        .section-header h2 {
            margin: 0;
        }

        .search-box {
            display: flex;
            align-items: center;
            width: min(100%, 360px);
        }

        .search-box input {
            width: 100%;
            box-sizing: border-box;
        }

        /* Count spacing */
        .table-section > p {
            margin: 0 0 16px;
        }

        /* Table layout */
        .table-responsive {
            width: 100%;
            overflow-x: auto;
        }

        .table-responsive table {
            width: 100%;
            border-collapse: collapse;
        }

        .table-responsive th,
        .table-responsive td {
            text-align: left;
            vertical-align: middle;
            padding: 12px 16px;
        }

        .table-responsive th:first-child,
        .table-responsive td:first-child {
            padding-left: 20px;
        }

        .table-responsive th:last-child,
        .table-responsive td:last-child {
            padding-right: 20px;
        }

        /* Message area */
        #messageBox {
            margin-bottom: 20px;
        }

        /* Responsive layout */
        @media (max-width: 700px) {
            .container {
                padding: 16px;
            }

            .page-header,
            .section-header {
                flex-direction: column;
                align-items: stretch;
            }

            .page-header button {
                align-self: flex-start;
            }

            .search-box {
                width: 100%;
            }

            .table-responsive th,
            .table-responsive td {
                padding: 10px;
                white-space: nowrap;
            }
        }
    </style>
</head>

<body>

<header>
    <h1>ERPFlow</h1>

    <nav>
        <a href="/erpflow/dashboard.jsp">Dashboard</a>
        <a href="/erpflow/items.jsp">Items</a>
        <a href="/erpflow/suppliers.jsp">Suppliers</a>
        <a href="/erpflow/purchaseOrders.jsp">Purchase Orders</a>
    </nav>
</header>

<main class="container">

    <div class="page-header">
        <div>
            <h1>Purchase Orders</h1>
            <p>Manage supplier orders and track their status.</p>
        </div>
    </div>

    <div id="messageBox" style="display:none;"></div>

    <!-- Create Purchase Order -->
    <section class="form-section">

        <h2>Create Purchase Order</h2>

        <form id="purchaseOrderForm">

            <div class="form-group">
                <label for="supplierSelect">Supplier</label>

                <select id="supplierSelect" required>
                    <option value="">Select Supplier</option>
                </select>
            </div>

            <h3>Purchase Order Items</h3>

            <div id="itemContainer"></div>

            <button type="button" id="addItemButton" class="btn">
                Add Item
            </button>

            <button type="submit" class="btn">
                Create Purchase Order
            </button>

        </form>

    </section>

    <!-- Purchase Order Listing -->
    <section class="table-section">

        <div class="section-header">
            <h2>All Purchase Orders</h2>

            <div class="search-box">
                <input
                    type="text"
                    id="searchInput"
                    placeholder="Search by order ID, supplier or status">
            </div>
        </div>

        <p>
            Total Purchase Orders:
            <strong id="purchaseOrderCount">0</strong>
        </p>

        <div class="table-responsive">
            <table>
                <thead>
                    <tr>
                        <th>Order ID</th>
                        <th>Supplier</th>
                        <th>Order Date</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>

                <tbody id="purchaseOrdersTableBody">
                    <tr>
                        <td colspan="5" style="text-align:center;">
                            Loading purchase orders...
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>

    </section>

</main>

<script src="/erpflow/js/purchaseOrders.js" defer></script>

</body>
</html>