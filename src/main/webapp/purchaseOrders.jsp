<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>

<head>

    <title>Purchase Orders - ERPFlow</title>

    <link rel="stylesheet" href="/erpflow/css/app.css">

</head>

<body>

<header class="navbar">

    <a href="/erpflow/home.jsp" class="logo">

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
        href="/erpflow/home.jsp"
    >
        ← Back to Dashboard
    </a>


    <div class="page-header">

        <div>

            <h1>Purchase Orders</h1>

            <p>
                Create and manage purchase orders.
            </p>

        </div>

    </div>


    <div id="messageBox"></div>


    <!-- CREATE PURCHASE ORDER -->

    <div class="form-section">

        <h2>Create Purchase Order</h2>


        <form id="purchaseOrderForm">


            <div class="form-grid">

                <div class="form-group">

                    <label>
                        Supplier
                    </label>

                    <select
                        id="supplierSelect"
                        required
                    >

                        <option value="">
                            Select Supplier
                        </option>

                    </select>

                </div>

            </div>


            <h3 style="margin-top: 25px;">
                Items
            </h3>


            <div id="itemContainer"></div>


            <button
                type="button"
                class="btn"
                id="addItemButton"
            >
                + Add Item
            </button>


            <div style="margin-top: 25px;">

                <button
                    type="submit"
                    class="btn"
                >
                    Create Purchase Order
                </button>

            </div>

        </form>

    </div>


    <!-- PURCHASE ORDER LIST -->

    <div class="table-section">

        <div class="section-header">

            <div>

                <h2>
                    Purchase Orders
                </h2>

                <p>
                    <span id="purchaseOrderCount">
                        0
                    </span>
                    purchase orders
                </p>

            </div>


            <div class="search-box">

                <input
                    type="text"
                    id="searchInput"
                    placeholder="Search purchase orders..."
                >

            </div>

        </div>


        <div class="table-container">

            <table>

                <thead>

                <tr>

                    <th>ID</th>

                    <th>Supplier</th>

                    <th>Order Date</th>

                    <th>Status</th>

                    <th>Action</th>

                </tr>

                </thead>


                <tbody
                    id="purchaseOrdersTableBody"
                >

                </tbody>

            </table>

        </div>

    </div>

</div>


<script
    src="/erpflow/js/purchaseOrders.js"
    defer
></script>

</body>

</html>