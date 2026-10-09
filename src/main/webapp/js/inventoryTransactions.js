const apiUrl = "/erpflow/api/inventory-transactions";

let transactions = [];

document.addEventListener("DOMContentLoaded", () => {
    loadTransactions();

    document.getElementById("searchInput")
        .addEventListener("input", filterTransactions);
});

async function loadTransactions() {

    try {

        const response = await fetch(apiUrl);

        if (!response.ok) {
            throw new Error("Failed to load transactions");
        }

        transactions = await response.json();

        displayTransactions(transactions);

    } catch (error) {

        console.error(error);

        document.getElementById("messageBox").textContent =
            "Failed to load transaction history.";
    }
}

function displayTransactions(data) {

    const tableBody =
        document.getElementById("transactionsTableBody");

    tableBody.innerHTML = "";

    document.getElementById("transactionCount")
        .textContent = data.length;

    if (data.length === 0) {

        tableBody.innerHTML = `
            <tr>
                <td colspan="6">No transactions found.</td>
            </tr>
        `;

        return;
    }

    data.forEach(transaction => {

        const row = document.createElement("tr");

        row.innerHTML = `
            <td>#${transaction.id}</td>

            <td>
                ${transaction.item?.name || "-"}
            </td>

            <td>
                <span class="status">
                    ${transaction.type || "-"}
                </span>
            </td>

            <td>
                ${transaction.quantity ?? "-"}
            </td>

            <td>
                ${transaction.item?.sku || "-"}
            </td>

            <td>
                ${formatDate(transaction.transactionDate)}
            </td>
        `;

        tableBody.appendChild(row);
    });
}

function formatDate(dateArray) {

    if (!Array.isArray(dateArray)) {
        return "-";
    }

    const [
        year,
        month,
        day,
        hour = 0,
        minute = 0,
        second = 0
    ] = dateArray;

    const date = new Date(
        year,
        month - 1,
        day,
        hour,
        minute,
        second
    );

    return date.toLocaleString();
}

function filterTransactions() {

    const search =
        document.getElementById("searchInput")
            .value
            .toLowerCase();

    const filtered = transactions.filter(transaction => {

        const text = `
            ${transaction.id}
            ${transaction.item?.name || ""}
            ${transaction.item?.sku || ""}
            ${transaction.type || ""}
            ${transaction.quantity || ""}
        `.toLowerCase();

        return text.includes(search);
    });

    displayTransactions(filtered);
}