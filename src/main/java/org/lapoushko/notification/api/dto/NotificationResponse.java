package org.lapoushko.notification.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID senderId,
        UUID recipientId,
        List<NotificationTypeDto> notificationTypes,
        String content,
        List<AttachmentDto> attachments,
        Instant dateOfCreate,
        Instant dateOfUpdate) {
}
