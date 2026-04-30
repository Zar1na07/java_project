package org.example;

import java.io.Serializable;

public class Rack implements Serializable {
    private static final long serialVersionUID = 1L;
    public int number;
    public String locationIdentifier;

    public Rack(int number, String locationIdentifier) {
        this.number = number;
        this.locationIdentifier = locationIdentifier;
    }

    @Override
    public String toString() { return "Rack " + number + " (" + locationIdentifier + ")"; }
}
