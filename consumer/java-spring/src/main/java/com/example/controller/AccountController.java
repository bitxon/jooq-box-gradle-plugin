package com.example.controller;

import static com.example.api.Constants.DIRTY_TRICK_HEADER;

import com.example.api.Account;
import com.example.api.Constants;
import com.example.api.MoneyTransfer;
import com.example.db.AccountDao;
import com.example.mapper.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    AccountDao dao;
    @Autowired
    AccountMapper mapper;

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public Account getById(@PathVariable("id") Integer id) {
        return dao.findById(id)
            .map(mapper::mapToApi)
            .orElseThrow(() -> new RuntimeException("Resource not found"));
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    public void transfer(@RequestBody MoneyTransfer transfer,
                         @RequestHeader(value = DIRTY_TRICK_HEADER, required = false) String dirtyTrick) {

        var senderId = transfer.senderId();
        var recipientId = transfer.recipientId();
        var transferMoneyAmount = transfer.moneyAmount();

        var sender = dao.findById(senderId)
            .orElseThrow(() -> new RuntimeException("Sender not found"));
        var recipient = dao.findById(recipientId)
            .orElseThrow(() -> new RuntimeException("Recipient not found"));

        dao.updateMoneyAmount(sender.getId(), sender.getMoneyAmount() - transferMoneyAmount);

        if (Constants.DirtyTrick.FAIL_TRANSFER.equals(dirtyTrick)) {
            throw new RuntimeException("Error during money transfer");
        }

        dao.updateMoneyAmount(recipient.getId(), recipient.getMoneyAmount() + transferMoneyAmount);
    }

}
