<%@ page contentType="text/html;charset=UTF-8"
         isELIgnored="false" %>

<%@ taglib prefix="c"
           uri="jakarta.tags.core" %>

<!DOCTYPE html>

<html>

<head>

    <title>Shipments - ERPFlow</title>

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
       href="${pageContext.request.contextPath}/home">

        ← Back to Dashboard

    </a>

    <h1>Shipments</h1>


    <table>

        <thead>

        <tr>

            <th>Shipment ID</th>
            <th>Package ID</th>
            <th>Sales Order</th>
            <th>Customer</th>
            <th>Shipment Date</th>
            <th>Status</th>
            <th>Action</th>

        </tr>

        </thead>


        <tbody>

        <c:forEach
                var="shipment"
                items="${shipments}">

            <tr>

                <td>
                    #${shipment.id}
                </td>

                <td>
                    #${shipment.packageEntity.id}
                </td>

                <td>
                    #${shipment.packageEntity.salesOrder.id}
                </td>

                <td>
                    ${shipment.packageEntity.salesOrder.customer.name}
                </td>

                <td>
                    ${shipment.shipmentDate}
                </td>

                <td>

                    <span class="status">
                        ${shipment.status}
                    </span>

                </td>

                <td>

                    <a class="action-button"
                       href="${pageContext.request.contextPath}/shipments?action=view&id=${shipment.id}">

                        View

                    </a>

                </td>

            </tr>

        </c:forEach>


        <c:if test="${empty shipments}">

            <tr>

                <td colspan="7">
                    No shipments found.
                </td>

            </tr>

        </c:if>

        </tbody>

    </table>

</div>

</body>

</html>