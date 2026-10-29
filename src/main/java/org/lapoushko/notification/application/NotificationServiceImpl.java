package org.lapoushko.notification.application;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.lapoushko.notification.connection.mail.MailConnectionService;
import org.lapoushko.notification.connection.push.PushConnectionService;
import org.lapoushko.notification.connection.sms.SmsConnectionService;
import org.lapoushko.notification.contracts.mail.AttachmentDto;
import org.lapoushko.notification.contracts.mail.GetMailStatusRequest;
import org.lapoushko.notification.contracts.mail.SendMailNotificationRequest;
import org.lapoushko.notification.contracts.push.SendPushNotificationRequest;
import org.lapoushko.notification.contracts.sms.SendSmsNotificationRequest;
import org.lapoushko.notification.domain.model.Notification;
import org.lapoushko.notification.domain.model.NotificationType;
import org.lapoushko.notification.domain.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {

    static final String PUSH_TITLE = "Title";

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;
    private final MailConnectionService mailConnectionService;
    private final SmsConnectionService smsConnectionService;
    private final PushConnectionService pushConnectionService;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            MailConnectionService mailConnectionService,
            SmsConnectionService smsConnectionService,
            PushConnectionService pushConnectionService) {
        this.notificationRepository = notificationRepository;
        this.mailConnectionService = mailConnectionService;
        this.smsConnectionService = smsConnectionService;
        this.pushConnectionService = pushConnectionService;
    }

    @Override
    @Transactional
    public void sendNotification(Notification notification) {
        validateRecipients(notification);

        notificationRepository.save(notification);

        if (notification.hasType(NotificationType.EMAIL)) {
            List<AttachmentDto> attachments = notification.getAttachments().stream()
                    .map(a -> new AttachmentDto(a.fileName(), a.fileUri(), a.contentType()))
                    .toList();
            mailConnectionService.sendNotification(new SendMailNotificationRequest(
                    notification.getId(),
                    notification.getContent(),
                    attachments,
                    notification.getRecipientId(),
                    notification.getRecipientMail(),
                    notification.getSenderId()));
        }
        if (notification.hasType(NotificationType.SMS)) {
            smsConnectionService.sendNotification(new SendSmsNotificationRequest(
                    notification.getId(),
                    notification.getSenderId(),
                    notification.getRecipientId(),
                    notification.getRecipientPhoneNumber(),
                    notification.getContent()));
        }
        if (notification.hasType(NotificationType.PUSH)) {
            pushConnectionService.sendNotification(new SendPushNotificationRequest(
                    notification.getId(),
                    PUSH_TITLE,
                    notification.getRecipientDeviceToken(),
                    notification.getContent()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Notification> getById(UUID id) {
        return notificationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getAll() {
        return notificationRepository.findAll();
    }

    @Override
    public Map<NotificationType, Integer> getStatuses(UUID id, List<NotificationType> types) {
        Map<NotificationType, Integer> statuses = new EnumMap<>(NotificationType.class);
        if (types.contains(NotificationType.EMAIL)) {
            mailConnectionService.getMailStatus(new GetMailStatusRequest(id)).ifPresentOrElse(
                    response -> statuses.put(NotificationType.EMAIL, response.status()),
                    () -> log.warn("Почтовый сервис не вернул статус уведомления {}", id));
        }
        return statuses;
    }

    private static void validateRecipients(Notification notification) {
        if (notification.hasType(NotificationType.EMAIL)) {
            requireNotBlank(notification.getRecipientMail(), "recipientMail", NotificationType.EMAIL);
        }
        if (notification.hasType(NotificationType.SMS)) {
            requireNotBlank(notification.getRecipientPhoneNumber(), "recipientPhoneNumber", NotificationType.SMS);
        }
        if (notification.hasType(NotificationType.PUSH)) {
            requireNotBlank(notification.getRecipientDeviceToken(), "recipientDeviceToken", NotificationType.PUSH);
        }
    }

    private static void requireNotBlank(String value, String field, NotificationType type) {
        if (value == null || value.isBlank()) {
            throw new InvalidNotificationException("Поле %s обязательно для уведомления типа %s".formatted(field, type));
        }
    }
}
