<%@ page contentType="text/html;charset=UTF-8"
         isELIgnored="false" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>

<html>

<head>

    <title>ERPFlow - Supplier Management</title>

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

<h1 >Supplier Management</h1>


<!-- ADD SUPPLIER -->

<h2>Add Supplier</h2>

<form action="suppliers" method="post">

    <label>Name:</label>

    <input
            type="text"
            name="name"
            required
    >

    <br><br>


    <label>Contact Person:</label>

    <input
            type="text"
            name="contactPerson"
    >

    <br><br>


    <label>Phone:</label>

    <input
            type="text"
            name="phone"
    >

    <br><br>


    <label>Email:</label>

    <input
            type="email"
            name="email"
    >

    <br><br>


    <label>Address:</label>

    <textarea
            name="address"
    ></textarea>

    <br><br>


    <button type="submit">

        Add Supplier

    </button>

</form>


<hr>


<!-- SUPPLIER LIST -->

<h2>All Suppliers</h2>


<table>

    <tr>

        <th>ID</th>

        <th>Name</th>

        <th>Contact Person</th>

        <th>Phone</th>

        <th>Email</th>

        <th>Address</th>

        <th>Actions</th>

    </tr>


    <c:forEach
            var="supplier"
            items="${suppliers}"
    >

        <tr>

            <td>
                ${supplier.id}
            </td>

            <td>
                ${supplier.name}
            </td>

            <td>
                ${supplier.contactPerson}
            </td>

            <td>
                ${supplier.phone}
            </td>

            <td>
                ${supplier.email}
            </td>

            <td>
                ${supplier.address}
            </td>
            <td>

    <!-- EDIT -->

    <a href="${pageContext.request.contextPath}/suppliers?action=edit&id=${supplier.id}">

        Edit

    </a>


    <!-- DELETE -->

    <form
            action="${pageContext.request.contextPath}/suppliers"
            method="post"
            style="display:inline;"
    >

        <input
                type="hidden"
                name="action"
                value="delete"
        >

        <input
                type="hidden"
                name="id"
                value="${supplier.id}"
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