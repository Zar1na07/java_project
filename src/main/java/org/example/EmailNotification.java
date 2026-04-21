package org.example;

public class EmailNotification extends Notification {
    public String email;

    public EmailNotification(String email) {
        this.email = email;
    }

    public static void send(String email, String message) {
        Notification.send("[EMAIL -> " + email + "] " + message);
    }
}
