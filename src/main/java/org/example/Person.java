package org.example;

import java.io.Serializable;

public class Person implements Serializable {
    private static final long serialVersionUID = 1L;
    public String name;
    public Address address;
    public String email;
    public String phone;

    public Person(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
}
