<%@ page contentType="text/html;charset=UTF-8"
         isELIgnored="false" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>

<html>

<head>

    <title>ERPFlow - Purchase Order Details</title>

    <link
            rel="stylesheet"
            href="${pageContext.request.contextPath}/css/app.css"
    >

</head>


<body>
    <header class="navbar">

    <a
            href="${pageContext.request.contextPath}/home"
            class="logo"
    >

        <img
                src="${pageContext.request.contextPath}/images/erpflow-logo.png"
                alt="ERPFlow Logo"
        >

        <span>ERPFlow</span>

    </a>

</header>


<h1>Purchase Order Details</h1>


<h2>

    Purchase Order #${purchaseOrder.id}

</h2>


<div>

    <p>

        <strong>Supplier:</strong>

        ${purchaseOrder.supplier.name}

    </p>


    <p>

        <strong>Status:</strong>

        ${purchaseOrder.status}

    </p>


    <p>

        <strong>Order Date:</strong>

        ${purchaseOrder.orderDate}

    </p>

</div>


<hr>


<h2>Order Items</h2>


<table>

    <tr>

        <th>Item</th>

        <th>SKU</th>

        <th>Quantity</th>

        <th>Purchase Price</th>

    </tr>


    <c:forEach
            var="orderItem"
            items="${purchaseOrderItems}"
    >

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

                ${orderItem.purchasePrice}

            </td>

        </tr>

    </c:forEach>


</table>
<c:if test="${purchaseOrder.status == 'CREATED'}">

    <form
            action="${pageContext.request.contextPath}/purchase-orders"
            method="post"
    >

        <input
                type="hidden"
                name="action"
                value="receive"
        >

        <input
                type="hidden"
                name="purchaseOrderId"
                value="${purchaseOrder.id}"
        >

        <button type="submit">

            Receive Order

        </button>

    </form>

</c:if>


<br>


<a
        href="${pageContext.request.contextPath}/purchase-orders"
>

    ← Back to Purchase Orders

</a>


</body>

</html>