package org.example;

import java.io.Serializable;
import java.util.List;

public class Librarian extends Account implements Serializable {
    private static final long serialVersionUID = 1L;

    public Librarian(String name, String email, String phone, String password) {
        super(name, email, phone, password);
    }

    public boolean addBookItem(Library library, BookItem item) {
        library.addBookItem(item);
        return true;
    }

    public boolean blockMember(Member member) {
        member.status = AccountStatus.BLACKLISTED;
        return true;
    }

    public boolean unblockMember(Member member) {
        member.status = AccountStatus.ACTIVE;
        return true;
    }
}
