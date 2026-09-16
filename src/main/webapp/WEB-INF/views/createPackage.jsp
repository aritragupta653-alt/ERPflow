<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>

    <title>Create Package - ERPFlow</title>

    <style>

        body {
            font-family: Arial, sans-serif;
            margin: 0;
            background: #f5f6f8;
        }

        .navbar {
            height: 70px;
            background: #1f2937;
            display: flex;
            align-items: center;
            padding: 0 40px;
        }

        .logo {
            color: white;
            font-size: 24px;
            font-weight: bold;
            text-decoration: none;
        }

        .container {
            padding: 40px;
            max-width: 900px;
            margin: auto;
        }

        .card {
            background: white;
            padding: 30px;
            border-radius: 8px;
        }

        h1 {
            margin-top: 0;
        }

        .order-info {
            margin-bottom: 25px;
            padding: 15px;
            background: #f8fafc;
            border-radius: 6px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }

        th,
        td {
            padding: 12px;
            border-bottom: 1px solid #ddd;
            text-align: left;
        }

        th {
            background: #f1f5f9;
        }

        input[type="number"] {
            width: 100px;
            padding: 8px;
            box-sizing: border-box;
        }

        .weight {
            margin-top: 25px;
        }

        .weight input {
            padding: 9px;
            width: 150px;
        }

        button {
            margin-top: 25px;
            padding: 11px 20px;
            border: none;
            background: #2563eb;
            color: white;
            border-radius: 6px;
            cursor: pointer;
        }

        button:hover {
            background: #1d4ed8;
        }

        .back {
            display: inline-block;
            margin-bottom: 20px;
            text-decoration: none;
            color: #2563eb;
        }

    </style>

</head>

<body>

<header class="navbar">

    <a href="${pageContext.request.contextPath}/home"
       class="logo">
        ERPFlow
    </a>

</header>


<div class="container">

    <a class="back"
       href="${pageContext.request.contextPath}/sales-orders?action=view&id=${salesOrder.id}">
        ← Back to Sales Order
    </a>


    <div class="card">

        <h1>Create Package</h1>


        <!-- SALES ORDER INFORMATION -->

        <div class="order-info">

            <strong>Sales Order:</strong>
            #${salesOrder.id}

            <br>

            <strong>Customer:</strong>
            ${salesOrder.customer.name}

            <br>

            <strong>Status:</strong>
            ${salesOrder.status}

        </div>


        <!-- PACKAGE FORM -->

        <form action="${pageContext.request.contextPath}/packages"
              method="post">

            <input type="hidden"
                   name="action"
                   value="create">


            <input type="hidden"
                   name="salesOrderId"
                   value="${salesOrder.id}">


            <h3>Select Items</h3>


            <table>

                <thead>

                <tr>

                    <th>Item</th>
                    <th>SKU</th>
                    <th>Ordered Quantity</th>
                    <th>Package Quantity</th>

                </tr>

                </thead>


                <tbody>

                <c:forEach
                        var="orderItem"
                        items="${salesOrderItems}">

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

                            <input type="hidden"
                                   name="itemId"
                                   value="${orderItem.item.id}">


                            <input type="number"
                                   name="quantity"
                                   value="0"
                                   min="0"
                                   max="${orderItem.quantity}">

                        </td>

                    </tr>

                </c:forEach>


                <c:if test="${empty salesOrderItems}">

                    <tr>

                        <td colspan="4">
                            No items found for this Sales Order.
                        </td>

                    </tr>

                </c:if>

                </tbody>

            </table>


            <!-- WEIGHT -->

            <div class="weight">

                <label>
                    <strong>Package Weight (kg)</strong>
                </label>

                <br>
                <br>

                <input type="number"
                       name="weight"
                       min="0"
                       step="0.01"
                       required>

            </div>


            <button type="submit">
                Create Package
            </button>

        </form>

    </div>

</div>

</body>
</html>