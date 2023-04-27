package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exceptions.LowAccountBalanceException;
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
    public boolean createTransfer(Transfer transfer) {
        boolean success = false;
        String sql = "INSERT INTO transfer (from_user_id, to_user_id, amount, status) VALUES (?, ?, ?, ?);";
        boolean hasSufficientFunds = (jdbcAccountDao.getBalance(transfer.getFromUserId()).compareTo(transfer.getAmount()) >= 0);
        if (hasSufficientFunds && (transfer.getFromUserId() != transfer.getToUserId())) {
            try {
                Account fromAccount = jdbcAccountDao.getAccountByUserId(transfer.getFromUserId());
                Account toAccount = jdbcAccountDao.getAccountByUserId(transfer.getToUserId());
                fromAccount.decrementBalance(transfer.getAmount());
                toAccount.incrementBalance(transfer.getAmount());
                jdbcTemplate.update(sql, transfer.getFromUserId(), transfer.getToUserId(), transfer.getAmount(), transfer.getStatus());
                boolean fromUpdateSuccess = jdbcAccountDao.updateAccount(fromAccount);
                boolean toUpdateSuccess = jdbcAccountDao.updateAccount(toAccount);
            } catch (LowAccountBalanceException e) {
                return success;
            }
            success = true;
        }
        return success;
    }


    @Override
    public List<Transfer> getUserTransfers(int userId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT transfer_id, from_user_id, to_user_id, amount, status FROM transfer WHERE from_user_id = ? OR to_user_id = ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId, userId);
            if (results.next()) {
                transfers.add(mapRowToTransfer(results));
            }
        } catch (DataAccessException e) {
            System.out.println(e.getMessage());
        }
        return transfers;
    }

    public Transfer mapRowToTransfer(SqlRowSet rs) {
        Transfer transfer = new Transfer();
        transfer.setFromUserId(rs.getInt("from_user_id"));
        transfer.setToUserId(rs.getInt("to_user_id"));
        transfer.setAmount(rs.getBigDecimal("amount"));
        transfer.setStatus(rs.getString("status"));
        return transfer;
    }

}
