<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<!DOCTYPE html>

<html>

<head>
    <title>ERPFlow - Edit Item</title>
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
<h1>Edit Item</h1>

<form action="items" method="post">

    <!-- Tell Servlet this is an UPDATE -->
    <input
            type="hidden"
            name="action"
            value="update"
    >

    <!-- Item ID -->
    <input
            type="hidden"
            name="id"
            value="${item.id}"
    >


    <label>Name:</label>

    <input
            type="text"
            name="name"
            value="${item.name}"
            required
    >

    <br><br>


    <label>SKU:</label>

    <input
            type="text"
            name="sku"
            value="${item.sku}"
            required
    >

    <br><br>


    <label>Description:</label>

    <textarea name="description">${item.description}</textarea>

    <br><br>


    <label>Purchase Price:</label>

    <input
            type="number"
            name="purchasePrice"
            value="${item.purchasePrice}"
            step="0.01"
            required
    >

    <br><br>


    <label>Selling Price:</label>

    <input
            type="number"
            name="sellingPrice"
            value="${item.sellingPrice}"
            step="0.01"
            required
    >

    <br><br>


    <label>Reorder Level:</label>

    <input
            type="number"
            name="reorderLevel"
            value="${item.reorderLevel}"
            required
    >

    <br><br>


    <button type="submit">
        Update Item
    </button>

</form>


<br>

<a href="items">
    Back to Items
</a>

</body>

</html>