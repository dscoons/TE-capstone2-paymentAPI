package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;

import java.util.List;

public interface TransferDao {
    public int createTransfer(Transfer transfer);
    public List<Transfer> getAccountTransfers(int userId);
    public Transfer getTransferById(int transferId);
    public int createTransferRequest(Transfer transfer);
    public List<Transfer> getPendingTransfers(int accountId);
    public Transfer approveTransfer(Transfer transfer);

    Transfer rejectTransfer(Transfer transfer);
}
