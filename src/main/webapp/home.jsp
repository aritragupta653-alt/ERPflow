<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<!DOCTYPE html>

<html lang="en">

<head>


<meta charset="UTF-8">

<meta name="viewport" content="width=device-width, initial-scale=1.0">

<title>ERPFlow - Dashboard</title>

<link
    rel="stylesheet"
    href="${pageContext.request.contextPath}/css/app.css"
>

<style>

    /* =====================================================
       NAVBAR
       ===================================================== */

    .navbar {
        width: 100%;
        height: 70px;

        display: flex;
        align-items: center;

        padding: 0 28px;

        box-sizing: border-box;

        background: #111827;

        border-bottom: 1px solid #1f2937;

        position: relative;

        z-index: 1000;
    }


    /* =====================================================
       LOGO - LEFT
       ===================================================== */

    .navbar .logo {
        display: flex;
        align-items: center;

        gap: 10px;

        text-decoration: none;

        color: #ffffff;

        font-size: 20px;
        font-weight: 700;

        flex-shrink: 0;
    }

    .navbar .logo img {
        width: 38px;
        height: 38px;

        object-fit: contain;
    }

    .navbar .logo span {
        color: #ffffff;

        white-space: nowrap;
    }


    /* =====================================================
       RIGHT SIDE NAVBAR
       ===================================================== */

    .navbar-actions {
        position: absolute;

        top: 0;
        right: 28px;

        height: 70px;

        display: flex;
        align-items: center;
        justify-content: flex-end;
    }


    /* =====================================================
       NOTIFICATION WRAPPER
       ===================================================== */

    .inventory-notification-wrapper {
        position: relative;

        display: flex;
        align-items: center;
        justify-content: center;
    }


    /* =====================================================
       NOTIFICATION BUTTON
       ===================================================== */

    .inventory-notification-button {
        position: relative;

        width: 44px;
        height: 44px;

        display: flex;
        align-items: center;
        justify-content: center;

        padding: 0;

        border: none;
        border-radius: 10px;

        background: #ffffff;

        cursor: pointer;

        box-shadow:
            0 2px 6px rgba(0, 0, 0, 0.15);

        transition:
            background 0.2s ease,
            transform 0.15s ease,
            box-shadow 0.2s ease;
    }

    .inventory-notification-button:hover {
        background: #f3f4f6;

        box-shadow:
            0 4px 10px rgba(0, 0, 0, 0.18);
    }

    .inventory-notification-button:active {
        transform: scale(0.95);
    }


    /* =====================================================
       BELL
       ===================================================== */

    .inventory-notification-bell {
        width: 100%;
        height: 100%;

        display: flex;
        align-items: center;
        justify-content: center;

        font-size: 21px;

        line-height: 1;
    }


    /* =====================================================
       NOTIFICATION BADGE
       ===================================================== */

    .inventory-notification-badge {
        position: absolute;

        top: -2px;
        right: -2px;

        min-width: 19px;
        height: 19px;

        padding: 0 5px;

        display: flex;
        align-items: center;
        justify-content: center;

        box-sizing: border-box;

        border-radius: 999px;

        background: #dc2626;

        color: #ffffff;

        border: 2px solid #111827;

        font-size: 10px;
        font-weight: 700;

        line-height: 1;
    }


    /* =====================================================
       NOTIFICATION DROPDOWN
       ===================================================== */

    .inventory-notification-dropdown {
        position: absolute;

        top: calc(100% + 10px);
        right: 0;

        width: 390px;

        max-width: calc(100vw - 30px);

        background: #ffffff;

        border: 1px solid #e5e7eb;

        border-radius: 14px;

        box-shadow:
            0 18px 45px rgba(0, 0, 0, 0.18);

        overflow: hidden;

        display: none;

        z-index: 99999;
    }

    .inventory-notification-dropdown.show {
        display: block;
    }


    /* =====================================================
       NOTIFICATION HEADER
       ===================================================== */

    .inventory-notification-header {
        min-height: 68px;

        display: flex;
        align-items: center;
        justify-content: space-between;

        gap: 16px;

        padding: 14px 18px;

        box-sizing: border-box;

        background: #ffffff;

        border-bottom: 1px solid #e5e7eb;
    }

    .inventory-notification-heading {
        min-width: 0;
    }

    .inventory-notification-header-title {
        color: #111827;

        font-size: 16px;
        font-weight: 700;

        line-height: 1.3;
    }

    .inventory-notification-header-subtitle {
        margin-top: 3px;

        color: #6b7280;

        font-size: 12px;

        line-height: 1.3;
    }


    /* =====================================================
       MARK ALL READ
       ===================================================== */

    .mark-all-notifications {
        flex-shrink: 0;

        padding: 5px 0;

        border: none;

        background: transparent;

        color: #2563eb;

        font-size: 12px;
        font-weight: 600;

        cursor: pointer;

        white-space: nowrap;
    }

    .mark-all-notifications:hover {
        color: #1d4ed8;

        text-decoration: underline;
    }


    /* =====================================================
       NOTIFICATION LIST
       ===================================================== */

    .inventory-notification-list {
        width: 100%;

        max-height: 430px;

        overflow-y: auto;

        box-sizing: border-box;
    }


    /* =====================================================
       NOTIFICATION ITEM
       ===================================================== */

    .inventory-notification-item {
        width: 100%;

        display: flex;

        align-items: flex-start;

        gap: 12px;

        padding: 15px 18px;

        box-sizing: border-box;

        border-bottom: 1px solid #f3f4f6;

        cursor: pointer;

        transition: background 0.15s ease;
    }

    .inventory-notification-item:hover {
        background: #f9fafb;
    }

    .inventory-notification-item:last-child {
        border-bottom: none;
    }


    /* =====================================================
       NOTIFICATION ICON
       ===================================================== */

    .inventory-notification-icon {
        width: 36px;
        height: 36px;

        flex-shrink: 0;

        display: flex;
        align-items: center;
        justify-content: center;

        border-radius: 50%;

        background: #fef3c7;

        font-size: 15px;
    }


    /* =====================================================
       NOTIFICATION CONTENT
       ===================================================== */

    .inventory-notification-content {
        min-width: 0;

        flex: 1;
    }

    .inventory-notification-title {
        color: #111827;

        font-size: 13px;
        font-weight: 700;

        line-height: 1.3;
    }

    .inventory-notification-message {
        margin-top: 4px;

        color: #4b5563;

        font-size: 13px;

        line-height: 1.45;

        word-break: break-word;
    }

    .inventory-notification-time {
        margin-top: 6px;

        color: #9ca3af;

        font-size: 11px;

        line-height: 1.3;
    }


    /* =====================================================
       LOADING / EMPTY
       ===================================================== */

    .notification-loading,
    .notification-empty {
        min-height: 140px;

        display: flex;
        align-items: center;
        justify-content: center;

        flex-direction: column;

        padding: 25px;

        box-sizing: border-box;

        color: #6b7280;

        font-size: 13px;

        text-align: center;
    }


    /* =====================================================
       SCROLLBAR
       ===================================================== */

    .inventory-notification-list::-webkit-scrollbar {
        width: 6px;
    }

    .inventory-notification-list::-webkit-scrollbar-track {
        background: transparent;
    }

    .inventory-notification-list::-webkit-scrollbar-thumb {
        background: #d1d5db;

        border-radius: 10px;
    }

    .inventory-notification-list::-webkit-scrollbar-thumb:hover {
        background: #9ca3af;
    }


    /* =====================================================
       MOBILE
       ===================================================== */

    @media (max-width: 600px) {

        .navbar {
            padding: 0 15px;
        }

        .navbar-actions {
            right: 15px;
        }

        .inventory-notification-dropdown {
            position: fixed;

            top: 80px;

            left: 15px;
            right: 15px;

            width: auto;

            max-width: none;
        }

    }

