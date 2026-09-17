<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>

<!DOCTYPE html>
<html>

<head>

    <title>ERPFlow - Item Management</title>

    <link
        rel="stylesheet"
        href="${pageContext.request.contextPath}/css/app.css"
    >

    <script>
        const contextPath = "/erpflow";
    </script>

    <script
        src="${pageContext.request.contextPath}/js/items.js"
        defer>
    </script>

</head>


<body>

<!-- ================= NAVBAR ================= -->

<header class="navbar">

    <a
        href="${pageContext.request.contextPath}/home.jsp"
        class="logo"
    >

        <img
            src="${pageContext.request.contextPath}/images/erpflow-logo.png"
            alt="ERPFlow Logo"
        >

        <span>ERPFlow</span>

    </a>


    <div class="nav-links">

        <a href="${pageContext.request.contextPath}/home">
            Dashboard
        </a>

        <a href="${pageContext.request.contextPath}/inventory">
            Inventory
        </a>

    </div>

</header>



<!-- ================= MAIN ================= -->

<main class="container">


    <!-- PAGE HEADER -->

    <div class="page-header">

        <div>

            <p class="page-eyebrow">
                PRODUCT MANAGEMENT
            </p>

            <h1>
                Item Management
            </h1>

            <p class="page-description">
                Create, manage and maintain the products available in ERPFlow.
            </p>

        </div>


        <a
            href="${pageContext.request.contextPath}/inventory.jsp"
            class="button secondary-button"
        >
            Check Inventory
        </a>

    </div>



    <!-- ================= ADD ITEM ================= -->

    <section class="form-card">

        <div class="section-header">

            <div>

                <h2>
                    Add New Item
                </h2>

                <p>
                    Enter the item details to create a new product.
                </p>

            </div>

        </div>


        <form
            id="addItemForm"
            class="item-form"
        >

            <div class="form-grid">


                <!-- NAME -->

                <div class="form-group">

                    <label for="name">
                        Item Name
                    </label>

                    <input
                        type="text"
                        id="name"
                        placeholder="Enter item name"
                        required
                    >

                </div>



                <!-- SKU -->

                <div class="form-group">

                    <label for="sku">
                        SKU
                    </label>

                    <input
                        type="text"
                        id="sku"
                        placeholder="Enter SKU"
                        required
                    >

                </div>



                <!-- PURCHASE PRICE -->

                <div class="form-group">

                    <label for="purchasePrice">
                        Purchase Price
                    </label>

                    <input
                        type="number"
                        id="purchasePrice"
                        min="0"
                        step="0.01"
                        placeholder="0.00"
                        required
                    >

                </div>



                <!-- SELLING PRICE -->

                <div class="form-group">

                    <label for="sellingPrice">
                        Selling Price
                    </label>

                    <input
                        type="number"
                        id="sellingPrice"
                        min="0"
                        step="0.01"
                        placeholder="0.00"
                        required
                    >

                </div>



                <!-- REORDER LEVEL -->

                <div class="form-group">

                    <label for="reorderLevel">
                        Reorder Level
                    </label>

                    <input
                        type="number"
                        id="reorderLevel"
                        min="0"
                        placeholder="Enter reorder level"
                        required
                    >

                </div>



                <!-- DESCRIPTION -->

                <div class="form-group full-width">

                    <label for="description">
                        Description
                    </label>

                    <textarea
                        id="description"
                        rows="4"
                        placeholder="Enter item description"
                    ></textarea>

                </div>

            </div>


            <div class="form-actions">

                <button
                    type="submit"
                    class="button"
                >
                    + Add Item
                </button>

            </div>

        </form>

    </section>



    <!-- ================= ITEM LIST ================= -->

    <section class="table-card">


        <div class="section-header">

            <div>

                <h2>
                    All Items
                </h2>

                <p>
                    View and manage products registered in the system.
                </p>

            </div>


            <div class="item-count">

                <span id="itemCount">
                    0
                </span>

                Items

            </div>

        </div>



        <!-- SEARCH -->

        <div class="search-container">

            <input
                type="text"
                id="searchInput"
                placeholder="Search by item name or SKU..."
            >

        </div>



        <!-- TABLE -->

        <div class="table-wrapper">

            <table class="data-table">

                <thead>

                    <tr>

                        <th>ID</th>

                        <th>Item</th>

                        <th>SKU</th>

                        <th>Description</th>

                        <th>Purchase Price</th>

                        <th>Selling Price</th>

                        <th>Reorder Level</th>

                        <th>Status</th>

                        <th>Actions</th>

                    </tr>

                </thead>


                <tbody id="itemsTableBody">

                    <!-- JavaScript will populate this -->

                </tbody>

            </table>

        </div>


    </section>

</main>



<!-- ================= EDIT MODAL ================= -->

<div
    id="editModal"
    class="modal"
    hidden
>

    <div class="modal">


        <div class="modal-header">

            <div>

                <h2>
                    Edit Item
                </h2>

                <p>
                    Update the item information.
                </p>

            </div>


            <button
                type="button"
                class="modal-close"
                id="closeModalButton"
            >
                ×
            </button>

        </div>



        <form id="editItemForm">

            <input
                type="hidden"
                id="editId"
            >


            <div class="form-grid">


                <div class="form-group">

                    <label for="editName">
                        Item Name
                    </label>

                    <input
                        type="text"
                        id="editName"
                        required
                    >

                </div>



                <div class="form-group">

                    <label for="editSku">
                        SKU
                    </label>

                    <input
                        type="text"
                        id="editSku"
                        required
                    >

                </div>



                <div class="form-group">

                    <label for="editPurchasePrice">
                        Purchase Price
                    </label>

                    <input
                        type="number"
                        id="editPurchasePrice"
                        min="0"
                        step="0.01"
                        required
                    >

                </div>



                <div class="form-group">

                    <label for="editSellingPrice">
                        Selling Price
                    </label>

                    <input
                        type="number"
                        id="editSellingPrice"
                        min="0"
                        step="0.01"
                        required
                    >

                </div>



                <div class="form-group">

                    <label for="editReorderLevel">
                        Reorder Level
                    </label>

                    <input
                        type="number"
                        id="editReorderLevel"
                        min="0"
                        required
                    >

                </div>



                <div class="form-group full-width">

                    <label for="editDescription">
                        Description
                    </label>

                    <textarea
                        id="editDescription"
                        rows="4"
                    ></textarea>

                </div>

            </div>



            <div class="modal-actions">

                <button
                    type="button"
                    class="button secondary-button"
                    id="cancelEditButton"
                >
                    Cancel
                </button>


                <button
                    type="submit"
                    class="button"
                >
                    Save Changes
                </button>

            </div>

        </form>

    </div>

</div>



<!-- ================= MESSAGE ================= -->

<div
    id="messageBox"
    class="message-box"
    hidden
></div>


</body>

</html>