package org.lapoushko.notification;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lapoushko.notification.api.dto.AttachmentDto;
import org.lapoushko.notification.api.dto.NotificationRequest;
import org.lapoushko.notification.api.dto.NotificationResponse;
import org.lapoushko.notification.api.dto.NotificationStatus;
import org.lapoushko.notification.api.dto.NotificationTypeDto;
import org.lapoushko.notification.api.dto.NotificationWithStatusResponse;
import org.lapoushko.notification.contracts.mail.GetMailStatusRequest;
import org.lapoushko.notification.contracts.mail.GetMailStatusResponse;
import org.lapoushko.notification.contracts.mail.SendMailNotificationRequest;
import org.lapoushko.notification.contracts.push.SendPushNotificationRequest;
import org.lapoushko.notification.contracts.sms.SendSmsNotificationRequest;
import org.lapoushko.notification.domain.DataConstants;
import org.lapoushko.notification.infrastructure.messaging.RabbitMqProperties;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({TestcontainersConfiguration.class, NotificationApiIT.MailStatusResponder.class})
@Testcontainers(disabledWithoutDocker = true)
class NotificationApiIT {

    private static final String URL = "/api/v1/notifications";
    private static final int MAIL_STATUS = 2;
    private static final long RECEIVE_TIMEOUT_MS = 5_000;
    private static final UUID RECIPIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final AttachmentDto ATTACHMENT =
            new AttachmentDto("invoice.pdf", "https://files/invoice.pdf", "application/pdf");

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private AmqpAdmin amqpAdmin;
    @Autowired
    private RabbitMqProperties properties;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM notifications");
        List.of(properties.mailSend(), properties.smsSend(), properties.pushSend())
                .forEach(route -> amqpAdmin.purgeQueue(route.queue(), false));
    }

    @Test
    void sendNotification_storesNotificationAndPublishesMessageToEveryChannel() {
        ResponseEntity<UUID> response = restTemplate.postForEntity(URL,
                request(List.of(NotificationTypeDto.EMAIL, NotificationTypeDto.SMS, NotificationTypeDto.PUSH)), UUID.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UUID id = response.getBody();
        assertThat(id).isNotNull();

        assertThat(receive(properties.mailSend(), new ParameterizedTypeReference<SendMailNotificationRequest>() {
        })).isEqualTo(new SendMailNotificationRequest(
                id,
                "Ваш заказ отправлен",
                List.of(new org.lapoushko.notification.contracts.mail.AttachmentDto(
                        ATTACHMENT.fileName(), ATTACHMENT.fileUri(), ATTACHMENT.contentType())),
                RECIPIENT_ID,
                "user@example.com",
                DataConstants.ADMIN_USER_ID));
        assertThat(receive(properties.smsSend(), new ParameterizedTypeReference<SendSmsNotificationRequest>() {
        })).isEqualTo(new SendSmsNotificationRequest(
                id, DataConstants.ADMIN_USER_ID, RECIPIENT_ID, "+79991234567", "Ваш заказ отправлен"));
        assertThat(receive(properties.pushSend(), new ParameterizedTypeReference<SendPushNotificationRequest>() {
        })).isEqualTo(new SendPushNotificationRequest(id, "Title", "device-token", "Ваш заказ отправлен"));

        NotificationResponse stored = restTemplate.getForObject(URL + "/" + id, NotificationResponse.class);
        assertThat(stored.notificationTypes())
                .containsExactly(NotificationTypeDto.EMAIL, NotificationTypeDto.SMS, NotificationTypeDto.PUSH);
        assertThat(stored.attachments()).containsExactly(ATTACHMENT);
        assertThat(stored.senderId()).isEqualTo(DataConstants.ADMIN_USER_ID);

        NotificationResponse[] all = restTemplate.getForObject(URL, NotificationResponse[].class);
        assertThat(all).extracting(NotificationResponse::id).containsExactly(id);
    }

    @Test
    void getNotificationWithStatus_returnsStatusReportedByMailService() {
        UUID id = restTemplate.postForObject(URL,
                request(List.of(NotificationTypeDto.EMAIL, NotificationTypeDto.SMS)), UUID.class);

        NotificationWithStatusResponse response =
                restTemplate.getForObject(URL + "/" + id + "/status", NotificationWithStatusResponse.class);

        assertThat(response.statuses()).containsExactly(
                new NotificationStatus(NotificationTypeDto.EMAIL.getCode(), MAIL_STATUS),
                new NotificationStatus(NotificationTypeDto.SMS.getCode(), 0));
    }

    @Test
    void sendNotification_withoutRequiredRecipient_returnsBadRequestAndPublishesNothing() {
        NotificationRequest request = new NotificationRequest(
                RECIPIENT_ID, null, null, null, List.of(NotificationTypeDto.EMAIL), "Текст", null);

        ResponseEntity<String> response = restTemplate.postForEntity(URL, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(restTemplate.getForEntity(URL, String.class).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(rabbitTemplate.receive(properties.mailSend().queue(), 500)).isNull();
    }

    @Test
    void getNotificationById_returnsNoContentForUnknownId() {
        ResponseEntity<String> response = restTemplate.getForEntity(URL + "/" + UUID.randomUUID(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private <T> T receive(RabbitMqProperties.Route route, ParameterizedTypeReference<T> type) {
        return rabbitTemplate.receiveAndConvert(route.queue(), RECEIVE_TIMEOUT_MS, type);
    }

    private static NotificationRequest request(List<NotificationTypeDto> types) {
        return new NotificationRequest(
                RECIPIENT_ID,
                "user@example.com",
                "+79991234567",
                "device-token",
                types,
                "Ваш заказ отправлен",
                List.of(ATTACHMENT));
    }

    // заглушка вместо mail-сервиса
    @TestConfiguration(proxyBeanMethods = false)
    static class MailStatusResponder {

        @RabbitListener(queues = "${notification.rabbitmq.mail-status.queue}")
        GetMailStatusResponse reply(GetMailStatusRequest request) {
            return new GetMailStatusResponse(MAIL_STATUS);
        }
    }
}
