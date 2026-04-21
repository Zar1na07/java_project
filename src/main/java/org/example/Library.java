package org.example;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Library implements Serializable {
    private static final long serialVersionUID = 1L;
    public String name;
    public Address address;
    public Catalog catalog;
    public List<Member> members;
    public List<Librarian> librarians;
    public List<BookLending> allLendings;

    public Library(String name) {
        this.name = name;
        this.catalog = new Catalog();
        this.members = new ArrayList<>();
        this.librarians = new ArrayList<>();
        this.allLendings = new ArrayList<>();
    }

    public void addBookItem(BookItem item) { catalog.addBook(item); }
    public void removeBookItem(BookItem item) { catalog.removeBook(item); }

    public void addMember(Member member) { members.add(member); }
    public void removeMember(Member member) { members.remove(member); }

    public void addLibrarian(Librarian librarian) { librarians.add(librarian); }

    public void recordLending(BookLending lending) { allLendings.add(lending); }

    public Address getAddress() { return address; }
    public Catalog getCatalog() { return catalog; }
    public List<Member> getMembers() { return members; }
    public List<Librarian> getLibrarians() { return librarians; }
    public List<BookLending> getAllLendings() { return allLendings; }
}

