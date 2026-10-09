
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page isELIgnored="false" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Composite Items | ERPFlow</title>

    <c:set var="contextPath" value="${pageContext.request.contextPath}" />

   <link rel="stylesheet" href="http://localhost:8080/erpflow/css/app.css
">
</head>

<body>

    <%-- Shared ERPFlow navbar --%>
    <nav class="navbar">
        <div class="navbar-brand">
            <a href="${contextPath}/dashboard.jsp">ERPFlow</a>
        </div>

        <div class="navbar-links">
            <a href="${contextPath}/dashboard.jsp">Dashboard</a>
            <a href="${contextPath}/items.jsp">Items</a>
            <a href="${contextPath}/composite-items.jsp"
               class="active">Composite Items</a>
        </div>
    </nav>

    <main class="container">

        <%-- Page header --%>
        <div class="page-header">
            <div>
                <h1>Composite Items</h1>
                <p>Manage composite products and their component items.</p>
            </div>

            <div class="action-buttons">
                <button type="button"
                        class="button button-secondary"
                        id="createAssemblyButton">
                    Create Assembly
                </button>

                <button type="button"
                        class="button button-primary"
                        id="createCompositeButton">
                    + Create Composite Item
                </button>
            </div>
        </div>

        <%-- Notification --%>
        <div id="messageBox"
             class="message-box"
             role="status"
             aria-live="polite"
             hidden></div>

        <%-- Listing card --%>
        <section class="table-card">

            <div class="table-header">
                <div>
                    <h2>Composite Item List</h2>
                    <p>
                        Total composite items:
                        <strong id="compositeItemCount">0</strong>
                    </p>
                </div>

                <div class="search-container">
                    <input type="search"
                           id="searchInput"
                           class="form-control"
                           placeholder="Search by name or SKU..."
                           aria-label="Search composite items">
                </div>
            </div>

            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Composite Name</th>
                            <th>SKU</th>
                            <th>Selling Price</th>
                            <th>Track Inventory</th>
                            <th>Stock</th>
                            <th>Actions</th>
                        </tr>
                    </thead>

                    <tbody id="compositeItemsTableBody">
                        <tr>
                            <td colspan="7" class="empty-state">
                                Loading composite items...
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

        </section>

    </main>

    <%-- Component details modal --%>
    <div id="componentsModal"
         class="modal"
         hidden
         aria-hidden="true">

        <div class="modal-content"
             role="dialog"
             aria-modal="true"
             aria-labelledby="componentsTitle">

            <div class="modal-header">
                <div>
                    <h2 id="componentsTitle">Composite Components</h2>
                    <p>Components and their quantities and prices.</p>
                </div>

                <button type="button"
                        id="closeComponentsButton"
                        class="modal-close"
                        aria-label="Close">
                    &times;
                </button>
            </div>

            <div class="table-responsive">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Component Name</th>
                            <th>SKU</th>
                            <th>Quantity</th>
                            <th>Selling Price</th>
                        </tr>
                    </thead>

                    <tbody id="componentsTableBody">
                        <tr>
                            <td colspan="4" class="empty-state">
                                Select a composite item to view components.
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div class="modal-footer">
                <button type="button"
                        class="button button-secondary"
                        id="closeComponentsFooterButton">
                    Close
                </button>
            </div>

        </div>
    </div>

    <script>
        window.contextPath = "${pageContext.request.contextPath}";
    </script>

   <script src="http://localhost:8080/erpflow/js/composite-items.js"></script>
    

</body>
</html>