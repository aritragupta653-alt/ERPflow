<%@ page contentType="text/html;charset=UTF-8" %>
    <!DOCTYPE html>
    <html>

    <head>

        <meta charset="UTF-8">

        <title>Sales Order Details - ERPFlow</title>

        <link rel="stylesheet" href="/erpflow/css/app.css">

        <style>
            .readonly-input {
                background: #f8fafc;
                cursor: default;
            }

            .section-description {
                color: #64748b;
                margin-top: -8px;
                margin-bottom: 20px;
            }

            .summary-grid {
                display: grid;
                grid-template-columns: repeat(4, 1fr);
                gap: 20px;
            }

            .package-checkbox {
                width: 18px;
                height: 18px;
                cursor: pointer;
            }

            .package-checkbox:disabled {
                cursor: not-allowed;
            }

            .action-button {
                display: inline-block;
                padding: 7px 14px;
                background: #374151;
                color: white;
                text-decoration: none;
                border-radius: 5px;
                border: none;
                cursor: pointer;
            }

            .action-button:hover {
                background: #1f2937;
            }

            .status-badge {
                display: inline-block;
                padding: 5px 10px;
                border-radius: 5px;
                font-size: 13px;
                font-weight: 600;
            }

            .table-container {
                overflow-x: auto;
            }

            .empty-message {
                text-align: center;
                padding: 20px;
                color: #64748b;
            }

            .shipping-rate-grid {
                display: grid;
                grid-template-columns: repeat(3, 1fr);
                gap: 20px;
            }

            .shipping-workflow {
                margin-top: 24px;
                border: 1px solid #e2e8f0;
                border-radius: 14px;
                background: #ffffff;
                box-shadow: 0 4px 18px rgba(15, 23, 42, 0.06);
                overflow: hidden;
            }

            .shipping-workflow-header {
                display: flex;
                align-items: center;
                justify-content: space-between;
                gap: 20px;
                padding: 20px 24px;
                border-bottom: 1px solid #e2e8f0;
                background: #f8fafc;
            }

            .shipping-workflow-header h2 {
                margin: 0 0 6px;
            }

            .shipping-workflow-header p {
                margin: 0;
                color: #64748b;
            }

            .workflow-badge {
                display: inline-flex;
                align-items: center;
                padding: 6px 10px;
                border-radius: 999px;
                background: #e0f2fe;
                color: #0369a1;
                font-size: 12px;
                font-weight: 700;
                text-transform: uppercase;
                letter-spacing: .04em;
            }

            .selected-packages-box {
                padding: 12px 14px;
                background: #f8fafc;
                border: 1px solid #e2e8f0;
                border-radius: 8px;
                min-height: 20px;
            }

            .shipping-action-row {
                display: flex;
                justify-content: flex-end;
                gap: 12px;
                margin-top: 22px;
            }

            .workflow-help {
                margin-top: 8px;
                color: #64748b;
                font-size: 13px;
            }

            .danger-note {
                padding: 12px 14px;
                border-radius: 8px;
                background: #fff7ed;
                border: 1px solid #fed7aa;
                color: #9a3412;
                font-size: 13px;
            }

            .date-input {
                width: 100%;
                box-sizing: border-box;
            }

            @media (max-width: 900px) {

                .summary-grid,
                .shipping-rate-grid {
                    grid-template-columns: 1fr 1fr;
                }

            }

            @media (max-width: 600px) {

                .summary-grid,
                .shipping-rate-grid {
                    grid-template-columns: 1fr;
                }

                .shipping-workflow-header {
                    flex-direction: column;
                    align-items: flex-start;
                }

            }
        </style>

    </head>


    <body>

        <!-- NAVBAR -->

        <header class="navbar">

            <a href="/erpflow/home.jsp" class="logo">

                <img src="/erpflow/images/erpflow-logo.png" alt="ERPFlow Logo">

                <span>ERPFlow</span>

            </a>

        </header>


        <!-- MAIN CONTAINER -->

        <div class="container">


            <!-- BACK -->

            <a class="back" href="/erpflow/salesOrders.jsp">
                ← Back to Sales Orders
            </a>


            <!-- PAGE HEADER -->

            <div class="page-header">

                <div>

                    <h1>
                        Sales Order Details
                    </h1>

                    <p>
                        View complete sales order information.
                    </p>

                </div>

            </div>


            <!-- MESSAGE -->

            <div id="messageBox"></div>


            <!-- ORDER INFORMATION -->

            <div class="form-section">

                <h2>
                    Order Information
                </h2>


                <div class="form-grid">

                    <div class="form-group">

                        <label>
                            Sales Order ID
                        </label>

                        <input type="text" id="orderId" class="readonly-input" readonly>

                    </div>


                    <div class="form-group">

                        <label>
                            Order Date
                        </label>

                        <input type="text" id="orderDate" class="readonly-input" readonly>

                    </div>


                    <div class="form-group">

                        <label>
                            Expected Delivery Date
                        </label>

                        <input type="text" id="expectedDeliveryDate" class="readonly-input" readonly>

                    </div>


                    <div class="form-group">

                        <label>
                            Status
                        </label>

                        <input type="text" id="orderStatus" class="readonly-input" readonly>

                    </div>

                </div>

            </div>


            <!-- CUSTOMER -->

            <div class="form-section">

                <h2>
                    Customer
                </h2>


                <div class="form-grid">

                    <div class="form-group">

                        <label>
                            Customer ID
                        </label>

                        <input type="text" id="customerId" class="readonly-input" readonly>

                    </div>


                    <div class="form-group">

                        <label>
                            Customer Name
                        </label>

                        <input type="text" id="customerName" class="readonly-input" readonly>

                    </div>


                    <div class="form-group">

                        <label>
                            Email
                        </label>

                        <input type="text" id="customerEmail" class="readonly-input" readonly>

                    </div>


                    <div class="form-group">

                        <label>
                            Phone
                        </label>

                        <input type="text" id="customerPhone" class="readonly-input" readonly>

                    </div>

                </div>

            </div>


            <!-- ORDER ITEMS -->

            <div class="table-section">

                <div class="section-header">

                    <div>

                        <h2>
                            Order Items
                        </h2>

                        <p class="section-description">
                            Items included in this sales order.
                        </p>

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
                                    Selling Price
                                </th>

                                <th>
                                    Total
                                </th>

                            </tr>

                        </thead>


                        <tbody id="orderItemsTableBody">

                            <tr>

                                <td colspan="6" class="empty-message">
                                    Loading order items...
                                </td>

                            </tr>

                        </tbody>

                    </table>

                </div>

            </div>


            <!-- ORDER SUMMARY -->

            <div class="form-section">

                <h2>
                    Order Summary
                </h2>


                <div class="summary-grid">

                    <div class="form-group">

                        <label>
                            Subtotal
                        </label>

                        <input type="text" id="subtotal" class="readonly-input" readonly>

                    </div>


                    <div class="form-group">

                        <label>
                            Tax Rate
                        </label>

                        <input type="text" id="taxRate" class="readonly-input" readonly>

                    </div>


                    <div class="form-group">

                        <label>
                            Tax Amount
                        </label>

                        <input type="text" id="taxAmount" class="readonly-input" readonly>

                    </div>


                    <div class="form-group">

                        <label>
                            Total Amount
                        </label>

                        <input type="text" id="totalAmount" class="readonly-input" readonly>

                    </div>

                </div>

            </div>


            <!-- PACKAGES -->

            <div class="table-section">

                <div class="section-header">

                    <div>

                        <h2>
                            Packages
                        </h2>

                        <p class="section-description">
                            Select packed packages to create a shipment.
                        </p>

                    </div>




                </div>


                <div class="table-container">

                    <table>

                        <thead>

                            <tr>

                                <th>
                                    Select
                                </th>

                                <th>
                                    Package
                                </th>

                                <th>
                                    Items
                                </th>

                                <th>
                                    Weight
                                </th>

                                <th>
                                    Length
                                </th>

                                <th>
                                    Width
                                </th>

                                <th>
                                    Height
                                </th>

                                <th>
                                    Package Date
                                </th>

                                <th>
                                    Status
                                </th>

                                <th>
                                    Action
                                </th>

                            </tr>

                        </thead>


                        <tbody id="packagesTableBody">

                            <tr>

                                <td colspan="10" class="empty-message">
                                    Loading packages...
                                </td>

                            </tr>

                        </tbody>

                    </table>

                </div>


                <!-- =====================================================
                 AUTO PACK & SHIP
                 ===================================================== -->

                <div class="shipping-workflow" id="packShipSection">

                    <div class="shipping-workflow-header">

                        <div>

                            <h2>
                                Auto Pack &amp; Ship
                            </h2>

                            <p>
                                Pack the remaining sales-order quantity
                                and create its shipment automatically.
                            </p>

                        </div>

                        <span class="workflow-badge">
                            Automatic
                        </span>

                    </div>


                    <div class="shipping-workflow-body">

                        <div class="form-grid">

                            <div class="form-group">

                                <label for="autoShipmentDate">
                                    Shipment Date
                                </label>

                                <input type="date" id="autoShipmentDate" name="autoShipmentDate" class="date-input"
                                    required>

                                <div class="workflow-help">
                                    The date on which the shipment is created.
                                </div>

                            </div>


                            <div class="form-group">

                                <label for="deliveryStatus">
                                    Shipment Status
                                </label>

                                <select id="deliveryStatus" name="deliveryStatus" required>

                                    <option value="CREATED">
                                        Created
                                    </option>

                                    <option value="IN_TRANSIT">
                                        In Transit
                                    </option>

                                </select>

                                <div class="workflow-help">
                                    Use Mark as Delivered later when
                                    the shipment is actually delivered.
                                </div>

                            </div>

                        </div>


                        <div class="shipping-action-row">

                            <button type="button" id="packShipButton" class="btn btn-primary">
                                Auto Pack &amp; Ship
                            </button>

                        </div>

                    </div>

                </div>


                <!-- =====================================================
                 MANUAL SHIPMENT
                 ===================================================== -->

                <div id="shippingSection" class="shipping-workflow" style="display:none;">

                    <div class="shipping-workflow-header">

                        <div>

                            <h2>
                                Create Shipment
                            </h2>

                            <p>
                                Create a shipment manually from
                                the selected existing packages.
                            </p>

                        </div>

                        <span class="workflow-badge">
                            Manual
                        </span>

                    </div>


                    <div class="shipping-workflow-body">


                        <!-- SELECTED PACKAGES -->

                        <div class="form-group">

                            <label>
                                Selected Packages
                            </label>

                            <div id="selectedPackages" class="selected-packages-box">
                                No packages selected.
                            </div>

                        </div>


                        <!-- DATE + METHOD -->

                        <div class="form-grid">

                            <div class="form-group">

                                <label for="manualShipmentDate">
                                    Shipment Date
                                </label>

                                <input type="date" id="manualShipmentDate" name="manualShipmentDate" class="date-input"
                                    required>

                            </div>


                            <div class="form-group">

                                <label for="manualShippingMethod">
                                    Shipping Method
                                </label>

                                <select id="manualShippingMethod" name="manualShippingMethod">

                                    <option value="CARRIER">
                                        Carrier
                                    </option>

                                    <option value="MANUAL">
                                        Manual
                                    </option>

                                </select>

                            </div>

                        </div>


                        <!-- CARRIER -->

                        <div class="form-grid">

                            <div class="form-group">

                                <label for="carrierSelect">
                                    Carrier
                                </label>

                                <select id="carrierSelect">

                                    <option value="">
                                        Select Carrier
                                    </option>

                                </select>

                            </div>


                            <div class="form-group">

                                <label for="serviceSelect">
                                    Carrier Service
                                </label>

                                <select id="serviceSelect">

                                    <option value="">
                                        Select Service
                                    </option>

                                </select>

                            </div>

                        </div>


                        <!-- TRACKING -->

                        <div class="form-grid">

                            <div class="form-group">

                                <label for="trackingNumber">
                                    Tracking Number
                                </label>

                                <input type="text" id="trackingNumber" placeholder="Optional">

                            </div>


                            <div class="form-group">

                                <label for="trackingUrl">
                                    Tracking URL
                                </label>

                                <input type="url" id="trackingUrl" placeholder="https://example.com/track/...">

                            </div>

                        </div>


                        <!-- ADDRESSES -->

                        <div class="form-grid">

                            <div class="form-group">

                                <label for="dispatchAddress">
                                    Dispatch Address
                                </label>

                                <textarea id="dispatchAddress" rows="3" placeholder="Enter dispatch address"></textarea>

                            </div>


                            <div class="form-group">

                                <label for="destinationAddress">
                                    Destination Address
                                </label>

                                <textarea id="destinationAddress" rows="3"
                                    placeholder="Enter destination address"></textarea>

                            </div>

                        </div>


                        <!-- SHIPPING RATE -->

                        <div class="form-section" style="margin-top:20px;">

                            <h3>
                                Shipping Rate
                            </h3>


                            <div class="shipping-rate-grid">

                                <div class="form-group">

                                    <label>
                                        Actual Weight
                                    </label>

                                    <input type="text" id="actualWeight" value="-" class="readonly-input" readonly>

                                </div>


                                <div class="form-group">

                                    <label>
                                        Dimensional Weight
                                    </label>

                                    <input type="text" id="dimensionalWeight" value="-" class="readonly-input" readonly>

                                </div>


                                <div class="form-group">

                                    <label>
                                        Chargeable Weight
                                    </label>

                                    <input type="text" id="chargeableWeight" value="-" class="readonly-input" readonly>

                                </div>


                                <div class="form-group">

                                    <label>
                                        Base Charge
                                    </label>

                                    <input type="text" id="baseCharge" value="-" class="readonly-input" readonly>

                                </div>


                                <div class="form-group">

                                    <label>
                                        Charge Per KG
                                    </label>

                                    <input type="text" id="chargePerKg" value="-" class="readonly-input" readonly>

                                </div>


                                <div class="form-group">
                                    <label>Total Shipping Charge</label>

                                    <input type="text" id="totalShippingCharge" value="-" class="readonly-input"
                                        readonly>

                                    <!-- Used internally when creating the shipment -->
                                    <input type="hidden" id="shippingCharge" value="">
                                </div>

                            </div>


                            <button type="button" id="calculateRateButton" class="btn btn-secondary">
                                Calculate Shipping Rate
                            </button>

                        </div>


                        <!-- NOTES -->

                        <div class="form-group">

                            <label for="shipmentNotes">
                                Notes
                            </label>

                            <textarea id="shipmentNotes" rows="4" placeholder="Optional shipment notes"></textarea>

                        </div>


                        <!-- CREATE -->

                        <div class="shipping-action-row">

                            <button type="button" id="createShipmentButton" class="btn btn-primary" disabled>
                                Create Shipment
                            </button>

                        </div>

                    </div>

                </div>

            </div>


            <!-- =====================================================
             CREATE PACKAGE MODAL
             ===================================================== -->

            <div id="createPackageModal" class="modal" style="display:none;">

                <div class="modal-content">

                    <div class="modal-header">

                        <h2>
                            Create Package
                        </h2>

                        <button class="close-btn" onclick="closeCreatePackage()">
                            &times;
                        </button>

                    </div>


                    <form id="createPackageForm">

                        <div class="form-group">

                            <label for="salesOrderId">
                                Sales Order
                            </label>

                            <select id="salesOrderId" required disabled>
                            </select>

                        </div>


                        <div class="form-group">

                            <label>
                                Items
                            </label>

                            <div id="orderItemsContainer">

                                <p class="muted">
                                    Loading items...
                                </p>

                            </div>

                        </div>


                        <div class="form-group">

                            <label for="weight">
                                Weight (kg)
                            </label>

                            <input type="number" id="weight" step="0.01" min="0.01" required>

                        </div>


                        <div class="form-row">

                            <div class="form-group">

                                <label for="length">
                                    Length (cm)
                                </label>

                                <input type="number" id="length" step="0.01" min="0.01" required>

                            </div>


                            <div class="form-group">

                                <label for="width">
                                    Width (cm)
                                </label>

                                <input type="number" id="width" step="0.01" min="0.01" required>

                            </div>


                            <div class="form-group">

                                <label for="height">
                                    Height (cm)
                                </label>

                                <input type="number" id="height" step="0.01" min="0.01" required>

                            </div>

                        </div>


                        <div id="createPackageError" class="error-message" style="display:none;">
                        </div>


                        <div class="modal-footer">

                            <button type="button" class="btn btn-secondary" onclick="closeCreatePackage()">
                                Cancel
                            </button>


                            <button type="submit" class="btn btn-primary">
                                Create Package
                            </button>

                        </div>

                    </form>

                </div>

            </div>


        </div>


        <!-- JAVASCRIPT -->

        <script src="/erpflow/js/salesOrderDetails.js?v=7" defer></script>


    </body>

    </html>