package org.lapoushko.notification.connection.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lapoushko.notification.contracts.mail.GetMailStatusRequest;
import org.lapoushko.notification.contracts.mail.GetMailStatusResponse;
import org.lapoushko.notification.contracts.mail.SendMailNotificationRequest;
import org.lapoushko.notification.infrastructure.messaging.RabbitMqProperties;
import org.lapoushko.notification.infrastructure.messaging.RabbitMqProperties.Route;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;

@ExtendWith(MockitoExtension.class)
class RabbitMqMailConnectionServiceTest {

    private static final RabbitMqProperties PROPERTIES = new RabbitMqProperties(
            "notifications",
            new Route("q.mail.send", "mail.send"),
            new Route("q.mail.status", "mail.status"),
            new Route("q.sms.send", "sms.send"),
            new Route("q.push.send", "push.send"));

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitMqMailConnectionService service;

    @BeforeEach
    void setUp() {
        service = new RabbitMqMailConnectionService(rabbitTemplate, PROPERTIES);
    }

    @Test
    void sendNotification_publishesToMailRoutingKey() {
        SendMailNotificationRequest request = new SendMailNotificationRequest(
                UUID.randomUUID(), "Привет", List.of(), UUID.randomUUID(), "user@example.com", UUID.randomUUID());

        service.sendNotification(request);

        verify(rabbitTemplate).convertAndSend("notifications", "mail.send", (Object) request);
    }

    @Test
    void getMailStatus_returnsReplyFromMailService() {
        GetMailStatusRequest request = new GetMailStatusRequest(UUID.randomUUID());
        when(rabbitTemplate.convertSendAndReceiveAsType(
                eq("notifications"), eq("mail.status"), eq(request), any(ParameterizedTypeReference.class)))
                .thenReturn(new GetMailStatusResponse(2));

        assertThat(service.getMailStatus(request)).contains(new GetMailStatusResponse(2));
    }

    @Test
    void getMailStatus_returnsEmptyWhenReplyTimedOut() {
        GetMailStatusRequest request = new GetMailStatusRequest(UUID.randomUUID());
        when(rabbitTemplate.convertSendAndReceiveAsType(
                eq("notifications"), eq("mail.status"), eq(request), any(ParameterizedTypeReference.class)))
                .thenReturn(null);

        assertThat(service.getMailStatus(request)).isEmpty();
    }
}
