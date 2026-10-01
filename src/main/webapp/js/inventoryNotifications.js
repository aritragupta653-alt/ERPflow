document.addEventListener("DOMContentLoaded", () => {
    initializeInventoryNotifications();
});


// =====================================================
// INITIALIZE
// =====================================================

function initializeInventoryNotifications() {

    const button =
        document.getElementById("inventoryNotificationButton");

    const dropdown =
        document.getElementById("inventoryNotificationDropdown");

    if (!button || !dropdown) {
        console.warn(
            "Inventory notification UI elements were not found."
        );
        return;
    }


    button.addEventListener("click", async (event) => {

        event.stopPropagation();

        const isOpen =
            dropdown.classList.contains("show");

        if (isOpen) {

            closeNotificationDropdown();

        } else {

            openNotificationDropdown();

            await loadInventoryNotifications();
        }
    });


    document.addEventListener("click", (event) => {

        if (
            !dropdown.contains(event.target) &&
            !button.contains(event.target)
        ) {
            closeNotificationDropdown();
        }
    });


    const markAllButton =
        document.getElementById(
            "markAllNotificationsRead"
        );

    if (markAllButton) {

        markAllButton.addEventListener(
            "click",
            markAllNotificationsAsRead
        );
    }


    // Initial badge load
    loadUnreadNotificationCount();
}


// =====================================================
// LOAD UNREAD COUNT
// =====================================================

async function loadUnreadNotificationCount() {

    try {

        const response =
            await fetch(
                "/erpflow/api/inventory-notifications/unread-count"
            );


        if (!response.ok) {
            throw new Error(
                `Failed to load unread count (${response.status})`
            );
        }


        const data =
            await response.json();


        updateNotificationBadge(
            data.count || 0
        );


    } catch (error) {

        console.error(
            "Unable to load notification count:",
            error
        );
    }
}


// =====================================================
// UPDATE BADGE
// =====================================================

function updateNotificationBadge(count) {

    const badge =
        document.getElementById(
            "inventoryNotificationBadge"
        );

    if (!badge) {
        return;
    }


    if (count <= 0) {

        badge.style.display = "none";
        badge.textContent = "";

        return;
    }


    badge.style.display = "flex";

    badge.textContent =
        count > 99
            ? "99+"
            : String(count);
}


// =====================================================
// OPEN DROPDOWN
// =====================================================

function openNotificationDropdown() {

    const dropdown =
        document.getElementById(
            "inventoryNotificationDropdown"
        );

    if (!dropdown) {
        return;
    }

    dropdown.classList.add("show");
}


// =====================================================
// CLOSE DROPDOWN
// =====================================================

function closeNotificationDropdown() {

    const dropdown =
        document.getElementById(
            "inventoryNotificationDropdown"
        );

    if (!dropdown) {
        return;
    }

    dropdown.classList.remove("show");
}


// =====================================================
// LOAD NOTIFICATIONS
// =====================================================

async function loadInventoryNotifications() {

    const container =
        document.getElementById(
            "inventoryNotificationList"
        );

    if (!container) {
        return;
    }


    container.innerHTML = `
        <div class="notification-loading">
            Loading notifications...
        </div>
    `;


    try {

        const response =
            await fetch(
                "/erpflow/api/inventory-notifications/unread"
            );


        if (!response.ok) {

            throw new Error(
                `Failed to load notifications (${response.status})`
            );
        }


        const notifications =
            await response.json();


        renderInventoryNotifications(
            notifications
        );


    } catch (error) {

        console.error(
            "Unable to load notifications:",
            error
        );


        container.innerHTML = `
            <div class="notification-empty">
                Unable to load notifications.
            </div>
        `;
    }
}


// =====================================================
// RENDER NOTIFICATIONS
// =====================================================