</style>
```

</head>

<body>

<!-- =====================================================
     NAVBAR
     ===================================================== -->

<header class="navbar">

```
<!-- =================================================
     LEFT SIDE - LOGO
     ================================================= -->

<a
    href="${pageContext.request.contextPath}/home.jsp"
    class="logo"
>

    <img
        src="${pageContext.request.contextPath}/images/erpflow-logo.png"
        alt="ERPFlow Logo"
    >

    <span>
        ERPFlow
    </span>

</a>


<!-- =================================================
     RIGHT SIDE - NOTIFICATIONS
     ================================================= -->

<div class="navbar-actions">

    <div class="inventory-notification-wrapper">


        <!-- NOTIFICATION BUTTON -->

        <button
            type="button"
            id="inventoryNotificationButton"
            class="inventory-notification-button"
            aria-label="Notifications"
            aria-expanded="false"
        >

            <span class="inventory-notification-bell">
                🔔
            </span>

            <span
                id="inventoryNotificationBadge"
                class="inventory-notification-badge"
                style="display: none;"
            ></span>

        </button>


        <!-- NOTIFICATION DROPDOWN -->

        <div
            id="inventoryNotificationDropdown"
            class="inventory-notification-dropdown"
        >


            <!-- HEADER -->

            <div class="inventory-notification-header">

                <div class="inventory-notification-heading">

                    <div class="inventory-notification-header-title">
                        Notifications
                    </div>

                    <div class="inventory-notification-header-subtitle">
                        Inventory alerts
                    </div>

                </div>


                <button
                    type="button"
                    id="markAllNotificationsRead"
                    class="mark-all-notifications"
                >
                    Mark all as read
                </button>

            </div>


            <!-- NOTIFICATION LIST -->

            <div
                id="inventoryNotificationList"
                class="inventory-notification-list"
            >

                <div class="notification-loading">
                    Loading notifications...
                </div>

            </div>

        </div>

    </div>

