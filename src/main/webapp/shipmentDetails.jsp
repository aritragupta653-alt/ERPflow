<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>
<head>
    <title>Shipment Details - ERPFlow</title>

    <link rel="stylesheet" href="/erpflow/css/app.css">
</head>

<body>

<header class="navbar">
    <a href="/erpflow/home" class="logo">
        <img src="/erpflow/images/erpflow-logo.png"
             alt="ERPFlow Logo">
        <span>ERPFlow</span>
    </a>
</header>

<div class="container">

    <a class="back" href="/erpflow/shipments.jsp">
        ← Back to Shipments
    </a>

    <h1>Shipment Details</h1>

    <div id="messageBox"></div>

    <div class="details-card">

        <div class="detail-row">
            <strong>Shipment ID</strong>
            <span id="shipmentId">-</span>
        </div>

        <div class="detail-row">
            <strong>Package ID</strong>
            <span id="packageId">-</span>
        </div>

        <div class="detail-row">
            <strong>Sales Order ID</strong>
            <span id="salesOrderId">-</span>
        </div>

        <div class="detail-row">
            <strong>Shipment Date</strong>
            <span id="shipmentDate">-</span>
        </div>

        <div class="detail-row">
            <strong>Status</strong>
            <span id="shipmentStatus" class="status">-</span>
        </div>

    </div>

</div>

<script src="/erpflow/js/shipmentDetails.js" defer></script>

</body>
</html>