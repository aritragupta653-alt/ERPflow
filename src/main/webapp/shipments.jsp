<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>

<head>
    <title>Shipments - ERPFlow</title>
    <link rel="stylesheet" href="/erpflow/css/app.css">
</head>

<body>

<header class="navbar">

    <a href="/erpflow/home.jsp" class="logo">

        <img
            src="/erpflow/images/erpflow-logo.png"
            alt="ERPFlow Logo"
        >

        <span>ERPFlow</span>

    </a>

</header>


<div class="container">

    <a class="back" href="/erpflow/home.jsp">
        ← Back to Dashboard
    </a>

    <h1>Shipments</h1>

    <div id="messageBox"></div>

<div class="section-header">
    <h2>All Shipments</h2>

    <div class="search-box">
        <select id="statusFilter">
            <option value="ALL">All Statuses</option>
            <option value="CREATED">Created</option>
            <option value="IN_TRANSIT">In Transit</option>
            <option value="SHIPPED">Shipped</option>
            <option value="DELIVERED">Delivered</option>
        </select>
    </div>
</div>


    <table>

        <thead>

        <tr>
            <th>Shipment</th>
            <th>Packages</th>
            <th>Sales Order</th>
            <th>Carrier</th>
            <th>Service</th>
            <th>Shipping Charge</th>
            <th>Shipment Date</th>
            <th>Status</th>
            <th>Action</th>
        </tr>

        </thead>


        <tbody id="shipmentsTableBody"></tbody>

    </table>

</div>


<script src="/erpflow/js/shipments.js?v=3" defer></script>

</body>

</html>