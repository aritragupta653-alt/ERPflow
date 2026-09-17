let allCustomers = [];


document.addEventListener("DOMContentLoaded", () => {

    loadCustomers();

    setupAddCustomerForm();

    setupEditCustomerForm();

    setupSearch();

    setupModal();

});


// ========================================
// LOAD CUSTOMERS
// ========================================

async function loadCustomers() {

    try {

        const response = await fetch(
            contextPath + "/api/customers"
        );

        if (!response.ok) {
            throw new Error("Failed to load customers");
        }

        allCustomers = await response.json();

        renderCustomers(allCustomers);

    } catch (error) {

        console.error(error);

        showMessage(
            "Failed to load customers.",
            "error"
        );
    }
}


// ========================================
// RENDER CUSTOMERS
// ========================================

function renderCustomers(customers) {

    const tableBody =
        document.getElementById("customersTableBody");

    const count =
        document.getElementById("customerCount");

    tableBody.innerHTML = "";

    count.textContent = customers.length;


    if (customers.length === 0) {

        const row =
            document.createElement("tr");

        const cell =
            document.createElement("td");

        cell.colSpan = 7;

        cell.textContent =
            "No customers found.";

        cell.style.textAlign = "center";

        row.appendChild(cell);

        tableBody.appendChild(row);

        return;
    }


    customers.forEach(customer => {

        const row =
            document.createElement("tr");


        // ID

        const idCell =
            document.createElement("td");

        idCell.textContent =
            customer.id;

        row.appendChild(idCell);


        // Name

        const nameCell =
            document.createElement("td");

        nameCell.textContent =
            customer.name || "-";

        row.appendChild(nameCell);


        // Email

        const emailCell =
            document.createElement("td");

        emailCell.textContent =
            customer.email || "-";

        row.appendChild(emailCell);


        // Phone

        const phoneCell =
            document.createElement("td");

        phoneCell.textContent =
            customer.phone || "-";

        row.appendChild(phoneCell);


        // Address

        const addressCell =
            document.createElement("td");

        addressCell.textContent =
            customer.address || "-";

        row.appendChild(addressCell);


        // Status

        const statusCell =
            document.createElement("td");

        const statusBadge =
            document.createElement("span");

        statusBadge.className =
            "status-badge " +
            (customer.status === "ACTIVE"
                ? "active"
                : "inactive");

        statusBadge.textContent =
            customer.status || "UNKNOWN";

        statusCell.appendChild(statusBadge);

        row.appendChild(statusCell);


        // Actions

        const actionCell =
            document.createElement("td");

        actionCell.className =
            "action-buttons";


        // Edit button

        const editButton =
            document.createElement("button");

        editButton.type = "button";

        editButton.className =
            "btn small-btn";

        editButton.textContent =
            "Edit";

        editButton.addEventListener(
            "click",
            () => openEditModal(customer)
        );


        actionCell.appendChild(editButton);


        // Delete button

        if (customer.status === "ACTIVE") {

            const deleteButton =
                document.createElement("button");

            deleteButton.type = "button";

            deleteButton.className =
                "btn small-btn danger-btn";

            deleteButton.textContent =
                "Deactivate";

            deleteButton.addEventListener(
                "click",
                () => deactivateCustomer(customer.id)
            );

            actionCell.appendChild(deleteButton);
        }


        row.appendChild(actionCell);

        tableBody.appendChild(row);

    });

}


// ========================================
// ADD CUSTOMER
// ========================================

function setupAddCustomerForm() {

    const form =
        document.getElementById("addCustomerForm");


    form.addEventListener(
        "submit",
        async event => {

            event.preventDefault();


            const customer = {

                name:
                    document.getElementById(
                        "customerName"
                    ).value.trim(),

                email:
                    document.getElementById(
                        "customerEmail"
                    ).value.trim(),

                phone:
                    document.getElementById(
                        "customerPhone"
                    ).value.trim(),

                address:
                    document.getElementById(
                        "customerAddress"
                    ).value.trim(),

                status: "ACTIVE"
            };


            try {

                const response =
                    await fetch(
                        contextPath + "/api/customers",
                        {
                            method: "POST",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body:
                                JSON.stringify(customer)
                        }
                    );


                const data =
                    await response.json();


                if (!response.ok) {

                    throw new Error(
                        data.error ||
                        "Failed to add customer"
                    );
                }


                showMessage(
                    "Customer added successfully.",
                    "success"
                );


                form.reset();

                await loadCustomers();


            } catch (error) {

                console.error(error);

                showMessage(
                    error.message,
                    "error"
                );
            }

        }
    );
}


// ========================================
// EDIT CUSTOMER
// ========================================

