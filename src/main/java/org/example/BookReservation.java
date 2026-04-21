package org.example;

import java.io.Serializable;
import java.util.Date;

public class BookReservation implements Serializable {
    private static final long serialVersionUID = 1L;
    public Date creationDate;
    public ReservationStatus status;
    public Member member;
    public BookItem book;

    public BookReservation(Member member, BookItem book) {
        this.creationDate = new Date();
        this.status = ReservationStatus.WAITING;
        this.member = member;
        this.book = book;
    }

    public void completeReservation() { this.status = ReservationStatus.COMPLETED; }
    public void cancelReservation() { this.status = ReservationStatus.CANCELED; }
    public ReservationStatus getStatus() { return status; }
}
