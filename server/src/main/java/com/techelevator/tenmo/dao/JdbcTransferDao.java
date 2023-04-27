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
    public int createTransfer(Transfer transfer) {
        int transferId = -1;
        String sql = "INSERT INTO transfer (from_user_id, to_user_id, amount, status) VALUES (?, ?, ?, ?) returning transfer_id;";
        boolean hasSufficientFunds = (jdbcAccountDao.getBalance(transfer.getFromUserId()).compareTo(transfer.getAmount()) >= 0);

        if (hasSufficientFunds && (transfer.getFromUserId() != transfer.getToUserId())) {
            try {
                Account fromAccount = jdbcAccountDao.getAccountByUserId(transfer.getFromUserId());
                Account toAccount = jdbcAccountDao.getAccountByUserId(transfer.getToUserId());
                fromAccount.decrementBalance(transfer.getAmount());
                toAccount.incrementBalance(transfer.getAmount());
                transferId =jdbcTemplate.queryForObject(sql, int.class,transfer.getFromUserId(), transfer.getToUserId(), transfer.getAmount(), transfer.getStatus());
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
    public List<Transfer> getUserTransfers(int userId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT transfer_id, from_user_id, to_user_id, amount, status FROM transfer WHERE from_user_id = ? OR to_user_id = ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, userId, userId);
            while (results.next()) {
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
        transfer.setTransferId(rs.getInt("transfer_id"));

        return transfer;
    }

}
