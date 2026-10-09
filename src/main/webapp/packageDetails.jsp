
<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Package Details - ERPFlow</title>

    <!-- Keep the existing ERPFlow stylesheet -->
    <link rel="stylesheet" href="/erpflow/css/app.css">

    <style>
        /* Compact layout adjustments only.
           Fonts, colors, buttons and global styles remain in app.css. */

        .package-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
            margin-bottom: 20px;
        }

        .package-header h1 {
            margin-bottom: 8px;
        }

        .package-header p {
            margin: 0;
        }

        .package-info-grid {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 18px 28px;
        }

        .package-info-field {
            min-width: 0;
        }

        .package-info-field label {
            display: block;
            margin-bottom: 6px;
        }

        .package-info-field p {
            margin: 0;
            overflow-wrap: anywhere;
        }

        .package-measurements {
            display: grid;
            grid-template-columns: repeat(4, minmax(0, 1fr));
            gap: 14px;
        }

        .package-measurement {
            padding: 14px 16px;
            border: 1px solid var(--border);
            border-radius: var(--radius);
            background: #f8fafc;
        }

        .package-measurement label {
            display: block;
            margin-bottom: 6px;
        }

        .package-measurement p {
            margin: 0;
        }

        .package-section-heading {
            margin: 0 0 16px;
        }

        .package-table-container {
            width: 100%;
            overflow-x: auto;
        }

        .package-table {
            width: 100%;
            min-width: 650px;
        }

        .package-table td,
        .package-table th {
            white-space: nowrap;
        }

        .package-table .item-name-cell {
            white-space: normal;
            min-width: 150px;
        }

        .package-table .item-id-cell {
            color: var(--primary);
            font-weight: 600;
        }

        .package-empty-row {
            text-align: center;
            padding: 24px;
        }

        .package-status {
            display: inline-flex;
            align-items: center;
            gap: 7px;
        }

        .package-status::before {
            content: "";
            width: 8px;
            height: 8px;
            display: inline-block;
            border-radius: 50%;
            background: #94a3b8;
        }

        .package-status[data-status="packed"]::before,
        .package-status[data-status="shipped"]::before,
        .package-status[data-status="delivered"]::before {
            background: var(--success);
        }

        .package-status[data-status="pending"]::before,
        .package-status[data-status="draft"]::before {
            background: var(--warning);
        }

        .package-error {
            color: var(--danger);
        }

        @media (max-width: 800px) {
            .package-info-grid {
                grid-template-columns: repeat(2, minmax(0, 1fr));
            }

            .package-measurements {
                grid-template-columns: repeat(2, minmax(0, 1fr));
            }
        }

        @media (max-width: 550px) {
            .package-header {
                align-items: flex-start;
                flex-direction: column;
            }

            .package-info-grid {
                grid-template-columns: 1fr;
                gap: 14px;
            }

            .package-measurements {
                grid-template-columns: repeat(2, minmax(0, 1fr));
                gap: 10px;
            }
        }
    </style>
</head>

<body>

<!-- Same navbar structure as the existing ERPFlow pages -->
<header class="navbar">

    <a href="${pageContext.request.contextPath}/home.jsp" class="logo">
        <img
            src="${pageContext.request.contextPath}/images/erpflow-logo.png"
            alt="ERPFlow Logo">
        <span>ERPFlow</span>
    </a>

</header>

<div class="container">

    <!-- Page heading -->
    <div class="package-header">

        <div class="page-header">
            <div>
                <h1 id="packageTitle">Package Details</h1>
                <p>View package information and contents.</p>
            </div>
        </div>

        <a href="${pageContext.request.contextPath}/packages.jsp"
           class="button">
            ← Back to Packages
        </a>

    </div>

    <!-- Package information -->
    <section class="card">

        <h2 class="package-section-heading">Package Information</h2>

        <div class="package-info-grid">

            <div class="package-info-field">
                <label>Package Number</label>
                <p id="packageNumber">-</p>
            </div>

            <div class="package-info-field">
                <label>Status</label>
                <p id="packageStatus" class="package-status">-</p>
            </div>

            <div class="package-info-field">
                <label>Sales Order</label>
                <p id="salesOrder">-</p>
            </div>

            <div class="package-info-field">
                <label>Customer</label>
                <p id="customer">-</p>
            </div>

            <div class="package-info-field">
                <label>Package Date</label>
                <p id="packageDate">-</p>
            </div>

        </div>

    </section>

    <!-- Package dimensions -->
    <section class="card">

        <h2 class="package-section-heading">Package Dimensions</h2>

        <div class="package-measurements">

            <div class="package-measurement">
                <label>Weight</label>
                <p id="packageWeight">-</p>
            </div>

            <div class="package-measurement">
                <label>Length</label>
                <p id="packageLength">-</p>
            </div>

            <div class="package-measurement">
                <label>Width</label>
                <p id="packageWidth">-</p>
            </div>

            <div class="package-measurement">
                <label>Height</label>
                <p id="packageHeight">-</p>
            </div>

        </div>

    </section>

    <!-- Package items -->
    <section class="card">

        <h2 class="package-section-heading">Package Items</h2>

        <div class="package-table-container">

            <table class="package-table">

                <thead>
                <tr>
                    <th>Item ID</th>
                    <th>Item</th>
                    <th>SKU</th>
                    <th>Sales Order Line ID</th>
                    <th>Quantity</th>
                    <th>Item Selling Price</th>
                </tr>
                </thead>

                <tbody id="packageItemsBody">
                <tr>
                    <td colspan="6" class="package-empty-row">
                        Loading package items...
                    </td>
                </tr>
                </tbody>

            </table>

        </div>

    </section>

</div>

<script src="/erpflow/js/packageDetails.js?v=1" defer></script>


</body>
</html>



