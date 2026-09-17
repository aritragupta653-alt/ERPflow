<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <title>Sales Order Details - ERPFlow</title>

    <link rel="stylesheet" href="/erpflow/css/app.css">
</head>

<body>

<header class="navbar">
    <a href="/erpflow/home.jsp" class="logo">
        <img src="/erpflow/images/erpflow-logo.png" alt="ERPFlow Logo">
        <span>ERPFlow</span>
    </a>
</header>

<div class="container">

    <a class="back" href="/erpflow/salesOrders.jsp">
        ← Back to Sales Orders
    </a>

    <div class="page-header">
        <div>
            <h1>Sales Order Details</h1>
            <p>View complete sales order information.</p>
        </div>
    </div>

    <div id="messageBox"></div>

    <div class="form-section">

        <h2>Order Information</h2>

        <div class="form-grid">

            <div class="form-group">
                <label>Sales Order ID</label>
                <input type="text" id="orderId" readonly>
            </div>

            <div class="form-group">
                <label>Order Date</label>
                <input type="text" id="orderDate" readonly>
            </div>

            <div class="form-group">
                <label>Status</label>
                <input type="text" id="orderStatus" readonly>
            </div>

        </div>

    </div>

    <div class="form-section">

        <h2>Customer</h2>

        <div class="form-grid">

            <div class="form-group">
                <label>Customer ID</label>
                <input type="text" id="customerId" readonly>
            </div>

            <div class="form-group">
                <label>Customer Name</label>
                <input type="text" id="customerName" readonly>
            </div>

            <div class="form-group">
                <label>Email</label>
                <input type="text" id="customerEmail" readonly>
            </div>

            <div class="form-group">
                <label>Phone</label>
                <input type="text" id="customerPhone" readonly>
            </div>

        </div>

    </div>

    <div class="table-section">

        <div class="section-header">
            <div>
                <h2>Order Items</h2>
            </div>
        </div>

        <div class="table-container">

            <table>

                <thead>
                    <tr>
                        <th>Item ID</th>
                        <th>Item Name</th>
                        <th>SKU</th>
                        <th>Quantity</th>
                        <th>Selling Price</th>
                        <th>Total</th>
                    </tr>
                </thead>

                <tbody id="orderItemsTableBody"></tbody>

            </table>

        </div>

    </div>

</div>

<script src="/erpflow/js/salesOrderDetails.js" defer></script>

</body>
</html>