function renderInventoryNotifications(
    notifications
) {

    const container =
        document.getElementById(
            "inventoryNotificationList"
        );

    if (!container) {
        return;
    }


    if (
        !notifications ||
        notifications.length === 0
    ) {

        container.innerHTML = `
            <div class="notification-empty">
                <div class="notification-empty-icon">
                    ✓
                </div>

                <div>
                    You're all caught up.
                </div>
            </div>
        `;

        return;
    }


    container.innerHTML =
        notifications
            .map(
                notification =>
                    createNotificationHTML(
                        notification
                    )
            )
            .join("");


    container
        .querySelectorAll(
            "[data-notification-id]"
        )
        .forEach(element => {

            element.addEventListener(
                "click",
                () => {

                    const id =
                        element.dataset.notificationId;

                    markNotificationAsRead(
                        id
                    );
                }
            );
        });
}


// =====================================================
// CREATE NOTIFICATION HTML
// =====================================================

function createNotificationHTML(
    notification
) {

    const alertType =
        notification.alertType || "";


    const type =
        alertType.toUpperCase();


    let icon = "⚠";

    if (type === "OUT_OF_STOCK") {
        icon = "⛔";
    }

    if (type === "LOW_STOCK") {
        icon = "⚠";
    }

    if (type === "REPLENISHMENT") {
        icon = "↻";
    }

    if (type === "OVERSTOCK") {
        icon = "📦";
    }


    return `
        <div
            class="inventory-notification-item"
            data-notification-id="${notification.id}"
        >

            <div class="inventory-notification-icon">
                ${icon}
            </div>

            <div class="inventory-notification-content">

                <div class="inventory-notification-title">
                    ${formatAlertType(alertType)}
                </div>

                <div class="inventory-notification-message">
                    ${escapeNotificationHTML(
                        notification.message || ""
                    )}
                </div>

                <div class="inventory-notification-time">
                    ${formatNotificationDate(
                        notification.createdAt
                    )}
                </div>

            </div>

        </div>
    `;
}


// =====================================================
// MARK ONE AS READ
// =====================================================

async function markNotificationAsRead(
    notificationId
) {

    try {

        const response =
            await fetch(
                `/erpflow/api/inventory-notifications/${notificationId}/read`,
                {
                    method: "PUT"
                }
            );


        if (!response.ok) {

            throw new Error(
                `Failed to mark notification as read (${response.status})`
            );
        }


        await loadInventoryNotifications();

        await loadUnreadNotificationCount();


    } catch (error) {

        console.error(
            "Unable to mark notification as read:",
            error
        );
    }
}


// =====================================================
// MARK ALL AS READ
// =====================================================

async function markAllNotificationsAsRead() {

    try {

        const response =
            await fetch(
                "/erpflow/api/inventory-notifications/read-all",
                {
                    method: "PUT"
                }
            );


        if (!response.ok) {

            throw new Error(
                `Failed to mark notifications as read (${response.status})`
            );
        }


        await loadInventoryNotifications();

        await loadUnreadNotificationCount();


    } catch (error) {

        console.error(
            "Unable to mark all notifications as read:",
            error
        );
    }
}


// =====================================================
// FORMAT ALERT TYPE
// =====================================================

function formatAlertType(
    alertType
) {

    if (!alertType) {
        return "Inventory Alert";
    }


    return alertType
        .toLowerCase()
        .split("_")
        .map(
            word =>
                word.charAt(0).toUpperCase()
                + word.slice(1)
        )
        .join(" ");
}


// =====================================================
// FORMAT DATE
// =====================================================

function formatNotificationDate(
    value
) {

    if (!value) {
        return "";
    }


    if (Array.isArray(value)) {

        const year =
            value[0];

        const month =
            String(value[1]).padStart(
                2,
                "0"
            );

        const day =
            String(value[2]).padStart(
                2,
                "0"
            );

        const hour =
            String(value[3] || 0).padStart(
                2,
                "0"
            );

        const minute =
            String(value[4] || 0).padStart(
                2,
                "0"
            );


        return `${day}/${month}/${year} ${hour}:${minute}`;
    }


    const date =
        new Date(value);


    if (
        Number.isNaN(
            date.getTime()
        )
    ) {

        return String(value);
    }


    return date.toLocaleString();
}


// =====================================================
// ESCAPE HTML
// =====================================================

function escapeNotificationHTML(
    value
) {

    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}