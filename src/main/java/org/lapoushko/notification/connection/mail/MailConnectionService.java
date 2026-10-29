package org.lapoushko.notification.connection.mail;

import java.util.Optional;
import org.lapoushko.notification.contracts.mail.GetMailStatusRequest;
import org.lapoushko.notification.contracts.mail.GetMailStatusResponse;
import org.lapoushko.notification.contracts.mail.SendMailNotificationRequest;

public interface MailConnectionService {

    void sendNotification(SendMailNotificationRequest request);

    Optional<GetMailStatusResponse> getMailStatus(GetMailStatusRequest request);
}
