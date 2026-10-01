<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta
            name="viewport"
            content="width=device-width, initial-scale=1.0"
    >

    <title>Inventory Alert Configuration</title>

    <style>

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family:
                    Inter,
                    -apple-system,
                    BlinkMacSystemFont,
                    "Segoe UI",
                    sans-serif;
            background: #f6f7fb;
            color: #1f2937;
        }

        .page {
            max-width: 1000px;
            margin: 0 auto;
            padding: 32px;
        }

        .page-header {
            margin-bottom: 28px;
        }

        .page-title {
            margin: 0;
            font-size: 28px;
            font-weight: 700;
        }

        .page-description {
            margin-top: 8px;
            color: #6b7280;
            font-size: 14px;
        }

        .card {
            background: #ffffff;
            border: 1px solid #e5e7eb;
            border-radius: 12px;
            padding: 24px;
            margin-bottom: 20px;
            box-shadow:
                    0 2px 8px rgba(0, 0, 0, 0.04);
        }

        .card-title {
            margin: 0;
            font-size: 18px;
            font-weight: 650;
        }

        .card-description {
            margin: 6px 0 20px;
            font-size: 13px;
            color: #6b7280;
        }

        .setting-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 20px;
            padding: 18px 0;
            border-bottom: 1px solid #f0f1f3;
        }

        .setting-row:last-child {
            border-bottom: none;
        }

        .setting-info {
            flex: 1;
        }

        .setting-name {
            font-size: 15px;
            font-weight: 600;
        }

        .setting-description {
            margin-top: 4px;
            font-size: 13px;
            color: #6b7280;
        }

        .switch {
            position: relative;
            width: 48px;
            height: 26px;
            flex-shrink: 0;
        }

        .switch input {
            opacity: 0;
            width: 0;
            height: 0;
        }

        .slider {
            position: absolute;
            cursor: pointer;
            inset: 0;
            background: #d1d5db;
            border-radius: 999px;
            transition: 0.2s;
        }

        .slider::before {
            content: "";
            position: absolute;
            width: 20px;
            height: 20px;
            left: 3px;
            top: 3px;
            background: #ffffff;
            border-radius: 50%;
            transition: 0.2s;
            box-shadow:
                    0 1px 3px rgba(0, 0, 0, 0.2);
        }

        .switch input:checked + .slider {
            background: #2563eb;
        }

        .switch input:checked + .slider::before {
            transform: translateX(22px);
        }

        .select-group {
            display: grid;
            grid-template-columns:
                    repeat(2, minmax(0, 1fr));
            gap: 20px;
        }

        .field {
            display: flex;
            flex-direction: column;
            gap: 7px;
        }

        .field label {
            font-size: 13px;
            font-weight: 600;
        }

        .field select {
            width: 100%;
            height: 42px;
            padding: 0 12px;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            background: #ffffff;
            color: #111827;
            font-size: 14px;
        }

        .field select:focus {
            outline: none;
            border-color: #2563eb;
            box-shadow:
                    0 0 0 3px rgba(37, 99, 235, 0.12);
        }

        .actions {
            display: flex;
            justify-content: flex-end;
            align-items: center;
            gap: 12px;
            margin-top: 24px;
        }

        .button {
            border: none;
            border-radius: 8px;
            padding: 10px 18px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
        }

        .button-primary {
            background: #2563eb;
            color: #ffffff;
        }

        .button-primary:hover {
            background: #1d4ed8;
        }

        .button-primary:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }

        .message {
            display: none;
            margin-bottom: 18px;
            padding: 12px 14px;
            border-radius: 8px;
            font-size: 13px;
        }

        .message.success {
            display: block;
            background: #ecfdf5;
            color: #047857;
            border: 1px solid #a7f3d0;
        }

        .message.error {
            display: block;
            background: #fef2f2;
            color: #b91c1c;
            border: 1px solid #fecaca;
        }

        .updated-at {
            margin-top: 18px;
            color: #9ca3af;
            font-size: 12px;
        }

        @media (max-width: 700px) {

            .page {
                padding: 20px;
            }

            .select-group {
                grid-template-columns: 1fr;
            }

            .setting-row {
                align-items: flex-start;
            }
        }

    </style>

</head>


<body>

<div class="page">

    <div class="page-header">

        <h1 class="page-title">
            Inventory Alert Configuration
        </h1>

        <p class="page-description">
            Configure which inventory conditions should generate
            notifications in ERPFlow.
        </p>

    </div>


    <div
            id="configurationMessage"
            class="message">
    </div>


    <!-- ========================================= -->
    <!-- STOCK ALERTS -->
    <!-- ========================================= -->

    <div class="card">

        <h2 class="card-title">
            Stock Alerts
        </h2>

        <p class="card-description">
            Enable or disable individual inventory alert types.
        </p>


        <!-- LOW STOCK -->

        <div class="setting-row">

            <div class="setting-info">

                <div class="setting-name">
                    Low Stock Alert
                </div>

                <div class="setting-description">
                    Notify when available stock reaches or falls
                    below the item's reorder level.
                </div>

            </div>

            <label class="switch">

                <input
                        type="checkbox"
                        id="lowStockEnabled"
                >

                <span class="slider"></span>

            </label>

        </div>


        <!-- OUT OF STOCK -->

        <div class="setting-row">

            <div class="setting-info">

                <div class="setting-name">
                    Out of Stock Alert
                </div>

                <div class="setting-description">
                    Notify when available stock reaches zero
                    or becomes negative.
                </div>

            </div>

            <label class="switch">

                <input
                        type="checkbox"
                        id="outOfStockEnabled"
                >

                <span class="slider"></span>

            </label>

        </div>


        <!-- REPLENISHMENT -->

        <div class="setting-row">

            <div class="setting-info">

                <div class="setting-name">
                    Replenishment Alert
                </div>

                <div class="setting-description">
                    Notify when stock has been replenished after
                    a previous low-stock condition.
                </div>

            </div>

            <label class="switch">

                <input
                        type="checkbox"
                        id="replenishmentEnabled"
                >

                <span class="slider"></span>

            </label>

        </div>


        <!-- OVERSTOCK -->

        <div class="setting-row">

            <div class="setting-info">

                <div class="setting-name">
                    Overstock Alert
                </div>

                <div class="setting-description">
                    Notify when physical stock exceeds the
                    configured maximum stock quantity.
                </div>

            </div>

            <label class="switch">

                <input
                        type="checkbox"
                        id="overstockEnabled"
                >

                <span class="slider"></span>

            </label>

        </div>

    </div>


    <!-- ========================================= -->
    <!-- NOTIFICATION SETTINGS -->
    <!-- ========================================= -->

    <div class="card">

        <h2 class="card-title">
            Notification Settings
        </h2>

        <p class="card-description">
            Configure how inventory alerts are delivered.
        </p>


        <div class="select-group">

            <div class="field">

                <label for="frequency">
                    Frequency
                </label>

                <select id="frequency">

                    <option value="IMMEDIATE">
                        Immediate
                    </option>

                </select>

            </div>


            <div class="field">

                <label for="notificationChannel">
                    Notification Channel
                </label>

                <select id="notificationChannel">

                    <option value="IN_APP">
                        In-App
                    </option>

                </select>

            </div>

        </div>


        <div
                id="updatedAt"
                class="updated-at">
        </div>


        <div class="actions">

            <button
                    type="button"
                    id="saveConfigurationButton"
                    class="button button-primary">

                Save Configuration

            </button>

        </div>

    </div>

</div>


<script src="js/inventoryAlertSettings.js"></script>

</body>

</html>