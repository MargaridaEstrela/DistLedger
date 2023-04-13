package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.*;
import pt.tecnico.distledger.server.domain.account.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class ServerState {

    //Private variables
    private UpdateLog unstables;
    private List<Operation> ledger;
    private List<Integer> valueTS;
    private Integer num;

    private HashMap<String, Account> accounts;
    private boolean activated;

    //Constructor
    public ServerState(Integer replicaNumber) {
        //creation of the log
        this.num = replicaNumber;
        this.valueTS = new ArrayList<Integer>();
        this.startTS();
        this.unstables = new UpdateLog(new ArrayList<Integer>(valueTS), replicaNumber);
        
        //creation of the ledger and account HashMap
        this.ledger = new ArrayList<Operation>();
        this.accounts = new HashMap<String, Account>();
        this.activated = true;

        //creation of the Broker Account
        Account broker = new Account("broker", 1000);
        this.accounts.put("broker",broker);
    }

    //inicialize TS
    private void startTS() {
        for(int i = 0; i < this.getNum(); i++) {
            this.getValueTS().add(0);
        }
    }

    public void activate () {
        setActivated(true);
    }

    public void deactivate () {
        setActivated(false);
    }

    //Setters:
    public void setActivated (boolean state) {
        this.activated = state;
    }

    //Getters:
    public boolean getActivated () {
        return this.activated;
    }

    public List<Integer> getValueTS() {
        return this.valueTS;
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

    public Integer getNum () {
        return this.num;
    }

    public Integer getMoneyAccount (String Id) {
        return getAccount(Id).getMoney();
    }

    public String getOperationType (Operation operation) {
        return operation.getType();
    }

    public UpdateLog getUnstables() {
        return this.unstables;
    }

    private boolean canExecute (List<Integer> ts) {
        while (ts.size() < this.getValueTS().size()) {
            ts.add(0);
        }
        while (ts.size() > this.getValueTS().size()) {
            this.getValueTS().add(0);
        }
        for(int i = 0; i < ts.size(); i++) {
            if(ts.get(i) > this.getValueTS().get(i)) {
                return false;
            }
        }
        return true;
    }

    private void merge(List<Integer> ts1, List<Integer> ts2) {
        while (ts1.size() < ts2.size()) {
            ts1.add(0);
        }
        while (ts2.size() > ts1.size()) {
            ts2.add(0);
        }
        for(int i = 0; i < ts2.size(); i++) {
            if(ts1.get(i) < ts2.get(i)) {
                ts1.set(i,ts2.get(i));
            }
        }
    }

    public boolean existsAccount (String Id) {
        return getAccount(Id) != null;
    }

    public boolean hasMoney (String Id, int amount) {
        return getMoneyAccount(Id) >= amount;
    }

    public boolean hasMoney (String Id) {
        return getMoneyAccount(Id) > 0;
    }

    //Execute/Add Update Operation to the ledger
    public List<Integer> updateOperation(Operation operation) {
        List<Integer> ts = this.getUnstables().addToUpdateLog(operation);
        if(this.canExecute(operation.getPrevTS())) {
            this.executeOperation(operation);
            this.merge(this.getValueTS(), operation.getPrevTS());
            this.getUnstables().removeOperation(ts);
            this.tryExecuteMore();
        }
        return ts;
    }

    //Check if more operations have become stable
    private void tryExecuteMore() {
        boolean update = false;
        for(List<Integer> ts : this.getUnstables().getUpdateLog().keySet()) {
            if(this.canExecute(ts)) {
                update = true;
                this.executeOperation(this.getUnstables().getOperation(ts));
                this.merge(this.getValueTS(), this.getUnstables().getOperation(ts).getPrevTS());
                this.getUnstables().removeOperation(ts);
            }
        }
        if(update) {
            this.tryExecuteMore();
        }
    }

    //Execute Query TODO Mover para service?
    public List<List<Integer>> queryOperation(List<Integer> prevTS, String accountID) {
        List<List<Integer>> answer = new ArrayList<List<Integer>>();
        answer.add(this.getValueTS());
        if(this.canExecute(prevTS)) {
            answer.add(this.getBalance(accountID));
        }
        return answer;
    }

    //Execute an operation
    private void executeOperation (Operation operation) {
        //Get the type of operation and execute it
        if(operation.getType() == "CREATE") {
            this.createAccount((CreateOp) operation);
        }
        else if(operation.getType() == "DELETE") {
            this.deleteAccount((DeleteOp) operation);
        }
        else if(operation.getType() == "TRANSFER") {
            this.transferTo((TransferOp) operation);
        }
    }

    private List<Integer> getBalance(String accountID) {
        List<Integer> answer = new ArrayList<Integer>();
        if(!this.existsAccount(accountID)) {
            answer.add(1);
            return answer;
        }
        answer.add(0);
        answer.add(this.getMoneyAccount(accountID));
        return answer;
    }

    private void createAccount(CreateOp operation) {
        if(!this.existsAccount(operation.getAccount())) {
            this.getAccounts().put(operation.getAccount(),new Account(operation.getAccount(), 0));
        }
    }

    private void deleteAccount(DeleteOp operation) {
        if(this.existsAccount(operation.getAccount())) {
            this.getAccounts().remove(operation.getAccount());
        }
    }

    private void transferTo(TransferOp operation) {
        if(this.existsAccount(operation.getAccount()) && this.existsAccount(operation.getDestAccount()) 
        && this.hasMoney(operation.getAccount(), operation.getAmount()) && operation.getAmount() > 0) {
            getAccount(operation.getAccount()).setMoney(getAccount(operation.getAccount()).getMoney() - operation.getAmount());
            getAccount(operation.getDestAccount()).setMoney(getAccount(operation.getDestAccount()).getMoney() + operation.getAmount());
        }
    }
}