package org.example;

public class CreditTransaction extends FineTransaction {
    public String nameOnCard;

    public CreditTransaction(double amount, String nameOnCard) {
        super(amount);
        this.nameOnCard = nameOnCard;
    }

    @Override
    public boolean initiateTransaction() {
        return nameOnCard != null && !nameOnCard.isEmpty();
    }
}