package pt.tecnico.distledger.server.domain;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc.UserServiceImplBase;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.tecnico.distledger.server.domain.operation.*;

import static pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.ResponseCode.*;

import static io.grpc.Status.UNAVAILABLE;
import java.util.List;
import java.util.ArrayList;

public class UserServiceImpl extends UserServiceImplBase {

    //Private variables
    private ServerState server;
    private boolean debugFlag;

    //Constructor
    public UserServiceImpl(ServerState server, boolean debugFlag) {
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
    public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {

        //Check debug flag
        if(debugFlag) {
            debug("balance Request started\n");
        }

        //Check if the server is active
        if(!server.getActivated()) {
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        ResponseCode code = OK;
        BalanceResponse.Builder response = BalanceResponse.newBuilder();

        List<List<Integer>> query = server.queryOperation(new ArrayList<Integer>(request.getPrevTSList()), request.getUserId());

        if(query.size() < 2) {
            code = UNABLE_TO_DETERMINE;
        }
        else {
            response.addAllValueTS(query.get(0));
            if(query.get(1).get(0) == 1) {
                code = NON_EXISTING_USER;
            }
            else {
                response.setValue(query.get(1).get(1));
            }
        }

        responseObserver.onNext(response.setCode(code).build());
        responseObserver.onCompleted();

        //Check debug flag
        if(debugFlag) {
            debug("balance Request completed\n");
        }
    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {

        //Check debug flag
        if(debugFlag) {
            debug("createAccount Request started\n");
        }

        //Check if the server is active
        if(!server.getActivated()) {
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        ResponseCode code = OK;
        CreateAccountResponse response;

        CreateOp operation = new CreateOp(request.getUserId(), new ArrayList<Integer>(request.getPrevTSList()),new ArrayList<Integer>());

        response = CreateAccountResponse.newBuilder().setCode(code).addAllTS(server.updateOperation(operation)).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        //Check debug flag
        if(debugFlag) {
            debug("createAccount Request completed\n");
        }
    }

    // @Override
    // public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {

    //     //Check debug flag
    //     if(debugFlag) {
    //         debug("deleteAccount Request started\n");
    //     }

    //     //Check if the server is active
    //     if(!server.getActivated()) {
    //         responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
    //         return;
    //     }

    //     ResponseCode code = OK;

    //     //Check if this operation can be performed on this server
    //     if (!this.type.equals("A")) {
    //         responseObserver.onError(UNAVAILABLE.withDescription("This server is not allowed to execute this operation").asRuntimeException());
    //         return;
    //     }

    //     synchronized(server) {
    //         //check if the account still exists.
    //         if(!server.existsAccount(request.getUserId())) {
    //             code = NON_EXISTING_USER;
    //         }
    //         else {
    //             //check if there is money on the account
    //             if(server.hasMoney(request.getUserId())) {
    //                 code = AMOUNT_NOT_SUPORTED;
    //             }
    //             else {
    //                 //remove account
    //                 server.removeAccount(request.getUserId());
    
    //                 //propagate the changes to the server B
    //                 if(propagate() < 0) {   //-1 if error on server B
    //                     //Rollback the changes by recreating the account deleted
    //                     server.addAccount(request.getUserId());
    
    //                     //Rollback the createAccount operation and the deleteAccount used to rollback the changes
    //                     server.removeOperation();
    //                     server.removeOperation();
    
    //                     //Server B is unavailable so the operation can not be performed
    //                     responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
    //                     return;
    //                 }
    //             }
    //         }
    //     }

    //     DeleteAccountResponse response = DeleteAccountResponse.newBuilder().setCode(code).build();

    //     responseObserver.onNext(response);
    //     responseObserver.onCompleted();

    //     //Check debug flag
    //     if(debugFlag) {
    //         debug("deleteAccount Request completed\n");
    //     }
    // }

    @Override
    public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {

        //Check debug flag
        if(debugFlag) {
            debug("transferTo Request started\n");
        }

        //Check if the server is active
        if(!server.getActivated()) {
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        ResponseCode code = OK;

        TransferOp operation = new TransferOp(request.getAccountFrom(), request.getAccountTo(), request.getAmount(), new ArrayList<Integer>(request.getPrevTSList()),new ArrayList<Integer>());
        
        TransferToResponse response = TransferToResponse.newBuilder().setCode(code).addAllTS(server.updateOperation(operation)).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        //Check debug flag
        if(debugFlag) {
            debug("transferTo Request completed\n");
        }
    }

    // private int propagate() {

    //     //get the list with type B servers
    //     List<String> addresses = this.lookup();

    //     //List of operations on the current server (ledger)
    //     ArrayList<pt.tecnico.distledger.server.domain.operation.Operation> operations = new ArrayList(server.getLedger());

    //     LedgerState.Builder ledger = LedgerState.newBuilder();

    //     //Retrieve all operations done so far
    //     for (pt.tecnico.distledger.server.domain.operation.Operation operation : operations) {
    //         pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Operation.Builder operationContract = DistLedgerCommonDefinitions.Operation.newBuilder();
    //         //Check the type of Operation
    //         if(operation.getType() == "CREATE") {
    //             operationContract.setType(OperationType.OP_CREATE_ACCOUNT);
    //         }
    //         else if(operation.getType() == "DELETE") {
    //             operationContract.setType(OperationType.OP_DELETE_ACCOUNT);
    //         }
    //         else if(operation.getType() == "TRANSFER") {
    //             operationContract.setType(OperationType.OP_TRANSFER_TO);
    //             TransferOp transferoperation = (TransferOp) operation;
    //             operationContract.setDestUserId(transferoperation.getDestAccount());
    //             operationContract.setAmount(transferoperation.getAmount());
    //         }
    //         else {
    //             operationContract.setType(OperationType.OP_UNSPECIFIED);
    //         }
    //         operationContract.setUserId(operation.getAccount());
    //         ledger.addLedger(operationContract.build());
    //     }
        
    //     //Request message
    //     PropagateStateRequest request = PropagateStateRequest.newBuilder().setState(ledger.build()).build();

    //     //propagate to all B servers
    //     for (String address : addresses) {

    //         final ManagedChannel channel = ManagedChannelBuilder.forTarget(address).usePlaintext().build();
    //         //Try to propagate to all server if unavailable return -1 else return 0
    //         try {
    //             DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel);

    //             PropagateStateResponse response = stub.propagateState(request);

    //             channel.shutdown();
    //         }

    //         catch (StatusRuntimeException e) {
    //             channel.shutdown();
    //             return -1;
    //         }
    //     }
    //     return 0;
    // }
}