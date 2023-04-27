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

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
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
        Account account = jdbcAccountDao.getAccountByUserName(principal.getName());
        return jdbcTransferDao.getUserTransfers(account.getUserId());
    }

    @PostMapping("/transfer")
    public Transfer createTransfer(@Valid @RequestBody TransferDTO transferDTO, Principal principal) {
        Account fromAccount = jdbcAccountDao.getAccountByUserName(transferDTO.getFromUsername());
        Account toAccount = jdbcAccountDao.getAccountByUserName(transferDTO.getToUsername());
        Transfer newTransfer = new Transfer(fromAccount.getUserId(),toAccount.getUserId(),transferDTO.getAmount(),"Approved");
        newTransfer.setTransferId(jdbcTransferDao.createTransfer(newTransfer));
        return newTransfer;
        
    }

}
