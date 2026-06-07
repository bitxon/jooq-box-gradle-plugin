package com.example.resource;

import static com.example.api.Constants.DIRTY_TRICK_HEADER;

import com.example.api.Account;
import com.example.api.Constants;
import com.example.api.MoneyTransfer;
import com.example.db.AccountDao;
import com.example.mapper.AccountMapper;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;


@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountResource {

    @Inject
    AccountDao dao;
    @Inject
    AccountMapper mapper;

    @GET
    @Path("/{id}")
    public Account getById(@PathParam("id") Integer id) {
        return dao.findById(id)
            .map(mapper::mapToApi)
            .orElseThrow(() -> new RuntimeException("Resource not found"));
    }

    @POST
    @Path("/transfers")
    @Transactional
    public void transfer(MoneyTransfer transfer,
                         @HeaderParam(DIRTY_TRICK_HEADER) String dirtyTrick) {

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
