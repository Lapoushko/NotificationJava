package org.lapoushko.notification.infrastructure.persistence;

import java.util.Arrays;
import org.lapoushko.notification.domain.model.Attachment;
import org.lapoushko.notification.domain.model.Notification;
import org.lapoushko.notification.domain.model.NotificationType;

final class NotificationEntityMapper {

    private NotificationEntityMapper() {
    }

    static NotificationEntity toEntity(Notification notification) {
        return new NotificationEntity(
                notification.getId(),
                notification.getSenderId(),
                notification.getRecipientId(),
                notification.getRecipientMail(),
                notification.getRecipientPhoneNumber(),
                notification.getRecipientDeviceToken(),
                notification.getNotificationTypes().stream().map(NotificationType::getCode).toArray(Integer[]::new),
                notification.getContent(),
                notification.getAttachments().stream()
                        .map(a -> new AttachmentEmbeddable(a.fileName(), a.fileUri(), a.contentType()))
                        .toList(),
                notification.getDateOfCreate(),
                notification.getDateOfUpdate());
    }

    static Notification toDomain(NotificationEntity entity) {
        return Notification.restore(
                entity.getId(),
                entity.getSenderId(),
                entity.getRecipientId(),
                entity.getRecipientMail(),
                entity.getRecipientPhoneNumber(),
                entity.getRecipientDeviceToken(),
                Arrays.stream(entity.getNotificationTypes()).map(NotificationType::fromCode).toList(),
                entity.getContent(),
                entity.getAttachments().stream()
                        .map(a -> new Attachment(a.getFileName(), a.getFileUri(), a.getContentType()))
                        .toList(),
                entity.getDateOfCreate(),
                entity.getDateOfUpdate());
    }
}
