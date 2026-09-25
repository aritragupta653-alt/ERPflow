<%@ page contentType="text/html;charset=UTF-8" %>

    <!DOCTYPE html>
    <html>

    <head>
        <meta charset="UTF-8">
        <title>Sales Orders - ERPFlow</title>

        <link rel="stylesheet" href="/erpflow/css/app.css">

        <style>
            .order-summary {
                max-width: 500px;
                margin: 25px 0 10px auto;
                padding: 20px;
                border: 1px solid #ddd;
                border-radius: 8px;
                background: #f8fafc;
            }

            .order-summary>div {
                display: flex;
                justify-content: space-between;
                padding: 10px 0;
            }

            .order-summary .grand-total {
                margin-top: 10px;
                padding-top: 15px;
                border-top: 2px solid #ddd;
                font-size: 18px;
            }

            .order-summary .grand-total strong {
                font-size: 20px;
            }

            .tax-note {
                margin-top: 6px;
                font-size: 12px;
                color: #666;
            }

            .form-section {
                margin-bottom: 25px;
            }

            .edit-mode-note {
                display: none;
                margin: 12px 0;
                padding: 12px;
                border-radius: 6px;
                background: #eff6ff;
                color: #1d4ed8;
            }

            .form-actions {
                display: flex;
                align-items: center;
                gap: 12px;
            }

            .secondary-button {
                padding: 10px 18px;
                border: 1px solid #cbd5e1;
                border-radius: 6px;
                background: #fff;
                color: #334155;
                cursor: pointer;
                font: inherit;
            }

            .secondary-button:hover {
                background: #f1f5f9;
            }

            .action-button {
                display: inline-block;
                padding: 7px 14px;
                margin: 2px;
                background: #374151;
                color: white;
                text-decoration: none;
                border: none;
                border-radius: 5px;
                cursor: pointer;
                font: inherit;
            }

            .action-button:hover {
                background: #1f2937;
            }

            .edit-button {
                background: #2563eb;
            }

            .edit-button:hover {
                background: #1d4ed8;
            }

            .disabled-action {
                opacity: 0.5;
                cursor: not-allowed;
            }

            @media (max-width: 600px) {
                .form-actions {
                    flex-wrap: wrap;
                }
            }
        </style>
    </head>

    <body>

        <header class="navbar">
            <a href="/erpflow/home.jsp" class="logo">
                <img src="/erpflow/images/erpflow-logo.png" alt="ERPFlow Logo">
                <span>ERPFlow</span>
            </a>
        </header>

        <div class="container">

            <div class="page-header">
                <div>
                    <h1>Sales Orders</h1>
                    <p>Create and manage customer sales orders.</p>
                </div>
            </div>

            <div id="messageBox"></div>

            <!-- ========================= -->
            <!-- CREATE / EDIT SALES ORDER -->
            <!-- ========================= -->

            <div class="form-section">

                <h2 id="salesOrderFormTitle">Create Sales Order</h2>

                <div id="editModeNote" class="edit-mode-note">
                    You are editing an existing sales order. Make your changes and select
                    Update Sales Order to save them.
                </div>

                <div class="form-grid">

                    <!-- Customer -->

                    <div class="form-group">
                        <label for="customerSelect">
                            Customer
                        </label>

                        <select id="customerSelect">
                            <option value="">
                                Select Customer
                            </option>
                        </select>
                    </div>
                    <div class="form-group">

                        <label for="orderDate">
                            Order Date
                        </label>

                        <input type="date" id="orderDate" required>

                    </div>
                    <div class="form-group">

                        <label for="expectedDeliveryDate">
                            Expected Delivery Date
                        </label>

                        <input type="date" id="expectedDeliveryDate" required>

                    </div>

                    <!-- Tax -->

                    <div class="form-group">
                        <label for="taxRate">
                            Tax Rate
                        </label>

                        <select id="taxRate">
                            <option value="0">No Tax (0%)</option>
                            <option value="5">GST 5%</option>
                            <option value="12">GST 12%</option>
                            <option value="18">GST 18%</option>
                            <option value="28">GST 28%</option>
                        </select>

                        <div class="tax-note">
                            Tax is calculated automatically from the order subtotal.
                        </div>
                    </div>

                </div>
            </div>

            <!-- ========================= -->
            <!-- ORDER ITEMS -->
            <!-- ========================= -->

            <div class="table-section">

                <div class="section-header">
                    <div>
                        <h2>Order Items</h2>
                    </div>

                    <button type="button" id="addItemButton" class="primary-button">
                        + Add Item
                    </button>
                </div>

                <div class="table-container">
                    <table>
                        <thead>
                            <tr>
                                <th>Item</th>
                                <th>SKU</th>
                                <th>Available Stock</th>
                                <th>Quantity</th>
                                <th>Selling Price</th>
                                <th>Line Total</th>
                                <th>Action</th>
                            </tr>
                        </thead>

                        <tbody id="salesOrderItemsTableBody"></tbody>
                    </table>
                </div>
            </div>

            <!-- ========================= -->
            <!-- ORDER SUMMARY -->
            <!-- ========================= -->

            <div class="form-section">

                <h2>Tax &amp; Order Summary</h2>

                <div class="order-summary">

                    <div>
                        <span>Subtotal</span>

                        <strong id="subtotalDisplay">
                            ₹0.00
                        </strong>
                    </div>

                    <div>
                        <span>
                            Tax
                            (<span id="taxRateDisplay">0.00</span>%)
                        </span>

                        <strong id="taxAmountDisplay">
                            ₹0.00
                        </strong>
                    </div>

                    <div class="grand-total">
                        <span>Grand Total</span>

                        <strong id="totalAmountDisplay">
                            ₹0.00
                        </strong>
                    </div>

                </div>
            </div>

            <!-- ========================= -->
            <!-- CREATE / UPDATE BUTTONS -->
            <!-- ========================= -->

            <div class="form-actions">

                <button type="button" id="createSalesOrderButton" class="primary-button">
                    Create Sales Order
                </button>

                <button type="button" id="cancelEditButton" class="secondary-button" style="display:none;">
                    Cancel Edit
                </button>

            </div>

            <!-- ========================= -->
            <!-- EXISTING SALES ORDERS -->
            <!-- ========================= -->

            <div class="table-section">

                <div class="section-header">

                    <div>
                        <h2>Sales Orders</h2>
                    </div>


                    <div class="section-header">
                        <div>
                            <h2>Sales Orders</h2>
                        </div>

                        <div class="search-box">
                            <select id="statusFilter">
                                <option value="ALL">All Statuses</option>
                                <option value="CREATED">Created</option>
                                <option value="PARTIALLY_SHIPPED">Partially Shipped</option>
                                <option value="SHIPPED">Shipped</option>
                                <option value="COMPLETED">Completed</option>
                            </select>

                            <input type="text" id="searchInput" placeholder="Search sales orders...">
                        </div>
                    </div>

                </div>

                <div class="table-container">
                    <table>

                        <thead>
                            <tr>
                                <th>Order ID</th>
                                <th>Customer</th>
                                <th>Order Date</th>
                                <th>Expected Delivery</th>

                                <th>Subtotal</th>
                                <th>Tax</th>
                                <th>Total</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                        </thead>

                        <tbody id="salesOrdersTableBody"></tbody>

                    </table>
                </div>
            </div>

        </div>

        <script src="/erpflow/js/salesOrders.js?v=edit1" defer></script>

    </body>

    </html>