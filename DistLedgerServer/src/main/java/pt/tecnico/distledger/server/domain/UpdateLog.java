package pt.tecnico.distledger.server.domain;

import pt.tecnico.distledger.server.domain.operation.Operation;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class UpdateLog {
    
    private List<Integer> replicaTS;
    private HashMap<List<Integer>, Operation> updateLog;
    private Integer replicaNumber;

    public UpdateLog(List<Integer> replicaTS, Integer replicaNumber) {
        this.replicaTS = replicaTS;
        this.updateLog = new HashMap<List<Integer>, Operation>();
        this.replicaNumber = replicaNumber;
    }

    public List<Integer> getReplicaTS() {
        return this.replicaTS;
    }

    public HashMap<List<Integer>, Operation> getUpdateLog() {
        return this.updateLog;
    }

    public Integer getReplicaNumber() {
        return this.replicaNumber;
    }

    public List<Integer> addToUpdateLog(Operation operation) {
        this.getReplicaTS().set(this.getReplicaNumber(),this.getReplicaTS().get(this.getReplicaNumber()+1));
        List<Integer> ts = new ArrayList<Integer>(operation.getPrevTS());
        ts.set(this.getReplicaNumber(),this.getReplicaTS().get(this.getReplicaNumber()));
        this.getUpdateLog().put(ts,operation);
        return ts;
    }

    public Operation getOperation(List<Integer> key) {
        return this.getUpdateLog().get(key);
    }

    public void removeOperation(List<Integer> key) {
        this.getUpdateLog().remove(key);
    }
}
