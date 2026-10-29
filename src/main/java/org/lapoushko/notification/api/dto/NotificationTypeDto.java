package org.lapoushko.notification.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(type = "integer", allowableValues = {"0", "1", "2", "4"},
        description = "0 - Unknown, 1 - Email, 2 - Sms, 4 - Push")
public enum NotificationTypeDto {
    UNKNOWN(0),
    EMAIL(1),
    SMS(2),
    PUSH(4);

    private final int code;

    NotificationTypeDto(int code) {
        this.code = code;
    }

    @JsonValue
    public int getCode() {
        return code;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static NotificationTypeDto fromJson(Object value) {
        if (value instanceof Number number) {
            return fromCode(number.intValue());
        }
        if (value instanceof String text) {
            String trimmed = text.trim();
            for (NotificationTypeDto type : values()) {
                if (type.name().equalsIgnoreCase(trimmed) || String.valueOf(type.code).equals(trimmed)) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException("Неизвестный тип уведомления: " + value);
    }

    private static NotificationTypeDto fromCode(int code) {
        for (NotificationTypeDto type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Неизвестный тип уведомления: " + code);
    }
}
