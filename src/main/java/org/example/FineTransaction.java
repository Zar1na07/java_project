package org.example;

import java.util.Date;

public abstract class FineTransaction {
    public Date creationDate;
    public double amount;

    public FineTransaction(double amount) {
        this.creationDate = new Date();
        this.amount = amount;
    }

    public abstract boolean initiateTransaction();

    public double getAmount() { return amount; }
}
