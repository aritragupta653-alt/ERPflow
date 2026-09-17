<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>

<head>

    <title>Purchase Order Details - ERPFlow</title>

    <link
        rel="stylesheet"
        href="/erpflow/css/app.css"
    >

</head>

<body>

<header class="navbar">

    <a
        href="/erpflow/home.jsp"
        class="logo"
    >

        <img
            src="/erpflow/images/erpflow-logo.png"
            alt="ERPFlow Logo"
        >

        <span>ERPFlow</span>

    </a>

</header>


<div class="container">

    <a
        class="back"
        href="/erpflow/purchaseOrders.jsp"
    >
        ← Back to Purchase Orders
    </a>


    <div class="page-header">

        <div>

            <h1>
                Purchase Order Details
            </h1>

            <p>
                View complete purchase order information.
            </p>

        </div>

    </div>


    <div id="messageBox"></div>


    <!-- PURCHASE ORDER -->

    <div class="form-section">

        <h2>Order Information</h2>


        <div class="form-grid">

            <div class="form-group">

                <label>
                    Purchase Order ID
                </label>

                <input
                    type="text"
                    id="orderId"
                    readonly
                >

            </div>


            <div class="form-group">

                <label>
                    Order Date
                </label>

                <input
                    type="text"
                    id="orderDate"
                    readonly
                >

            </div>


            <div class="form-group">

                <label>
                    Status
                </label>

                <input
                    type="text"
                    id="orderStatus"
                    readonly
                >

            </div>

        </div>

    </div>


    <!-- SUPPLIER -->

    <div class="form-section">

        <h2>Supplier</h2>


        <div class="form-grid">

            <div class="form-group">

                <label>
                    Supplier ID
                </label>

                <input
                    type="text"
                    id="supplierId"
                    readonly
                >

            </div>


            <div class="form-group">

                <label>
                    Supplier Name
                </label>

                <input
                    type="text"
                    id="supplierName"
                    readonly
                >

            </div>


            <div class="form-group">

                <label>
                    Contact Person
                </label>

                <input
                    type="text"
                    id="contactPerson"
                    readonly
                >

            </div>


            <div class="form-group">

                <label>
                    Phone
                </label>

                <input
                    type="text"
                    id="supplierPhone"
                    readonly
                >

            </div>


            <div class="form-group">

                <label>
                    Email
                </label>

                <input
                    type="text"
                    id="supplierEmail"
                    readonly
                >

            </div>


            <div class="form-group">

                <label>
                    Address
                </label>

                <input
                    type="text"
                    id="supplierAddress"
                    readonly
                >

            </div>

        </div>

    </div>


    <!-- ITEMS -->

    <div class="table-section">

        <div class="section-header">

            <div>

                <h2>
                    Purchase Order Items
                </h2>

            </div>

        </div>


        <div class="table-container">

            <table>

                <thead>

                <tr>

                    <th>
                        Item ID
                    </th>

                    <th>
                        Item Name
                    </th>

                    <th>
                        SKU
                    </th>

                    <th>
                        Quantity
                    </th>

                    <th>
                        Purchase Price
                    </th>

                    <th>
                        Total
                    </th>

                </tr>

                </thead>


                <tbody
                    id="orderItemsTableBody"
                ></tbody>

            </table>

        </div>

    </div>

</div>


<script
    src="/erpflow/js/purchaseOrderDetails.js"
    defer
></script>

</body>

</html>