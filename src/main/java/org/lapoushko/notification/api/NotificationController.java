package org.lapoushko.notification.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.lapoushko.notification.api.dto.NotificationRequest;
import org.lapoushko.notification.api.dto.NotificationResponse;
import org.lapoushko.notification.api.dto.NotificationWithStatusResponse;
import org.lapoushko.notification.api.mapper.NotificationApiMapper;
import org.lapoushko.notification.application.NotificationService;
import org.lapoushko.notification.domain.DataConstants;
import org.lapoushko.notification.domain.model.Notification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Отправка уведомлений и получение их статусов")
public class NotificationController {

    private final NotificationService notificationService;
    private final Clock clock;

    public NotificationController(NotificationService notificationService, Clock clock) {
        this.notificationService = notificationService;
        this.clock = clock;
    }

    @PostMapping
    @Operation(summary = "Отправить уведомление", description = "Возвращает идентификатор созданного уведомления")
    public ResponseEntity<UUID> sendNotification(@Valid @RequestBody NotificationRequest request) {
        Instant now = Instant.now(clock).truncatedTo(ChronoUnit.MILLIS);
        Notification notification = NotificationApiMapper.toDomain(request, DataConstants.ADMIN_USER_ID, now);
        notificationService.sendNotification(notification);
        return ResponseEntity.ok(notification.getId());
    }

    @GetMapping
    @Operation(summary = "Получить все уведомления")
    @ApiResponse(responseCode = "204", description = "Уведомлений нет")
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        List<Notification> notifications = notificationService.getAll();
        if (notifications.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(notifications.stream().map(NotificationApiMapper::toResponse).toList());
    }

    @GetMapping("/{notificationId}")
    @Operation(summary = "Получить уведомление по идентификатору")
    @ApiResponse(responseCode = "204", description = "Уведомление не найдено")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable UUID notificationId) {
        return notificationService.getById(notificationId)
                .map(NotificationApiMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{notificationId}/status")
    @Operation(summary = "Получить уведомление со статусами доставки по каналам")
    @ApiResponse(responseCode = "204", description = "Уведомление не найдено")
    public ResponseEntity<NotificationWithStatusResponse> getNotificationWithStatus(@PathVariable UUID notificationId) {
        return notificationService.getById(notificationId)
                .map(notification -> NotificationApiMapper.toWithStatusResponse(
                        notification,
                        notificationService.getStatuses(notification.getId(), notification.getNotificationTypes())))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
