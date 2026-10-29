package org.lapoushko.notification.infrastructure.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "notifications")
public class NotificationEntity implements Persistable<UUID> {

    @Id
    private UUID id;

    @Column(name = "sender_id", nullable = false)
    private UUID senderId;

    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    @Column(name = "recipient_mail", length = 320)
    private String recipientMail;

    @Column(name = "recipient_phone_number", length = 16)
    private String recipientPhoneNumber;

    @Column(name = "recipient_device_token", columnDefinition = "text")
    private String recipientDeviceToken;

    @Column(name = "notification_types", nullable = false)
    private Integer[] notificationTypes;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @ElementCollection
    @CollectionTable(name = "notification_attachments", joinColumns = @JoinColumn(name = "notification_id"))
    @OrderColumn(name = "position")
    private List<AttachmentEmbeddable> attachments = new ArrayList<>();

    @Column(name = "date_of_create", nullable = false)
    private Instant dateOfCreate;

    @Column(name = "date_of_update", nullable = false)
    private Instant dateOfUpdate;

    // id задаем сами, без этого save() делает лишний select
    @Transient
    private boolean isNew = true;

    protected NotificationEntity() {
    }

    public NotificationEntity(
            UUID id,
            UUID senderId,
            UUID recipientId,
            String recipientMail,
            String recipientPhoneNumber,
            String recipientDeviceToken,
            Integer[] notificationTypes,
            String content,
            List<AttachmentEmbeddable> attachments,
            Instant dateOfCreate,
            Instant dateOfUpdate) {
        this.id = id;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.recipientMail = recipientMail;
        this.recipientPhoneNumber = recipientPhoneNumber;
        this.recipientDeviceToken = recipientDeviceToken;
        this.notificationTypes = notificationTypes;
        this.content = content;
        this.attachments = new ArrayList<>(attachments);
        this.dateOfCreate = dateOfCreate;
        this.dateOfUpdate = dateOfUpdate;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        isNew = false;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public UUID getRecipientId() {
        return recipientId;
    }

    public String getRecipientMail() {
        return recipientMail;
    }

    public String getRecipientPhoneNumber() {
        return recipientPhoneNumber;
    }

    public String getRecipientDeviceToken() {
        return recipientDeviceToken;
    }

    public Integer[] getNotificationTypes() {
        return notificationTypes;
    }

    public String getContent() {
        return content;
    }

    public List<AttachmentEmbeddable> getAttachments() {
        return attachments;
    }

    public Instant getDateOfCreate() {
        return dateOfCreate;
    }

    public Instant getDateOfUpdate() {
        return dateOfUpdate;
    }
}
