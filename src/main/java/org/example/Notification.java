package org.example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Notification {
    public static List<String> log = new ArrayList<>();

    public static void send(String message) {
        String entry = "[" + new Date() + "] " + message;
        log.add(entry);
        System.out.println(entry);
    }

    public static List<String> getLog() { return log; }
}

