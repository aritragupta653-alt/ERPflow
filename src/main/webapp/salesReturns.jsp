
<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Sales Returns - ERPFlow</title>
    <link rel="stylesheet" href="/erpflow/css/app.css">

    <style>
        .return-toolbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 16px;
            flex-wrap: wrap;
            margin: 24px 0;
        }

        .return-actions {
            display: flex;
            gap: 8px;
            flex-wrap: wrap;
        }

        .return-stats {
            display: grid;
            grid-template-columns: repeat(3, minmax(0, 1fr));
            gap: 16px;
            margin: 20px 0;
        }

        .return-stat {
            background: #fff;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 20px;
        }

        .return-stat span {
            display: block;
            color: #64748b;
            font-size: 13px;
            margin-bottom: 8px;
        }

        .return-stat strong {
            font-size: 25px;
            color: #0f172a;
        }

        .return-panel {
            background: #fff;
            border: 1px solid #e2e8f0;
            border-radius: 12px;
            padding: 22px;
            margin-bottom: 22px;
        }

        .return-panel h2 {
            margin-top: 0;
        }

        .return-grid {
            display: grid;
            grid-template-columns: repeat(2, minmax(0, 1fr));
            gap: 16px;
        }

        .return-grid .form-group {
            margin-bottom: 0;
        }

        .return-grid .full-width {
            grid-column: 1 / -1;
        }

        .return-table-wrap {
            width: 100%;
            overflow-x: auto;
        }

        .return-table {
            width: 100%;
            border-collapse: collapse;
            min-width: 780px;
        }

        .return-table th,
        .return-table td {
            text-align: left;
            padding: 13px 12px;
            border-bottom: 1px solid #e2e8f0;
            vertical-align: middle;
        }

        .return-table th {
            background: #f8fafc;
            color: #475569;
            font-size: 12px;
            text-transform: uppercase;
            letter-spacing: .03em;
        }

        .return-table input,
        .return-table select {
            min-width: 100px;
            max-width: 160px;
        }

        .return-status {
            display: inline-block;
            padding: 5px 10px;
            border-radius: 999px;
            font-size: 12px;
            font-weight: 700;
        }

        .status-created {
            color: #92400e;
            background: #fef3c7;
        }

        .status-received {
            color: #166534;
            background: #dcfce7;
        }

        .return-message {
            display: none;
            padding: 12px 15px;
            margin: 16px 0;
            border-radius: 8px;
        }

        .return-message.success {
            display: block;
            color: #166534;
            background: #dcfce7;
        }

        .return-message.error {
            display: block;
            color: #991b1b;
            background: #fee2e2;
        }

        .return-message.info {
            display: block;
            color: #075985;
            background: #e0f2fe;
        }

        .return-total {
            display: flex;
            justify-content: flex-end;
            align-items: center;
            gap: 18px;
            padding-top: 18px;
            font-weight: 600;
        }

        .return-total strong {
            font-size: 24px;
            color: #2563eb;
        }

        .return-hidden {
            display: none !important;
        }

        .return-detail-row {
            display: flex;
            justify-content: space-between;
            gap: 16px;
            padding: 10px 0;
            border-bottom: 1px solid #e2e8f0;
        }

        .return-detail-row span:first-child {
            color: #64748b;
        }

        .return-detail-row span:last-child {
            text-align: right;
            font-weight: 600;
        }

        .return-footer {
            display: flex;
            justify-content: flex-end;
            gap: 10px;
            flex-wrap: wrap;
            margin-top: 20px;
        }

        .muted-text {
            color: #64748b;
            font-size: 13px;
        }

        @media (max-width: 700px) {
            .return-stats,
            .return-grid {
                grid-template-columns: 1fr;
            }

            .return-grid .full-width {
                grid-column: auto;
            }
        }
    </style>
</head>

<body>
<header class="navbar">
    <a href="/erpflow/home.jsp" class="logo">
        <img src="/erpflow/images/erpflow-logo.png" alt="ERPFlow Logo">
        <span>ERPFlow</span>
    </a>
</header>

