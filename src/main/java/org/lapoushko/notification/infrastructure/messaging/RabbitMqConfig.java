package org.lapoushko.notification.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RabbitMqConfig {

    @Bean
    TopicExchange notificationsExchange(RabbitMqProperties properties) {
        return ExchangeBuilder.topicExchange(properties.exchange()).durable(true).build();
    }

    @Bean
    Declarables notificationQueues(RabbitMqProperties properties, TopicExchange notificationsExchange) {
        List<Declarable> declarables = new ArrayList<>();
        for (RabbitMqProperties.Route route : List.of(
                properties.mailSend(), properties.mailStatus(), properties.smsSend(), properties.pushSend())) {
            Queue queue = QueueBuilder.durable(route.queue()).build();
            declarables.add(queue);
            declarables.add(BindingBuilder.bind(queue).to(notificationsExchange).with(route.routingKey()));
        }
        return new Declarables(declarables);
    }

    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
