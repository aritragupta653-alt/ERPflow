<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Shipment Details - ERPFlow</title>

    <link rel="stylesheet" href="/erpflow/css/app.css">

    <style>

        /* =====================================================
           SHIPMENT HEADER
           ===================================================== */

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

        .shipment-title h1 {
            margin: 0;
        }


        /* =====================================================
           STATUS
           ===================================================== */

        .status-badge {
            display: inline-flex;
            align-items: center;
            justify-content: center;

            padding: 5px 12px;

            border-radius: 20px;

            font-size: 13px;
            font-weight: 600;

            white-space: nowrap;
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

        .status-in-transit {
            background: #fff4d6;
            color: #8a6100;
        }

        .status-default {
            background: #f1f3f5;
            color: #555;
        }


        /* =====================================================
           SUMMARY
           ===================================================== */

        .summary-grid {
            display: grid;

            grid-template-columns:
                repeat(3, minmax(0, 1fr));

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


        /* =====================================================
           FORM
           ===================================================== */

        .form-group {
            display: flex;
            flex-direction: column;
            gap: 6px;

            margin-bottom: 14px;
        }

        .form-group label {
            font-size: 13px;
            font-weight: 600;
        }

        .form-control {
            width: 100%;

            padding: 10px 12px;

            border: 1px solid #d5d5d5;

            border-radius: 6px;

            font: inherit;

            box-sizing: border-box;

            background: #fff;
        }

        .form-control:focus {
            outline: none;

            border-color: #4f7cff;

            box-shadow:
                0 0 0 2px
                rgba(79, 124, 255, 0.12);
        }

        select.form-control {
            cursor: pointer;
        }

        textarea.form-control {
            resize: vertical;
        }


        /* =====================================================
           EDIT SHIPMENT
           ===================================================== */

        #editShipmentSection {
            margin-top: 22px;
        }

        #editShipmentSection h2 {
            margin-top: 0;
            margin-bottom: 20px;
        }

        .edit-shipment-grid {
            display: grid;

            grid-template-columns:
                repeat(3, minmax(0, 1fr));

            gap: 18px;
        }

        .edit-shipment-grid .form-group {
            min-width: 0;
        }

        .full-width {
            grid-column: 1 / -1;
        }


        /* =====================================================
           CARRIER DROPDOWNS
           ===================================================== */

        #editCarrier,
        #editCarrierService {
            width: 100%;

            min-height: 42px;

            padding: 10px 12px;

            border: 1px solid #d5d5d5;

            border-radius: 6px;

            background: #fff;

            font: inherit;

            cursor: pointer;

            box-sizing: border-box;
        }

        #editCarrier:focus,
        #editCarrierService:focus {
            outline: none;

            border-color: #4f7cff;

            box-shadow:
                0 0 0 2px
                rgba(79, 124, 255, 0.12);
        }

        #editCarrierService:disabled {
            background: #f5f5f5;

            color: #777;

            cursor: not-allowed;
        }


        /* =====================================================
           DELIVERY CHECKBOX
           ===================================================== */

        .delivery-checkbox-group {
            margin-top: 0;

            padding: 14px 16px;

            border: 1px solid #ddd;

            border-radius: 8px;

            background: #fafafa;

            justify-content: center;
        }

        .checkbox-label {
            display: flex;

            align-items: center;

            gap: 9px;

            font-weight: 600;

            cursor: pointer;
        }

        .checkbox-label input[type="checkbox"] {
            width: 18px;
            height: 18px;

            margin: 0;

            cursor: pointer;
        }

        .field-hint {
            display: block;

            margin-top: 5px;

            color: #666;

            font-size: 12px;

            line-height: 1.4;
        }


        /* =====================================================
           DATE INPUTS
           ===================================================== */

        input[type="date"] {
            cursor: pointer;
        }


        /* =====================================================
           TRACKING
           ===================================================== */

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


        /* =====================================================
           ADDRESSES
           ===================================================== */

        .address-grid {
            display: grid;

            grid-template-columns:
                repeat(2, minmax(0, 1fr));

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


        /* =====================================================
           PACKAGES
           ===================================================== */

        .package-number {
            font-weight: 600;
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

        .package-choice input {
            width: auto;
        }

        .empty-message {
            text-align: center;

            padding: 18px;

            color: #777;
        }


        /* =====================================================
           ACTIONS
           ===================================================== */

        .shipment-actions {
            display: flex;

            align-items: center;

            gap: 10px;

            flex-wrap: wrap;
        }

        .section-actions {
            display: flex;

            gap: 12px;

            flex-wrap: wrap;

            margin-top: 18px;
        }

        button:disabled {
            opacity: 0.6;

            cursor: not-allowed;
        }


        /* =====================================================
           RESPONSIVE
           ===================================================== */

        @media (max-width: 900px) {

            .edit-shipment-grid {
                grid-template-columns:
                    repeat(2, minmax(0, 1fr));
            }

            .summary-grid {
                grid-template-columns:
                    repeat(2, minmax(0, 1fr));
            }

        }


        @media (max-width: 650px) {

            .edit-shipment-grid {
                grid-template-columns: 1fr;
            }

            .summary-grid {
                grid-template-columns: 1fr;
            }

            .address-grid {
                grid-template-columns: 1fr;
            }

            .tracking-box {
                flex-direction: column;

                align-items: flex-start;
            }

        }

    </style>
</head>


<body>

<div class="app-container">


    <!-- =====================================================
         TOP NAVIGATION
         ===================================================== -->

    <header class="topbar">

        <nav>

            <a href="/erpflow/home.jsp">
                Dashboard
            </a>

            <a href="/erpflow/items.jsp">
                Items
            </a>

            <a href="/erpflow/customers.jsp">
                Customers
            </a>

            <a href="/erpflow/salesOrders.jsp">
                Sales Orders
            </a>

            <a href="/erpflow/shipments.jsp">
                Shipments
            </a>

        </nav>

    </header>


    <!-- =====================================================
         MAIN CONTENT
         ===================================================== -->

    <main class="main-content">


        <!-- =================================================
             SHIPMENT HEADER
             ================================================= -->

        <div class="shipment-header">

            <div class="shipment-title">

                <h1 id="shipmentNumber">
                    Shipment
                </h1>

                <span
                    id="shipmentStatus"
                    class="status-badge status-default"
                >
                    -
                </span>

            </div>


            <div class="shipment-actions">

                <button
                    type="button"
                    id="editShipmentButton"
                    class="btn btn-secondary"
                >
                    Edit Shipment
                </button>


                <button
                    type="button"
                    class="btn btn-secondary"
                    onclick="goBack()"
                >
                    Back
                </button>

            </div>

        </div>


        <!-- =================================================
             SHIPMENT SUMMARY
             ================================================= -->

        <section class="card">

            <div class="summary-grid">


                <div class="summary-item">

                    <div class="summary-label">
                        Sales Order
                    </div>

                    <div class="summary-value">

                        <a
                            id="salesOrderLink"
                            href="#"
                        >
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
                        class="summary-value"
                    >
                        -
                    </div>

                </div>


                <div class="summary-item">

                    <div class="summary-label">
                        Shipment Date
                    </div>

                    <div
                        id="shipmentDate"
                        class="summary-value"
                    >
                        -
                    </div>

                </div>


                <div class="summary-item">

                    <div class="summary-label">
                        Carrier
                    </div>

                    <div
                        id="carrierName"
                        class="summary-value"
                    >
                        -
                    </div>

                </div>


                <div class="summary-item">

                    <div class="summary-label">
                        Service
                    </div>

                    <div
                        id="carrierService"
                        class="summary-value"
                    >
                        -
                    </div>

                </div>


                <div class="summary-item">

                    <div class="summary-label">
                        Shipping Charge
                    </div>

                    <div
                        id="shippingCharge"
                        class="summary-value cost-value"
                    >
                        ₹0.00
                    </div>

                </div>


                <div class="summary-item">

                    <div class="summary-label">
                        Estimated Delivery
                    </div>

                    <div
                        id="estimatedDeliveryDate"
                        class="summary-value"
                    >
                        -
                    </div>

                </div>


                <div class="summary-item">

                    <div class="summary-label">
                        Actual Delivery
                    </div>

                    <div
                        id="actualDeliveryDate"
                        class="summary-value"
                    >
                        -
                    </div>

                </div>


            </div>

        </section>


        <!-- =================================================
             EDIT SHIPMENT
             ================================================= -->

        <section
            class="card"
            id="editShipmentSection"
            style="display:none;"
        >

            <h2>
                Edit Shipment
            </h2>


            <form id="editShipmentForm">


                <!-- =========================================
                     BASIC SHIPMENT INFORMATION
                     ========================================= -->

                <div class="edit-shipment-grid">


                    <!-- SHIPMENT DATE -->

                    <div class="form-group">

                        <label for="editShipmentDate">
                            Shipment Date
                        </label>

                        <input
                            type="date"
                            id="editShipmentDate"
                            class="form-control"
                            required
                        >

                    </div>


                    <!-- STATUS -->

                    <div class="form-group">

                        <label for="editShipmentStatus">
                            Status
                        </label>

                        <select
                            id="editShipmentStatus"
                            class="form-control"
                        >

                            <option value="CREATED">
                                Created
                            </option>

                            <option value="SHIPPED">
                                Shipped
                            </option>

                            <option value="IN_TRANSIT">
                                In Transit
                            </option>

                            <option value="DELIVERED">
                                Delivered
                            </option>

                        </select>

                    </div>


                    <!-- MARK AS DELIVERED -->

                    <div class="form-group delivery-checkbox-group">

                        <label
                            class="checkbox-label"
                            for="markAsDeliveredCheckbox"
                        >

                            <input
                                type="checkbox"
                                id="markAsDeliveredCheckbox"
                            >

                            <span>
                                Mark as Delivered
                            </span>

                        </label>


                        <small class="field-hint">

                            Checking this will set the
                            shipment status to
                            <strong>DELIVERED</strong>
                            and require an actual delivery date.

                        </small>

                    </div>


                    <!-- SHIPPING METHOD -->

                    <div class="form-group">

                        <label for="editShippingMethod">
                            Shipping Method
                        </label>

                        <input
                            type="text"
                            id="editShippingMethod"
                            class="form-control"
                        >

                    </div>


                    <!-- SHIPPING CHARGE -->

                    <div class="form-group">

                        <label for="editShippingCharge">
                            Shipping Charge
                        </label>

                        <input
                            type="number"
                            min="0"
                            step="0.01"
                            id="editShippingCharge"
                            class="form-control"
                        >

                    </div>


                    <!-- CARRIER -->

                    <div class="form-group">

                        <label for="editCarrier">
                            Carrier
                        </label>

                        <select
                            id="editCarrier"
                            class="form-control"
                        >

                            <option value="">
                                Select Carrier
                            </option>

                        </select>

                    </div>


                    <!-- CARRIER SERVICE -->

                    <div class="form-group">

                        <label for="editCarrierService">
                            Carrier Service
                        </label>

                        <select
                            id="editCarrierService"
                            class="form-control"
                            disabled
                        >

                            <option value="">
                                Select Carrier Service
                            </option>

                        </select>

                    </div>


                    <!-- TRACKING NUMBER -->

                    <div class="form-group">

                        <label for="editTrackingNumber">
                            Tracking Number
                        </label>

                        <input
                            type="text"
                            id="editTrackingNumber"
                            class="form-control"
                        >

                    </div>


                    <!-- TRACKING URL -->

                    <div class="form-group">

                        <label for="editTrackingUrl">
                            Tracking URL
                        </label>

                        <input
                            type="url"
                            id="editTrackingUrl"
                            class="form-control"
                        >

                    </div>


                    <!-- ESTIMATED DELIVERY -->

                    <div class="form-group">

                        <label for="editEstimatedDeliveryDate">
                            Estimated Delivery
                        </label>

                        <input
                            type="date"
                            id="editEstimatedDeliveryDate"
                            class="form-control"
                        >

                    </div>


                    <!-- ACTUAL DELIVERY -->

                    <div class="form-group">

                        <label for="editActualDeliveryDate">
                            Actual Delivery
                        </label>

                        <input
                            type="date"
                            id="editActualDeliveryDate"
                            class="form-control"
                        >

                    </div>


                </div>


                <!-- =========================================
                     ADDRESSES
                     ========================================= -->

                <div
                    class="address-grid"
                    style="margin-top:18px;"
                >


                    <div class="form-group">

                        <label for="editDispatchAddress">
                            Dispatch Address
                        </label>

                        <textarea
                            id="editDispatchAddress"
                            class="form-control"
                            rows="4"
                        ></textarea>

                    </div>


                    <div class="form-group">

                        <label for="editDestinationAddress">
                            Destination Address
                        </label>

                        <textarea
                            id="editDestinationAddress"
                            class="form-control"
                            rows="4"
                        ></textarea>

                    </div>


                </div>


                <!-- =========================================
                     NOTES
                     ========================================= -->

                <div
                    class="form-group"
                    style="margin-top:18px;"
                >

                    <label for="editShipmentNotes">
                        Notes
                    </label>

                    <textarea
                        id="editShipmentNotes"
                        class="form-control"
                        rows="3"
                    ></textarea>

                </div>


                <!-- =========================================
                     MESSAGE
                     ========================================= -->

                <div
                    id="editShipmentMessage"
                    class="empty-message"
                    role="status"
                ></div>


                <!-- =========================================
                     ACTIONS
                     ========================================= -->

                <div class="section-actions">

                    <button
                        type="submit"
                        id="saveShipmentButton"
                        class="btn btn-primary"
                    >
                        Save Shipment
                    </button>


                    <button
                        type="button"
                        id="cancelEditShipmentButton"
                        class="btn btn-secondary"
                    >
                        Cancel
                    </button>

                </div>


            </form>

        </section>


        <!-- =================================================
             PACKAGES
             ================================================= -->

        <section class="card">

            <div class="shipment-header">

                <h2>
                    Packages
                </h2>


                <button
                    type="button"
                    id="editPackagesButton"
                    class="btn btn-secondary"
                >
                    Edit Packages
                </button>

            </div>


            <div class="table-container">

                <table class="data-table">

                    <thead>

                        <tr>

                            <th>
                                Package
                            </th>

                            <th>
                                Weight
                            </th>

                            <th>
                                Dimensions
                            </th>

                            <th>
                                Status
                            </th>

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


            <!-- =============================================
                 PACKAGE EDITOR
                 ============================================= -->

            <div
                id="editPackagesSection"
                style="
                    display:none;
                    margin-top:22px;
                "
            >

                <h3>
                    Manage Shipment Packages
                </h3>


                <p>
                    Select the packages that should belong
                    to this shipment.
                    Unselected packages will be removed
                    from this shipment.
                </p>


                <div
                    id="packageEditMessage"
                    class="empty-message"
                    role="status"
                ></div>


                <div id="availablePackagesContainer">

                    Loading packages...

                </div>


                <div class="section-actions">

                    <button
                        type="button"
                        id="savePackagesButton"
                        class="btn btn-primary"
                    >
                        Save Packages
                    </button>


                    <button
                        type="button"
                        id="cancelEditPackagesButton"
                        class="btn btn-secondary"
                    >
                        Cancel
                    </button>

                </div>

            </div>

        </section>


        <!-- =================================================
             TRACKING
             ================================================= -->

        <section class="card">

            <h2>
                Tracking
            </h2>


            <div class="tracking-box">

                <div>

                    <div class="summary-label">
                        Tracking Number
                    </div>

                    <div
                        id="trackingNumber"
                        class="tracking-number"
                    >
                        -
                    </div>

                </div>


                <a
                    id="trackingLink"
                    href="#"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="btn btn-primary"
                    style="display:none;"
                >
                    Track Shipment
                </a>

            </div>

        </section>


        <!-- =================================================
             DELIVERY INFORMATION
             ================================================= -->

        <section class="card">

            <h2>
                Delivery Information
            </h2>


            <div class="address-grid">


                <div class="address-box">

                    <h4>
                        Dispatch From
                    </h4>

                    <p id="dispatchAddress">
                        -
                    </p>

                </div>


                <div class="address-box">

                    <h4>
                        Deliver To
                    </h4>

                    <p id="destinationAddress">
                        -
                    </p>

                </div>


            </div>

        </section>


    </main>

</div>


<script
    src="/erpflow/js/shipmentDetails.js?v=7"
    defer
></script>

</body>

</html>