package org.lapoushko.notification.connection.push;

import org.lapoushko.notification.contracts.push.SendPushNotificationRequest;
import org.lapoushko.notification.infrastructure.messaging.RabbitMqProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
class RabbitMqPushConnectionService implements PushConnectionService {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqProperties properties;

    RabbitMqPushConnectionService(RabbitTemplate rabbitTemplate, RabbitMqProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Override
    public void sendNotification(SendPushNotificationRequest request) {
        rabbitTemplate.convertAndSend(properties.exchange(), properties.pushSend().routingKey(), request);
    }
}
