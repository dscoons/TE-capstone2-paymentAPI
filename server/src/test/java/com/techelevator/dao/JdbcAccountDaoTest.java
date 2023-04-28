package com.techelevator.dao;

import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.jdbc.core.JdbcTemplate;
@DisplayName("Account Should")
public class JdbcAccountDaoTest extends BaseDaoTests{
    private JdbcAccountDao sut;


    @Before
    public void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcAccountDao(jdbcTemplate);


    }

    @Test
    public void createNewUser() {
        boolean accountCreated = sut.createAccount(1002);
        Assert.assertTrue(accountCreated);
        sut.createAccount(1001);
        Account account = sut.getAccountByUserName("bob");
        Assert.assertEquals(2001, account.getAccountId());
    }

}
