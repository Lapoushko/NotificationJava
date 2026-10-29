package org.lapoushko.notification.application;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.lapoushko.notification.domain.model.Notification;
import org.lapoushko.notification.domain.model.NotificationType;

public interface NotificationService {

    void sendNotification(Notification notification);

    Optional<Notification> getById(UUID id);

    List<Notification> getAll();

    Map<NotificationType, Integer> getStatuses(UUID id, List<NotificationType> types);
}
