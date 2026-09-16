<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>

<head>

    <title>ERPFlow - Sales Orders</title>

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

    <h1>Sales Orders</h1>


    <!-- Create Sales Order -->

    <h2>Create Sales Order</h2>

    <form action="${pageContext.request.contextPath}/sales-orders"
          method="post">

        <label>Customer</label>

        <select name="customerId" required>

            <option value="">
                Select Customer
            </option>

            <c:forEach var="customer"
                       items="${customers}">

                <option value="${customer.id}">
                    ${customer.name}
                </option>

            </c:forEach>

        </select>


        <h3>Order Items</h3>

        <div id="itemContainer">

            <div class="sales-item-row">

                <select name="itemId" required>

                    <option value="">
                        Select Item
                    </option>

                    <c:forEach var="item"
                               items="${items}">

                        <option value="${item.id}">
                            ${item.name} - ${item.sku}
                        </option>

                    </c:forEach>

                </select>


                <input type="number"
                       name="quantity"
                       placeholder="Quantity"
                       min="1"
                       required>


                <input type="number"
                       name="sellingPrice"
                       placeholder="Selling Price"
                       min="0"
                       step="0.01"
                       required>

            </div>

        </div>


        <br>

        <button type="button"
                onclick="addItemRow()">

            + Add Another Item

        </button>


        <button type="submit">

            Create Sales Order

        </button>

    </form>


    <hr>


    <!-- Sales Order List -->

    <h2>Sales Order List</h2>

    <table>

        <tr>

            <th>ID</th>
            <th>Customer</th>
            <th>Order Date</th>
            <th>Status</th>
            <th>Action</th>

        </tr>


        <c:forEach var="salesOrder"
                   items="${salesOrders}">

            <tr>

                <td>
                    ${salesOrder.id}
                </td>

                <td>
                    ${salesOrder.customer.name}
                </td>

                <td>
                    ${salesOrder.orderDate}
                </td>

                <td>
                    ${salesOrder.status}
                </td>

                <td>

                    <a href="${pageContext.request.contextPath}/sales-orders?action=view&id=${salesOrder.id}">
                        View
                    </a>

                </td>

            </tr>

        </c:forEach>

    </table>


    <br>

    <a href="${pageContext.request.contextPath}/home">
        ← Back to Home
    </a>

</div>


<script>

function addItemRow() {

    const container =
        document.getElementById("itemContainer");

    const row =
        document.createElement("div");

    row.className =
        "sales-item-row";

    row.innerHTML = `
        <select name="itemId" required>

            <option value="">
                Select Item
            </option>

            <c:forEach var="item" items="${items}">
                <option value="${item.id}">
                    ${item.name} - ${item.sku}
                </option>
            </c:forEach>

        </select>

        <input type="number"
               name="quantity"
               placeholder="Quantity"
               min="1"
               required>

        <input type="number"
               name="sellingPrice"
               placeholder="Selling Price"
               min="0"
               step="0.01"
               required>
    `;

    container.appendChild(row);
}

</script>

</body>

</html>