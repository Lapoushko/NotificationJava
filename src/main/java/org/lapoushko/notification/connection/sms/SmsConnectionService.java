package org.lapoushko.notification.connection.sms;

import org.lapoushko.notification.contracts.sms.SendSmsNotificationRequest;

public interface SmsConnectionService {

    void sendNotification(SendSmsNotificationRequest request);
}
