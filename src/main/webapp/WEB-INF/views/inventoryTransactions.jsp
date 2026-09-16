<%@ page contentType="text/html;charset=UTF-8"
         isELIgnored="false" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>

<html>

<head>

    <title>ERPFlow - Inventory Transactions</title>

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

<h1>Inventory Transaction History</h1>


<table>

    <tr>

        <th>ID</th>

        <th>Item</th>

        <th>SKU</th>

        <th>Transaction Type</th>

        <th>Quantity</th>

        <th>Date & Time</th>

    </tr>


    <c:forEach
            var="transaction"
            items="${transactions}"
    >

        <tr>

            <td>
                ${transaction.id}
            </td>


            <td>
                ${transaction.item.name}
            </td>


            <td>
                ${transaction.item.sku}
            </td>


            <td>
                ${transaction.type}
            </td>


            <td>
                ${transaction.quantity}
            </td>


            <td>
                ${transaction.transactionDate}
            </td>

        </tr>

    </c:forEach>

</table>


<br>


<a href="${pageContext.request.contextPath}/inventory">

    ← Back to Inventory

</a>


</body>

</html>