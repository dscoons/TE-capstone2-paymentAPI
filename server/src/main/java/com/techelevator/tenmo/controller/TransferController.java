package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

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
        return jdbcTransferDao.getAccountTransfers(account.getAccountId());
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public Transfer createTransfer(@Valid @RequestBody TransferDTO transferDTO, Principal principal) {
        Account fromAccount = jdbcAccountDao.getAccountByUserName(transferDTO.getFromUsername());
        Account toAccount = jdbcAccountDao.getAccountByUserName(transferDTO.getToUsername());
        Transfer newTransfer = new Transfer(fromAccount.getAccountId(),toAccount.getAccountId(),transferDTO.getAmount(),"Approved");
        newTransfer.setTransferId(jdbcTransferDao.createTransfer(newTransfer));
        return newTransfer;
        
    }

    @GetMapping("/transfers/{id}")
    public Transfer getTransfersById(@PathVariable int id, Principal principal) {
        Account account = jdbcAccountDao.getAccountByUserName(principal.getName());
        Optional<Transfer> transferOptional = jdbcTransferDao.getAccountTransfers(account.getAccountId()).stream().filter(t -> t.getTransferId() == id).findFirst();
        if (transferOptional.isPresent()) {
            return transferOptional.get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
