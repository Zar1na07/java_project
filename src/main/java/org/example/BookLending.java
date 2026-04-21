package org.example;

import java.io.Serializable;
import java.util.Date;

public class BookLending implements Serializable {
    private static final long serialVersionUID = 1L;
    public Date creationDate;
    public Date dueDate;
    public Date returnDate;
    public BookItem book;
    public Member member;

    public static final int MAX_LENDING_DAYS = 10;

    public BookLending(BookItem book, Member member) {
        this.creationDate = new Date();
        this.dueDate = new Date(System.currentTimeMillis() + (long) MAX_LENDING_DAYS * 24 * 60 * 60 * 1000);
        this.book = book;
        this.member = member;
    }

    public void returnBook() {
        this.returnDate = new Date();
        book.status = BookStatus.AVAILABLE;
        book.borrowed = null;
        book.dueDate = null;
    }

    public boolean isOverdue() {
        return returnDate == null && new Date().after(dueDate);
    }

    public double calculateFine() {
        if (!isOverdue()) return 0;
        long diff = new Date().getTime() - dueDate.getTime();
        long daysLate = diff / (24 * 60 * 60 * 1000);
        return daysLate * 1.0; // $1 per day
    }

    public Date getReturnDate() { return returnDate; }
    public Date getDueDate() { return dueDate; }
}
