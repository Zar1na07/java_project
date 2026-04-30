package org.example;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class LibraryCard implements Serializable {
    private static final long serialVersionUID = 1L;
    public String cardNumber;
    public String barcode;
    public Date issuedAt;
    public boolean active;

    public LibraryCard() {
        this.cardNumber = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.barcode = UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        this.issuedAt = new Date();
        this.active = true;
    }

    public boolean isActive() { return active; }
}
