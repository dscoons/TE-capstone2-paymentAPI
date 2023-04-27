package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

public interface TransferDao {
    public boolean createTransfer(Transfer transfer);
    public List<Transfer> getUserTransfers(int userId);
}
