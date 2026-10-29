package org.lapoushko.notification.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record NotificationWithStatusResponse(
        UUID id,
        UUID senderId,
        UUID recipientId,
        List<NotificationStatus> statuses,
        String content,
        List<AttachmentDto> attachments,
        Instant dateOfCreate,
        Instant dateOfUpdate) {
}
