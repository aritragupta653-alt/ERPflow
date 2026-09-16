<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>

<!DOCTYPE html>
<html>

<head>

    <title>ERPFlow - Edit Customer</title>

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

    <h1>Edit Customer</h1>


    <form action="${pageContext.request.contextPath}/customers"
          method="post">

        <!-- Tell servlet this is an update -->

        <input type="hidden"
               name="action"
               value="update">


        <!-- Customer ID -->

        <input type="hidden"
               name="id"
               value="${customer.id}">


        <label>Name</label>

        <input type="text"
               name="name"
               value="${customer.name}"
               required>


        <label>Email</label>

        <input type="email"
               name="email"
               value="${customer.email}"
               required>


        <label>Phone</label>

        <input type="text"
               name="phone"
               value="${customer.phone}"
               required>


        <label>Address</label>

        <input type="text"
               name="address"
               value="${customer.address}"
               required>


        <button type="submit">
            Update Customer
        </button>

    </form>


    <br>

    <a href="${pageContext.request.contextPath}/customers">
        ← Back to Customers
    </a>

</div>

</body>

</html>