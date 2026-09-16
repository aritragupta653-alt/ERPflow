<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>

<head>

    <title>ERPFlow - Sales Order Details</title>

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

    <h1>Sales Order Details</h1>

    <h2>
        Sales Order #${salesOrder.id}
    </h2>


    <div>

        <p>
            <strong>Customer:</strong>
            ${salesOrder.customer.name}
        </p>

        <p>
            <strong>Email:</strong>
            ${salesOrder.customer.email}
        </p>

        <p>
            <strong>Phone:</strong>
            ${salesOrder.customer.phone}
        </p>

        <p>
            <strong>Order Date:</strong>
            ${salesOrder.orderDate}
        </p>

        <p>
            <strong>Status:</strong>
            ${salesOrder.status}
        </p>

    </div>


    <hr>


    <h2>Order Items</h2>


    <table>

        <tr>

            <th>Item</th>
            <th>SKU</th>
            <th>Quantity</th>
            <th>Selling Price</th>
            <th>Total</th>

        </tr>


        <c:forEach var="orderItem"
                   items="${salesOrderItems}">

            <tr>

                <td>
                    ${orderItem.item.name}
                </td>

                <td>
                    ${orderItem.item.sku}
                </td>

                <td>
                    ${orderItem.quantity}
                </td>

                <td>
                    ${orderItem.sellingPrice}
                </td>

                <td>
                    ${orderItem.quantity * orderItem.sellingPrice}
                </td>

            </tr>

        </c:forEach>

    </table>


    <br>


    <!-- Complete Sales Order -->

   <c:if test="${salesOrder.status == 'CREATED'}">

    <a href="${pageContext.request.contextPath}/packages?action=create&salesOrderId=${salesOrder.id}"
       style="
           display: inline-block;
           padding: 10px 18px;
           background: #2563eb;
           color: white;
           text-decoration: none;
           border-radius: 6px;
           margin-top: 20px;
       ">
        Create Package
    </a>

</c:if>


<c:if test="${salesOrder.status == 'COMPLETED'}">

    <p>
        <strong>✓ Order Completed</strong>
    </p>

</c:if>

    <br>

    <a href="${pageContext.request.contextPath}/sales-orders">
        ← Back to Sales Orders
    </a>

</div>

</body>

</html>