package org.example;

import java.io.Serializable;
import java.util.UUID;

public abstract class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    public String id;
    public String password;
    public AccountStatus status;
    public Person person;

    public Account(String name, String email, String phone, String password) {
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.password = password;
        this.status = AccountStatus.ACTIVE;
        this.person = new Person(name, email, phone);
    }

    public boolean resetPassword(String newPassword) {
        this.password = newPassword;
        return true;
    }

    public String getName() { return person.name; }
    public String getEmail() { return person.email; }
    public String getPhone() { return person.phone; }
    public String getId() { return id; }
    public AccountStatus getStatus() { return status; }
}
