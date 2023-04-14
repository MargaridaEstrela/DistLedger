package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.Operation;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class UpdateLog {
    
    private List<Integer> replicaTS;
    private List<Operation> updateLog;
    private Integer replicaNumber;

    public UpdateLog(List<Integer> replicaTS, Integer replicaNumber) {
        this.replicaTS = replicaTS;
        this.updateLog = new ArrayList<Operation>();
        this.replicaNumber = replicaNumber-1;
    }

    public List<Integer> getReplicaTS() {
        return this.replicaTS;
    }

    public  List<Operation> getUpdateLog() {
        return this.updateLog;
    }

    public Integer getReplicaNumber() {
        return this.replicaNumber;
    }

    public List<Integer> addToUpdateLog(Operation operation) {
        this.getReplicaTS().set(this.getReplicaNumber(),this.getReplicaTS().get(this.getReplicaNumber())+1);
        this.setOperationTS(operation, this.getReplicaNumber(), this.getReplicaTS().get(this.getReplicaNumber()));
        this.getUpdateLog().add(operation);
        return new ArrayList<Integer>(operation.getTS());
    }

    private void setOperationTS(Operation operation, Integer position, Integer value) {
        while(operation.getTS().size() < position+1) {
            operation.getTS().add(0);
        }
        operation.getTS().set(position,value);
    }

    public Operation getOperation(List<Integer> ts) {
        for(Operation operation : getUpdateLog()) {
            if(this.equalTS(operation.getTS(),ts)) {
                return operation;
            }
        }
        return null;
    }

    private boolean equalTS (List<Integer> ts1, List<Integer> ts2) {
        while (ts2.size() < ts1.size()) {
            ts2.add(0);
        }
        while (ts2.size() > ts1.size()) {
            ts1.add(0);
        }
        for(int i = 0; i < ts2.size(); i++) {
            if(ts1.get(i) != ts2.get(i)) {
                return false;
            }
        }
        return true;
    }

    public void removeOperation(Operation operation) {
        this.getUpdateLog().remove(operation);
    }
}
