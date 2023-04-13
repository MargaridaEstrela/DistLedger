package pt.tecnico.distledger.server.domain.operation;

import java.util.List;

public class Operation {
    private String account;
    private boolean stable;
    private List<Integer> prevTS;
    private List<Integer> ts;

    public Operation(String fromAccount, List<Integer> prevTS, List<Integer> ts) {
        this.stable = false;
        this.account = fromAccount;
        this.prevTS = prevTS;
        this.ts = ts;
    }

    public void stabilize() {
        this.stable = true;
    }

    public boolean getStable() {
        return this.stable;
    }

    public String getAccount() {
        return account;
    }

    public String getType () {
        return "UNSPECIFIED";
    }

    public List<Integer> getPrevTS() {
        return this.prevTS;
    }

    public List<Integer> getTS() {
        return this.ts;
    }

    public void setTS(List<Integer> ts) {
        this.ts = ts;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
