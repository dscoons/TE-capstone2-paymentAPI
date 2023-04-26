package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;

public interface AccountDao {
    public Account getAccountByUserId(int id);
    public boolean createAccount(int userId);
    public BigDecimal getBalance(int userId);
    public Account getAccountByUserName(String userName);
}
