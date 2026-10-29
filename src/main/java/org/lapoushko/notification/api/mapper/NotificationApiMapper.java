package org.lapoushko.notification.api.mapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.lapoushko.notification.api.dto.AttachmentDto;
import org.lapoushko.notification.api.dto.NotificationRequest;
import org.lapoushko.notification.api.dto.NotificationResponse;
import org.lapoushko.notification.api.dto.NotificationStatus;
import org.lapoushko.notification.api.dto.NotificationTypeDto;
import org.lapoushko.notification.api.dto.NotificationWithStatusResponse;
import org.lapoushko.notification.domain.model.Attachment;
import org.lapoushko.notification.domain.model.Notification;
import org.lapoushko.notification.domain.model.NotificationType;

public final class NotificationApiMapper {

    private NotificationApiMapper() {
    }

    public static Notification toDomain(NotificationRequest request, UUID senderId, Instant dateOfCreate) {
        List<Attachment> attachments = request.attachments() == null
                ? List.of()
                : request.attachments().stream()
                        .map(a -> new Attachment(a.fileName(), a.fileUri(), a.contentType()))
                        .toList();
        return new Notification(
                senderId,
                request.recipientId(),
                request.recipientMail(),
                request.recipientPhoneNumber(),
                request.recipientDeviceToken(),
                request.notificationTypes().stream().map(NotificationApiMapper::toDomain).toList(),
                request.content(),
                attachments,
                dateOfCreate);
    }

    public static NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getSenderId(),
                notification.getRecipientId(),
                notification.getNotificationTypes().stream().map(NotificationApiMapper::toDto).toList(),
                notification.getContent(),
                toAttachmentDtos(notification.getAttachments()),
                notification.getDateOfCreate(),
                notification.getDateOfUpdate());
    }

    public static NotificationWithStatusResponse toWithStatusResponse(
            Notification notification, Map<NotificationType, Integer> statuses) {
        List<NotificationStatus> notificationStatuses = notification.getNotificationTypes().stream()
                .map(type -> new NotificationStatus(type.getCode(), statuses.getOrDefault(type, 0)))
                .toList();
        return new NotificationWithStatusResponse(
                notification.getId(),
                notification.getSenderId(),
                notification.getRecipientId(),
                notificationStatuses,
                notification.getContent(),
                toAttachmentDtos(notification.getAttachments()),
                notification.getDateOfCreate(),
                notification.getDateOfUpdate());
    }

    static NotificationType toDomain(NotificationTypeDto dto) {
        return NotificationType.valueOf(dto.name());
    }

    static NotificationTypeDto toDto(NotificationType type) {
        return NotificationTypeDto.valueOf(type.name());
    }

    private static List<AttachmentDto> toAttachmentDtos(List<Attachment> attachments) {
        return attachments.stream()
                .map(a -> new AttachmentDto(a.fileName(), a.fileUri(), a.contentType()))
                .toList();
    }
}