</div>
```

</header>

<!-- =====================================================
     DASHBOARD
     ===================================================== -->

<div class="dashboard-container">

```
<!-- =================================================
     DASHBOARD HEADER
     ================================================= -->

<div class="dashboard-header">

    <h1>
        ERPFlow
    </h1>

    <p>
        Enterprise Resource Planning System
    </p>

</div>


<!-- =================================================
     DASHBOARD TITLE
     ================================================= -->

<h2 class="dashboard-title">
    Dashboard
</h2>


<!-- =================================================
     DASHBOARD GRID
     ================================================= -->

<div class="dashboard-grid">


    <!-- =================================================
         ITEMS
         ================================================= -->

    <a
        href="${pageContext.request.contextPath}/items.jsp"
        class="dashboard-card"
    >

        <div class="card-icon">
            📦
        </div>

        <h3>
            Items
        </h3>

        <p>
            Manage products, SKUs and item details.
        </p>

    </a>


    <!-- =================================================
         INVENTORY
         ================================================= -->

    <a
        href="${pageContext.request.contextPath}/inventory.jsp"
        class="dashboard-card"
    >

        <div class="card-icon">
            📊
        </div>

        <h3>
            Inventory
        </h3>

        <p>
            Manage stock levels and inventory operations.
        </p>

    </a>


    <!-- =================================================
         SUPPLIERS
         ================================================= -->

    <a
        href="${pageContext.request.contextPath}/suppliers.jsp"
        class="dashboard-card"
    >

        <div class="card-icon">
            🏭
        </div>

        <h3>
            Suppliers
        </h3>

        <p>
            Manage supplier information and contacts.
        </p>

    </a>


    <!-- =================================================
         CUSTOMERS
         ================================================= -->

    <a
        href="${pageContext.request.contextPath}/customers.jsp"
        class="dashboard-card"
    >

        <div class="card-icon">
            👥
        </div>

        <h3>
            Customers
        </h3>

        <p>
            Manage customer information and accounts.
        </p>

    </a>


    <!-- =================================================
         PURCHASE ORDERS
         ================================================= -->

    <a
        href="${pageContext.request.contextPath}/purchaseOrders.jsp"
        class="dashboard-card"
    >

        <div class="card-icon">
            🛒
        </div>

        <h3>
            Purchase Orders
        </h3>

        <p>
            Create and manage purchase orders.
        </p>

    </a>


    <!-- =================================================
         INVENTORY TRANSACTIONS
         ================================================= -->

    <a
        href="${pageContext.request.contextPath}/inventoryTransactions.jsp"
        class="dashboard-card"
    >

        <div class="card-icon">
            🔄
        </div>

        <h3>
            Transactions
        </h3>

        <p>
            View inventory transaction history.
        </p>

    </a>


    <!-- =================================================
         SALES ORDERS
         ================================================= -->

    <a
        href="${pageContext.request.contextPath}/salesOrders.jsp"
        class="dashboard-card"
    >

        <div class="card-icon">
            🧾
        </div>

        <h3>
            Sales Orders
        </h3>

        <p>
            Create and manage customer sales orders.
        </p>

    </a>


    <!-- =================================================
         SHIPMENTS
         ================================================= -->

    <a
        href="${pageContext.request.contextPath}/shipments.jsp"
        class="dashboard-card"
    >

        <div class="card-icon">
            🚚
        </div>

        <h3>
            Shipments
        </h3>

        <p>
            View shipped packages and shipment details.
        </p>

    </a>


    <!-- =================================================
         PACKAGES
         ================================================= -->

    <a
        href="${pageContext.request.contextPath}/packages.jsp"
        class="dashboard-card"
    >

        <div class="card-icon">
            📦
        </div>

        <h3>
            Packages
        </h3>

        <p>
            View and manage packages.
        </p>

    </a>


    <!-- =================================================
         REPORTS
         ================================================= -->

    <a
        href="${pageContext.request.contextPath}/reports.jsp"
        class="dashboard-card"
    >

        <div class="card-icon">
            📈
        </div>

        <h3>
            Reports
        </h3>

        <p>
            View ERPFlow reports and business insights.
        </p>

    </a>


    <!-- =================================================
         SETTINGS
         ================================================= -->

    <a
        href="${pageContext.request.contextPath}/inventoryAlertSettings.jsp"
        class="dashboard-card"
    >

        <div class="card-icon">
            ⚙️
        </div>

        <h3>
            Settings
        </h3>

        <p>
            Configure inventory alert settings.
        </p>

    </a>


</div>

</div>

<!-- =====================================================
     NOTIFICATION JAVASCRIPT
     ===================================================== -->

<script
    src="${pageContext.request.contextPath}/js/inventoryNotifications.js">
</script>

</body>

</html>
