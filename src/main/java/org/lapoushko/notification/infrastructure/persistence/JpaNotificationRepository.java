package org.lapoushko.notification.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.lapoushko.notification.domain.model.Notification;
import org.lapoushko.notification.domain.repository.NotificationRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
class JpaNotificationRepository implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;

    JpaNotificationRepository(NotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public Notification save(Notification notification) {
        jpaRepository.saveAndFlush(NotificationEntityMapper.toEntity(notification));
        return notification;
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findWithAttachmentsById(id).map(NotificationEntityMapper::toDomain);
    }

    @Override
    public List<Notification> findAll() {
        return jpaRepository.findAllByOrderByDateOfCreateAsc().stream()
                .map(NotificationEntityMapper::toDomain)
                .toList();
    }
}
