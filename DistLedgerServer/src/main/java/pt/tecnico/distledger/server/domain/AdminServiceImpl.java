package pt.tecnico.distledger.server.domain;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc.AdminServiceImplBase;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import static pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.ResponseCode.*;
import pt.tecnico.distledger.server.domain.operation.*;


import java.util.ArrayList;
import java.util.List;

public class AdminServiceImpl extends AdminServiceImplBase {

    private ServerState server;

    private boolean debugFlag;

    public AdminServiceImpl(ServerState server, boolean debugFlag) {
        this.server = server;
        this.debugFlag = debugFlag;
    }

    @Override
    public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {

        if(debugFlag) {
            System.err.println("[DEBUG: activate Request started]\n");
        }

        ResponseCode code = OK;

        server.activate();

        ActivateResponse response = ActivateResponse.newBuilder().setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            System.err.println("[DEBUG: activate Request completed]\n");
        }
    }

    @Override
    public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {

        if(debugFlag) {
            System.err.println("[DEBUG: deactivate Request started]\n");
        }

        ResponseCode code = OK;

        server.deactivate();

        DeactivateResponse response = DeactivateResponse.newBuilder().setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            System.err.println("[DEBUG: deactivate Request completed]\n");
        }
    }

    @Override
    public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {

        if(debugFlag) {
            System.err.println("[DEBUG: gossip Request started]\n");
        }

        ResponseCode code = OK;

        //TODO 3ª fase

        GossipResponse response = GossipResponse.newBuilder().setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            System.err.println("[DEBUG: gossip Request completed]\n");
        }
    }

    @Override
    public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {

        if(debugFlag) {
            System.err.println("[DEBUG: getLedgerState Request started]\n");
        }

        ResponseCode code = OK;

        ArrayList<pt.tecnico.distledger.server.domain.operation.Operation> operations = new ArrayList(server.getLedger());
        LedgerState.Builder ledger = LedgerState.newBuilder();
        for (pt.tecnico.distledger.server.domain.operation.Operation operation : operations) {
            pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Operation.Builder operationContract = DistLedgerCommonDefinitions.Operation.newBuilder();
            if(operation.getType() == "CREATE") {
                operationContract.setType(OperationType.OP_CREATE_ACCOUNT);
            }
            else if(operation.getType() == "DELETE") {
                operationContract.setType(OperationType.OP_DELETE_ACCOUNT);
            }
            else if(operation.getType() == "TRANSFER") {
                operationContract.setType(OperationType.OP_TRANSFER_TO);
                TransferOp transferoperation = (TransferOp) operation;
                operationContract.setDestUserId(transferoperation.getDestAccount());
                operationContract.setAmount(transferoperation.getAmount());
            }
            else {
                operationContract.setType(OperationType.OP_UNSPECIFIED);
            }
            operationContract.setUserId(operation.getAccount());
            ledger.addLedger(operationContract);
        }
        
        getLedgerStateResponse response = getLedgerStateResponse.newBuilder().setLedgerState(ledger).setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            System.err.println("[DEBUG: getLedgerState Request completed]\n");
        }
    }
}