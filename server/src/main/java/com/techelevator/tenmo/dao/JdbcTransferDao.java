package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exceptions.LowAccountBalanceException;
import com.techelevator.tenmo.exceptions.SelfTransferException;
import com.techelevator.tenmo.exceptions.TransferIdNotFoundException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao{
    private JdbcTemplate jdbcTemplate;
    private JdbcUserDao jdbcUserDao;
    private JdbcAccountDao jdbcAccountDao;

    public JdbcTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcUserDao = new JdbcUserDao(jdbcTemplate);
        this.jdbcAccountDao = new JdbcAccountDao(jdbcTemplate);
    }

    @Override
    public int createTransfer(Transfer transfer) {
        if(transfer.getFromAccountId() == transfer.getToAccountId()){
            throw new SelfTransferException("You are Attempting to Make a Transfer To Yourself, Please Enter a Different Account ID");
        }
        int transferId = -1;
        String sql = "INSERT INTO transfer (from_account_id, to_account_id, amount, status) VALUES (?, ?, ?, ?) returning transfer_id;";
        boolean hasSufficientFunds = (jdbcAccountDao.getBalance(transfer.getFromAccountId()).compareTo(transfer.getAmount()) >= 0);

        if (hasSufficientFunds) {
            try {
                Account fromAccount = jdbcAccountDao.getAccountByAccountId(transfer.getFromAccountId());
                Account toAccount = jdbcAccountDao.getAccountByAccountId(transfer.getToAccountId());
                fromAccount.decrementBalance(transfer.getAmount());
                toAccount.incrementBalance(transfer.getAmount());
                transferId =jdbcTemplate.queryForObject(sql, int.class,transfer.getFromAccountId(), transfer.getToAccountId(), transfer.getAmount(), transfer.getStatus());
                boolean fromUpdateSuccess = jdbcAccountDao.updateAccount(fromAccount);
                boolean toUpdateSuccess = jdbcAccountDao.updateAccount(toAccount);
            } catch (LowAccountBalanceException e) {
                System.out.println(e.getMessage());
            }
        }
        if(transferId==-1){
            throw new LowAccountBalanceException("Could not make deposit");
        }
        return transferId;
    }


    @Override
    public List<Transfer> getAccountTransfers(int accountId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT transfer_id, from_account_id, to_account_id, amount, status FROM transfer WHERE from_account_id = ? OR to_account_id = ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId, accountId);
            while (results.next()) {
                transfers.add(mapRowToTransfer(results));
            }
        } catch (DataAccessException e) {
            System.out.println(e.getMessage());
        }
        return transfers;
    }

    @Override
    public Transfer getTransferById(int transferId) {
        Transfer transfer = null;
        String sql = "SELECT transfer_id, from_account_id, to_account_id, amount, status FROM transfer WHERE transfer_id = ?;";
        SqlRowSet result = jdbcTemplate.queryForRowSet(sql, transferId);
        if (result.next()) {
            transfer = mapRowToTransfer(result);
        } else {
            throw new TransferIdNotFoundException("Invalid Transfer ID");
        }
        return transfer;
    }

    public Transfer mapRowToTransfer(SqlRowSet rs) {
        Transfer transfer = new Transfer();
        transfer.setFromAccountId(rs.getInt("from_account_id"));
        transfer.setToAccountId(rs.getInt("to_account_id"));
        transfer.setAmount(rs.getBigDecimal("amount"));
        transfer.setStatus(rs.getString("status"));
        transfer.setTransferId(rs.getInt("transfer_id"));

        return transfer;
    }

}
