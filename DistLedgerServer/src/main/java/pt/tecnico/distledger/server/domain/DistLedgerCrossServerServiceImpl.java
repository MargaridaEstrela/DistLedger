package pt.tecnico.distledger.server.domain;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import pt.tecnico.distledger.server.domain.operation.*;

import java.util.ArrayList;
import java.util.List;


public class DistLedgerCrossServerServiceImpl extends DistLedgerCrossServerServiceImplBase {

    //Private variables
    private ServerState server;
    private boolean debugFlag;

    //Constructor
    public DistLedgerCrossServerServiceImpl(ServerState server, boolean debugFlag) {
        this.server = server;
        this.debugFlag = debugFlag;
    }

    @Override
    public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {

        List<pt.tecnico.distledger.server.domain.operation.Operation> ledger = new ArrayList<pt.tecnico.distledger.server.domain.operation.Operation>();
        request.getState().getLedgerList().forEach(op -> addOperation(ledger, op));
        server.update(ledger);

        PropagateStateResponse response = PropagateStateResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private void addOperation(List<pt.tecnico.distledger.server.domain.operation.Operation> ledger, pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Operation op) {
        pt.tecnico.distledger.server.domain.operation.Operation operation = null;
        if(op.getType() == OperationType.OP_TRANSFER_TO) {
            operation = new TransferOp(op.getUserId(), op.getDestUserId(), (int) op.getAmount());
        }
        else if(op.getType() == OperationType.OP_DELETE_ACCOUNT) {
            operation = new DeleteOp(op.getUserId());
        }
        else if(op.getType() == OperationType.OP_CREATE_ACCOUNT) {
            operation = new CreateOp(op.getUserId());
        }
        ledger.add(operation);
    }
}