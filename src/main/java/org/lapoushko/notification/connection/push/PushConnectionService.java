package org.lapoushko.notification.connection.push;

import org.lapoushko.notification.contracts.push.SendPushNotificationRequest;

public interface PushConnectionService {

    void sendNotification(SendPushNotificationRequest request);
}
