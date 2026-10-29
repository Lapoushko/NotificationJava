package org.lapoushko.notification.contracts.mail;

import java.util.List;
import java.util.UUID;

public record SendMailNotificationRequest(
        UUID id,
        String message,
        List<AttachmentDto> attachments,
        UUID recipientId,
        String recipientMail,
        UUID senderId) {
}
