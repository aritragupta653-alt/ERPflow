<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>

<html>

<head>
    <title>ERPFlow - Item Management</title>
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

<h1>Item Management</h1>

<div>
    
<a href="http://localhost:8080/erpflow/inventory">CHECK INVENTORY</a>
    
</div>


<!-- ADD ITEM FORM -->

<h2>Add Item</h2>

<form action="items" method="post">

    <label>Name:</label>
    <input type="text" name="name" required>

    <br><br>

    <label>SKU:</label>
    <input type="text" name="sku" required>

    <br><br>

    <label>Description:</label>
    <textarea name="description"></textarea>

    <br><br>

    <label>Purchase Price:</label>
    <input
        type="number"
        name="purchasePrice"
        step="0.01"
        required
    >

    <br><br>

    <label>Selling Price:</label>
    <input
        type="number"
        name="sellingPrice"
        step="0.01"
        required
    >

    <br><br>

    <label>Reorder Level:</label>
    <input
        type="number"
        name="reorderLevel"
        required
    >

    <br><br>

    <button type="submit">
        Add Item
    </button>

</form>


<hr>


<!-- ITEM LIST -->

<h2>All Items</h2>


<table border="1">

    <tr>
        <th>ID</th>
        <th>Name</th>
        <th>SKU</th>
        <th>Description</th>
        <th>Purchase Price</th>
        <th>Selling Price</th>
        <th>Reorder Level</th>
        <th>Action</th>
    </tr>


    <c:forEach var="item" items="${items}">

        <tr>

            <td>${item.id}</td>

            <td>${item.name}</td>

            <td>${item.sku}</td>

            <td>${item.description}</td>

            <td>${item.purchasePrice}</td>

            <td>${item.sellingPrice}</td>

            <td>${item.reorderLevel}</td>

            <td>
                <a href="items?action=edit&id=${item.id}">
        Edit
    </a>
    <form action="items" method="post">

        <input
            type="hidden"
            name="action"
            value="delete"
        >

        <input
            type="hidden"
            name="id"
            value="${item.id}"
        >

        <button type="submit">
            Mark as Inactive
        </button>

    </form>
</td>


        </tr>

    </c:forEach>

</table>


</body>

</html>