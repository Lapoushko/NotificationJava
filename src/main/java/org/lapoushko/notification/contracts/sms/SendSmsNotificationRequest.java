package org.lapoushko.notification.contracts.sms;

import java.util.UUID;

public record SendSmsNotificationRequest(UUID id, UUID senderId, UUID recipientId, String phoneNumber, String body) {
}
