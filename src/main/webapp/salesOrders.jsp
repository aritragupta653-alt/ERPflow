<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>

<head>

    <title>ERPFlow - Sales Orders</title>

    <link rel="stylesheet"
          href="/erpflow/css/app.css">

</head>

<body>

<header class="navbar">

    <a href="/erpflow/home.jsp"
       class="logo">

        <img src="/erpflow/images/erpflow-logo.png"
             alt="ERPFlow Logo">

        <span>ERPFlow</span>

    </a>

</header>


<div class="container">

    <div class="page-header">

        <div>
            <h1>Sales Orders</h1>
            <p>Create and manage customer sales orders.</p>
        </div>

        <a href="/erpflow/home.jsp"
           class="btn secondary-btn">
            ← Back to Dashboard
        </a>

    </div>


    <!-- MESSAGE -->

    <div id="messageBox"
         class="message-box"
         style="display:none;">
    </div>


    <!-- CREATE SALES ORDER -->

    <div class="form-section">

        <div class="section-header">

            <div>
                <h2>Create Sales Order</h2>
                <p>Select a customer and add products to the order.</p>
            </div>

        </div>


        <form id="salesOrderForm">

            <!-- CUSTOMER -->

            <div class="form-group">

                <label for="customerSelect">
                    Customer
                </label>

                <select id="customerSelect"
                        required>

                    <option value="">
                        Loading customers...
                    </option>

                </select>

            </div>


            <!-- ORDER ITEMS -->

            <div class="section-header">

                <div>
                    <h3>Order Items</h3>
                </div>

            </div>


            <div id="itemContainer">

                <div class="sales-item-row">

                    <select class="item-select"
                            required>

                        <option value="">
                            Loading items...
                        </option>

                    </select>


                    <input type="number"
                           class="quantity-input"
                           placeholder="Quantity"
                           min="1"
                           required>


                    <input type="number"
                           class="price-input"
                           placeholder="Selling Price"
                           min="0"
                           step="0.01"
                           required>


                    <button type="button"
                            class="btn small-btn danger-btn remove-item-btn">
                        Remove
                    </button>

                </div>

            </div>


            <div class="form-actions">

                <button type="button"
                        id="addItemButton"
                        class="btn secondary-btn">
                    + Add Another Item
                </button>


                <button type="submit"
                        class="btn primary-btn">
                    Create Sales Order
                </button>

            </div>

        </form>

    </div>


    <!-- SALES ORDER LIST -->

    <div class="table-section">

        <div class="section-header">

            <div>

                <h2>Sales Order List</h2>

                <p>
                    <span id="salesOrderCount">0</span>
                    orders
                </p>

            </div>


            <div class="search-box">

                <input type="text"
                       id="searchInput"
                       placeholder="Search orders...">

            </div>

        </div>


        <div class="table-container">

            <table>

                <thead>

                <tr>

                    <th>ID</th>

                    <th>Customer</th>

                    <th>Order Date</th>

                    <th>Status</th>

                    <th>Action</th>

                </tr>

                </thead>


                <tbody id="salesOrdersTableBody">

                </tbody>

            </table>

        </div>

    </div>

</div>


<script>

    const contextPath = "/erpflow";

</script>

<script src="/erpflow/js/salesOrders.js"
        defer></script>

</body>

</html>