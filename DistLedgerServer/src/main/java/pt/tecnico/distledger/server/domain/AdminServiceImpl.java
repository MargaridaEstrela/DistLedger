package pt.tecnico.distledger.server.domain;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc.AdminServiceImplBase;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import pt.tecnico.distledger.server.domain.operation.*;


import java.util.ArrayList;
import java.util.List;

public class AdminServiceImpl extends AdminServiceImplBase {

    private ServerState server;

    public AdminServiceImpl(ServerState server) {
        this.server = server;
    }

    @Override
    public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {

        server.activate();

        ActivateResponse response = ActivateResponse.newBuilder().build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();
    }

    @Override
    public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {

        server.deactivate();

        DeactivateResponse response = DeactivateResponse.newBuilder().build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();
    }

    @Override
    public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {

        //TODO 3ª fase

        GossipResponse response = GossipResponse.newBuilder().build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();
    }

    @Override
    public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {

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
        
        getLedgerStateResponse response = getLedgerStateResponse.newBuilder().setLedgerState(ledger).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();
    }
}