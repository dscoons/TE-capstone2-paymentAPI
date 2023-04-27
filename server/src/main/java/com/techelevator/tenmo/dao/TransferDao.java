package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.util.List;

public interface TransferDao {
    public int createTransfer(Transfer transfer);
    public List<Transfer> getUserTransfers(int userId);
}
