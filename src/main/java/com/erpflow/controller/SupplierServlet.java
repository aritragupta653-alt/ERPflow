package com.erpflow.controller;

import com.erpflow.model.Supplier;
import com.erpflow.service.SupplierService;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/suppliers/*")
public class SupplierServlet extends HttpServlet {

    private final SupplierService supplierService =
            new SupplierService();

    private final ObjectMapper objectMapper = new ObjectMapper()
                        .registerModule(
                                        new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());


    // ========================================
    // GET
    // ========================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        setJsonResponse(response);


        String pathInfo =
                request.getPathInfo();


        try {

            // GET /api/suppliers

            if (pathInfo == null ||
                    pathInfo.equals("/")) {

                List<Supplier> suppliers =
                        supplierService
                                .getAllSuppliers();


                objectMapper.writeValue(
                        response.getWriter(),
                        suppliers
                );

                return;
            }


            // GET /api/suppliers/{id}

            int id =
                    Integer.parseInt(
                            pathInfo.substring(1)
                    );


            Supplier supplier =
                    supplierService
                            .getSupplierById(id);


            if (supplier == null) {

                sendError(
                        response,
                        404,
                        "Supplier not found"
                );

                return;
            }


            objectMapper.writeValue(
                    response.getWriter(),
                    supplier
            );


        } catch (NumberFormatException e) {

            sendError(
                    response,
                    400,
                    "Invalid supplier ID"
            );

        } catch (RuntimeException e) {

            e.printStackTrace();

            sendError(
                    response,
                    400,
                    e.getMessage()
            );
        }
    }


    // ========================================
    // POST
    // ========================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        setJsonResponse(response);


        try {

            Supplier supplier =
                    objectMapper.readValue(
                            request.getReader(),
                            Supplier.class
                    );


            supplierService.addSupplier(
                    supplier
            );


            response.setStatus(
                    HttpServletResponse.SC_CREATED
            );


            objectMapper.writeValue(
                    response.getWriter(),
                    supplier
            );


        } catch (RuntimeException e) {

            e.printStackTrace();

            sendError(
                    response,
                    400,
                    e.getMessage()
            );
        }
    }


    // ========================================
    // PUT
    // ========================================

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        setJsonResponse(response);


        String pathInfo =
                request.getPathInfo();


        if (pathInfo == null ||
                pathInfo.equals("/")) {

            sendError(
                    response,
                    400,
                    "Supplier ID is required"
            );

            return;
        }


        try {

            int id =
                    Integer.parseInt(
                            pathInfo.substring(1)
                    );


            Supplier supplier =
                    objectMapper.readValue(
                            request.getReader(),
                            Supplier.class
                    );


            supplier.setId(id);


            Supplier existing =
                    supplierService
                            .getSupplierById(id);


            if (existing == null) {

                sendError(
                        response,
                        404,
                        "Supplier not found"
                );

                return;
            }


            supplierService.updateSupplier(
                    supplier
            );


            objectMapper.writeValue(
                    response.getWriter(),
                    supplier
            );


        } catch (NumberFormatException e) {

            sendError(
                    response,
                    400,
                    "Invalid supplier ID"
            );

        } catch (RuntimeException e) {

            e.printStackTrace();

            sendError(
                    response,
                    400,
                    e.getMessage()
            );
        }
    }


    // ========================================
    // DELETE
    // ========================================

    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        setJsonResponse(response);


        String pathInfo =
                request.getPathInfo();


        if (pathInfo == null ||
                pathInfo.equals("/")) {

            sendError(
                    response,
                    400,
                    "Supplier ID is required"
            );

            return;
        }


        try {

            int id =
                    Integer.parseInt(
                            pathInfo.substring(1)
                    );


            Supplier existing =
                    supplierService
                            .getSupplierById(id);


            if (existing == null) {

                sendError(
                        response,
                        404,
                        "Supplier not found"
                );

                return;
            }


            supplierService.deleteSupplier(
                    id
            );


            response.setStatus(
                    HttpServletResponse.SC_NO_CONTENT
            );


        } catch (NumberFormatException e) {

            sendError(
                    response,
                    400,
                    "Invalid supplier ID"
            );

        } catch (RuntimeException e) {

            e.printStackTrace();

            sendError(
                    response,
                    400,
                    e.getMessage()
            );
        }
    }


    // ========================================
    // JSON RESPONSE
    // ========================================

    private void setJsonResponse(
            HttpServletResponse response
    ) {

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );
    }


    // ========================================
    // ERROR
    // ========================================

    private void sendError(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        response.setStatus(status);


        objectMapper.writeValue(
                response.getWriter(),
                Map.of(
                        "error",
                        message != null
                                ? message
                                : "Unknown error"
                )
        );
    }
}