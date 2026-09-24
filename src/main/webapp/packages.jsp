<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Packages - ERPFlow</title>

    <link rel="stylesheet" href="/erpflow/css/app.css">
</head>

<body>

<div class="container">

    <div class="page-header">
        <div>
            <h1>Packages</h1>
            <p>Manage packages created from sales orders</p>
        </div>

        <button class="btn btn-primary" onclick="openCreatePackage()">
            + Create Package
        </button>
    </div>
    <div class="section-header">
    <div>
        <h2>Sales Orders</h2>
    </div>

    <div class="search-box">
    <select id="statusFilter">
    <option value="ALL">All Statuses</option>
    <option value="PACKING">Packing</option>
    <option value="PACKED">Packed</option>
    <option value="SHIPPED">Shipped</option>
    <option value="CANCELLED">Cancelled</option>
</select>

    </div>
</div>

    <!-- Packages Table -->
    <div class="card">

        <div class="card-header">
            <h2>All Packages</h2>
        </div>

        <div class="table-container">
            <table>
                <thead>
                <tr>
                    <th>Package</th>
                    <th>Sales Order</th>
                    <th>Customer</th>
                    <th>Weight</th>
                    <th>Dimensions</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>
                </thead>

                <tbody id="packagesTableBody">
                <tr>
                    <td colspan="7" style="text-align:center;">
                        Loading packages...
                    </td>
                </tr>
                </tbody>

            </table>
        </div>

    </div>

</div>


<!-- Create Package Modal -->

<div id="createPackageModal" class="modal" style="display:none;">

    <div class="modal-content">

        <div class="modal-header">
            <h2>Create Package</h2>

            <button class="close-btn"
                    onclick="closeCreatePackage()">
                &times;
            </button>
        </div>

        <form id="createPackageForm">

            <!-- Sales Order -->

            <div class="form-group">

                <label for="salesOrderId">
                    Sales Order
                </label>

                <select id="salesOrderId"
                        required
                        onchange="loadSalesOrderItems()">

                    <option value="">
                        Select Sales Order
                    </option>

                </select>

            </div>


            <!-- Order Items -->

            <div class="form-group">

                <label>
                    Items
                </label>

                <div id="orderItemsContainer">

                    <p class="muted">
                        Select a sales order first.
                    </p>

                </div>

            </div>


            <!-- Weight -->

            <div class="form-group">

                <label for="weight">
                    Weight (kg)
                </label>

                <input type="number"
                       id="weight"
                       step="0.01"
                       min="0.01"
                       required>

            </div>


            <!-- Dimensions -->

            <div class="form-row">

                <div class="form-group">

                    <label for="length">
                        Length (cm)
                    </label>

                    <input type="number"
                           id="length"
                           step="0.01"
                           min="0.01"
                           required>

                </div>


                <div class="form-group">

                    <label for="width">
                        Width (cm)
                    </label>

                    <input type="number"
                           id="width"
                           step="0.01"
                           min="0.01"
                           required>

                </div>


                <div class="form-group">

                    <label for="height">
                        Height (cm)
                    </label>

                    <input type="number"
                           id="height"
                           step="0.01"
                           min="0.01"
                           required>

                </div>

            </div>


            <div id="createPackageError"
                 class="error-message"
                 style="display:none;">
            </div>


            <div class="modal-footer">

                <button type="button"
                        class="btn btn-secondary"
                        onclick="closeCreatePackage()">
                    Cancel
                </button>

                <button type="submit"
                        class="btn btn-primary">
                    Create Package
                </button>

            </div>

        </form>

    </div>

</div>


<script src="/erpflow/js/packages.js?v=1" defer></script>

</body>
</html>