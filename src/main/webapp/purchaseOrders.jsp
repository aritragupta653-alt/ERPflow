
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Purchase Orders | ERPFlow</title>
    <link rel="stylesheet" href="/erpflow/css/app.css">
</head>
<body>


<main class="container">
    <div class="page-header">
        <div>
            <h1>Purchase Orders</h1>
            <p>Create, view, edit, and manage supplier purchase orders.</p>
        </div>

        <a href="erpflow/dashboard.jsp" class="btn btn-secondary">
            Back to Dashboard
        </a>
    </div>

    <div id="messageBox" class="message-box" style="display: none;"></div>

    <!-- Create Purchase Order -->
    <section class="form-section">
        <h2 id="purchaseOrderFormTitle">Create Purchase Order</h2>

        <form id="purchaseOrderForm">
            <input type="hidden" id="purchaseOrderId" name="purchaseOrderId">

            <div class="form-grid">
                <div class="form-group">
                    <label for="supplierSelect">Supplier</label>
                    <select id="supplierSelect" name="supplierId" required>
                        <option value="">Loading suppliers...</option>
                    </select>
                </div>
            </div>

            <div class="section-header">
                <h3>Order Items</h3>
                <button type="button" id="addItemButton" class="btn btn-secondary">
                    Add Item
                </button>
            </div>

            <div id="itemContainer">
                <!-- Item rows are added by purchaseOrders.js -->
            </div>

            <div class="form-actions">
                <button type="submit" id="submitPurchaseOrderButton" class="btn btn-primary">
                    Create Purchase Order
                </button>

                <button type="button" id="cancelEditButton" class="btn btn-secondary"
                        style="display: none;">
                    Cancel Edit
                </button>
            </div>
        </form>
    </section>

    <!-- Purchase Order List -->
    <section class="table-section">
        <div class="section-header">
            <div>
                <h2>Purchase Orders</h2>
                <p>Total orders: <span id="purchaseOrderCount">0</span></p>
            </div>

            <div class="form-group">
                <label for="searchInput">Search Orders</label>
                <input type="text" id="searchInput"
                       placeholder="Search by order ID, supplier, or status">
            </div>
        </div>

        <div class="table-responsive">
            <table class="data-table">
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
                    <td colspan="5">Loading purchase orders...</td>
                </tr>
                </tbody>
            </table>
        </div>
    </section>
</main>

<script src="/erpflow/js/purchaseOrders.js"></script>

</body>
</html>