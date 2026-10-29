package org.lapoushko.notification.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AttachmentEmbeddable {

    @Column(name = "file_name", nullable = false, length = 128)
    private String fileName;

    @Column(name = "file_uri", nullable = false, columnDefinition = "text")
    private String fileUri;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    protected AttachmentEmbeddable() {
    }

    public AttachmentEmbeddable(String fileName, String fileUri, String contentType) {
        this.fileName = fileName;
        this.fileUri = fileUri;
        this.contentType = contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUri() {
        return fileUri;
    }

    public String getContentType() {
        return contentType;
    }
}
