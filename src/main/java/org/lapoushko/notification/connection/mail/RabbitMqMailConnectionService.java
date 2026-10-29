package org.lapoushko.notification.connection.mail;

import java.util.Optional;
import org.lapoushko.notification.contracts.mail.GetMailStatusRequest;
import org.lapoushko.notification.contracts.mail.GetMailStatusResponse;
import org.lapoushko.notification.contracts.mail.SendMailNotificationRequest;
import org.lapoushko.notification.infrastructure.messaging.RabbitMqProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
class RabbitMqMailConnectionService implements MailConnectionService {

    private static final ParameterizedTypeReference<GetMailStatusResponse> STATUS_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqProperties properties;

    RabbitMqMailConnectionService(RabbitTemplate rabbitTemplate, RabbitMqProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @Override
    public void sendNotification(SendMailNotificationRequest request) {
        rabbitTemplate.convertAndSend(properties.exchange(), properties.mailSend().routingKey(), request);
    }

    @Override
    public Optional<GetMailStatusResponse> getMailStatus(GetMailStatusRequest request) {
        return Optional.ofNullable(rabbitTemplate.convertSendAndReceiveAsType(
                properties.exchange(), properties.mailStatus().routingKey(), request, STATUS_RESPONSE_TYPE));
    }
}
