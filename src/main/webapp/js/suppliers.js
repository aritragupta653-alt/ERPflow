const apiUrl = "/erpflow/api/suppliers";

let allSuppliers = [];


// ========================================
// PAGE LOAD
// ========================================

document.addEventListener(
    "DOMContentLoaded",
    () => {

        loadSuppliers();

        setupForm();

        setupSearch();

        setupCancelButton();

    }
);


// ========================================
// LOAD SUPPLIERS
// ========================================

async function loadSuppliers() {

    try {

        const response =
            await fetch(apiUrl);


        if (!response.ok) {

            throw new Error(
                "Failed to load suppliers"
            );
        }


        allSuppliers =
            await response.json();


        renderSuppliers(
            allSuppliers
        );


    } catch (error) {

        console.error(error);

        showMessage(
            "Failed to load suppliers.",
            "error"
        );
    }
}


// ========================================
// RENDER SUPPLIERS
// ========================================

function renderSuppliers(
    suppliers
) {

    const tableBody =
        document.getElementById(
            "suppliersTableBody"
        );


    const count =
        document.getElementById(
            "supplierCount"
        );


    tableBody.innerHTML = "";

    count.textContent =
        suppliers.length;


    if (suppliers.length === 0) {

        tableBody.innerHTML = `
            <tr>
                <td
                    colspan="7"
                    style="text-align:center;"
                >
                    No suppliers found.
                </td>
            </tr>
        `;

        return;
    }


    suppliers.forEach(
        supplier => {

            const row =
                document.createElement("tr");


            row.innerHTML = `

                <td>
                    #${supplier.id}
                </td>

                <td>
                    ${supplier.name || "-"}
                </td>

                <td>
                    ${supplier.contactPerson || "-"}
                </td>

                <td>
                    ${supplier.phone || "-"}
                </td>

                <td>
                    ${supplier.email || "-"}
                </td>

                <td>
                    ${supplier.address || "-"}
                </td>

                <td>

                    <button
                        class="btn small-btn"
                        onclick="editSupplier(${supplier.id})"
                    >
                        Edit
                    </button>

                    <button
                        class="btn small-btn danger-btn"
                        onclick="deleteSupplier(${supplier.id})"
                    >
                        Delete
                    </button>

                </td>

            `;


            tableBody.appendChild(row);

        }
    );
}


// ========================================
// ADD / UPDATE FORM
// ========================================

function setupForm() {

    const form =
        document.getElementById(
            "supplierForm"
        );


    form.addEventListener(
        "submit",
        async event => {

            event.preventDefault();


            const id =
                document.getElementById(
                    "supplierId"
                ).value;


            const supplier = {

                name:
                    document.getElementById(
                        "name"
                    ).value.trim(),

                contactPerson:
                    document.getElementById(
                        "contactPerson"
                    ).value.trim(),

                phone:
                    document.getElementById(
                        "phone"
                    ).value.trim(),

                email:
                    document.getElementById(
                        "email"
                    ).value.trim(),

                address:
                    document.getElementById(
                        "address"
                    ).value.trim()

            };


            try {

                let response;


                // UPDATE

                if (id) {

                    response =
                        await fetch(
                            `${apiUrl}/${id}`,
                            {

                                method: "PUT",

                                headers: {
                                    "Content-Type":
                                        "application/json"
                                },

                                body:
                                    JSON.stringify(
                                        supplier
                                    )
                            }
                        );

                }

                // ADD

                else {

                    response =
                        await fetch(
                            apiUrl,
                            {

                                method: "POST",

                                headers: {
                                    "Content-Type":
                                        "application/json"
                                },

                                body:
                                    JSON.stringify(
                                        supplier
                                    )
                            }
                        );
                }


                const data =
                    await response.json();


                if (!response.ok) {

                    throw new Error(
                        data.error ||
                        "Operation failed"
                    );
                }


                if (id) {

                    showMessage(
                        "Supplier updated successfully.",
                        "success"
                    );

                } else {

                    showMessage(
                        "Supplier added successfully.",
                        "success"
                    );

                }


                resetForm();

                await loadSuppliers();


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
// EDIT SUPPLIER
// ========================================

async function editSupplier(id) {

    try {

        const response =
            await fetch(
                `${apiUrl}/${id}`
            );


        if (!response.ok) {

            throw new Error(
                "Failed to load supplier"
            );
        }


        const supplier =
            await response.json();


        document.getElementById(
            "supplierId"
        ).value =
            supplier.id;


        document.getElementById(
            "name"
        ).value =
            supplier.name || "";


        document.getElementById(
            "contactPerson"
        ).value =
            supplier.contactPerson || "";


        document.getElementById(
            "phone"
        ).value =
            supplier.phone || "";


        document.getElementById(
            "email"
        ).value =
            supplier.email || "";


        document.getElementById(
            "address"
        ).value =
            supplier.address || "";


        document.getElementById(
            "formTitle"
        ).textContent =
            "Edit Supplier";


        document.getElementById(
            "saveButton"
        ).textContent =
            "Update Supplier";


        document.getElementById(
            "cancelButton"
        ).style.display =
            "inline-block";


        // Scroll to form

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });


    } catch (error) {

        console.error(error);

        showMessage(
            "Failed to load supplier.",
            "error"
        );
    }
}


// ========================================
// DELETE SUPPLIER
// ========================================

async function deleteSupplier(id) {

    if (
        !confirm(
            "Are you sure you want to delete this supplier?"
        )
    ) {

        return;
    }


    try {

        const response =
            await fetch(
                `${apiUrl}/${id}`,
                {
                    method: "DELETE"
                }
            );


        if (!response.ok) {

            let data = {};

            try {
                data =
                    await response.json();
            } catch (_) {
            }


            throw new Error(
                data.error ||
                "Failed to delete supplier"
            );
        }


        showMessage(
            "Supplier deleted successfully.",
            "success"
        );


        await loadSuppliers();


    } catch (error) {

        console.error(error);

        showMessage(
            error.message,
            "error"
        );
    }
}


// ========================================
// CANCEL EDIT
// ========================================

function setupCancelButton() {

    document
        .getElementById(
            "cancelButton"
        )
        .addEventListener(
            "click",
            resetForm
        );
}


// ========================================
// RESET FORM
// ========================================

function resetForm() {

    document
        .getElementById(
            "supplierForm"
        )
        .reset();


    document.getElementById(
        "supplierId"
    ).value = "";


    document.getElementById(
        "formTitle"
    ).textContent =
        "Add Supplier";


    document.getElementById(
        "saveButton"
    ).textContent =
        "Add Supplier";


    document.getElementById(
        "cancelButton"
    ).style.display =
        "none";
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

            const term =
                searchInput.value
                    .toLowerCase()
                    .trim();


            if (!term) {

                renderSuppliers(
                    allSuppliers
                );

                return;
            }


            const filtered =
                allSuppliers.filter(
                    supplier => {

                        const text = `

                            ${supplier.id || ""}

                            ${supplier.name || ""}

                            ${supplier.contactPerson || ""}

                            ${supplier.phone || ""}

                            ${supplier.email || ""}

                            ${supplier.address || ""}

                        `.toLowerCase();


                        return text.includes(
                            term
                        );
                    }
                );


            renderSuppliers(
                filtered
            );

        }
    );
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