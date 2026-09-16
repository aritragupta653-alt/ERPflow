<%@ page contentType="text/html;charset=UTF-8"
         isELIgnored="false" %>

<!DOCTYPE html>

<html>

<head>

    <title>Shipment Details - ERPFlow</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/app.css">

</head>

<body>

<header class="navbar">

    <a href="${pageContext.request.contextPath}/home"
       class="logo">

        <img src="${pageContext.request.contextPath}/images/erpflow-logo.png"
             alt="ERPFlow Logo">

        <span>ERPFlow</span>

    </a>

</header>


<div class="container">

    <a class="back"
       href="${pageContext.request.contextPath}/shipments">

        ← Back to Shipments

    </a>


    <div class="card">

        <h1>
            Shipment #${shipment.id}
        </h1>


        <div class="info">

            <strong>Shipment ID:</strong>
            #${shipment.id}

            <br>

            <strong>Package:</strong>
            #${shipment.packageEntity.id}

            <br>

            <strong>Sales Order:</strong>
            #${shipment.packageEntity.salesOrder.id}

            <br>

            <strong>Customer:</strong>
            ${shipment.packageEntity.salesOrder.customer.name}

            <br>

            <strong>Shipment Date:</strong>
            ${shipment.shipmentDate}

            <br>

            <strong>Status:</strong>

            <span class="status">
                ${shipment.status}
            </span>

        </div>

    </div>


    <a class="action-button"
       href="${pageContext.request.contextPath}/packages?action=view&id=${shipment.packageEntity.id}">

        View Package

    </a>


    <a class="action-button"
       href="${pageContext.request.contextPath}/sales-orders?action=view&id=${shipment.packageEntity.salesOrder.id}">

        View Sales Order

    </a>

</div>

</body>

</html>