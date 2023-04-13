package pt.tecnico.distledger.server.domain.operation;

import java.util.List;

public class DeleteOp extends Operation {

    public DeleteOp(String account, List<Integer> prevTS, List<Integer> ts) {
        super(account, prevTS, ts);
    }

    public String getType () {
        return "DELETE";
    }

}
