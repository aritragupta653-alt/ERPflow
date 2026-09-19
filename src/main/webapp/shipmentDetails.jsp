<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Shipment Details - ERPFlow</title>

    <link rel="stylesheet" href="/erpflow/css/app.css">

    <style>
        .shipment-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 24px;
        }

        .shipment-title {
            display: flex;
            align-items: center;
            gap: 14px;
        }

        .shipment-title h1 {
            margin: 0;
        }

        .status-badge {
            display: inline-block;
            padding: 5px 12px;
            border-radius: 20px;
            font-size: 13px;
            font-weight: 600;
        }

        .status-created {
            background: #e8f0fe;
            color: #2457a6;
        }

        .status-shipped {
            background: #e6f7ed;
            color: #16794c;
        }

        .status-delivered {
            background: #e6f7ed;
            color: #16794c;
        }

        .status-default {
            background: #f1f3f5;
            color: #555;
        }

        .summary-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 18px;
        }

        .summary-item {
            padding: 4px 0;
        }

        .summary-label {
            font-size: 12px;
            color: #777;
            margin-bottom: 5px;
        }

        .summary-value {
            font-size: 15px;
            font-weight: 500;
        }

        .cost-value {
            font-size: 20px;
            font-weight: 700;
        }

        .tracking-box {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 20px;
            padding: 16px;
            border: 1px solid #e5e5e5;
            border-radius: 8px;
        }

        .tracking-number {
            font-size: 16px;
            font-weight: 600;
        }

        .address-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 24px;
        }

        .address-box {
            padding: 15px;
            border: 1px solid #e5e5e5;
            border-radius: 8px;
            background: #fafafa;
        }

        .address-box h4 {
            margin: 0 0 8px;
            font-size: 13px;
            color: #666;
        }

        .address-box p {
            margin: 0;
            line-height: 1.5;
        }

        .package-number {
            font-weight: 600;
        }

        .empty-message {
            text-align: center;
            padding: 25px;
            color: #777;
        }

        @media (max-width: 800px) {
            .summary-grid {
                grid-template-columns: 1fr 1fr;
            }

            .address-grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>

<body>

<div class="app-container">

    <header class="topbar">
        <div class="logo-section">
            <img
                src="/erpflow/images/erpflow-logo.png"
                alt="ERPFlow"
                class="logo">
        </div>

        <nav>
            <a href="/erpflow/home.jsp">Dashboard</a>
            <a href="/erpflow/items.jsp">Items</a>
            <a href="/erpflow/customers.jsp">Customers</a>
            <a href="/erpflow/salesOrders.jsp">Sales Orders</a>
            <a href="/erpflow/shipments.jsp">Shipments</a>
        </nav>
    </header>


    <main class="main-content">

        <!-- HEADER -->
        <div class="shipment-header">

            <div class="shipment-title">

                <h1 id="shipmentNumber">
                    Shipment
                </h1>

                <span
                    id="shipmentStatus"
                    class="status-badge status-default">
                    -
                </span>

            </div>

            <button
                type="button"
                class="btn btn-secondary"
                onclick="goBack()">
                Back
            </button>

        </div>


        <!-- SUMMARY -->
        <section class="card">

            <div class="summary-grid">

                <div class="summary-item">
                    <div class="summary-label">
                        Sales Order
                    </div>

                    <div class="summary-value">
                        <a id="salesOrderLink" href="#">
                            -
                        </a>
                    </div>
                </div>


                <div class="summary-item">
                    <div class="summary-label">
                        Customer
                    </div>

                    <div
                        id="customerName"
                        class="summary-value">
                        -
                    </div>
                </div>


                <div class="summary-item">
                    <div class="summary-label">
                        Shipment Date
                    </div>

                    <div
                        id="shipmentDate"
                        class="summary-value">
                        -
                    </div>
                </div>


                <div class="summary-item">
                    <div class="summary-label">
                        Carrier
                    </div>

                    <div
                        id="carrierName"
                        class="summary-value">
                        -
                    </div>
                </div>


                <div class="summary-item">
                    <div class="summary-label">
                        Service
                    </div>

                    <div
                        id="carrierService"
                        class="summary-value">
                        -
                    </div>
                </div>


                <div class="summary-item">
                    <div class="summary-label">
                        Shipping Charge
                    </div>

                    <div
                        id="shippingCharge"
                        class="summary-value cost-value">
                        ₹0.00
                    </div>
                </div>


                <div class="summary-item">
                    <div class="summary-label">
                        Estimated Delivery
                    </div>

                    <div
                        id="estimatedDeliveryDate"
                        class="summary-value">
                        -
                    </div>
                </div>

            </div>

        </section>


        <!-- PACKAGES -->
        <section class="card">

            <h2>Packages</h2>

            <div class="table-container">

                <table class="data-table">

                    <thead>
                    <tr>
                        <th>Package</th>
                        <th>Weight</th>
                        <th>Dimensions</th>
                        <th>Status</th>
                    </tr>
                    </thead>

                    <tbody id="packagesTableBody">

                    <tr>
                        <td colspan="4">
                            Loading...
                        </td>
                    </tr>

                    </tbody>

                </table>

            </div>

        </section>


        <!-- TRACKING -->
        <section class="card">

            <h2>Tracking</h2>

            <div class="tracking-box">

                <div>

                    <div class="summary-label">
                        Tracking Number
                    </div>

                    <div
                        id="trackingNumber"
                        class="tracking-number">
                        -
                    </div>

                </div>

                <a
                    id="trackingLink"
                    href="#"
                    target="_blank"
                    class="btn btn-primary"
                    style="display:none;">
                    Track Shipment
                </a>

            </div>

        </section>


        <!-- ADDRESSES -->
        <section class="card">

            <h2>Delivery Information</h2>

            <div class="address-grid">

                <div class="address-box">

                    <h4>Dispatch From</h4>

                    <p id="dispatchAddress">
                        -
                    </p>

                </div>


                <div class="address-box">

                    <h4>Deliver To</h4>

                    <p id="destinationAddress">
                        -
                    </p>

                </div>

            </div>

        </section>


    </main>

</div>


<script src="/erpflow/js/shipmentDetails.js?v=5" defer></script>

</body>
</html>