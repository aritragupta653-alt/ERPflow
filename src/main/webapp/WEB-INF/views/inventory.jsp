<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>

<html>

<head>
    <title>ERPFlow - Inventory</title>
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

<h1>Inventory Management</h1>

<h2>Adjust Inventory</h2>

<form action="inventory" method="post">

    <label>Select Item:</label>

    <select name="itemId" required>

        <c:forEach var="item" items="${items}">

            <option value="${item.id}">
                ${item.name} (${item.sku})
            </option>

        </c:forEach>

    </select>

    <br><br>


    <label>Operation:</label>

    <select name="action" required>

        <option value="stockIn">
            Stock In
        </option>

        <option value="stockOut">
            Stock Out
        </option>

    </select>

    <br><br>


    <label>Quantity:</label>

    <input
            type="number"
            name="quantity"
            min="1"
            required
    >

    <br><br>


    <button type="submit">
        Adjust Inventory
    </button>
    

</form>
<a href="http://localhost:8080/erpflow/items" >BACK TO ITEMS</a>

<hr>
<h2>Current Inventory</h2>


<table border="1">

   


    <h2>Current Inventory</h2>

<table border="1">

    <tr>
        <th>Item ID</th>
        <th>Name</th>
        <th>SKU</th>
        <th>Current Stock</th>
    </tr>

    <c:forEach var="inventory" items="${inventories}">

        <tr>

            <td>${inventory.item.id}</td>

            <td>${inventory.item.name}</td>

            <td>${inventory.item.sku}</td>

            <td>${inventory.quantity}</td>

        </tr>

    </c:forEach>

</table>
</table>


</body>

</html>
