package com.techelevator.dao;

import com.techelevator.dao.BaseDaoTests;
import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.exceptions.LowAccountBalanceException;
import com.techelevator.tenmo.exceptions.SelfTransferException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;


public class JdbcTransferDaoTest extends BaseDaoTests {
    private  JdbcTransferDao sut;
    private JdbcUserDao userDao;
    private  JdbcAccountDao accountDao;
    Account jeffAccount, scottAccount;


    @Before

    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new JdbcUserDao(jdbcTemplate);
        accountDao = new JdbcAccountDao(jdbcTemplate);
        sut = new JdbcTransferDao(jdbcTemplate);
        userDao.create("jeff","costas");
        userDao.create("scott","ooons");


    }


    @Test
    public void list_user_transfers() {
        Account jeff = accountDao.getAccountByUserName("jeff");
        Account scott = accountDao.getAccountByUserName("scott");
        accountDao.listAllAccount();
        sut.createTransfer(new Transfer(jeff.getAccountId(), scott.getAccountId(), new BigDecimal("10"), "Approved"));
        Assert.assertEquals(1, sut.getAccountTransfers(jeff.getAccountId()).size());
    }

    @Test
    public void createNewTransfer() {

        Account jeff = accountDao.getAccountByUserName("jeff");
        Account scott = accountDao.getAccountByUserName("scott");
        accountDao.listAllAccount();
        Transfer t = new Transfer(scott.getAccountId(), jeff.getAccountId(), new BigDecimal("10"), "Approved");
        int transferCreated = sut.createTransfer(t);
        Assert.assertNotNull(transferCreated);

        List<Transfer> transfers = sut.getAccountTransfers(scott.getAccountId());
        System.out.println("List size: " + transfers.size());
        Assert.assertEquals(true, new BigDecimal("10").compareTo(transfers.get(0).getAmount()) == 0);
    }


    @Test
    public void transfer_to_self_should_throw_error(){
        Account jeff = accountDao.getAccountByUserName("jeff");
        assertThrows(SelfTransferException.class,()->sut.createTransfer(new Transfer(jeff.getAccountId(), jeff.getAccountId(),new BigDecimal("10"),"approved")) );
    }
    @Test
    public void trasnfer_resulting_in_overdraft_should_throw_an_error(){
        Account jeff = accountDao.getAccountByUserName("jeff");
        Account scott = accountDao.getAccountByUserName("scott");
        assertThrows(LowAccountBalanceException.class,()->sut.createTransfer(new Transfer(jeff.getAccountId(), scott.getAccountId(),new BigDecimal("2000"),"approved")) );
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