<%@ page contentType="text/html;charset=UTF-8"
         isELIgnored="false" %>

<!DOCTYPE html>

<html>

<head>

    <title>ERPFlow - Edit Supplier</title>

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

<h1>Edit Supplier</h1>


<form
        action="${pageContext.request.contextPath}/suppliers"
        method="post"
>

    <!-- ACTION -->

    <input
            type="hidden"
            name="action"
            value="update"
    >


    <!-- SUPPLIER ID -->

    <input
            type="hidden"
            name="id"
            value="${supplier.id}"
    >


    <!-- NAME -->

    <label>Name:</label>

    <input
            type="text"
            name="name"
            value="${supplier.name}"
            required
    >

    <br><br>


    <!-- CONTACT PERSON -->

    <label>Contact Person:</label>

    <input
            type="text"
            name="contactPerson"
            value="${supplier.contactPerson}"
    >

    <br><br>


    <!-- PHONE -->

    <label>Phone:</label>

    <input
            type="text"
            name="phone"
            value="${supplier.phone}"
    >

    <br><br>


    <!-- EMAIL -->

    <label>Email:</label>

    <input
            type="email"
            name="email"
            value="${supplier.email}"
    >

    <br><br>


    <!-- ADDRESS -->

    <label>Address:</label>

    <textarea
            name="address"
    >${supplier.address}</textarea>

    <br><br>


    <button type="submit">

        Update Supplier

    </button>


    <a
            href="${pageContext.request.contextPath}/suppliers"
    >

        Cancel

    </a>

</form>


</body>

</html>