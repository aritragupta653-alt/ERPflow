
const packageApiUrl = "/erpflow/api/packages";

const params = new URLSearchParams(window.location.search);
const packageId = params.get("id");

document.addEventListener("DOMContentLoaded", loadPackageDetails);

async function loadPackageDetails() {
    const body = document.getElementById("packageItemsBody");

    if (!packageId || !/^\d+$/.test(packageId)) {
        showPackageError("A valid package ID was not provided.");
        return;
    }

    try {
        const response = await fetch(
            `${packageApiUrl}/${encodeURIComponent(packageId)}`
        );

        const data = await response.json().catch(() => ({}));

        if (!response.ok) {
            throw new Error(
                data.message || "Could not load package details."
            );
        }

        const pkg = data.package || {};
        const items = Array.isArray(data.items) ? data.items : [];
        const order = pkg.salesOrder || {};
        const customer = order.customer || {};

        setText(
            "packageTitle",
            `Package ${pkg.packageNumber || `PKG-${pkg.id || packageId}`}`
        );

        setText(
            "packageNumber",
            pkg.packageNumber || `PKG-${pkg.id || packageId}`
        );

        setText("packageStatus", pkg.status || "-");
        setText("salesOrder", order.id ? `SO #${order.id}` : "-");
        setText("customer", customer.name || "-");
        setText("packageDate", formatDate(pkg.packageDate));
        setText("packageWeight", formatMeasure(pkg.weight, "kg"));
        setText("packageLength", formatMeasure(pkg.length, "cm"));
        setText("packageWidth", formatMeasure(pkg.width, "cm"));
        setText("packageHeight", formatMeasure(pkg.height, "cm"));

        if (!items.length) {
            body.innerHTML = `
                <tr>
                    <td colspan="6" style="text-align:center;">
                        No items in this package.
                    </td>
                </tr>
            `;
            return;
        }

        body.innerHTML = items.map(row => {
            const item = row.item || {};
            const itemId = item.id ?? "-";
            const lineId = row.salesOrderItemId ?? "-";
            const price = item.sellingPrice;

            return `
                <tr>
                    <td>${escapeHtml(itemId)}</td>
                    <td>${escapeHtml(item.name || "-")}</td>
                    <td>${escapeHtml(item.sku || "-")}</td>
                    <td>${escapeHtml(lineId)}</td>
                    <td>${escapeHtml(row.quantity ?? "-")}</td>
                    <td>
                        ${price == null
                            ? "-"
                            : escapeHtml(formatMoney(price))}
                    </td>
                </tr>
            `;
        }).join("");

    } catch (error) {
        console.error("Package details error:", error);
        showPackageError(
            error.message || "Failed to load package details."
        );
    }
}

function setText(id, value) {
    const element = document.getElementById(id);

    if (element) {
        element.textContent =
            value == null || value === "" ? "-" : String(value);
    }
}

function formatDate(value) {
    if (!value) return "-";

    const date = new Date(value);

    return Number.isNaN(date.getTime())
        ? String(value).replace("T", " ")
        : date.toLocaleString();
}

function formatMeasure(value, unit) {
    return value == null || value === ""
        ? "-"
        : `${value} ${unit}`;
}

function formatMoney(value) {
    const number = Number(value);

    return Number.isFinite(number)
        ? number.toFixed(2)
        : String(value);
}

function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[character]);
}

function showPackageError(message) {
    setText("packageTitle", "Package Details");

    const body = document.getElementById("packageItemsBody");

    if (body) {
        body.innerHTML = `
            <tr>
                <td colspan="6"
                    style="text-align:center;color:#b42318;">
                    ${escapeHtml(message)}
                </td>
            </tr>
        `;
    }
}