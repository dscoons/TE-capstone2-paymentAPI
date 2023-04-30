package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exceptions.DaoException;
import com.techelevator.tenmo.exceptions.LowAccountBalanceException;
import com.techelevator.tenmo.exceptions.SelfTransferException;
import com.techelevator.tenmo.exceptions.TransferIdNotFoundException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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

    @Override
    public int createTransferRequest(Transfer transfer) {
        int transferId = -1;
        if (transfer.getFromAccountId() == transfer.getToAccountId()) {
            throw new SelfTransferException("You are Attempting to Make a Transfer To Yourself, Please Enter a Different Account ID");
        }
        if (transfer.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new LowAccountBalanceException("Transfer request amount must be more than zero.");
        }
        String sql = "INSERT INTO transfer (from_account_id, to_account_id, amount, status) VALUES (?, ?, ?, ?) RETURNING transfer_id;";
        try {
            transferId = jdbcTemplate.queryForObject(sql, Integer.class, transfer.getFromAccountId(), transfer.getToAccountId(), transfer.getAmount(), transfer.getStatus());
        }  catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (BadSqlGrammarException e) {
            throw new DaoException("SQL syntax error", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return transferId;
    }

    @Override
    public List<Transfer> getPendingTransfers(int accountId) {
        List<Transfer> transfers = new ArrayList<>();
        String sql = "SELECT transfer_id, from_account_id, to_account_id, amount, status FROM transfer WHERE from_account_id = ?;";
        try {
            SqlRowSet results = jdbcTemplate.queryForRowSet(sql, accountId);
            while (results.next()) {
                transfers.add(mapRowToTransfer(results));
            }
        } catch (DataAccessException e) {
            System.out.println(e.getMessage());
        }
        return transfers;
    }

    @Override
    public Transfer approveTransfer(Transfer transfer) {
        Transfer updatedTransfer = null;
        String sql = "UPDATE transfer SET status = 'Approved' WHERE transfer_id = ? AND status = 'Pending';";
        boolean hasSufficientFunds = (jdbcAccountDao.getBalance(transfer.getFromAccountId()).compareTo(transfer.getAmount()) >= 0);
        if (hasSufficientFunds) {
            try {
                int numRows = jdbcTemplate.update(sql, transfer.getTransferId());
                if (numRows == 0) {
                    throw new DaoException("Unable to update transfer.");
                } else {
                    updatedTransfer = getTransferById(transfer.getTransferId());
                }
                Account fromAccount = jdbcAccountDao.getAccountByAccountId(transfer.getFromAccountId());
                Account toAccount = jdbcAccountDao.getAccountByAccountId(transfer.getToAccountId());
                fromAccount.decrementBalance(transfer.getAmount());
                toAccount.incrementBalance(transfer.getAmount());
                if (!jdbcAccountDao.updateAccount(fromAccount) || !jdbcAccountDao.updateAccount(toAccount)) {
                    throw new DaoException("Problem updating account balance.");
                }
            } catch (CannotGetJdbcConnectionException e) {
                throw new DaoException("Unable to connect to server or database", e);
            } catch (BadSqlGrammarException e) {
                throw new DaoException("SQL syntax error", e);
            } catch (DataIntegrityViolationException e) {
                throw new DaoException("Data integrity violation", e);
            }
        } else {
            throw new LowAccountBalanceException("Not enough funds to approve transfer.");
        }
        return updatedTransfer;
    }

    @Override
    public Transfer rejectTransfer(Transfer transfer) {
        Transfer updatedTransfer = null;
        String sql = "UPDATE transfer SET status = 'Rejected' WHERE transfer_id = ? AND status = 'Pending';";
        try {
            int numRows = jdbcTemplate.update(sql, transfer.getTransferId());
            if (numRows == 0) {
                throw new DaoException("Unable to update transfer.");
            } else {
                updatedTransfer = getTransferById(transfer.getTransferId());
            }
        } catch (CannotGetJdbcConnectionException e) {
            throw new DaoException("Unable to connect to server or database", e);
        } catch (BadSqlGrammarException e) {
            throw new DaoException("SQL syntax error", e);
        } catch (DataIntegrityViolationException e) {
            throw new DaoException("Data integrity violation", e);
        }
        return updatedTransfer;
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
