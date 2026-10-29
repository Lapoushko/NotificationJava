package org.lapoushko.notification.contracts.push;

import java.util.UUID;

public record SendPushNotificationRequest(UUID id, String title, String deviceToken, String body) {
}
