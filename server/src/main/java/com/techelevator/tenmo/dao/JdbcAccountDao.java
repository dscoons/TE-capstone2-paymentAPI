package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.exceptions.DaoException;
import com.techelevator.tenmo.exceptions.TenmoAccountNotFoundException;
import com.techelevator.tenmo.model.Account;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class JdbcAccountDao implements AccountDao {
    private JdbcTemplate jdbcTemplate;

    public JdbcAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Account getAccountByUserId(int id) {
        String sql = "SELECT * FROM account WHERE user_id = ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, id);
        if (rs.next()) {
            return mapRowToAccount(rs);
        }
        throw new TenmoAccountNotFoundException("User id does not exist.");
    }

    @Override
    public Account getAccountByUserName(String userName) {
        String sql = "SELECT * FROM account a JOIN tenmo_user t ON t.user_id = a.user_id WHERE username = ? ;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, userName);
        if (rs.next()) {
            return mapRowToAccount(rs);
        }
        throw new TenmoAccountNotFoundException("Username "+userName+" does not exist.");
    }
    @Override
    public boolean createAccount(int userId) {
        // update to return account_id?
        String sql = "INSERT INTO account (user_id, balance) VALUES(?, 1000);";
        try {
            jdbcTemplate.update(sql, userId);
        } catch (CannotGetJdbcConnectionException | BadSqlGrammarException | DataIntegrityViolationException e) {
            return false;
        }
        return true;
    }
    public BigDecimal getBalance(int userId){
        String sql = "SELECT * FROM account WHERE user_id = ? ";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql,userId);

        if(rs.next()){
            return rs.getBigDecimal("balance");
        }
        throw new TenmoAccountNotFoundException("User ID " + userId + " doesn't Exist");
    }


    private Account mapRowToAccount(SqlRowSet rs) {
        Account account = new Account();
        account.setAccountId(rs.getInt("account_id"));
        account.setBalance(rs.getBigDecimal("balance"));
        account.setUserId(rs.getInt("user_id"));
        return account;
    }

    public boolean updateAccount(Account account) {
        String sql = "UPDATE account SET balance = ? WHERE user_id = ?";
        boolean success = false;
        try {
            jdbcTemplate.update(sql, account.getBalance(), account.getUserId());
            success = true;
        } catch (CannotGetJdbcConnectionException | BadSqlGrammarException | DataIntegrityViolationException e) {
            return false;
        }
        return success;
    }
}
