package pt.tecnico.distledger.server.domain.operation;

import java.util.List;

public class TransferOp extends Operation {
    private String destAccount;
    private int amount;

    public TransferOp(String fromAccount, String destAccount, int amount, List<Integer> prevTS) {
        super(fromAccount, prevTS);
        this.destAccount = destAccount;
        this.amount = amount;
    }

    public String getType () {
        return "TRANSFER";
    }

    public String getDestAccount() {
        return destAccount;
    }

    public void setDestAccount(String destAccount) {
        this.destAccount = destAccount;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

}
