package pt.tecnico.distledger.server.domain;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import static io.grpc.Status.UNAVAILABLE;
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

    //debug
    public void debug(String debugMessage) {
        if (debugFlag) {
            System.err.println("DEBUG: " + debugMessage);
        }
    }

    @Override
    public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {

        //Check debug
        if(debugFlag) {
            debug("propagateState Request started\n");
        }

        //Check if server is active
        if(!server.getActivated()) {
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        //New ledger
        List<pt.tecnico.distledger.server.domain.operation.Operation> ledger = new ArrayList<pt.tecnico.distledger.server.domain.operation.Operation>();

        //Read the operations and create an Operation object corresponing to the operation type
        request.getState().getLedgerList().forEach(op -> addOperation(ledger, op));

        synchronized(server) {
            server.gossip(ledger, new ArrayList<Integer>(request.getReplicaTSList()));
        }

        //send response
        PropagateStateResponse response = PropagateStateResponse.newBuilder().build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        //Check debug
        if(debugFlag) {
            debug("propagateState Request ended\n");
        }
    }

    //TODO
    //Convert a Proto Operation into a ServerState Operation
    private void addOperation(List<pt.tecnico.distledger.server.domain.operation.Operation> ledger, pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Operation op) {
        pt.tecnico.distledger.server.domain.operation.Operation operation = null;
        //get the type of operation and call the constructor
        if(op.getType() == OperationType.OP_TRANSFER_TO) {
            operation = new TransferOp(op.getUserId(), op.getDestUserId(), (int) op.getAmount(), new ArrayList<Integer>(op.getPrevTSList()), new ArrayList<Integer>(op.getTSList()));
        }
         else if(op.getType() == OperationType.OP_DELETE_ACCOUNT) {
             operation = new DeleteOp(op.getUserId(), new ArrayList<Integer>(op.getPrevTSList()), new ArrayList<Integer>(op.getTSList()));
        }
        else if(op.getType() == OperationType.OP_CREATE_ACCOUNT) {
            operation = new CreateOp(op.getUserId(), new ArrayList<Integer>(op.getPrevTSList()), new ArrayList<Integer>(op.getTSList()));
        }
        //Add the new operation to a list (ledger)
        ledger.add(operation);
    }
}