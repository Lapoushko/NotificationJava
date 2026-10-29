package org.lapoushko.notification.connection.sms;

import org.lapoushko.notification.contracts.sms.SendSmsNotificationRequest;
import org.lapoushko.notification.infrastructure.messaging.RabbitMqProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
class RabbitMqSmsConnectionService implements SmsConnectionService {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqProperties properties;

    RabbitMqSmsConnectionService(RabbitTemplate rabbitTemplate, RabbitMqProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Override
    public void sendNotification(SendSmsNotificationRequest request) {
        rabbitTemplate.convertAndSend(properties.exchange(), properties.smsSend().routingKey(), request);
    }
}
