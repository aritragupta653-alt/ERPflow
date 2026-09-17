<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>

<head>

    <title>Suppliers - ERPFlow</title>

    <link rel="stylesheet" href="/erpflow/css/app.css">

</head>

<body>

<header class="navbar">

    <a href="/erpflow/home.jsp" class="logo">

        <img
            src="/erpflow/images/erpflow-logo.png"
            alt="ERPFlow Logo"
        >

        <span>ERPFlow</span>

    </a>

</header>


<div class="container">

    <a
        class="back"
        href="/erpflow/home.jsp"
    >
        ← Back to Dashboard
    </a>


    <div class="page-header">

        <div>

            <h1>Suppliers</h1>

            <p>
                Manage your suppliers.
            </p>

        </div>

    </div>


    <div id="messageBox"></div>


    <!-- ADD / EDIT SUPPLIER -->

    <div class="form-section">

        <h2 id="formTitle">
            Add Supplier
        </h2>


        <form id="supplierForm">

            <input
                type="hidden"
                id="supplierId"
            >


            <div class="form-grid">

                <div class="form-group">

                    <label>
                        Supplier Name
                    </label>

                    <input
                        type="text"
                        id="name"
                        required
                        placeholder="Enter supplier name"
                    >

                </div>


                <div class="form-group">

                    <label>
                        Contact Person
                    </label>

                    <input
                        type="text"
                        id="contactPerson"
                        placeholder="Enter contact person"
                    >

                </div>


                <div class="form-group">

                    <label>
                        Phone
                    </label>

                    <input
                        type="text"
                        id="phone"
                        placeholder="Enter phone"
                    >

                </div>


                <div class="form-group">

                    <label>
                        Email
                    </label>

                    <input
                        type="email"
                        id="email"
                        placeholder="Enter email"
                    >

                </div>


                <div class="form-group">

                    <label>
                        Address
                    </label>

                    <input
                        type="text"
                        id="address"
                        placeholder="Enter address"
                    >

                </div>

            </div>


            <div style="margin-top: 20px;">

                <button
                    type="submit"
                    class="btn"
                    id="saveButton"
                >
                    Add Supplier
                </button>


                <button
                    type="button"
                    class="btn"
                    id="cancelButton"
                    style="display: none;"
                >
                    Cancel
                </button>

            </div>

        </form>

    </div>


    <!-- SUPPLIER TABLE -->

    <div class="table-section">

        <div class="section-header">

            <div>

                <h2>
                    Supplier List
                </h2>

                <p>
                    <span id="supplierCount">0</span>
                    suppliers
                </p>

            </div>


            <div class="search-box">

                <input
                    type="text"
                    id="searchInput"
                    placeholder="Search suppliers..."
                >

            </div>

        </div>


        <div class="table-container">

            <table>

                <thead>

                <tr>

                    <th>ID</th>

                    <th>Name</th>

                    <th>Contact Person</th>

                    <th>Phone</th>

                    <th>Email</th>

                    <th>Address</th>

                    <th>Action</th>

                </tr>

                </thead>


                <tbody
                    id="suppliersTableBody"
                >

                </tbody>

            </table>

        </div>

    </div>

</div>


<script
    src="/erpflow/js/suppliers.js"
    defer
></script>

</body>

</html>