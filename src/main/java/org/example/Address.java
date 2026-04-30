package org.example;

import java.io.Serializable;

public class Address implements Serializable {
    private static final long serialVersionUID = 1L;
    public String streetAddress;
    public String city;
    public String state;
    public String zipCode;
    public String country;

    public Address(String streetAddress, String city, String state, String zipCode, String country) {
        this.streetAddress = streetAddress;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }

    @Override
    public String toString() {
        return streetAddress + ", " + city + ", " + state + " " + zipCode + ", " + country;
    }
}
