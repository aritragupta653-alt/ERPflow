const apiUrl =
    "/erpflow/api/purchase-orders";


document.addEventListener(
    "DOMContentLoaded",
    loadPurchaseOrder
);


// ========================================
// LOAD PURCHASE ORDER
// ========================================

async function loadPurchaseOrder() {

    const params =
        new URLSearchParams(
            window.location.search
        );


    const orderId =
        params.get("id");


    if (!orderId) {

        showError(
            "Purchase Order ID is missing."
        );

        return;
    }


    try {

        const response =
            await fetch(
                `${apiUrl}/${orderId}`
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load purchase order"
            );
        }


        const order =
            await response.json();


        console.log(
            "Purchase Order:",
            order
        );


        displayPurchaseOrder(
            order
        );


    } catch (error) {

        console.error(error);

        showError(
            "Failed to load purchase order details."
        );
    }
}


// ========================================
// DISPLAY PURCHASE ORDER
// ========================================

function displayPurchaseOrder(
    order
) {

    document.getElementById(
        "orderId"
    ).value =
        order.id ?? "-";


    document.getElementById(
        "orderDate"
    ).value =
        formatDate(
            order.orderDate
        );


    document.getElementById(
        "orderStatus"
    ).value =
        order.status ?? "-";


    // ====================================
    // SUPPLIER
    // ====================================

    if (order.supplier) {

        document.getElementById(
            "supplierId"
        ).value =
            order.supplier.id ?? "-";


        document.getElementById(
            "supplierName"
        ).value =
            order.supplier.name ?? "-";


        document.getElementById(
            "contactPerson"
        ).value =
            order.supplier.contactPerson ?? "-";


        document.getElementById(
            "supplierPhone"
        ).value =
            order.supplier.phone ?? "-";


        document.getElementById(
            "supplierEmail"
        ).value =
            order.supplier.email ?? "-";


        document.getElementById(
            "supplierAddress"
        ).value =
            order.supplier.address ?? "-";
    }


    // ====================================
    // ITEMS
    // ====================================

    const tableBody =
        document.getElementById(
            "orderItemsTableBody"
        );


    tableBody.innerHTML = "";


    const items =
        order.items || [];


    if (items.length === 0) {

        tableBody.innerHTML = `

            <tr>

                <td
                    colspan="6"
                    style="text-align:center;"
                >
                    No items found.
                </td>

            </tr>

        `;

        return;
    }


    items.forEach(
        orderItem => {

            const item =
                orderItem.item || {};


            const quantity =
                Number(
                    orderItem.quantity || 0
                );


            const purchasePrice =
                Number(
                    orderItem.purchasePrice || 0
                );


            const total =
                quantity *
                purchasePrice;


            const row =
                document.createElement(
                    "tr"
                );


            row.innerHTML = `

                <td>
                    #${item.id ?? "-"}
                </td>

                <td>
                    ${item.name ?? "-"}
                </td>

                <td>
                    ${item.sku ?? "-"}
                </td>

                <td>
                    ${quantity}
                </td>

                <td>
                    ₹${purchasePrice.toFixed(2)}
                </td>

                <td>
                    ₹${total.toFixed(2)}
                </td>

            `;


            tableBody.appendChild(
                row
            );

        }
    );
}


// ========================================
// FORMAT DATE
// ========================================

function formatDate(
    dateValue
) {

    if (!dateValue) {
        return "-";
    }


    // Jackson LocalDateTime array

    if (
        Array.isArray(dateValue)
    ) {

        const [
            year,
            month,
            day,
            hour = 0,
            minute = 0,
            second = 0
        ] = dateValue;


        const date =
            new Date(
                year,
                month - 1,
                day,
                hour,
                minute,
                second
            );


        if (
            isNaN(
                date.getTime()
            )
        ) {

            return "-";
        }


        return date.toLocaleString();
    }


    // ISO string fallback

    const date =
        new Date(
            dateValue
        );


    if (
        isNaN(
            date.getTime()
        )
    ) {

        return "-";
    }


    return date.toLocaleString();
}


// ========================================
// ERROR
// ========================================

function showError(
    message
) {

    const messageBox =
        document.getElementById(
            "messageBox"
        );


    messageBox.textContent =
        message;


    messageBox.className =
        "message-box error";


    messageBox.style.display =
        "block";
}