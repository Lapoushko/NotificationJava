package org.lapoushko.notification.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record NotificationRequest(
        @NotNull UUID recipientId,

        @Size(max = 320)
        @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Не валидная почта.")
        String recipientMail,

        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Не валидный номер телефона.")
        String recipientPhoneNumber,

        String recipientDeviceToken,

        @NotEmpty List<@NotNull NotificationTypeDto> notificationTypes,

        @NotBlank String content,

        List<@Valid @NotNull AttachmentDto> attachments) {
}
