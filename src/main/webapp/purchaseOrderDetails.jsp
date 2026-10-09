<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Purchase Order Details - ERPFlow</title>
    <link rel="stylesheet" href="/erpflow/css/app.css">
</head>
<body>

<header>
    <h1>ERPFlow</h1>
    <nav>
        <a href="/erpflow/dashboard.jsp">Dashboard</a>
        <a href="/erpflow/purchaseOrders.jsp">Purchase Orders</a>
    </nav>
</header>

<main class="container">

    <div id="messageBox" style="display:none;"></div>

    <section class="details-section">
        <h2>Purchase Order Details</h2>

        <div class="details-grid">

            <div class="form-group">
                <label for="orderId">Purchase Order ID</label>
                <input type="text" id="orderId" readonly>
            </div>

            <div class="form-group">
                <label for="orderDate">Order Date</label>
                <input type="text" id="orderDate" readonly>
            </div>

            <div class="form-group">
                <label for="orderStatus">Status</label>
                <input type="text" id="orderStatus" readonly>
            </div>

        </div>
    </section>

    <!-- Supplier Details -->
    <section class="details-section">
        <h2>Supplier Details</h2>

        <div class="details-grid">

            <div class="form-group">
                <label for="supplierId">Supplier ID</label>
                <input type="text" id="supplierId" readonly>
            </div>

            <div class="form-group">
                <label for="supplierName">Supplier Name</label>
                <input type="text" id="supplierName" readonly>
            </div>

            <div class="form-group">
                <label for="contactPerson">Contact Person</label>
                <input type="text" id="contactPerson" readonly>
            </div>

            <div class="form-group">
                <label for="supplierPhone">Phone</label>
                <input type="text" id="supplierPhone" readonly>
            </div>

            <div class="form-group">
                <label for="supplierEmail">Email</label>
                <input type="text" id="supplierEmail" readonly>
            </div>

            <div class="form-group">
                <label for="supplierAddress">Address</label>
                <textarea id="supplierAddress" readonly></textarea>
            </div>

        </div>
    </section>

    <!-- Ordered Items -->
    <section class="table-section">
        <h2>Order Items</h2>

        <div class="table-responsive">
            <table>
                <thead>
                    <tr>
                        <th>Item ID</th>
                        <th>Item Name</th>
                        <th>SKU</th>
                        <th>Type</th>
                        <th>Ordered Quantity</th>
                        <th>Received Quantity</th>
                        <th>Remaining Quantity</th>
                        <th>Purchase Price</th>
                        <th>Total</th>
                    </tr>
                </thead>

                <tbody id="orderItemsTableBody">
                </tbody>
            </table>
        </div>
    </section>

    <!-- Receive Items -->
    <section id="receivingSection" class="form-section">
        <h2>Receive Items</h2>

        <p>
            Enter the quantities being received for each outstanding item.
        </p>

        <form id="receiveForm">

            <div class="table-responsive">
                <table>
                    <thead>
                        <tr>
                            <th>Item</th>
                            <th>Remaining Quantity</th>
                            <th>Quantity to Receive</th>
                        </tr>
                    </thead>

                    <tbody id="receiveItemsTableBody">
                    </tbody>
                </table>
            </div>

            <br>

            <button type="submit" id="receiveButton" class="btn">
                Receive Selected Quantities
            </button>

        </form>
    </section>

    <br>

    <a href="/erpflow/purchaseOrders.jsp" class="btn">
        Back to Purchase Orders
    </a>

</main>

<script src="/erpflow/js/purchaseOrderDetails.js"></script>

</body>
</html>
