package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

public interface AccountDao {
    public Account getAccountByUserId(int id);
    public boolean createAccount(int userId);
}
