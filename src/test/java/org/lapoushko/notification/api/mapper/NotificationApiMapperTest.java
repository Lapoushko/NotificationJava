package org.lapoushko.notification.api.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.lapoushko.notification.api.dto.AttachmentDto;
import org.lapoushko.notification.api.dto.NotificationRequest;
import org.lapoushko.notification.api.dto.NotificationResponse;
import org.lapoushko.notification.api.dto.NotificationStatus;
import org.lapoushko.notification.api.dto.NotificationTypeDto;
import org.lapoushko.notification.api.dto.NotificationWithStatusResponse;
import org.lapoushko.notification.domain.model.Attachment;
import org.lapoushko.notification.domain.model.Notification;
import org.lapoushko.notification.domain.model.NotificationType;

class NotificationApiMapperTest {

    private static final UUID SENDER_ID = UUID.randomUUID();
    private static final UUID RECIPIENT_ID = UUID.randomUUID();
    private static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");

    @Test
    void toDomain_mapsRequestFields() {
        NotificationRequest request = new NotificationRequest(
                RECIPIENT_ID,
                "user@example.com",
                "+79991234567",
                "device-token",
                List.of(NotificationTypeDto.EMAIL, NotificationTypeDto.PUSH),
                "Привет",
                List.of(new AttachmentDto("a.txt", "https://files/a.txt", "text/plain")));

        Notification notification = NotificationApiMapper.toDomain(request, SENDER_ID, NOW);

        assertThat(notification.getId()).isNotNull();
        assertThat(notification.getSenderId()).isEqualTo(SENDER_ID);
        assertThat(notification.getRecipientId()).isEqualTo(RECIPIENT_ID);
        assertThat(notification.getRecipientMail()).isEqualTo("user@example.com");
        assertThat(notification.getRecipientPhoneNumber()).isEqualTo("+79991234567");
        assertThat(notification.getRecipientDeviceToken()).isEqualTo("device-token");
        assertThat(notification.getNotificationTypes()).containsExactly(NotificationType.EMAIL, NotificationType.PUSH);
        assertThat(notification.getContent()).isEqualTo("Привет");
        assertThat(notification.getAttachments()).containsExactly(new Attachment("a.txt", "https://files/a.txt", "text/plain"));
        assertThat(notification.getDateOfCreate()).isEqualTo(NOW);
        assertThat(notification.getDateOfUpdate()).isEqualTo(NOW);
    }

    @Test
    void toDomain_treatsMissingAttachmentsAsEmpty() {
        NotificationRequest request = new NotificationRequest(
                RECIPIENT_ID, null, null, null, List.of(NotificationTypeDto.SMS), "Привет", null);

        assertThat(NotificationApiMapper.toDomain(request, SENDER_ID, NOW).getAttachments()).isEmpty();
    }

    @Test
    void toResponse_mapsDomainFields() {
        Notification notification = notification();

        NotificationResponse response = NotificationApiMapper.toResponse(notification);

        assertThat(response).isEqualTo(new NotificationResponse(
                notification.getId(),
                SENDER_ID,
                RECIPIENT_ID,
                List.of(NotificationTypeDto.EMAIL, NotificationTypeDto.SMS),
                "Привет",
                List.of(new AttachmentDto("a.txt", "https://files/a.txt", "text/plain")),
                NOW,
                NOW));
    }

    @Test
    void toWithStatusResponse_usesZeroForChannelsWithoutStatus() {
        Notification notification = notification();

        NotificationWithStatusResponse response = NotificationApiMapper.toWithStatusResponse(
                notification, Map.of(NotificationType.EMAIL, 3));

        assertThat(response.statuses()).containsExactly(
                new NotificationStatus(NotificationType.EMAIL.getCode(), 3),
                new NotificationStatus(NotificationType.SMS.getCode(), 0));
        assertThat(response.id()).isEqualTo(notification.getId());
        assertThat(response.content()).isEqualTo("Привет");
    }

    private static Notification notification() {
        return new Notification(
                SENDER_ID,
                RECIPIENT_ID,
                "user@example.com",
                "+79991234567",
                null,
                List.of(NotificationType.EMAIL, NotificationType.SMS),
                "Привет",
                List.of(new Attachment("a.txt", "https://files/a.txt", "text/plain")),
                NOW);
    }
}
