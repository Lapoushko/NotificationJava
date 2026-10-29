package org.lapoushko.notification.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.lapoushko.notification.domain.model.Notification;

public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    List<Notification> findAll();
}
