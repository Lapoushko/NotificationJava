package org.lapoushko.notification.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    @Bean
    OpenAPI notificationOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Notification Service API")
                .version("v1")
                .description("Сервис отправки уведомлений (email, sms, push)"));
    }
}
