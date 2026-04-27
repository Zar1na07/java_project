package org.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Member extends Account implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int MAX_BOOKS_CHECKOUT = 5;
    public static final int MAX_LENDING_DAYS = 10;

    public Date dateOfMembership;
    public int totalBooksCheckedOut;
    public List<BookLending> activeLoans;
    public List<BookReservation> activeReservations;
    public LibraryCard libraryCard;

    public Member(String name, String email, String phone, String password) {
        super(name, email, phone, password);
        this.dateOfMembership = new Date();
        this.totalBooksCheckedOut = 0;
        this.activeLoans = new ArrayList<>();
        this.activeReservations = new ArrayList<>();
        this.libraryCard = new LibraryCard();
    }

    public boolean checkoutBook(BookItem book) {
        if (totalBooksCheckedOut >= MAX_BOOKS_CHECKOUT) return false;
        if (book.status != BookStatus.AVAILABLE && book.status != BookStatus.RESERVED) return false;
        BookLending lending = new BookLending(book, this);
        book.status = BookStatus.LOANED;
        book.borrowed = new Date();
        book.dueDate = lending.dueDate;
        activeLoans.add(lending);
        totalBooksCheckedOut++;
        return true;
    }

    public BookLending returnBook(BookItem book) {
        for (BookLending lending : activeLoans) {
            if (lending.book == book) {
                lending.returnBook();
                activeLoans.remove(lending);
                totalBooksCheckedOut--;
                return lending;
            }
        }
        return null;
    }

    public boolean reserveBook(BookItem book) {
        if (book.status == BookStatus.AVAILABLE) return false;
        BookReservation reservation = new BookReservation(this, book);
        book.status = BookStatus.RESERVED;
        activeReservations.add(reservation);
        return true;
    }

    public boolean renewBook(BookItem book, Library library) {
        boolean reservedByOther = library.getMembers().stream()
                .filter(m -> m != this)
                .flatMap(m -> m.activeReservations.stream())
                .anyMatch(r -> r.book == book && r.status == ReservationStatus.WAITING);
        if (reservedByOther) return false;

        for (BookLending lending : activeLoans) {
            if (lending.book == book) {
                lending.dueDate = new Date(lending.dueDate.getTime() + (long) MAX_LENDING_DAYS * 24 * 60 * 60 * 1000);
                book.dueDate = lending.dueDate;
                return true;
            }
        }
        return false;
    }

    public int getTotalCheckedOutBooks() { return totalBooksCheckedOut; }
}
