package com.invoiceapp.notification;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

public class NotificationDTO {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private UUID id;
        private String type;
        private String title;
        private String body;
        private String status;
        private String actionUrl;
        private Boolean isRead;
        private Instant sentAt;
        private Instant readAt;
    }

    public static Response toResponse(Notification n) {
        return Response.builder()
                .id(n.getId()).type(n.getType()).title(n.getTitle()).body(n.getBody())
                .status(n.getStatus()).actionUrl(n.getActionUrl()).isRead(n.getIsRead())
                .sentAt(n.getSentAt()).readAt(n.getReadAt()).build();
    }
}
