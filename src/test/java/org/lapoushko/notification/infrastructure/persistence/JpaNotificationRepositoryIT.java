package org.lapoushko.notification.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.lapoushko.notification.PostgresContainerConfiguration;
import org.lapoushko.notification.domain.model.Attachment;
import org.lapoushko.notification.domain.model.Notification;
import org.lapoushko.notification.domain.model.NotificationType;
import org.lapoushko.notification.domain.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostgresContainerConfiguration.class, JpaNotificationRepository.class})
@Testcontainers(disabledWithoutDocker = true)
class JpaNotificationRepositoryIT {

    private static final Instant NOW = Instant.parse("2026-01-15T10:00:00.123Z");

    @Autowired
    private NotificationRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_persistsNotificationWithTypesAndAttachments() {
        Notification notification = notification(NOW, List.of(NotificationType.EMAIL, NotificationType.PUSH), List.of(
                new Attachment("a.pdf", "https://files/a.pdf", "application/pdf"),
                new Attachment("b.png", "https://files/b.png", "image/png")));

        repository.save(notification);
        entityManager.clear();

        Notification loaded = repository.findById(notification.getId()).orElseThrow();
        assertThat(loaded).usingRecursiveComparison().isEqualTo(notification);
    }

    @Test
    void save_persistsNotificationWithoutAttachments() {
        Notification notification = notification(NOW, List.of(NotificationType.SMS), List.of());

        repository.save(notification);
        entityManager.clear();

        assertThat(repository.findById(notification.getId()).orElseThrow().getAttachments()).isEmpty();
    }

    @Test
    void findAll_returnsNotificationsOrderedByCreationDate() {
        Notification later = notification(NOW.plusSeconds(60), List.of(NotificationType.SMS), List.of());
        Notification earlier = notification(NOW, List.of(NotificationType.EMAIL), List.of(
                new Attachment("a.pdf", "https://files/a.pdf", "application/pdf")));
        repository.save(later);
        repository.save(earlier);
        entityManager.clear();

        assertThat(repository.findAll())
                .extracting(Notification::getId)
                .containsExactly(earlier.getId(), later.getId());
    }

    @Test
    void findById_returnsEmptyForUnknownId() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }

    private static Notification notification(Instant createdAt, List<NotificationType> types, List<Attachment> attachments) {
        return new Notification(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "user@example.com",
                "+79991234567",
                "device-token",
                types,
                "Ваш заказ отправлен",
                attachments,
                createdAt);
    }
}
