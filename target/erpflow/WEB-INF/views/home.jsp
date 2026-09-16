<%@ page contentType="text/html;charset=UTF-8"
         isELIgnored="false" %>

<!DOCTYPE html>

<html>

<head>

    <title>ERPFlow - Home</title>

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



<div class="dashboard-container">


    <div class="dashboard-header">

        <h1>ERPFlow</h1>

        <p>
            Enterprise Resource Planning System
        </p>

    </div>


    <h2 class="dashboard-title">

        Dashboard

    </h2>


    <div class="dashboard-grid">


        <!-- ITEMS -->

        <a
                href="${pageContext.request.contextPath}/items"
                class="dashboard-card"
        >

            <div class="card-icon">
                
            </div>

            <h3>
                Items
            </h3>

            <p>
                Manage products and item details
            </p>

        </a>


        <!-- INVENTORY -->

        <a
                href="${pageContext.request.contextPath}/inventory"
                class="dashboard-card"
        >

            <div class="card-icon">
                
            </div>

            <h3>
                Inventory
            </h3>

            <p>
                Manage stock and inventory
            </p>

        </a>


        <!-- SUPPLIERS -->

        <a
                href="${pageContext.request.contextPath}/suppliers"
                class="dashboard-card"
        >

            <div class="card-icon">
                
            </div>

            <h3>
                Suppliers
            </h3>

            <p>
                Manage suppliers
            </p>

        </a>


        <!-- CUSTOMERS -->

        <a
                href="${pageContext.request.contextPath}/customers"
                class="dashboard-card"
        >

            <div class="card-icon">
                
            </div>

            <h3>
                Customers
            </h3>

            <p>
                Manage customer information
            </p>

        </a>


        <!-- PURCHASE ORDERS -->

        <a
                href="${pageContext.request.contextPath}/purchase-orders"
                class="dashboard-card"
        >

            <div class="card-icon">
                
            </div>

            <h3>
                Purchase Orders
            </h3>

            <p>
                Create and manage purchase orders
            </p>

        </a>


        <!-- INVENTORY TRANSACTIONS -->

        <a
                href="${pageContext.request.contextPath}/inventory-transactions"
                class="dashboard-card"
        >

            <div class="card-icon">
                
            </div>

            <h3>
                Transactions
            </h3>

            <p>
                View inventory transaction history
            </p>

        </a>
        <a
                href="${pageContext.request.contextPath}/sales-orders"
                class="dashboard-card"
        >

            <div class="card-icon">
                
            </div>

            <h3>
                Sales Orders
            </h3>

            <p>
                View and create Sales Orders
            </p>

        </a>


    </div>


</div>


</body>

</html>