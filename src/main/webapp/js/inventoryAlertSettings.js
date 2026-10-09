document.addEventListener(
    'DOMContentLoaded',
    () => {

        loadConfiguration();

        document
            .getElementById(
                'saveConfigurationButton'
            )
            ?.addEventListener(
                'click',
                saveConfiguration
            );
    }
);


// =====================================================
// LOAD CONFIGURATION
// =====================================================

async function loadConfiguration() {

    try {

        const response =
            await fetch(
                '/erpflow/api/inventory-alerts/config'
            );


        if (!response.ok) {

            throw new Error(
                `Failed to load configuration (${response.status})`
            );
        }


        const settings =
            await response.json();


        document
            .getElementById(
                'lowStockEnabled'
            )
            .checked =
                settings.lowStockEnabled === true;


        document
            .getElementById(
                'outOfStockEnabled'
            )
            .checked =
                settings.outOfStockEnabled === true;


        document
            .getElementById(
                'replenishmentEnabled'
            )
            .checked =
                settings.replenishmentEnabled === true;


        document
            .getElementById(
                'overstockEnabled'
            )
            .checked =
                settings.overstockEnabled === true;


        document
            .getElementById(
                'frequency'
            )
            .value =
                settings.frequency ||
                'IMMEDIATE';


        document
            .getElementById(
                'notificationChannel'
            )
            .value =
                settings.notificationChannel ||
                'IN_APP';


        if (settings.updatedAt) {

            document
                .getElementById(
                    'updatedAt'
                )
                .textContent =
                    `Last updated: ${formatDateTime(settings.updatedAt)}`;
        }

    } catch (error) {

        console.error(
            'Load configuration error:',
            error
        );


        showMessage(
            error.message ||
            'Unable to load inventory alert configuration.',
            'error'
        );
    }
}


// =====================================================
// SAVE CONFIGURATION
// =====================================================

async function saveConfiguration() {

    const button =
        document
            .getElementById(
                'saveConfigurationButton'
            );


    const settings = {

        lowStockEnabled:
            document
                .getElementById(
                    'lowStockEnabled'
                )
                .checked,

        outOfStockEnabled:
            document
                .getElementById(
                    'outOfStockEnabled'
                )
                .checked,

        replenishmentEnabled:
            document
                .getElementById(
                    'replenishmentEnabled'
                )
                .checked,

        overstockEnabled:
            document
                .getElementById(
                    'overstockEnabled'
                )
                .checked,

        frequency:
            document
                .getElementById(
                    'frequency'
                )
                .value,

        notificationChannel:
            document
                .getElementById(
                    'notificationChannel'
                )
                .value
    };


    try {

        button.disabled = true;

        button.textContent =
            'Saving...';


        const response =
            await fetch(
                '/erpflow/api/inventory-alerts/config',
                {
                    method: 'PUT',

                    headers: {
                        'Content-Type':
                            'application/json'
                    },

                    body:
                        JSON.stringify(
                            settings
                        )
                }
            );


        const data =
            await response.json();


        if (!response.ok) {

            throw new Error(
                data?.message ||
                'Failed to save configuration.'
            );
        }


        showMessage(
            'Inventory alert configuration saved successfully.',
            'success'
        );


        if (data.updatedAt) {

            document
                .getElementById(
                    'updatedAt'
                )
                .textContent =
                    `Last updated: ${formatDateTime(data.updatedAt)}`;
        }


    } catch (error) {

        console.error(
            'Save configuration error:',
            error
        );


        showMessage(
            error.message ||
            'Unable to save configuration.',
            'error'
        );


    } finally {

        button.disabled = false;

        button.textContent =
            'Save Configuration';
    }
}


// =====================================================
// MESSAGE
// =====================================================

function showMessage(
    message,
    type
) {

    const element =
        document
            .getElementById(
                'configurationMessage'
            );


    if (!element) {
        return;
    }


    element.textContent =
        message;


    element.className =
        `message ${type}`;


    window.setTimeout(
        () => {

            element.className =
                'message';

            element.textContent =
                '';

        },
        4000
    );
}


// =====================================================
// DATE/TIME
// =====================================================

function formatDateTime(value) {

    if (!value) {
        return '';
    }


    if (Array.isArray(value)) {

        const year =
            value[0];

        const month =
            String(
                value[1]
            ).padStart(
                2,
                '0'
            );

        const day =
            String(
                value[2]
            ).padStart(
                2,
                '0'
            );

        const hour =
            String(
                value[3] || 0
            ).padStart(
                2,
                '0'
            );

        const minute =
            String(
                value[4] || 0
            ).padStart(
                2,
                '0'
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