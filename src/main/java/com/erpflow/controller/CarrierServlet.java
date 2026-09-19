package com.erpflow.controller;

import com.erpflow.model.Carrier;
import com.erpflow.model.CarrierService;
import com.erpflow.service.CarrierServiceService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/carriers/*")
public class CarrierServlet extends HttpServlet {

    private final CarrierServiceService service =
            new CarrierServiceService();

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            String path = request.getPathInfo();

            if (path == null || path.equals("/")) {

                List<Carrier> carriers =
                        service.getAllCarriers();

                objectMapper.writeValue(
                        response.getWriter(),
                        carriers
                );

                return;
            }

            String[] parts =
                    path.substring(1).split("/");

            int carrierId =
                    Integer.parseInt(parts[0]);

            // GET /api/carriers/{id}/services
            if (parts.length == 2 &&
                    parts[1].equals("services")) {

                List<CarrierService> services =
                        service.getServicesByCarrier(carrierId);

                objectMapper.writeValue(
                        response.getWriter(),
                        services
                );

                return;
            }

            // GET /api/carriers/{id}
            Carrier carrier =
                    service.getCarrierById(carrierId);

            if (carrier == null) {

                response.setStatus(
                        HttpServletResponse.SC_NOT_FOUND);

                objectMapper.writeValue(
                        response.getWriter(),
                        new ErrorResponse("Carrier not found")
                );

                return;
            }

            objectMapper.writeValue(
                    response.getWriter(),
                    carrier
            );

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            objectMapper.writeValue(
                    response.getWriter(),
                    new ErrorResponse(e.getMessage())
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            String path = request.getPathInfo();

            if (path != null &&
                    path.matches("/\\d+/services")) {

                int carrierId = Integer.parseInt(
                        path.split("/")[1]
                );

                CarrierService carrierService =
                        objectMapper.readValue(
                                request.getReader(),
                                CarrierService.class
                        );

                Carrier carrier = new Carrier();
                carrier.setId(carrierId);

                carrierService.setCarrier(carrier);

                service.createCarrierService(
                        carrierService
                );

                response.setStatus(
                        HttpServletResponse.SC_CREATED);

                objectMapper.writeValue(
                        response.getWriter(),
                        carrierService
                );

                return;
            }

            Carrier carrier =
                    objectMapper.readValue(
                            request.getReader(),
                            Carrier.class
                    );

            service.createCarrier(carrier);

            response.setStatus(
                    HttpServletResponse.SC_CREATED);

            objectMapper.writeValue(
                    response.getWriter(),
                    carrier
            );

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            objectMapper.writeValue(
                    response.getWriter(),
                    new ErrorResponse(e.getMessage())
            );
        }
    }

    public static class ErrorResponse {

        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}