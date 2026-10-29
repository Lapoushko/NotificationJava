package org.lapoushko.notification.domain.model;

import java.util.Arrays;

public enum NotificationType {
    UNKNOWN(0),
    EMAIL(1),
    SMS(2),
    PUSH(4);

    private final int code;

    NotificationType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static NotificationType fromCode(int code) {
        return Arrays.stream(values())
                .filter(type -> type.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестный тип уведомления: " + code));
    }
}
