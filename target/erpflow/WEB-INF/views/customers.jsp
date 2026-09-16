<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>

    <title>ERPFlow - Customers</title>

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

    <h1>Customers</h1>


    <!-- Add Customer -->

    <h2>Add Customer</h2>
<form action="${pageContext.request.contextPath}/customers"
      method="post"
      style="display: flex !important;
             flex-direction: column !important;
             align-items: flex-start !important;
             gap: 12px !important;
             width: 500px !important;
             max-width: 100% !important;">

    <input type="text"
           name="name"
           placeholder="Customer Name"
           required
           style="width: 100%; box-sizing: border-box;">

    <input type="email"
           name="email"
           placeholder="Email"
           required
           style="width: 100%; box-sizing: border-box;">

    <input type="text"
           name="phone"
           placeholder="Phone"
           required
           style="width: 100%; box-sizing: border-box;">

    <input type="text"
           name="address"
           placeholder="Address"
           required
           style="width: 100%; box-sizing: border-box;">

    <button type="submit">
        Add Customer
    </button>

</form>


    <hr>


    <!-- Customer List -->

    <h2>Customer List</h2>

    <table>

        <tr>

            <th>ID</th>
            <th>Name</th>
            <th>Email</th>
            <th>Phone</th>
            <th>Address</th>
            <th>Actions</th>

        </tr>


        <c:forEach var="customer"
                   items="${customers}">

            <tr>

                <td>${customer.id}</td>

                <td>${customer.name}</td>

                <td>${customer.email}</td>

                <td>${customer.phone}</td>

                <td>${customer.address}</td>

                <td>

                    <!-- Edit -->

                    <a href="${pageContext.request.contextPath}/customers?action=edit&id=${customer.id}">
                        Edit
                    </a>


                    <!-- Delete -->

                    <form action="${pageContext.request.contextPath}/customers"
                          method="post"
                          style="display:inline;">

                        <input type="hidden"
                               name="action"
                               value="delete">

                        <input type="hidden"
                               name="id"
                               value="${customer.id}">

                        <button type="submit">
                            Delete
                        </button>

                    </form>

                </td>

            </tr>

        </c:forEach>

    </table>


    <br>

    <a href="${pageContext.request.contextPath}/home">
        ← Back to Home
    </a>

</div>

</body>
</html>