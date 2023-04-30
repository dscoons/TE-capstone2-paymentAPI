package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.JdbcTransferDao;
import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferDTO;
import com.techelevator.tenmo.model.TransferRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.math.BigDecimal;
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

    @GetMapping("/transfers/requests")
    public List<Transfer> getTransferRequestsByUser(Principal principal) {
        Account account = jdbcAccountDao.getAccountByUserName(principal.getName());
        return jdbcTransferDao.getPendingTransfers(account.getAccountId());
    }

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.CREATED)
    public Transfer createTransfer(@Valid @RequestBody TransferDTO transferDTO, Principal principal) {
        Account fromAccount = jdbcAccountDao.getAccountByUserName(principal.getName());
        Account toAccount = jdbcAccountDao.getAccountByUserName(transferDTO.getToUsername());
        Transfer newTransfer = new Transfer(fromAccount.getAccountId(),toAccount.getAccountId(),transferDTO.getAmount(),"Approved");
        newTransfer.setTransferId(jdbcTransferDao.createTransfer(newTransfer));
        return newTransfer;
        
    }

    @PostMapping("transfer/request")
    @ResponseStatus(HttpStatus.CREATED)
    public Transfer createTransferRequest(@Valid @RequestBody TransferRequestDTO transferRequestDTO, Principal principal) {
        if (transferRequestDTO.getFromUsername() == principal.getName()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request. From and to user cannot be the same");
        }
        if (transferRequestDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requested amount must be over zero.");
        }
        Account fromAccount = jdbcAccountDao.getAccountByUserName(transferRequestDTO.getFromUsername());
        Account toAccount = jdbcAccountDao.getAccountByUserName(principal.getName());
        Transfer newTransferRequest = new Transfer(fromAccount.getAccountId(), toAccount.getAccountId(), transferRequestDTO.getAmount(), "Pending");

        int transferId = jdbcTransferDao.createTransferRequest(newTransferRequest);
        if (transferId == -1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer request could not be created.");
        }
        return jdbcTransferDao.getTransferById(transferId);
    }

    @GetMapping("/transfers/{id}")
    public Transfer getTransfersById(@PathVariable int id, Principal principal) {
        Account account = jdbcAccountDao.getAccountByUserName(principal.getName());
        return jdbcTransferDao.getAccountTransfers(account.getAccountId())
                .stream()
                .filter(t -> t.getTransferId() == id)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("transfer/approve/{id}")
    public Transfer approveTransfer(@PathVariable int id, Principal principal) {
        Transfer transfer = jdbcTransferDao.getTransferById(id);
        // only 'from' account can approve transfer
        if (transfer.getFromAccountId() != jdbcAccountDao.getAccountByUserName(principal.getName()).getAccountId()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized to approve this transfer request.");
        }
        Transfer updatedTransfer = jdbcTransferDao.approveTransfer(transfer);
        return updatedTransfer;
    }

    @GetMapping("transfer/reject/{id}")
    public Transfer rejectTransfer(@PathVariable int id, Principal principal) {
        Transfer transfer = jdbcTransferDao.getTransferById(id);
        // only 'from' account can reject transfer
        if (transfer.getFromAccountId() != jdbcAccountDao.getAccountByUserName(principal.getName()).getAccountId()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized to reject this transfer request.");
        }
        Transfer updatedTransfer = jdbcTransferDao.rejectTransfer(transfer);
        return updatedTransfer;
    }

}
