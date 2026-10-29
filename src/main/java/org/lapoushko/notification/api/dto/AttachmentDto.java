package org.lapoushko.notification.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AttachmentDto(
        @NotBlank @Size(max = 128) String fileName,
        @NotBlank String fileUri,
        @NotBlank @Size(max = 255) String contentType) {
}
