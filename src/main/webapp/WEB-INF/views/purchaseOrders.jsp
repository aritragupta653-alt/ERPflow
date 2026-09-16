<%@ page contentType="text/html;charset=UTF-8"
         isELIgnored="false" %>

<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>

<html>

<head>

    <title>ERPFlow - Purchase Orders</title>

    <link
            rel="stylesheet"
            href="${pageContext.request.contextPath}/css/app.css"
    >

</head>


<body>
<header class="navbar">

    <a
            href="${pageContext.request.contextPath}/home"
            class="logo"
    >

        <img
                src="${pageContext.request.contextPath}/images/erpflow-logo.png"
                alt="ERPFlow Logo"
        >

        <span>ERPFlow</span>

    </a>

</header>
<h1>Purchase Order Management</h1>


<!-- CREATE PURCHASE ORDER -->

<h2>Create Purchase Order</h2>


<form class = "po"
        action="${pageContext.request.contextPath}/purchase-orders"
        method="post"
>


    <!-- SELECT SUPPLIER -->

    <label>Supplier:</label>

    <select
            name="supplierId"
            required
    >

        <option value="">
            Select Supplier
        </option>


        <c:forEach
                var="supplier"
                items="${suppliers}"
        >

            <option value="${supplier.id}">

                ${supplier.name}

            </option>

        </c:forEach>

    </select>


    <br><br>


    <!-- PURCHASE ORDER ITEMS -->

    <h3>Items</h3>


    <table id="itemsTable">

        <tr>

            <th>Item</th>

            <th>Quantity</th>

            <th>Purchase Price</th>

            <th>Action</th>

        </tr>


        <!-- FIRST ITEM ROW -->

        <tr>

            <td>

                <select
                        name="itemId"
                        required
                >

                    <option value="">
                        Select Item
                    </option>


                    <c:forEach
                            var="item"
                            items="${items}"
                    >

                        <option value="${item.id}">

                            ${item.name}
                            (${item.sku})

                        </option>

                    </c:forEach>

                </select>

            </td>


            <td>

                <input
                        type="number"
                        name="quantity"
                        min="1"
                        required
                >

            </td>


            <td>

                <input
                        type="number"
                        name="purchasePrice"
                        min="0"
                        step="0.01"
                        required
                >

            </td>


            <td>

                <button
                        type="button"
                        onclick="removeRow(this)"
                >

                    Remove

                </button>

            </td>

        </tr>

    </table>


    <br>


    <!-- ADD ITEM -->

    <button
            type="button"
            onclick="addItemRow()"
    >

        + Add Item

    </button>


    <br><br>


    <!-- CREATE PURCHASE ORDER -->

    <button type="submit">

        Create Purchase Order

    </button>


</form>


<hr>


<!-- PURCHASE ORDER LIST -->

<h2>Purchase Orders</h2>


<table>

    <tr>

        <th>Order ID</th>

        <th>Supplier</th>

        <th>Status</th>

        <th>Order Date</th>
        <th>Action</th>

    </tr>


    <c:forEach
            var="purchaseOrder"
            items="${purchaseOrders}"
    >

        <tr>

            <td>

                ${purchaseOrder.id}

            </td>


            <td>

                ${purchaseOrder.supplier.name}

            </td>


            <td>

                ${purchaseOrder.status}

            </td>


            <td>

                ${purchaseOrder.orderDate}

            </td>
            <td>
    <a href="${pageContext.request.contextPath}/purchase-orders?action=view&id=${purchaseOrder.id}">
        View
    </a>
</td>

        </tr>

    </c:forEach>

</table>


<!-- JAVASCRIPT -->

<script>


    function addItemRow() {


        const table =
                document.getElementById(
                        "itemsTable"
                );


        const row =
                table.insertRow();


        row.innerHTML = `

            <td>

                <select
                        name="itemId"
                        required
                >

                    <option value="">

                        Select Item

                    </option>


                    <c:forEach
                            var="item"
                            items="${items}"
                    >

                        <option
                                value="${item.id}"
                        >

                            ${item.name}
                            (${item.sku})

                        </option>

                    </c:forEach>

                </select>

            </td>


            <td>

                <input
                        type="number"
                        name="quantity"
                        min="1"
                        required
                >

            </td>


            <td>

                <input
                        type="number"
                        name="purchasePrice"
                        min="0"
                        step="0.01"
                        required
                >

            </td>


            <td>

                <button
                        type="button"
                        onclick="removeRow(this)"
                >

                    Remove

                </button>

            </td>

        `;
    }


    function removeRow(button) {


        const row =
                button.parentNode.parentNode;


        const table =
                document.getElementById(
                        "itemsTable"
                );


        // DON'T ALLOW REMOVING ALL ITEM ROWS

        if (
                table.rows.length > 2
        ) {

            row.remove();

        }
    }


</script>


</body>

</html>