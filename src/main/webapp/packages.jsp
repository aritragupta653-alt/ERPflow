<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>

<head>
    <title>ERPFlow - Packages</title>

    <link rel="stylesheet"
          href="/erpflow/css/app.css">
</head>

<body>

<header class="navbar">

    <a href="/erpflow/home.jsp" class="logo">

        <img src="/erpflow/images/erpflow-logo.png"
             alt="ERPFlow Logo">

        <span>ERPFlow</span>

    </a>

</header>


<div class="container">

    <div class="page-header">

        <div>
            <h1>Packages</h1>
            <p>Create and manage sales order packages.</p>
        </div>

        <a href="/erpflow/home.jsp"
           class="btn secondary-btn">
            ← Dashboard
        </a>

    </div>


    <div id="messageBox"
         class="message-box"
         style="display:none;">
    </div>


    <!-- CREATE PACKAGE -->

    <div class="form-section">

        <h2>Create Package</h2>

        <p>Package items from an existing sales order.</p>


        <form id="packageForm">

            <div class="form-grid">

                <div class="form-group">

                    <label>Sales Order</label>

                    <select id="salesOrderSelect"
                            required>

                        <option value="">
                            Loading orders...
                        </option>

                    </select>

                </div>


                <div class="form-group">

                    <label>Weight</label>

                    <input type="number"
                           id="weightInput"
                           min="0"
                           step="0.01"
                           placeholder="Weight"
                           required>

                </div>

            </div>


            <h3>Package Items</h3>

            <div id="packageItems">

                <div class="package-item-row">

                    <select class="item-select"
                            required>

                        <option value="">
                            Select Item
                        </option>

                    </select>

                    <input type="number"
                           class="quantity-input"
                           min="1"
                           placeholder="Quantity"
                           required>

                    <button type="button"
                            class="btn small-btn danger-btn remove-btn">
                        Remove
                    </button>

                </div>

            </div>


            <div class="form-actions">

                <button type="button"
                        id="addItemButton"
                        class="btn secondary-btn">
                    + Add Item
                </button>

                <button type="submit"
                        class="btn primary-btn">
                    Create Package
                </button>

            </div>

        </form>

    </div>


    <!-- PACKAGE LIST -->

    <div class="table-section">

        <div class="section-header">

            <div>

                <h2>Package List</h2>

                <p>
                    <span id="packageCount">0</span>
                    packages
                </p>

            </div>

            <div class="search-box">

                <input type="text"
                       id="searchInput"
                       placeholder="Search packages...">

            </div>

        </div>


        <div class="table-container">

            <table>

                <thead>

                <tr>
                    <th>ID</th>
                    <th>Sales Order</th>
                    <th>Customer</th>
                    <th>Date</th>
                    <th>Weight</th>
                    <th>Status</th>
                    <th>Action</th>
                </tr>

                </thead>

                <tbody id="packagesTableBody">
                </tbody>

            </table>

        </div>

    </div>

</div>


<script>

    const contextPath = "/erpflow";

</script>

<script src="/erpflow/js/packages.js"
        defer></script>

</body>

</html>