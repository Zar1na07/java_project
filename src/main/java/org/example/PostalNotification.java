package org.example;

public class PostalNotification extends Notification {
    public Address address;

    public PostalNotification(Address address) {
        this.address = address;
    }

    public static void send(Address address, String message) {
        Notification.send("[POSTAL -> " + address + "] " + message);
    }
}

