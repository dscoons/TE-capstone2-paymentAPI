package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class Account {
    private int accountId;
    private int userId;
    private BigDecimal balance;

    public int getAccountId() {
        return accountId;
    }
    public int getUserId() {
        return userId;
    }
    public BigDecimal getBalance() {
        return balance;
    }

    public Account() {
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
