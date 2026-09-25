package com.erpflow.service;

import com.erpflow.dao.OrderFulfillmentReportDAO;
import com.erpflow.model.OrderFulfillmentReport;

import java.sql.SQLException;
import java.util.List;

public class OrderFulfillmentReportService {

    private final OrderFulfillmentReportDAO reportDAO =
            new OrderFulfillmentReportDAO();


    public List<OrderFulfillmentReport> getReport(
            String fromDate,
            String toDate,
            String status) {

        return reportDAO.getReport(
                fromDate,
                toDate,
                status
        );
    }
    public List<OrderFulfillmentReport> getSummaryReport(
        String fromDate,
        String toDate,
        String status) throws SQLException {

    return reportDAO.getSummaryReport(
            fromDate,
            toDate,
            status
    );
}
public List<OrderFulfillmentReport> getItemReport(
        String fromDate,
        String toDate,
        String status) throws SQLException {

    return reportDAO.getItemReport(
            fromDate,
            toDate,
            status
    );
}
}