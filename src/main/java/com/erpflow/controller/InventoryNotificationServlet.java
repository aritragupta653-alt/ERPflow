package com.erpflow.controller;

import com.erpflow.dao.InventoryNotificationDAO;
import com.erpflow.model.InventoryNotification;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/inventory-notifications/*")
public class InventoryNotificationServlet extends HttpServlet {

    private final InventoryNotificationDAO notificationDAO =
            new InventoryNotificationDAO();

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());


    // =====================================================
    // GET
    // =====================================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            String path =
                    request.getPathInfo();


            // =================================================
            // GET /api/inventory-notifications
            // =================================================

            if (path == null
                    || path.equals("/")
                    || path.isBlank()) {

                List<InventoryNotification> notifications =
                        notificationDAO.findAll();

                objectMapper.writeValue(
                        response.getWriter(),
                        notifications
                );

                return;
            }


            // =================================================
            // GET /api/inventory-notifications/unread
            // =================================================

            if (path.equals("/unread")) {

                List<InventoryNotification> notifications =
                        notificationDAO.findUnread();

                objectMapper.writeValue(
                        response.getWriter(),
                        notifications
                );

                return;
            }


            // =================================================
            // GET /api/inventory-notifications/unread-count
            // =================================================

            if (path.equals("/unread-count")) {

                int count =
                        notificationDAO.getUnreadCount();

                objectMapper.writeValue(
                        response.getWriter(),
                        new UnreadCountResponse(count)
                );

                return;
            }


            response.setStatus(
                    HttpServletResponse.SC_NOT_FOUND
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    new ErrorResponse(
                            "Notification endpoint not found."
                    )
            );


        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    new ErrorResponse(
                            e.getMessage()
                    )
            );
        }
    }


    // =====================================================
    // PUT
    // =====================================================

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            String path =
                    request.getPathInfo();


            // =================================================
            // PUT /api/inventory-notifications/read-all
            // =================================================

            if ("/read-all".equals(path)) {

                notificationDAO.markAllAsRead();

                objectMapper.writeValue(
                        response.getWriter(),
                        new SuccessResponse(
                                "All notifications marked as read."
                        )
                );

                return;
            }


            // =================================================
            // PUT /api/inventory-notifications/{id}/read
            // =================================================

            if (path != null
                    && path.matches("/\\d+/read")) {

                String idPart =
                        path.substring(
                                1,
                                path.length() - 5
                        );

                long notificationId =
                        Long.parseLong(idPart);


                boolean updated =
                        notificationDAO.markAsRead(
                                notificationId
                        );


                if (!updated) {

                    response.setStatus(
                            HttpServletResponse.SC_NOT_FOUND
                    );

                    objectMapper.writeValue(
                            response.getWriter(),
                            new ErrorResponse(
                                    "Notification not found."
                            )
                    );

                    return;
                }


                objectMapper.writeValue(
                        response.getWriter(),
                        new SuccessResponse(
                                "Notification marked as read."
                        )
                );

                return;
            }


            response.setStatus(
                    HttpServletResponse.SC_NOT_FOUND
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    new ErrorResponse(
                            "Notification endpoint not found."
                    )
            );


        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    new ErrorResponse(
                            "Invalid notification ID."
                    )
            );


        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            objectMapper.writeValue(
                    response.getWriter(),
                    new ErrorResponse(
                            e.getMessage()
                    )
            );
        }
    }


    // =====================================================
    // RESPONSE CLASSES
    // =====================================================

    public static class UnreadCountResponse {

        private int count;

        public UnreadCountResponse(int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }


    public static class SuccessResponse {

        private String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
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