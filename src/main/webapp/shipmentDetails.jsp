
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
            gap: 16px;
            flex-wrap: wrap;
        }

        .shipment-title {
            display: flex;
            align-items: center;
            gap: 14px;
        }

        .shipment-title h1 { margin: 0; }

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

        .status-shipped,
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

        .summary-item { padding: 4px 0; }

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
            white-space: pre-wrap;
        }

        .package-number { font-weight: 600; }

        .empty-message {
            text-align: center;
            padding: 18px;
            color: #777;
        }

        .shipment-actions {
            display: flex;
            align-items: center;
            gap: 10px;
            flex-wrap: wrap;
        }

        .form-group {
            display: flex;
            flex-direction: column;
            gap: 6px;
            margin-bottom: 12px;
        }

        .form-control {
            width: 100%;
            padding: 10px;
            border: 1px solid #d5d5d5;
            border-radius: 6px;
            font: inherit;
            box-sizing: border-box;
        }

        .package-choice {
            display: flex;
            gap: 10px;
            align-items: center;
            padding: 12px;
            border: 1px solid #e5e5e5;
            border-radius: 6px;
            margin: 8px 0;
        }

        .package-choice input { width: auto; }

        .section-actions {
            display: flex;
            gap: 12px;
            flex-wrap: wrap;
            margin-top: 16px;
        }

        button:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }

        @media (max-width: 800px) {
            .summary-grid { grid-template-columns: 1fr 1fr; }
            .address-grid { grid-template-columns: 1fr; }
        }

        @media (max-width: 500px) {
            .summary-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>

<body>
<div class="app-container">

    <header class="topbar">
        

        <nav>
            <a href="/erpflow/home.jsp">Dashboard</a>
            <a href="/erpflow/items.jsp">Items</a>
            <a href="/erpflow/customers.jsp">Customers</a>
            <a href="/erpflow/salesOrders.jsp">Sales Orders</a>
            <a href="/erpflow/shipments.jsp">Shipments</a>
        </nav>
    </header>

    <main class="main-content">

        <!-- SHIPMENT HEADER -->
        <div class="shipment-header">
            <div class="shipment-title">
                <h1 id="shipmentNumber">Shipment</h1>
                <span id="shipmentStatus"
                      class="status-badge status-default">-</span>
            </div>

            <div class="shipment-actions">
                <button type="button"
                        id="editShipmentButton"
                        class="btn btn-secondary">
                    Edit Shipment
                </button>

                <button type="button"
                        id="markDeliveredButton"
                        class="btn btn-primary">
                    Mark as Delivered
                </button>

                <button type="button"
                        class="btn btn-secondary"
                        onclick="goBack()">
                    Back
                </button>
            </div>
        </div>

        <!-- SHIPMENT SUMMARY -->
        <section class="card">
            <div class="summary-grid">

                <div class="summary-item">
                    <div class="summary-label">Sales Order</div>
                    <div class="summary-value">
                        <a id="salesOrderLink" href="#">-</a>
                    </div>
                </div>

                <div class="summary-item">
                    <div class="summary-label">Customer</div>
                    <div id="customerName" class="summary-value">-</div>
                </div>

                <div class="summary-item">
                    <div class="summary-label">Shipment Date</div>
                    <div id="shipmentDate" class="summary-value">-</div>
                </div>

                <div class="summary-item">
                    <div class="summary-label">Carrier</div>
                    <div id="carrierName" class="summary-value">-</div>
                </div>

                <div class="summary-item">
                    <div class="summary-label">Service</div>
                    <div id="carrierService" class="summary-value">-</div>
                </div>

                <div class="summary-item">
                    <div class="summary-label">Shipping Charge</div>
                    <div id="shippingCharge"
                         class="summary-value cost-value">₹0.00</div>
                </div>

                <div class="summary-item">
                    <div class="summary-label">Estimated Delivery</div>
                    <div id="estimatedDeliveryDate"
                         class="summary-value">-</div>
                </div>

                <div class="summary-item">
                    <div class="summary-label">Actual Delivery</div>
                    <div id="actualDeliveryDate"
                         class="summary-value">-</div>
                </div>

            </div>
        </section>

        <!-- EDIT SHIPMENT -->
        <section class="card"
                 id="editShipmentSection"
                 style="display:none;">

            <h2>Edit Shipment</h2>

            <form id="editShipmentForm">
                <div class="summary-grid">

                    <div class="form-group">
                        <label for="editShipmentDate">Shipment Date</label>
                        <input type="datetime-local"
                               id="editShipmentDate"
                               class="form-control">
                    </div>

                    <div class="form-group">
                        <label for="editShipmentStatus">Status</label>
                        <select id="editShipmentStatus"
                                class="form-control">
                            <option value="CREATED">Created</option>
                            <option value="SHIPPED">Shipped</option>
                            <option value="DELIVERED">Delivered</option>
                            <option value="IN_TRANSIT">In Transit</option>


                        </select>
                    </div>

                    <div class="form-group">
                        <label for="editShippingMethod">Shipping Method</label>
                        <input id="editShippingMethod"
                               class="form-control">
                    </div>

                    <div class="form-group">
                        <label for="editShippingCharge">Shipping Charge</label>
                        <input type="number"
                               min="0"
                               step="0.01"
                               id="editShippingCharge"
                               class="form-control">
                    </div>

                    <div class="form-group">
                        <label for="editCarrier">Carrier</label>
                        <input id="editCarrier"
                               class="form-control"
                               readonly>
                    </div>

                    <div class="form-group">
                        <label for="editCarrierService">Carrier Service</label>
                        <input id="editCarrierService"
                               class="form-control"
                               readonly>
                    </div>

                    <div class="form-group">
                        <label for="editTrackingNumber">Tracking Number</label>
                        <input id="editTrackingNumber"
                               class="form-control">
                    </div>

                    <div class="form-group">
                        <label for="editTrackingUrl">Tracking URL</label>
                        <input type="url"
                               id="editTrackingUrl"
                               class="form-control">
                    </div>

                    <div class="form-group">
                        <label for="editEstimatedDeliveryDate">
                            Estimated Delivery
                        </label>
                        <input type="date"
                               id="editEstimatedDeliveryDate"
                               class="form-control">
                    </div>

                    <div class="form-group">
                        <label for="editActualDeliveryDate">
                            Actual Delivery
                        </label>
                        <input type="date"
                               id="editActualDeliveryDate"
                               class="form-control">
                    </div>
                </div>

                <div class="address-grid" style="margin-top:18px;">
                    <div class="form-group">
                        <label for="editDispatchAddress">Dispatch Address</label>
                        <textarea id="editDispatchAddress"
                                  class="form-control"
                                  rows="4"></textarea>
                    </div>

                    <div class="form-group">
                        <label for="editDestinationAddress">
                            Destination Address
                        </label>
                        <textarea id="editDestinationAddress"
                                  class="form-control"
                                  rows="4"></textarea>
                    </div>
                </div>

                <div class="form-group" style="margin-top:18px;">
                    <label for="editShipmentNotes">Notes</label>
                    <textarea id="editShipmentNotes"
                              class="form-control"
                              rows="3"></textarea>
                </div>

                <div id="editShipmentMessage"
                     class="empty-message"
                     role="status"></div>

                <div class="section-actions">
                    <button type="submit"
                            id="saveShipmentButton"
                            class="btn btn-primary">
                        Save Shipment
                    </button>

                    <button type="button"
                            id="cancelEditShipmentButton"
                            class="btn btn-secondary">
                        Cancel
                    </button>
                </div>
            </form>
        </section>

        <!-- PACKAGES -->
        <section class="card">
            <div class="shipment-header">
                <h2>Packages</h2>

                <button type="button"
                        id="editPackagesButton"
                        class="btn btn-secondary">
                    Edit Packages
                </button>
            </div>

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
                        <td colspan="4">Loading...</td>
                    </tr>
                    </tbody>
                </table>
            </div>

            <!-- PACKAGE EDITOR -->
            <div id="editPackagesSection"
                 style="display:none; margin-top:22px;">

                <h3>Manage Shipment Packages</h3>

                <p>
                    Select the packages that should belong to this shipment.
                    Unselected packages will be removed from this shipment.
                </p>

                <div id="packageEditMessage"
                     class="empty-message"
                     role="status"></div>

                <div id="availablePackagesContainer">
                    Loading packages...
                </div>

                <div class="section-actions">
                    <button type="button"
                            id="savePackagesButton"
                            class="btn btn-primary">
                        Save Packages
                    </button>

                    <button type="button"
                            id="cancelEditPackagesButton"
                            class="btn btn-secondary">
                        Cancel
                    </button>
                </div>
            </div>
        </section>

        <!-- TRACKING -->
        <section class="card">
            <h2>Tracking</h2>

            <div class="tracking-box">
                <div>
                    <div class="summary-label">Tracking Number</div>
                    <div id="trackingNumber" class="tracking-number">-</div>
                </div>

                <a id="trackingLink"
                   href="#"
                   target="_blank"
                   rel="noopener noreferrer"
                   class="btn btn-primary"
                   style="display:none;">
                    Track Shipment
                </a>
            </div>
        </section>

        <!-- DELIVERY INFORMATION -->
        <section class="card">
            <h2>Delivery Information</h2>

            <div class="address-grid">
                <div class="address-box">
                    <h4>Dispatch From</h4>
                    <p id="dispatchAddress">-</p>
                </div>

                <div class="address-box">
                    <h4>Deliver To</h4>
                    <p id="destinationAddress">-</p>
                </div>
            </div>
        </section>

    </main>
</div>

<script src="/erpflow/js/shipmentDetails.js?v=7" defer></script>
</body>
</html>