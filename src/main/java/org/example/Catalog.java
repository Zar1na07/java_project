package org.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Catalog implements Serializable {
    private static final long serialVersionUID = 1L;
    public List<BookItem> books;

    public Catalog() {
        this.books = new ArrayList<>();
    }

    public void addBook(BookItem book) { books.add(book); }
    public void removeBook(BookItem book) { books.remove(book); }

    public List<BookItem> searchByTitle(String title) {
        return books.stream()
                .filter(b -> b.title.toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<BookItem> searchByAuthor(String author) {
        return books.stream()
                .filter(b -> b.getAuthorsString().toLowerCase().contains(author.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<BookItem> searchBySubject(String subject) {
        return books.stream()
                .filter(b -> b.subject.toLowerCase().contains(subject.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<BookItem> searchByISBN(String isbn) {
        return books.stream()
                .filter(b -> b.ISBN.equalsIgnoreCase(isbn))
                .collect(Collectors.toList());
    }

    public List<BookItem> searchByPublicationDate(Date date) {
        return books.stream()
                .filter(b -> b.publicationDate != null && SDF.format(b.publicationDate).equals(SDF.format(date)))
                .collect(Collectors.toList());
    }

    private static final java.text.SimpleDateFormat SDF = new java.text.SimpleDateFormat("yyyy-MM-dd");

    public List<BookItem> getAllBooks() { return books; }
}

