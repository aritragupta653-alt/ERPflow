<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ page isELIgnored="false" %>

        <!DOCTYPE html>

        <html>

        <head>

            <meta charset="UTF-8">

            <title>ERPFlow - Dashboard</title>

            <link rel="stylesheet" href="${pageContext.request.contextPath}/css/app.css">

        </head>


        <body>


            <!-- =====================================================
     NAVBAR
     ===================================================== -->

            <header class="navbar">

                <a href="${pageContext.request.contextPath}/home.jsp" class="logo">

                    <img src="${pageContext.request.contextPath}/images/erpflow-logo.png" alt="ERPFlow Logo">

                    <span>ERPFlow</span>

                </a>

            </header>



            <!-- =====================================================
     DASHBOARD
     ===================================================== -->

            <div class="dashboard-container">


                <!-- HEADER -->

                <div class="dashboard-header">

                    <h1>ERPFlow</h1>

                    <p>
                        Enterprise Resource Planning System
                    </p>

                </div>



                <!-- TITLE -->

                <h2 class="dashboard-title">
                    Dashboard
                </h2>



                <!-- =================================================
         MODULES
         ================================================= -->

                <div class="dashboard-grid">


                    <!-- ITEMS -->

                    <a href="${pageContext.request.contextPath}/items.jsp" class="dashboard-card">

                        <div class="card-icon">
                            📦
                        </div>

                        <h3>
                            Items
                        </h3>

                        <p>
                            Manage products, SKUs and item details.
                        </p>

                    </a>



                    <!-- INVENTORY -->

                    <a href="${pageContext.request.contextPath}/inventory.jsp" class="dashboard-card">

                        <div class="card-icon">
                            📊
                        </div>

                        <h3>
                            Inventory
                        </h3>

                        <p>
                            Manage stock levels and inventory operations.
                        </p>

                    </a>



                    <!-- SUPPLIERS -->

                    <a href="${pageContext.request.contextPath}/suppliers.jsp" class="dashboard-card">

                        <div class="card-icon">
                            🏭
                        </div>

                        <h3>
                            Suppliers
                        </h3>

                        <p>
                            Manage supplier information and contacts.
                        </p>

                    </a>



                    <!-- CUSTOMERS -->

                    <a href="${pageContext.request.contextPath}/customers.jsp" class="dashboard-card">

                        <div class="card-icon">
                            👥
                        </div>

                        <h3>
                            Customers
                        </h3>

                        <p>
                            Manage customer information and accounts.
                        </p>

                    </a>



                    <!-- PURCHASE ORDERS -->

                    <a href="${pageContext.request.contextPath}/purchaseOrders.jsp" class="dashboard-card">

                        <div class="card-icon">
                            🛒
                        </div>

                        <h3>
                            Purchase Orders
                        </h3>

                        <p>
                            Create and manage purchase orders.
                        </p>

                    </a>



                    <!-- INVENTORY TRANSACTIONS -->

                    <a href="${pageContext.request.contextPath}/inventoryTransactions.jsp" class="dashboard-card">

                        <div class="card-icon">
                            🔄
                        </div>

                        <h3>
                            Transactions
                        </h3>

                        <p>
                            View inventory transaction history.
                        </p>

                    </a>



                    <!-- SALES ORDERS -->

                    <a href="${pageContext.request.contextPath}/salesOrders.jsp" class="dashboard-card">

                        <div class="card-icon">
                            🧾
                        </div>

                        <h3>
                            Sales Orders
                        </h3>

                        <p>
                            Create and manage customer sales orders.
                        </p>

                    </a>



                    <!-- SHIPMENTS -->

                    <a href="${pageContext.request.contextPath}/shipments.jsp" class="dashboard-card">

                        <div class="card-icon">
                            🚚
                        </div>

                        <h3>
                            Shipments
                        </h3>

                        <p>
                            View shipped packages and shipment details.
                        </p>

                    </a>



                    <!-- PACKAGES -->

                    <a href="${pageContext.request.contextPath}/packages.jsp" class="dashboard-card">

                        <div class="card-icon">
                            📦
                        </div>

                        <h3>
                            Packages
                        </h3>

                        <p>
                            View and manage packages.
                        </p>

                    </a>


                </div>


            </div>


        </body>

        </html>