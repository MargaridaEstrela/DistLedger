package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.account.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class ServerState {
    private List<Operation> ledger;
    private HashMap<String, Account> accounts;
    private boolean activated;

    public ServerState() {
        this.ledger = new ArrayList<>();
        this.accounts = new HashMap<String, Account>();

        //TODO adicionar broker
    }

    public void setActivated (boolean state) {
        this.activated = state;
    }

    public boolean getActivated () {
        return this.activated;
    }

    public HashMap<String, Account> getAccounts () {
        return this.accounts;
    }

    public Account getAccount (String Id) {
        return this.getAccounts().get(Id);
    }

    public List<Operation> getLedger () {
        return this.ledger;
    }

    public void addAccount (Account account) {
        this.getAccounts().put(account.getId(),account);
        this.addOperation(new CreateOp(account.getId()));
    }

    public void addOperation (Operation operation) {
        this.getLedger().add(operation);
    }

    public void removeAccount (String Id) {
        this.getAccounts().remove(Id);
        this.addOperation(new DeleteOp(Id));
    }

    public void transferTo (Account account1, Account account2, Integer amount) {
        account1.setMoney(account1.getMoney() - amount);
        account2.setMoney(account2.getMoney() + amount);
        addOperation(new TransferOp(account1.getId(), account2.getId(), amount));
    }

    public void activate () {
        setActivated(true);
    }

    public void desactivate () {
        setActivated(false);
    }

    /* TODO: Here should be declared all the server state attributes
         as well as the methods to access and interact with the state. */

}
