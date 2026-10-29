package org.lapoushko.notification.application;

public class InvalidNotificationException extends RuntimeException {

    public InvalidNotificationException(String message) {
        super(message);
    }
}
