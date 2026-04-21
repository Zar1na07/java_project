package org.example;

import java.util.Date;

public class BarcodeReader {
    public String id;
    public Date registeredAt;
    public boolean active;

    public BarcodeReader(String id) {
        this.id = id;
        this.registeredAt = new Date();
        this.active = true;
    }

    public boolean isActive() { return active; }


    public String readBarcode(String barcode) {
        if (!active) return null;
        return barcode;
    }
}
