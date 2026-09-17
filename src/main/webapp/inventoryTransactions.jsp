<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <title>Transaction History - ERPFlow</title>
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

    <a class="back" href="/erpflow/home.jsp">
        ← Back to Dashboard
    </a>

    <div class="page-header">
        <div>
            <h1>Transaction History</h1>
            <p>View inventory stock movements and transactions.</p>
        </div>
    </div>

    <div id="messageBox"></div>

    <div class="table-section">

        <div class="section-header">
            <div>
                <h2>Inventory Transactions</h2>
                <p>
                    <span id="transactionCount">0</span>
                    transactions
                </p>
            </div>

            <div class="search-box">
                <input type="text"
                       id="searchInput"
                       placeholder="Search transactions...">
            </div>
        </div>

        <div class="table-container">

            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>Item</th>
                    <th>Type</th>
                    <th>Quantity</th>
                    <th>SKU</th>
                    <th>Date</th>
                </tr>
                </thead>

                <tbody id="transactionsTableBody">
                </tbody>

            </table>

        </div>
    </div>

</div>

<script src="/erpflow/js/inventoryTransactions.js" defer></script>

</body>
</html>