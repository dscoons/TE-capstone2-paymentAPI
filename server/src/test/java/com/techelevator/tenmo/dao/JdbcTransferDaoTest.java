package com.techelevator.tenmo.dao;

import com.techelevator.dao.BaseDaoTests;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

public class JdbcTransferDaoTest extends BaseDaoTests {
    private JdbcTransferDao sut;
    private JdbcUserDao userDao;
    private JdbcAccountDao accountDao;

    @Before
    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new JdbcUserDao(jdbcTemplate);
        accountDao = new JdbcAccountDao(jdbcTemplate);
        sut = new JdbcTransferDao(jdbcTemplate);
    }

    @Test
    public void list_user_transfers() {
        List<User> users = userDao.findAll();
        sut.createTransfer(new Transfer(1001, 1002, new BigDecimal("10"), "Approved"));
        Assert.assertEquals(1, sut.getAccountTransfers(1001).size());
    }

    @Test
    public void createNewTransfer() {
        Transfer t = new Transfer(1001, 1002, new BigDecimal("10"), "Approved");
        int transferCreated = sut.createTransfer(t);
        Assert.assertNotNull(transferCreated);
        List<Transfer> transfers = sut.getAccountTransfers(1001);
        System.out.println("List size: " + transfers.size());
        Assert.assertEquals(true, new BigDecimal("10").compareTo(transfers.get(0).getAmount()) == 0);
    }


 /*   @Test
    public void createNewTransfer_test_balances() {
        Transfer t = new Transfer(1001, 1002, new BigDecimal("10"), "Approved");
        accountDao.createAccount(1001);
        accountDao.createAccount(1002);
        int transferCreated = sut.createTransfer(t);
        Account fromAccount = accountDao.getAccountByUserId(1001);
        Account toAccount = accountDao.getAccountByUserId(1002);
        List<Transfer> transfers = sut.getUserTransfers(1001);
        Assert.assertEquals(true, new BigDecimal("990").compareTo(fromAccount.getBalance()) == 0);
    }*/
}