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
        Account broker = new Account("broker", 1000);
        this.accounts.put("broker",broker);
        this.activated = true;
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

    public void addAccount (String AccountId) {
        Account account = new Account(AccountId, 0);
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

    public void transferTo (String accountIdFrom, String accountIdTo, Integer amount) {
        Account accountFrom = getAccount(accountIdFrom);
        Account accountTo = getAccount(accountIdTo);
        accountFrom.setMoney(accountFrom.getMoney() - amount);
        accountTo.setMoney(accountTo.getMoney() + amount);
        addOperation(new TransferOp(accountFrom.getId(), accountTo.getId(), amount));
    }

    public boolean existsAccount (String Id) {
        return getAccount(Id) != null;
    }

    public Integer getMoneyAccount (String Id) {
        return getAccount(Id).getMoney();
    }

    public boolean hasMoney (String Id, int amount) {
        return getMoneyAccount(Id) >= amount;
    }

    public boolean hasMoney (String Id) {
        return getMoneyAccount(Id) > 0;
    }

    public void activate () {
        setActivated(true);
    }

    public void deactivate () {
        setActivated(false);
    }

    public String getOperationType (Operation operation) {
        return operation.getType();
    }
}
