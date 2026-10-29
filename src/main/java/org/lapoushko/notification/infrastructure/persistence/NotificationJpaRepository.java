package org.lapoushko.notification.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface NotificationJpaRepository extends JpaRepository<NotificationEntity, UUID> {

    @EntityGraph(attributePaths = "attachments")
    Optional<NotificationEntity> findWithAttachmentsById(UUID id);

    @EntityGraph(attributePaths = "attachments")
    List<NotificationEntity> findAllByOrderByDateOfCreateAsc();
}
