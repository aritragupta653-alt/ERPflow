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
        }
    </style>
</head>

<body>

    <!-- NAVBAR -->
    <header class="navbar">
        <a href="/erpflow/home.jsp" class="logo">
            <img
                src="/erpflow/images/erpflow-logo.png"
                alt="ERPFlow Logo"
            >
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
                <h1>Sales Order Details</h1>
                <p>View complete sales order information.</p>
            </div>
        </div>

        <!-- MESSAGE -->
        <div id="messageBox"></div>

        <!-- ORDER INFORMATION -->
        <div class="form-section">
            <h2>Order Information</h2>

            <div class="form-grid">
                <div class="form-group">
                    <label>Sales Order ID</label>
                    <input
                        type="text"
                        id="orderId"
                        class="readonly-input"
                        readonly
                    >
                </div>

                <div class="form-group">
                    <label>Order Date</label>
                    <input
                        type="text"
                        id="orderDate"
                        class="readonly-input"
                        readonly
                    >
                </div>

                <div class="form-group">
                    <label>Status</label>
                    <input
                        type="text"
                        id="orderStatus"
                        class="readonly-input"
                        readonly
                    >
                </div>
            </div>
        </div>

        <!-- CUSTOMER -->
        <div class="form-section">
            <h2>Customer</h2>

            <div class="form-grid">
                <div class="form-group">
                    <label>Customer ID</label>
                    <input
                        type="text"
                        id="customerId"
                        class="readonly-input"
                        readonly
                    >
                </div>

                <div class="form-group">
                    <label>Customer Name</label>
                    <input
                        type="text"
                        id="customerName"
                        class="readonly-input"
                        readonly
                    >
                </div>

                <div class="form-group">
                    <label>Email</label>
                    <input
                        type="text"
                        id="customerEmail"
                        class="readonly-input"
                        readonly
                    >
                </div>

                <div class="form-group">
                    <label>Phone</label>
                    <input
                        type="text"
                        id="customerPhone"
                        class="readonly-input"
                        readonly
                    >
                </div>
            </div>
        </div>

        <!-- ORDER ITEMS -->
        <div class="table-section">
            <div class="section-header">
                <div>
                    <h2>Order Items</h2>
                    <p class="section-description">
                        Items included in this sales order.
                    </p>
                </div>
            </div>

            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>Item ID</th>
                            <th>Item Name</th>
                            <th>SKU</th>
                            <th>Quantity</th>
                            <th>Selling Price</th>
                            <th>Total</th>
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
            <h2>Order Summary</h2>

            <div class="summary-grid">
                <div class="form-group">
                    <label>Subtotal</label>
                    <input
                        type="text"
                        id="subtotal"
                        class="readonly-input"
                        readonly
                    >
                </div>

                <div class="form-group">
                    <label>Tax Rate</label>
                    <input
                        type="text"
                        id="taxRate"
                        class="readonly-input"
                        readonly
                    >
                </div>

                <div class="form-group">
                    <label>Tax Amount</label>
                    <input
                        type="text"
                        id="taxAmount"
                        class="readonly-input"
                        readonly
                    >
                </div>

                <div class="form-group">
                    <label>Total Amount</label>
                    <input
                        type="text"
                        id="totalAmount"
                        class="readonly-input"
                        readonly
                    >
                </div>
            </div>
        </div>

        <!-- PACKAGES -->
        <div class="table-section">
            <div class="section-header">
                <div>
                    <h2>Packages</h2>
                    <p class="section-description">
                        Select packed packages to create a shipment.
                    </p>
                </div>

                <div>
                    <button
                        type="button"
                        id="shipSelectedButton"
                        class="btn btn-primary"
                        disabled
                    >
                        Ship Selected Packages
                    </button>
                </div>
            </div>

            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>Select</th>
                            <th>Package</th>
                            <th>Status</th>
                            <th>Weight</th>
                            <th>Dimensions</th>
                            <th>Package Date</th>
                            <th>Action</th>
                        </tr>
                    </thead>

                    <tbody id="packagesTableBody">
                        <tr>
                            <td colspan="7" class="empty-message">
                                Loading packages...
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- SHIPPING SECTION -->
        <div
            id="shippingSection"
            class="form-section"
            style="display:none;"
        >
            <h2>Create Shipment</h2>

            <!-- SELECTED PACKAGES -->
            <div class="form-group">
                <label>Selected Packages</label>

                <div
                    id="selectedPackages"
                    style="
                        padding:12px;
                        background:#f5f7fa;
                        border-radius:8px;
                    "
                ></div>
            </div>

            <!-- CARRIER -->
            <div class="form-grid">
                <div class="form-group">
                    <label for="carrierSelect">Carrier</label>

                    <select id="carrierSelect">
                        <option value="">Select Carrier</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="serviceSelect">Carrier Service</label>

                    <select id="serviceSelect">
                        <option value="">Select Service</option>
                    </select>
                </div>
            </div>

            <!-- TRACKING -->
            <div class="form-grid">
                <div class="form-group">
                    <label for="trackingNumber">Tracking Number</label>

                    <input
                        type="text"
                        id="trackingNumber"
                        placeholder="Optional"
                    >
                </div>

                <div class="form-group">
                    <label for="trackingUrl">Tracking URL</label>

                    <input
                        type="url"
                        id="trackingUrl"
                        placeholder="https://example.com/track/..."
                    >
                </div>
            </div>

            <!-- ADDRESSES -->
            <div class="form-grid">
                <div class="form-group">
                    <label for="dispatchAddress">Dispatch Address</label>

                    <textarea
                        id="dispatchAddress"
                        rows="3"
                        placeholder="Enter dispatch address"
                    ></textarea>
                </div>

                <div class="form-group">
                    <label for="destinationAddress">
                        Destination Address
                    </label>

                    <textarea
                        id="destinationAddress"
                        rows="3"
                        placeholder="Enter destination address"
                    ></textarea>
                </div>
            </div>

            <!-- SHIPPING RATE -->
            <div class="form-section" style="margin-top:20px;">
                <h3>Shipping Rate</h3>

                <div class="shipping-rate-grid">
                    <div class="form-group">
                        <label>Actual Weight</label>
                        <input
                            type="text"
                            id="actualWeight"
                            value="-"
                            class="readonly-input"
                            readonly
                        >
                    </div>

                    <div class="form-group">
                        <label>Dimensional Weight</label>
                        <input
                            type="text"
                            id="dimensionalWeight"
                            value="-"
                            class="readonly-input"
                            readonly
                        >
                    </div>

                    <div class="form-group">
                        <label>Chargeable Weight</label>
                        <input
                            type="text"
                            id="chargeableWeight"
                            value="-"
                            class="readonly-input"
                            readonly
                        >
                    </div>

                    <div class="form-group">
                        <label>Base Charge</label>
                        <input
                            type="text"
                            id="baseCharge"
                            value="-"
                            class="readonly-input"
                            readonly
                        >
                    </div>

                    <div class="form-group">
                        <label>Charge Per KG</label>
                        <input
                            type="text"
                            id="chargePerKg"
                            value="-"
                            class="readonly-input"
                            readonly
                        >
                    </div>

                    <div class="form-group">
                        <label>Total Shipping Charge</label>
                        <input
                            type="text"
                            id="totalShippingCharge"
                            value="-"
                            class="readonly-input"
                            readonly
                        >
                    </div>
                </div>

                <button
                    type="button"
                    id="calculateRateButton"
                    class="btn btn-secondary"
                >
                    Calculate Shipping Rate
                </button>
            </div>

            <!-- NOTES -->
            <div class="form-group">
                <label for="shipmentNotes">Notes</label>

                <textarea
                    id="shipmentNotes"
                    rows="4"
                    placeholder="Optional shipment notes"
                ></textarea>
            </div>

            <!-- CREATE SHIPMENT -->
            <div
                style="
                    display:flex;
                    justify-content:flex-end;
                    margin-top:20px;
                "
            >
                <button
                    type="button"
                    id="createShipmentButton"
                    class="btn btn-primary"
                    disabled
                >
                    Create Shipment
                </button>
            </div>
        </div>
    </div>

    <!-- JAVASCRIPT -->
    <script
        src="/erpflow/js/salesOrderDetails.js?v=6"
        defer
    ></script>

</body>
</html>