<div class="container">
    <a class="back" href="/erpflow/home.jsp">← Back to Dashboard</a>

    <div class="page-header">
        <div>
            <h1>Sales Returns</h1>
            <p>Create customer returns and receive returned goods into inventory.</p>
        </div>
    </div>

    <div id="messageBox" class="return-message" role="status"></div>

    <div class="return-stats">
        <div class="return-stat">
            <span>Total Returns</span>
            <strong id="totalReturns">0</strong>
        </div>
        <div class="return-stat">
            <span>Awaiting Receipt</span>
            <strong id="createdReturns">0</strong>
        </div>
        <div class="return-stat">
            <span>Received</span>
            <strong id="receivedReturns">0</strong>
        </div>
    </div>

    <div class="return-toolbar">
        <div>
            <h2 style="margin:0 0 6px">Return Register</h2>
            <p class="muted-text" style="margin:0">
                Only CREATED and RECEIVED statuses are used.
            </p>
        </div>
        <button type="button" class="btn btn-primary" id="showCreateButton">
            + Create Sales Return
        </button>
    </div>

    <section id="createPanel" class="return-panel return-hidden">
        <h2>Create Sales Return</h2>
        <p class="muted-text">
            Enter a sales order ID to load its shipped quantities available for return.
        </p>

        <div class="return-grid">
            <div class="form-group">
                <label for="salesOrderId">Sales Order ID *</label>
                <input type="number" id="salesOrderId" min="1"
                       placeholder="Enter sales order ID" required>
            </div>

            <div class="form-group" style="align-self:end">
                <button type="button" class="btn btn-secondary" id="loadLinesButton">
                    Load Returnable Items
                </button>
            </div>

            <div class="form-group">
                <label for="returnDate">Return Date *</label>
                <input type="date" id="returnDate" required>
            </div>

            <div class="form-group">
                <label for="overallReason">Overall Reason</label>
                <select id="overallReason">
                    <option value="">Select reason</option>
                    <option value="DEFECTIVE_PRODUCT">Defective Product</option>
                    <option value="WRONG_ITEM">Wrong Item</option>
                    <option value="DAMAGED_IN_TRANSIT">Damaged in Transit</option>
                    <option value="CUSTOMER_REQUEST">Customer Request</option>
                    <option value="OTHER">Other</option>
                </select>
            </div>

            <div class="form-group full-width">
                <label for="returnNotes">Notes</label>
                <textarea id="returnNotes" rows="3"
                          placeholder="Optional notes about this return"></textarea>
            </div>
        </div>

        <div class="return-table-wrap" style="margin-top:22px">
            <table class="return-table">
                <thead>
                <tr>
                    <th>Item</th>
                    <th>SKU</th>
                    <th>Shipped</th>
                    <th>Available</th>
                    <th>Unit Price</th>
                    <th>Return Qty</th>
                    <th>Reason</th>
                    <th>Line Amount</th>
                </tr>
                </thead>
                <tbody id="eligibleLinesBody">
                <tr>
                    <td colspan="8" class="empty-message">
                        Enter a sales order ID and load returnable items.
                    </td>
                </tr>
                </tbody>
            </table>
        </div>

        <div class="return-total">
            <span>Total Return Amount</span>
            <strong id="returnTotal">₹0.00</strong>
        </div>

        <div class="return-footer">
            <button type="button" class="btn btn-secondary" id="cancelCreateButton">
                Cancel
            </button>
            <button type="button" class="btn btn-primary" id="createReturnButton" disabled>
                Create Return
            </button>
        </div>
    </section>

    <section class="return-panel">
        <div class="return-toolbar" style="margin-top:0">
            <div class="form-group" style="min-width:220px; margin:0">
                <label for="statusFilter">Filter by Status</label>
                <select id="statusFilter">
                    <option value="">All Returns</option>
                    <option value="CREATED">Created</option>
                    <option value="RECEIVED">Received</option>
                </select>
            </div>

            <button type="button" class="btn btn-secondary" id="refreshButton">
                Refresh
            </button>
        </div>

        <div class="return-table-wrap">
            <table class="return-table">
                <thead>
                <tr>
                    <th>Return Number</th>
                    <th>Sales Order</th>
                    <th>Customer ID</th>
                    <th>Return Date</th>
                    <th>Items</th>
                    <th>Amount</th>
                    <th>Status</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody id="returnsBody">
                <tr><td colspan="8" class="empty-message">Loading returns...</td></tr>
                </tbody>
            </table>
        </div>
    </section>

    <section id="detailPanel" class="return-panel return-hidden">
        <div class="return-toolbar" style="margin-top:0">
            <div>
                <h2 id="detailTitle">Sales Return Details</h2>
                <p class="muted-text" id="detailSubtitle"></p>
            </div>
            <button type="button" class="btn btn-secondary" id="closeDetailsButton">
                Close
            </button>
        </div>

        <div id="returnDetails"></div>

        <div class="return-footer">
            <button type="button" class="btn btn-primary return-hidden" id="receiveReturnButton">
                Receive Return
            </button>
        </div>
    </section>
</div>

<script src="/erpflow/js/salesReturns.js?v=1" defer></script>
</body>
</html>