function openEditModal(customer) {

    document.getElementById(
        "editId"
    ).value = customer.id;

    document.getElementById(
        "editName"
    ).value = customer.name || "";

    document.getElementById(
        "editEmail"
    ).value = customer.email || "";

    document.getElementById(
        "editPhone"
    ).value = customer.phone || "";

    document.getElementById(
        "editAddress"
    ).value = customer.address || "";

    document.getElementById(
        "editStatus"
    ).value =
        customer.status || "ACTIVE";


    document.getElementById(
        "editModal"
    ).classList.add("show");

}


// ========================================
// UPDATE CUSTOMER
// ========================================

function setupEditCustomerForm() {

    const form =
        document.getElementById(
            "editCustomerForm"
        );


    form.addEventListener(
        "submit",
        async event => {

            event.preventDefault();


            const id =
                document.getElementById(
                    "editId"
                ).value;


            const customer = {

                name:
                    document.getElementById(
                        "editName"
                    ).value.trim(),

                email:
                    document.getElementById(
                        "editEmail"
                    ).value.trim(),

                phone:
                    document.getElementById(
                        "editPhone"
                    ).value.trim(),

                address:
                    document.getElementById(
                        "editAddress"
                    ).value.trim(),

                status:
                    document.getElementById(
                        "editStatus"
                    ).value
            };


            try {

                const response =
                    await fetch(
                        contextPath +
                        "/api/customers/" +
                        id,
                        {
                            method: "PUT",

                            headers: {
                                "Content-Type":
                                    "application/json"
                            },

                            body:
                                JSON.stringify(customer)
                        }
                    );


                const data =
                    await response.json();


                if (!response.ok) {

                    throw new Error(
                        data.error ||
                        "Failed to update customer"
                    );
                }


                closeEditModal();


                showMessage(
                    "Customer updated successfully.",
                    "success"
                );


                await loadCustomers();


            } catch (error) {

                console.error(error);

                showMessage(
                    error.message,
                    "error"
                );
            }

        }
    );
}


// ========================================
// DEACTIVATE CUSTOMER
// ========================================

async function deactivateCustomer(id) {

    const confirmed =
        confirm(
            "Are you sure you want to deactivate this customer?"
        );


    if (!confirmed) {
        return;
    }


    try {

        const response =
            await fetch(
                contextPath +
                "/api/customers/" +
                id,
                {
                    method: "DELETE"
                }
            );


        if (!response.ok) {

            let message =
                "Failed to deactivate customer";

            try {

                const data =
                    await response.json();

                message =
                    data.error || message;

            } catch (_) {
                // Ignore JSON parsing failure
            }

            throw new Error(message);
        }


        showMessage(
            "Customer deactivated successfully.",
            "success"
        );


        await loadCustomers();


    } catch (error) {

        console.error(error);

        showMessage(
            error.message,
            "error"
        );
    }
}


// ========================================
// SEARCH
// ========================================

function setupSearch() {

    const searchInput =
        document.getElementById(
            "searchInput"
        );


    searchInput.addEventListener(
        "input",
        () => {

            const searchTerm =
                searchInput.value
                    .toLowerCase()
                    .trim();


            if (!searchTerm) {

                renderCustomers(allCustomers);

                return;
            }


            const filtered =
                allCustomers.filter(
                    customer =>

                        (customer.name || "")
                            .toLowerCase()
                            .includes(searchTerm)

                        ||

                        (customer.email || "")
                            .toLowerCase()
                            .includes(searchTerm)

                        ||

                        (customer.phone || "")
                            .toLowerCase()
                            .includes(searchTerm)

                        ||

                        (customer.address || "")
                            .toLowerCase()
                            .includes(searchTerm)
                );


            renderCustomers(filtered);

        }
    );
}


// ========================================
// MODAL
// ========================================

function setupModal() {

    const modal =
        document.getElementById(
            "editModal"
        );

    const closeButton =
        document.getElementById(
            "closeModal"
        );

    const cancelButton =
        document.getElementById(
            "cancelEdit"
        );


    // Make sure modal is hidden initially

    modal.classList.remove("show");


    closeButton.addEventListener(
        "click",
        closeEditModal
    );


    cancelButton.addEventListener(
        "click",
        closeEditModal
    );


    modal.addEventListener(
        "click",
        event => {

            if (event.target === modal) {
                closeEditModal();
            }

        }
    );
}


function closeEditModal() {

    document.getElementById(
        "editModal"
    ).classList.remove("show");

}


// ========================================
// MESSAGE
// ========================================

function showMessage(
    message,
    type
) {

    const messageBox =
        document.getElementById(
            "messageBox"
        );


    messageBox.textContent =
        message;


    messageBox.className =
        "message-box " + type;


    messageBox.style.display =
        "block";


    setTimeout(
        () => {

            messageBox.style.display =
                "none";

        },
        3000
    );
}