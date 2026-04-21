package org.example;

public class Fine {
    public double amount;
    public BookLending lending;

    public Fine(BookLending lending) {
        this.lending = lending;
        this.amount = lending.calculateFine();
    }

    public double getAmount() { return amount; }
}
