package org.lapoushko.notification.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Notification {

    private final UUID id;
    private final UUID senderId;
    private final UUID recipientId;
    private final String recipientMail;
    private final String recipientPhoneNumber;
    private final String recipientDeviceToken;
    private final List<NotificationType> notificationTypes;
    private final String content;
    private final List<Attachment> attachments;
    private final Instant dateOfCreate;
    private final Instant dateOfUpdate;

    public Notification(
            UUID senderId,
            UUID recipientId,
            String recipientMail,
            String recipientPhoneNumber,
            String recipientDeviceToken,
            List<NotificationType> notificationTypes,
            String content,
            List<Attachment> attachments,
            Instant dateOfCreate) {
        this(UUID.randomUUID(), senderId, recipientId, recipientMail, recipientPhoneNumber, recipientDeviceToken,
                notificationTypes, content, attachments, dateOfCreate, dateOfCreate);
    }

    private Notification(
            UUID id,
            UUID senderId,
            UUID recipientId,
            String recipientMail,
            String recipientPhoneNumber,
            String recipientDeviceToken,
            List<NotificationType> notificationTypes,
            String content,
            List<Attachment> attachments,
            Instant dateOfCreate,
            Instant dateOfUpdate) {
        this.id = Objects.requireNonNull(id, "id");
        this.senderId = Objects.requireNonNull(senderId, "senderId");
        this.recipientId = Objects.requireNonNull(recipientId, "recipientId");
        this.recipientMail = recipientMail;
        this.recipientPhoneNumber = recipientPhoneNumber;
        this.recipientDeviceToken = recipientDeviceToken;
        this.notificationTypes = List.copyOf(notificationTypes);
        this.content = Objects.requireNonNull(content, "content");
        this.attachments = attachments == null ? List.of() : List.copyOf(attachments);
        this.dateOfCreate = Objects.requireNonNull(dateOfCreate, "dateOfCreate");
        this.dateOfUpdate = Objects.requireNonNull(dateOfUpdate, "dateOfUpdate");
    }

    public static Notification restore(
            UUID id,
            UUID senderId,
            UUID recipientId,
            String recipientMail,
            String recipientPhoneNumber,
            String recipientDeviceToken,
            List<NotificationType> notificationTypes,
            String content,
            List<Attachment> attachments,
            Instant dateOfCreate,
            Instant dateOfUpdate) {
        return new Notification(id, senderId, recipientId, recipientMail, recipientPhoneNumber, recipientDeviceToken,
                notificationTypes, content, attachments, dateOfCreate, dateOfUpdate);
    }

    public boolean hasType(NotificationType type) {
        return notificationTypes.contains(type);
    }

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

    public List<NotificationType> getNotificationTypes() {
        return notificationTypes;
    }

    public String getContent() {
        return content;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public Instant getDateOfCreate() {
        return dateOfCreate;
    }

    public Instant getDateOfUpdate() {
        return dateOfUpdate;
    }
}
