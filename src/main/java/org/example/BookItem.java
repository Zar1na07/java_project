package org.example;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class BookItem extends Book implements Serializable {
    private static final long serialVersionUID = 1L;
    public String barcode;
    public boolean isReferenceOnly;
    public Date borrowed;
    public Date dueDate;
    public double price;
    public BookFormat format;
    public BookStatus status;
    public Date dateOfPurchase;
    public Date publicationDate;
    public Rack rack;

    public BookItem(String ISBN, String title, String subject, String publisher,
                    String language, int numberOfPages, String author,
                    double price, BookFormat format, boolean isReferenceOnly) {
        super(ISBN, title, subject, publisher, language, numberOfPages, author);
        this.barcode = UUID.randomUUID().toString().substring(0, 10).toUpperCase();
        this.price = price;
        this.format = format;
        this.isReferenceOnly = isReferenceOnly;
        this.status = BookStatus.AVAILABLE;
        this.dateOfPurchase = new Date();
        this.publicationDate = new Date();
    }

    public boolean checkout() {
        if (isReferenceOnly || status != BookStatus.AVAILABLE) return false;
        status = BookStatus.LOANED;
        return true;
    }

    public String getStatusString() { return status.name(); }
    public String getBarcode() { return barcode; }
}
