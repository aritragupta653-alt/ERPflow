<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>
<head>

    <title>Package Details - ERPFlow</title>

    
    <link
            rel="stylesheet"
            href="${pageContext.request.contextPath}/css/app.css"
    >

</head>


<body>


<header class="navbar">

    <a href="${pageContext.request.contextPath}/home"
       class="logo">

        ERPFlow

    </a>

</header>


<div class="container">


    <!-- BACK -->

    <a class="back"
       href="${pageContext.request.contextPath}/packages">

        ← Back to Packages

    </a>


    <!-- PACKAGE INFORMATION -->

    <div class="card">

        <h1>
            Package #${packageEntity.id}
        </h1>


        <div class="info">

            <strong>Sales Order:</strong>
            #${packageEntity.salesOrder.id}

            <br>


            <strong>Customer:</strong>
            ${packageEntity.salesOrder.customer.name}

            <br>


            <strong>Package Date:</strong>
            ${packageEntity.packageDate}

            <br>


            <strong>Weight:</strong>
            ${packageEntity.weight} kg

            <br>


            <strong>Status:</strong>

            <span class="status">
                ${packageEntity.status}
            </span>

        </div>

    </div>


    <!-- PACKAGE ITEMS -->

    <div class="card">

        <h2>
            Package Items
        </h2>


        <table>

            <thead>

                <tr>

                    <th>Item</th>

                    <th>SKU</th>

                    <th>Quantity</th>

                </tr>

            </thead>


            <tbody>


                <c:forEach
                        var="packageItem"
                        items="${packageItems}">

                    <tr>

                        <td>
                            ${packageItem.item.name}
                        </td>


                        <td>
                            ${packageItem.item.sku}
                        </td>


                        <td>
                            ${packageItem.quantity}
                        </td>

                    </tr>

                </c:forEach>


                <c:if test="${empty packageItems}">

                    <tr>

                        <td colspan="3">
                            No items found.
                        </td>

                    </tr>

                </c:if>


            </tbody>

        </table>

    </div>


    <!-- VIEW SALES ORDER -->

    <a class="button"
       href="${pageContext.request.contextPath}/sales-orders?action=view&id=${packageEntity.salesOrder.id}">

        View Sales Order

    </a>
    <c:if test="${packageEntity.status == 'PACKED'}">

    <form action="${pageContext.request.contextPath}/shipping"
          method="post"
          style="display: inline;">

        <input type="hidden"
               name="action"
               value="ship">

        <input type="hidden"
               name="packageId"
               value="${packageEntity.id}">

        <button type="submit" class = "ship-button"
                onclick="return confirm('Are you sure you want to ship this package?');">

            Ship Package

        </button>

    </form>

</c:if>


</div>


</body>

</html>