<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>
<!DOCTYPE html>
<html>
<head>

    <title>Packages - ERPFlow</title>

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
        }

        h1 {
            margin-bottom: 25px;
        }

        .button {
            display: inline-block;
            padding: 10px 18px;
            background: #2563eb;
            color: white;
            text-decoration: none;
            border-radius: 6px;
            margin-bottom: 25px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            background: white;
        }

        th,
        td {
            padding: 14px;
            border-bottom: 1px solid #ddd;
            text-align: left;
        }

        th {
            background: #f1f5f9;
        }

        .view-button {
            padding: 7px 14px;
            background: #374151;
            color: white;
            text-decoration: none;
            border-radius: 5px;
        }

        .status {
            font-weight: bold;
        }
        .top{
            display: flex;
            flex-direction: row;
            gap:1000px;
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

    <h1>Packages</h1>

    <div class = "top">
        <a href="${pageContext.request.contextPath}/sales-orders"
       class="button">
        ← Sales Orders
    </a>

    <button class = "button">+ New Package</button>
</div>

    

    <table>

        <thead>

        <tr>

            <th>Package ID</th>

            <th>Sales Order</th>

            <th>Package Date</th>

            <th>Weight</th>

            <th>Status</th>

            <th>Action</th>

        </tr>

        </thead>


        <tbody>

        <c:forEach var="pkg" items="${packages}">

            <tr>

                <td>
                    #${pkg.id}
                </td>


                <td>
                    #${pkg.salesOrder.id}
                </td>


                <td>
                    ${pkg.packageDate}
                </td>


                <td>
                    ${pkg.weight} kg
                </td>


                <td class="status">
                    ${pkg.status}
                </td>


                <td>

                    <a class="view-button"
                       href="${pageContext.request.contextPath}/packages?action=view&id=${pkg.id}">
                        View
                    </a>

                </td>

            </tr>

        </c:forEach>


        <c:if test="${empty packages}">

            <tr>

                <td colspan="6">
                    No packages found.
                </td>

            </tr>

        </c:if>

        </tbody>

    </table>

</div>

</body>
</html>