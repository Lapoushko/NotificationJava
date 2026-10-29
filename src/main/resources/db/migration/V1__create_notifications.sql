CREATE TABLE notifications
(
    id                     UUID PRIMARY KEY,
    sender_id              UUID        NOT NULL,
    recipient_id           UUID        NOT NULL,
    recipient_mail         VARCHAR(320),
    recipient_phone_number VARCHAR(16),
    recipient_device_token TEXT,
    notification_types     INTEGER[]   NOT NULL,
    content                TEXT        NOT NULL,
    date_of_create         TIMESTAMPTZ NOT NULL,
    date_of_update         TIMESTAMPTZ NOT NULL
);

CREATE TABLE notification_attachments
(
    notification_id UUID         NOT NULL REFERENCES notifications (id) ON DELETE CASCADE,
    position        INTEGER      NOT NULL,
    file_name       VARCHAR(128) NOT NULL,
    file_uri        TEXT         NOT NULL,
    content_type    VARCHAR(255) NOT NULL,
    PRIMARY KEY (notification_id, position)
);
