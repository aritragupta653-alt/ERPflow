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
        <img src="/erpflow/images/erpflow-logo.png" alt="ERPFlow Logo">
        <span>ERPFlow</span>
    </a>
</header>

<div class="container">

    <a class="back" href="/erpflow/home.jsp">
        ← Back to Dashboard
    </a>

    <h1>Shipments</h1>

    <div id="messageBox"></div>

    <table>
        <thead>
        <tr>
            <th>Shipment ID</th>
            <th>Package ID</th>
            <th>Sales Order</th>
            <th>Shipment Date</th>
            <th>Status</th>
            <th>Action</th>
        </tr>
        </thead>

        <tbody id="shipmentsTableBody"></tbody>
    </table>

</div>

<script>
    const contextPath = "/erpflow";
</script>

<script src="/erpflow/js/shipments.js" ></script>

</body>
</html>