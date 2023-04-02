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

    //Private variables
    private ServerState server;
    private boolean debugFlag;
    private String type;

    //Constructor
    public AdminServiceImpl(ServerState server, boolean debugFlag, String type) {
        this.server = server;
        this.debugFlag = debugFlag;
        this.type = type;
    }

    //debug
    public void debug(String debugMessage) {
        if (debugFlag) {
            System.err.println("DEBUG: " + debugMessage);
        }
    }

    @Override
    public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {

        if(debugFlag) {
            debug("activate Request started\n");
        }

        ResponseCode code = OK;

        //call the activate function
        server.activate();

        ActivateResponse response = ActivateResponse.newBuilder().setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            debug("activate Request completed\n");
        }
    }

    @Override
    public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {

        if(debugFlag) {
            debug("deactivate Request started\n");
        }

        ResponseCode code = OK;

        //call the deactivate function
        server.deactivate();

        DeactivateResponse response = DeactivateResponse.newBuilder().setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            debug("deactivate Request completed\n");
        }
    }

    @Override
    public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {

        if(debugFlag) {
            debug("gossip Request started\n");
        }

        ResponseCode code = OK;

        //TODO 3ª fase

        GossipResponse response = GossipResponse.newBuilder().setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            debug("gossip Request completed\n");
        }
    }

    @Override
    public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {

        if(debugFlag) {
            debug("getLedgerState Request started\n");
        }

        ResponseCode code = OK;

        //ArrayList of operations from the server ledger
        ArrayList<pt.tecnico.distledger.server.domain.operation.Operation> operations = new ArrayList(server.getLedger());

        LedgerState.Builder ledger = LedgerState.newBuilder();

        for (pt.tecnico.distledger.server.domain.operation.Operation operation : operations) {
            pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Operation.Builder operationContract = DistLedgerCommonDefinitions.Operation.newBuilder();

            //Check the type of Operation
            if(operation.getType() == "CREATE") {
                operationContract.setType(OperationType.OP_CREATE_ACCOUNT);
            }
            // else if(operation.getType() == "DELETE") {
            //     operationContract.setType(OperationType.OP_DELETE_ACCOUNT);
            // }
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
            ledger.addLedger(operationContract.build());
        }
        
        getLedgerStateResponse response = getLedgerStateResponse.newBuilder().setLedgerState(ledger.build()).setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            debug("getLedgerState Request completed\n");
        }
    }
}