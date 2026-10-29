package org.lapoushko.notification.connection.sms;

import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lapoushko.notification.contracts.sms.SendSmsNotificationRequest;
import org.lapoushko.notification.infrastructure.messaging.RabbitMqProperties;
import org.lapoushko.notification.infrastructure.messaging.RabbitMqProperties.Route;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class RabbitMqSmsConnectionServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void sendNotification_publishesToSmsRoutingKey() {
        RabbitMqProperties properties = new RabbitMqProperties(
                "notifications",
                new Route("q.mail.send", "mail.send"),
                new Route("q.mail.status", "mail.status"),
                new Route("q.sms.send", "sms.send"),
                new Route("q.push.send", "push.send"));
        SendSmsNotificationRequest request = new SendSmsNotificationRequest(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "+79991234567", "Привет");

        new RabbitMqSmsConnectionService(rabbitTemplate, properties).sendNotification(request);

        verify(rabbitTemplate).convertAndSend("notifications", "sms.send", (Object) request);
    }
}
