package org.example;

public class CashTransaction extends FineTransaction {
    public double cashTendered;

    public CashTransaction(double amount, double cashTendered) {
        super(amount);
        this.cashTendered = cashTendered;
    }

    @Override
    public boolean initiateTransaction() {
        return cashTendered >= amount;
    }
}
