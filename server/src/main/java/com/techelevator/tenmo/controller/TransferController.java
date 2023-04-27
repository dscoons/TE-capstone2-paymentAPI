package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/account")
@PreAuthorize("isAuthenticated()")
public class TransferController {
    @Autowired
    private JdbcTransferDao jdbcTransferDao;
    @Autowired
    private JdbcAccountDao jdbcAccountDao;
    @Autowired
    private JdbcUserDao jdbcUserDao;

    // list transfers, create transfer
    @GetMapping("/transfers")
    public List<Transfer> getTransfersByUser(Principal principal) {
        int userId = jdbcUserDao.findIdByUsername(principal.getName());
        Account account = jdbcAccountDao.getAccountByUserId(userId);
        return jdbcTransferDao.getUserTransfers(account.getAccountId());
    }

    @PostMapping("/transfer")
    public Transfer createTransfer(@RequestBody TransferDTO transferDTO, Principal principal) {
        int fromId = jdbcUserDao.findIdByUsername(transferDTO.getFromUsername());
        int toId = jdbcUserDao.findIdByUsername(transferDTO.getFromUsername());
        
    }

}
