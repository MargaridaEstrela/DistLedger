package pt.tecnico.distledger.server.domain.operation;

import java.util.List;

public class DeleteOp extends Operation {

    public DeleteOp(String account, List<Integer> prevTS) {
        super(account, prevTS);
    }

    public String getType () {
        return "DELETE";
    }

}
