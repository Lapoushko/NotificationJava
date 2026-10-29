# NotificationJava

Сервис отправки уведомлений по email, sms и push. Java-версия проекта [NotificationsProject](https://github.com/Lapoushko/NotificationsProject) (C#).

Сервис сохраняет уведомление в PostgreSQL и кладет сообщения в очереди RabbitMQ, а дальше их забирают сервисы рассылки (mail/sms/push). Статус письма запрашивается у mail-сервиса через request-reply.

## Стек

- Java 21, Spring Boot 3.5
- PostgreSQL, Flyway
- RabbitMQ (Spring AMQP)
- Docker Compose
- JUnit 5, Mockito, Testcontainers

## Запуск

```bash
docker compose up --build
```

После старта:
- api: http://localhost:8080/api/v1/notifications
- swagger: http://localhost:8080/swagger-ui.html
- rabbitmq: http://localhost:15672 (notifications / notifications)

Из IDEA можно поднять только базу и кролика (`docker compose up postgres rabbitmq`) и запустить `NotificationApplication`. Или запустить `TestNotificationApplication` из тестов, он сам поднимет контейнеры через Testcontainers.

## API

- `POST /api/v1/notifications` - отправить уведомление, в ответе id
- `GET /api/v1/notifications` - все уведомления
- `GET /api/v1/notifications/{id}` - уведомление по id
- `GET /api/v1/notifications/{id}/status` - статусы по каналам

Если ничего не найдено, возвращается 204, как в старой версии.

Типы уведомлений: `1` - Email, `2` - Sms, `4` - Push.

```bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
        "recipientId": "22222222-2222-2222-2222-222222222222",
        "recipientMail": "user@example.com",
        "recipientPhoneNumber": "+79991234567",
        "notificationTypes": [1, 2],
        "content": "Ваш заказ отправлен"
      }'
```

## Очереди

Exchange `notifications` (topic):

- `mail.send` -> `notifications.mail.send`
- `sms.send` -> `notifications.sms.send`
- `push.send` -> `notifications.push.send`
- `mail.status` -> `notifications.mail.status` (запрос статуса письма)

## Тесты

```bash
./mvnw test
./mvnw verify
```

`test` запускает unit-тесты. `verify` дополнительно гоняет интеграционные тесты (`*IT`) на Testcontainers, для них нужен запущенный Docker.
