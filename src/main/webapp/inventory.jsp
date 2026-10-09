<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <title>ERPFlow - Inventory</title>

    <link rel="stylesheet" href="/erpflow/css/app.css">
</head>

<body>

<header class="navbar">
    <a href="/erpflow/home.jsp" class="logo">
        <img src="/erpflow/images/erpflow-logo.png"
             alt="ERPFlow Logo">
        <span>ERPFlow</span>
    </a>
</header>

<div class="container">

    <div class="page-header">
        <div>
            <h1>Inventory Management</h1>
            <p>Manage stock levels and inventory.</p>
        </div>

        <a href="/erpflow/home.jsp"
           class="btn secondary-btn">
            ← Dashboard
        </a>
    </div>

    <div id="messageBox"
         class="message-box"
         style="display:none;">
    </div>


    <!-- STOCK ADJUSTMENT -->

    <div class="form-section">

        <h2>Adjust Inventory</h2>
        <p>Stock in or stock out an item.</p>

        <form id="inventoryForm">

            <div class="form-grid">

                <div class="form-group">
                    <label>Item</label>

                    <select id="itemSelect" required>
                        <option value="">Loading items...</option>
                    </select>
                </div>

                <div class="form-group">
                    <label>Operation</label>

                    <select id="operationSelect" required>
                        <option value="stock-in">Stock In</option>
                        <option value="stock-out">Stock Out</option>
                    </select>
                </div>

                <div class="form-group">
                    <label>Quantity</label>

                    <input type="number"
                           id="quantityInput"
                           min="1"
                           required
                           placeholder="Quantity">
                </div>

            </div>

            <div class="form-actions">

                <button type="submit"
                        class="btn primary-btn">
                    Adjust Inventory
                </button>

            </div>

        </form>

    </div>


    <!-- INVENTORY TABLE -->

    <div class="table-section">

        <div class="section-header">

            <div>
                <h2>Current Inventory</h2>
                <p>
                    <span id="inventoryCount">0</span>
                    items
                </p>
            </div>

            <div class="search-box">
                <input type="text"
                       id="searchInput"
                       placeholder="Search item or SKU...">
            </div>

        </div>

        <div class="table-container">

            <table>

                <thead>
                <tr>
                    <th>ID</th>
                    <th>Item</th>
                    <th>SKU</th>
                    <th>Current Stock</th>
                    <th>Committed</th>
                    <th>Available</th>
                </tr>
                </thead>

                <tbody id="inventoryTableBody">
                </tbody>

            </table>

        </div>

    </div>

</div>


<script>
    const contextPath = "/erpflow";
</script>

<script src="/erpflow/js/inventory.js" defer></script>

</body>
</html>