package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/account")
@PreAuthorize("isAuthenticated()")
public class AccountController {

    @Autowired
    private JdbcAccountDao jdbcAccountDao;
    @Autowired
    private JdbcUserDao jdbcUserDao;


    @RequestMapping("/balance")
    public BigDecimal getAccount(Principal principal){

        Account account  =jdbcAccountDao.getAccountByUserName(principal.getName());
       if(account !=null){
           BigDecimal balance = account.getBalance();
           return balance;
       }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,"userId not found");
    }
    @GetMapping("/listUsers")
    public List<String> list(Principal principal){

        return jdbcUserDao.findAll().stream()
                .map(User::getUsername)
                .filter(username -> !username.equals(principal.getName()))
                .collect(Collectors.toList());
    }
}
