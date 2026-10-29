package org.lapoushko.notification.connection.push;

import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lapoushko.notification.contracts.push.SendPushNotificationRequest;
import org.lapoushko.notification.infrastructure.messaging.RabbitMqProperties;
import org.lapoushko.notification.infrastructure.messaging.RabbitMqProperties.Route;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

@ExtendWith(MockitoExtension.class)
class RabbitMqPushConnectionServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Test
    void sendNotification_publishesToPushRoutingKey() {
        RabbitMqProperties properties = new RabbitMqProperties(
                "notifications",
                new Route("q.mail.send", "mail.send"),
                new Route("q.mail.status", "mail.status"),
                new Route("q.sms.send", "sms.send"),
                new Route("q.push.send", "push.send"));
        SendPushNotificationRequest request =
                new SendPushNotificationRequest(UUID.randomUUID(), "Title", "device-token", "Привет");

        new RabbitMqPushConnectionService(rabbitTemplate, properties).sendNotification(request);

        verify(rabbitTemplate).convertAndSend("notifications", "push.send", (Object) request);
    }
}
