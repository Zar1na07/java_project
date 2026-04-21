package org.example;

public class CheckTransaction extends FineTransaction {
    public String bankName;
    public String checkNumber;

    public CheckTransaction(double amount, String bankName, String checkNumber) {
        super(amount);
        this.bankName = bankName;
        this.checkNumber = checkNumber;
    }

    @Override
    public boolean initiateTransaction() {
        return bankName != null && !bankName.isEmpty() && checkNumber != null && !checkNumber.isEmpty();
    }
}
