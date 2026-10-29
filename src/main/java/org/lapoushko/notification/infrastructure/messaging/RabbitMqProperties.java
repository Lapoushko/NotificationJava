package org.lapoushko.notification.infrastructure.messaging;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "notification.rabbitmq")
public record RabbitMqProperties(
        @NotBlank String exchange,
        @Valid @NotNull Route mailSend,
        @Valid @NotNull Route mailStatus,
        @Valid @NotNull Route smsSend,
        @Valid @NotNull Route pushSend) {

    public record Route(@NotBlank String queue, @NotBlank String routingKey) {
    }
}
