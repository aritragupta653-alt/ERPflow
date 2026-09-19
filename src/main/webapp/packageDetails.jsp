<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <title>Package Details - ERPFlow</title>

    <link rel="stylesheet" href="/erpflow/css/app.css">

</head>

<body>

<div class="container">

    <div class="page-header">

        <div>

            <h1 id="packageTitle">
                Package Details
            </h1>

            <p>
                View package information and contents
            </p>

        </div>

        <button class="btn btn-secondary"
                onclick="window.location.href='/erpflow/packages.jsp'">
            ← Back to Packages
        </button>

    </div>


    <!-- Package Information -->

    <div class="card">

        <div class="card-header">

            <h2>Package Information</h2>

        </div>

        <div class="details-grid">

            <div>
                <label>Package Number</label>
                <p id="packageNumber">-</p>
            </div>

            <div>
                <label>Status</label>
                <p id="packageStatus">-</p>
            </div>

            <div>
                <label>Sales Order</label>
                <p id="salesOrder">-</p>
            </div>

            <div>
                <label>Customer</label>
                <p id="customer">-</p>
            </div>

            <div>
                <label>Package Date</label>
                <p id="packageDate">-</p>
            </div>

            <div>
                <label>Weight</label>
                <p id="packageWeight">-</p>
            </div>

            <div>
                <label>Length</label>
                <p id="packageLength">-</p>
            </div>

            <div>
                <label>Width</label>
                <p id="packageWidth">-</p>
            </div>

            <div>
                <label>Height</label>
                <p id="packageHeight">-</p>
            </div>

        </div>

    </div>


    <!-- Package Items -->

    <div class="card">

        <div class="card-header">

            <h2>Package Items</h2>

        </div>

        <div class="table-container">

            <table>

                <thead>

                <tr>
                    <th>Item</th>
                    <th>SKU</th>
                    <th>Quantity</th>
                    <th>Selling Price</th>
                </tr>

                </thead>

                <tbody id="packageItemsBody">

                <tr>
                    <td colspan="4"
                        style="text-align:center;">
                        Loading...
                    </td>
                </tr>

                </tbody>

            </table>

        </div>

    </div>

</div>


<script src="/erpflow/js/packageDetails.js?v=1"
        defer></script>

</body>
</html>