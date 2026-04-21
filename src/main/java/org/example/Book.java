package org.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    public String ISBN;
    public String title;
    public String subject;
    public String publisher;
    public String language;
    public int numberOfPages;
    public List<String> authors;

    public Book(String ISBN, String title, String subject, String publisher, String language, int numberOfPages, String author) {
        this.ISBN = ISBN;
        this.title = title;
        this.subject = subject;
        this.publisher = publisher;
        this.language = language;
        this.numberOfPages = numberOfPages;
        this.authors = new ArrayList<>();
        this.authors.add(author);
    }

    public String getTitle() { return title; }
    public String getISBN() { return ISBN; }
    public String getAuthorsString() { return String.join(", ", authors); }
}
