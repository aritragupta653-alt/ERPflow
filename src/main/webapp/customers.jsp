<%@ page contentType="text/html;charset=UTF-8" isELIgnored="false" %>

<!DOCTYPE html>
<html>

<head>
    <title>ERPFlow - Customers</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/app.css">
</head>

<body>

<header class="navbar">

    <a href="${pageContext.request.contextPath}/home.jsp"
       class="logo">

        <img src="${pageContext.request.contextPath}/images/erpflow-logo.png"
             alt="ERPFlow Logo">

        <span>ERPFlow</span>

    </a>

</header>

<div class="container">

    <div class="page-header">
        <div>
            <h1>Customers</h1>
            <p>Manage your customers and their information.</p>
        </div>

        <a href="${pageContext.request.contextPath}/home.jsp"
           class="btn secondary-btn">
            ← Back to Dashboard
        </a>
    </div>


    <!-- Message -->

    <div id="messageBox"
         class="message-box"
         style="display: none;">
    </div>


    <!-- Add Customer -->

    <div class="form-section">

        <div class="section-header">
            <div>
                <h2>Add Customer</h2>
                <p>Create a new customer record.</p>
            </div>
        </div>

        <form id="addCustomerForm">

            <div class="form-grid">

                <div class="form-group">
                    <label for="customerName">
                        Customer Name
                    </label>

                    <input type="text"
                           id="customerName"
                           placeholder="Enter customer name"
                           required>
                </div>


                <div class="form-group">
                    <label for="customerEmail">
                        Email
                    </label>

                    <input type="email"
                           id="customerEmail"
                           placeholder="Enter email"
                           required>
                </div>


                <div class="form-group">
                    <label for="customerPhone">
                        Phone
                    </label>

                    <input type="text"
                           id="customerPhone"
                           placeholder="Enter phone number"
                           required>
                </div>


                <div class="form-group">
                    <label for="customerAddress">
                        Address
                    </label>

                    <input type="text"
                           id="customerAddress"
                           placeholder="Enter address"
                           required>
                </div>

            </div>

            <div class="form-actions">

                <button type="submit"
                        class="btn primary-btn">
                    + Add Customer
                </button>

            </div>

        </form>

    </div>


    <!-- Customer List -->

    <div class="table-section">

        <div class="section-header">

            <div>
                <h2>Customer List</h2>
                <p>
                    <span id="customerCount">0</span>
                    customers
                </p>
            </div>

            <div class="search-box">

                <input type="text"
                       id="searchInput"
                       placeholder="Search customers...">

            </div>

        </div>


        <div class="table-container">

            <table>

                <thead>

                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Address</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>

                </thead>

                <tbody id="customersTableBody">

                </tbody>

            </table>

        </div>

    </div>

</div>


<!-- Edit Customer Modal -->

<div id="editModal"
     class="modal">

    <div class="modal-content">

        <div class="modal-header">

            <div>
                <h2>Edit Customer</h2>
                <p>Update customer information.</p>
            </div>

            <button type="button"
                    id="closeModal"
                    class="modal-close">
                ×
            </button>

        </div>


        <form id="editCustomerForm">

            <input type="hidden"
                   id="editId">


            <div class="form-group">

                <label for="editName">
                    Customer Name
                </label>

                <input type="text"
                       id="editName"
                       required>

            </div>


            <div class="form-group">

                <label for="editEmail">
                    Email
                </label>

                <input type="email"
                       id="editEmail"
                       required>

            </div>


            <div class="form-group">

                <label for="editPhone">
                    Phone
                </label>

                <input type="text"
                       id="editPhone"
                       required>

            </div>


            <div class="form-group">

                <label for="editAddress">
                    Address
                </label>

                <input type="text"
                       id="editAddress"
                       required>

            </div>


            <div class="form-group">

                <label for="editStatus">
                    Status
                </label>

                <select id="editStatus">

                    <option value="ACTIVE">
                        ACTIVE
                    </option>

                    <option value="INACTIVE">
                        INACTIVE
                    </option>

                </select>

            </div>


            <div class="modal-actions">

                <button type="button"
                        id="cancelEdit"
                        class="btn secondary-btn">
                    Cancel
                </button>

                <button type="submit"
                        class="btn primary-btn">
                    Save Changes
                </button>

            </div>

        </form>

    </div>

</div>


<script>

    const contextPath =
        "${pageContext.request.contextPath}";

</script>

<script src="${pageContext.request.contextPath}/js/customers.js"
        defer>
</script>

</body>

</html>