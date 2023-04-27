package com.techelevator.tenmo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.techelevator.tenmo.exceptions.LowAccountBalanceException;

import java.math.BigDecimal;

public class Account {
    @JsonIgnore
    private int accountId;
    @JsonIgnore
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

    public void incrementBalance(BigDecimal amount){
       this.balance = this.balance.add(amount);
    }

    public BigDecimal decrementBalance(BigDecimal amount){
        if(this.balance.subtract(amount).compareTo(BigDecimal.ZERO)>=0){
            this.balance = this.balance.subtract(amount);
            return amount;
        }else{
            throw new LowAccountBalanceException("You don't have enough money");
        }

    }
}
