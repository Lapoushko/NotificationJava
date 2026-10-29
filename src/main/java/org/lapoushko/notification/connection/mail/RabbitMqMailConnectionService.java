package org.lapoushko.notification.connection.mail;

import java.time.Duration;
import java.util.Optional;
import org.lapoushko.notification.contracts.mail.GetMailStatusRequest;
import org.lapoushko.notification.contracts.mail.GetMailStatusResponse;
import org.lapoushko.notification.contracts.mail.SendMailNotificationRequest;
import org.lapoushko.notification.infrastructure.messaging.RabbitMqProperties;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
class RabbitMqMailConnectionService implements MailConnectionService {

    private static final ParameterizedTypeReference<GetMailStatusResponse> STATUS_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private static final Duration DEFAULT_REPLY_TIMEOUT = Duration.ofSeconds(5);

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqProperties properties;
    private final String statusRequestExpiration;

    RabbitMqMailConnectionService(
            RabbitTemplate rabbitTemplate, RabbitMqProperties properties, RabbitProperties rabbitProperties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
        Duration replyTimeout = rabbitProperties.getTemplate().getReplyTimeout();
        this.statusRequestExpiration =
                String.valueOf((replyTimeout != null ? replyTimeout : DEFAULT_REPLY_TIMEOUT).toMillis());
    }

    @Override
    public void sendNotification(SendMailNotificationRequest request) {
        rabbitTemplate.convertAndSend(properties.exchange(), properties.mailSend().routingKey(), request);
    }

    @Override
    public Optional<GetMailStatusResponse> getMailStatus(GetMailStatusRequest request) {
        // если mail-сервис не запущен, запросы не должны копиться в очереди
        MessagePostProcessor expireWithReplyTimeout = message -> {
            message.getMessageProperties().setExpiration(statusRequestExpiration);
            return message;
        };
        return Optional.ofNullable(rabbitTemplate.convertSendAndReceiveAsType(
                properties.exchange(),
                properties.mailStatus().routingKey(),
                request,
                expireWithReplyTimeout,
                STATUS_RESPONSE_TYPE));
    }
}
