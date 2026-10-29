package org.lapoushko.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.lapoushko.notification.domain.model.NotificationType.EMAIL;
import static org.lapoushko.notification.domain.model.NotificationType.PUSH;
import static org.lapoushko.notification.domain.model.NotificationType.SMS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.lapoushko.notification.connection.mail.MailConnectionService;
import org.lapoushko.notification.connection.push.PushConnectionService;
import org.lapoushko.notification.connection.sms.SmsConnectionService;
import org.lapoushko.notification.contracts.mail.AttachmentDto;
import org.lapoushko.notification.contracts.mail.GetMailStatusRequest;
import org.lapoushko.notification.contracts.mail.GetMailStatusResponse;
import org.lapoushko.notification.contracts.mail.SendMailNotificationRequest;
import org.lapoushko.notification.contracts.push.SendPushNotificationRequest;
import org.lapoushko.notification.contracts.sms.SendSmsNotificationRequest;
import org.lapoushko.notification.domain.model.Attachment;
import org.lapoushko.notification.domain.model.Notification;
import org.lapoushko.notification.domain.model.NotificationType;
import org.lapoushko.notification.domain.repository.NotificationRepository;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    private static final UUID SENDER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID RECIPIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-01-15T10:00:00Z");
    private static final Attachment ATTACHMENT = new Attachment("invoice.pdf", "https://files/invoice.pdf", "application/pdf");

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private MailConnectionService mailConnectionService;
    @Mock
    private SmsConnectionService smsConnectionService;
    @Mock
    private PushConnectionService pushConnectionService;

    @InjectMocks
    private NotificationServiceImpl service;

    @Test
    void sendNotification_savesNotificationThenPublishesToEveryRequestedChannel() {
        Notification notification = notification(List.of(EMAIL, SMS, PUSH));

        service.sendNotification(notification);

        InOrder inOrder = inOrder(notificationRepository, mailConnectionService, smsConnectionService, pushConnectionService);
        inOrder.verify(notificationRepository).save(notification);
        inOrder.verify(mailConnectionService).sendNotification(new SendMailNotificationRequest(
                notification.getId(),
                "Ваш заказ отправлен",
                List.of(new AttachmentDto("invoice.pdf", "https://files/invoice.pdf", "application/pdf")),
                RECIPIENT_ID,
                "user@example.com",
                SENDER_ID));
        inOrder.verify(smsConnectionService).sendNotification(new SendSmsNotificationRequest(
                notification.getId(), SENDER_ID, RECIPIENT_ID, "+79991234567", "Ваш заказ отправлен"));
        inOrder.verify(pushConnectionService).sendNotification(new SendPushNotificationRequest(
                notification.getId(), NotificationServiceImpl.PUSH_TITLE, "device-token", "Ваш заказ отправлен"));
    }

    @Test
    void sendNotification_publishesOnlyToRequestedChannels() {
        Notification notification = notification(List.of(SMS));

        service.sendNotification(notification);

        verify(notificationRepository).save(notification);
        verify(smsConnectionService).sendNotification(any());
        verifyNoInteractions(mailConnectionService, pushConnectionService);
    }

    @ParameterizedTest
    @EnumSource(value = NotificationType.class, names = {"EMAIL", "SMS", "PUSH"})
    void sendNotification_rejectsChannelWithoutRecipient(NotificationType type) {
        Notification notification = new Notification(
                SENDER_ID, RECIPIENT_ID, null, " ", null, List.of(type), "Текст", List.of(), NOW);

        assertThatThrownBy(() -> service.sendNotification(notification))
                .isInstanceOf(InvalidNotificationException.class)
                .hasMessageContaining(type.name());

        verifyNoInteractions(notificationRepository, mailConnectionService, smsConnectionService, pushConnectionService);
    }

    @Test
    void sendNotification_validatesAllChannelsBeforeSavingOrPublishing() {
        Notification notification = new Notification(
                SENDER_ID, RECIPIENT_ID, "user@example.com", null, null, List.of(EMAIL, SMS), "Текст", List.of(), NOW);

        assertThatThrownBy(() -> service.sendNotification(notification))
                .isInstanceOf(InvalidNotificationException.class)
                .hasMessageContaining("recipientPhoneNumber");

        verifyNoInteractions(notificationRepository, mailConnectionService, smsConnectionService, pushConnectionService);
    }

    @Test
    void getStatuses_returnsMailStatusForEmailChannel() {
        UUID id = UUID.randomUUID();
        when(mailConnectionService.getMailStatus(new GetMailStatusRequest(id)))
                .thenReturn(Optional.of(new GetMailStatusResponse(3)));

        Map<NotificationType, Integer> statuses = service.getStatuses(id, List.of(EMAIL, SMS));

        assertThat(statuses).containsExactly(entry(EMAIL, 3));
    }

    @Test
    void getStatuses_omitsEmailWhenMailServiceDoesNotRespond() {
        UUID id = UUID.randomUUID();
        when(mailConnectionService.getMailStatus(new GetMailStatusRequest(id))).thenReturn(Optional.empty());

        assertThat(service.getStatuses(id, List.of(EMAIL))).isEmpty();
    }

    @Test
    void getStatuses_doesNotQueryMailServiceWithoutEmailChannel() {
        assertThat(service.getStatuses(UUID.randomUUID(), List.of(SMS, PUSH))).isEmpty();

        verifyNoInteractions(mailConnectionService);
    }

    @Test
    void getById_delegatesToRepository() {
        Notification notification = notification(List.of(EMAIL));
        when(notificationRepository.findById(notification.getId())).thenReturn(Optional.of(notification));

        assertThat(service.getById(notification.getId())).contains(notification);
    }

    @Test
    void getAll_delegatesToRepository() {
        Notification notification = notification(List.of(PUSH));
        when(notificationRepository.findAll()).thenReturn(List.of(notification));

        assertThat(service.getAll()).containsExactly(notification);
    }

    private static Notification notification(List<NotificationType> types) {
        return new Notification(
                SENDER_ID,
                RECIPIENT_ID,
                "user@example.com",
                "+79991234567",
                "device-token",
                types,
                "Ваш заказ отправлен",
                List.of(ATTACHMENT),
                NOW);
    }
}
