package pt.tecnico.distledger.server.domain;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc.UserServiceImplBase;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.tecnico.distledger.server.domain.account.Account;
import pt.tecnico.distledger.server.domain.operation.*;
import static pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.ResponseCode.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.namingserver.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupResponse;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateResponse;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger;

import io.grpc.StatusRuntimeException;
import static io.grpc.Status.UNAVAILABLE;
import java.util.ArrayList;
import java.util.List;

public class UserServiceImpl extends UserServiceImplBase {

    //Private variables
    private ServerState server;
    private boolean debugFlag;
    private String type;

    //Constructor
    public UserServiceImpl(ServerState server, boolean debugFlag, String type) {
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
    public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {

        if(debugFlag) {
            debug("balance Request started\n");
        }

        if(!server.getActivated()) {
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        ResponseCode code = OK;
        BalanceResponse.Builder response = BalanceResponse.newBuilder();

        //Check existance of account
        if(!server.existsAccount(request.getUserId())) {
            code = NON_EXISTING_USER;
        }
        else {
            response.setValue(server.getMoneyAccount(request.getUserId()));
        }

        responseObserver.onNext(response.setCode(code).build());
            
        responseObserver.onCompleted();

        if(debugFlag) {
            debug("balance Request completed\n");
        }
    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {

        if(debugFlag) {
            debug("createAccount Request started\n");
        }

        if(!server.getActivated()) {
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        ResponseCode code = OK;

        if (!this.type.equals("A")) {
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        //Check if account already exists
        if(server.existsAccount(request.getUserId())) {
            code = USER_ALREADY_EXISTS;
        }
        else {
            //Add/Create account
            server.addAccount(request.getUserId());
        }
        if(propagate() < 0) {
            server.removeAccount(request.getUserId());
            //TODO REMOVE LAST 2 OPERATIONS
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        CreateAccountResponse response = CreateAccountResponse.newBuilder().setCode(code).build();

        responseObserver.onNext(response);

        responseObserver.onCompleted();

        if(debugFlag) {
            debug("createAccount Request completed\n");
        }
    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {

        if(debugFlag) {
            debug("deleteAccount Request started\n");
        }

        if(!server.getActivated()) {
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        ResponseCode code = OK;

        if (!this.type.equals("A")) {
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        //account to be deleted, used only for the synchronization
        Account account = server.getAccount(request.getUserId());

        //check if account exists
        if(account == null) {
            code = NON_EXISTING_USER;
        }
        else{
            synchronized(account) {
                //check if the account still exists. In order to prevent a case where an account was deleted between the getter and the synchronization
                if(!server.existsAccount(request.getUserId())) {
                    code = NON_EXISTING_USER;
                }
                //check if there is money on the account
                else if(server.hasMoney(request.getUserId())) {
                    code = AMOUNT_NOT_SUPORTED;
                }
                else {
                    //remove account
                    server.removeAccount(request.getUserId());
                }
                if(propagate() < 0) {
                    server.addAccount(request.getUserId());
                    responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
                    return;
                }
            }
        }

        DeleteAccountResponse response = DeleteAccountResponse.newBuilder().setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            debug("deleteAccount Request completed\n");
        }
    }

    @Override
    public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {

        if(debugFlag) {
            debug("transferTo Request started\n");
        }

        if(!server.getActivated()) {
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        ResponseCode code = OK;

        if (!this.type.equals("A")) {
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        //accounts for the synchronization
        Account accountTo = server.getAccount(request.getAccountTo());
        Account accountFrom = server.getAccount(request.getAccountFrom());

        if(accountTo == null || accountFrom == null) {
            code = NON_EXISTING_USER;
        }
        else{
            synchronized(accountFrom){
                synchronized(accountTo){
                    //check if the accounts still exist
                    if(!server.existsAccount(request.getAccountTo()) || !server.existsAccount(request.getAccountFrom())) {
                        code = NON_EXISTING_USER;
                    }
                    //check if the amount from has enought money for the trasnsaction
                    else if(!server.hasMoney(request.getAccountFrom(), request.getAmount()) || !(request.getAmount() > 0)) {
                        code = AMOUNT_NOT_SUPORTED;
                    }
                    else {
                        server.transferTo(request.getAccountFrom(), request.getAccountTo(), request.getAmount());
                    }
                    if(propagate() < 0) {
                        server.transferTo(request.getAccountTo(), request.getAccountFrom(), request.getAmount());
                        responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
                        return;
                    }
                }
            }
        }

        TransferToResponse response = TransferToResponse.newBuilder().setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            debug("transferTo Request completed\n");
        }
    }

    private List<String> lookup() {

        List<String> res = new ArrayList<String>();
        String serviceName = "DistLedger";
        String type = "B";

        try {
            final String host = "localhost";
            final int namingServerPort = 5001;
            final String target = host + ":" + namingServerPort;
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            NamingServerServiceGrpc.NamingServerServiceBlockingStub stub2 = NamingServerServiceGrpc.newBlockingStub(channel);

            LookupRequest lookupRequest = LookupRequest.newBuilder().setServiceName(serviceName).setType(type).build();
            LookupResponse lookupResponse = stub2.lookup(lookupRequest);
            
            for (String server : lookupResponse.getServersList()) {
                res.add(server);
            }

        } catch (StatusRuntimeException e) {
            // Debug message
            debug("Server " + serviceName + " is unreachable");

            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }
        return res;
    }

    private int propagate() {
        List<String> addresses = this.lookup();

        ArrayList<pt.tecnico.distledger.server.domain.operation.Operation> operations = new ArrayList(server.getLedger());

        LedgerState.Builder ledger = LedgerState.newBuilder();

        for (pt.tecnico.distledger.server.domain.operation.Operation operation : operations) {
            pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Operation.Builder operationContract = DistLedgerCommonDefinitions.Operation.newBuilder();

            //Check the type of Operation
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
            ledger.addLedger(operationContract.build());
        }
        
        PropagateStateRequest request = PropagateStateRequest.newBuilder().setState(ledger.build()).build();


        for (String address : addresses) {

            try {
                final ManagedChannel channel = ManagedChannelBuilder.forTarget(address).usePlaintext().build();
                DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel);

                PropagateStateResponse response = stub.propagateState(request);
            }

            catch (StatusRuntimeException e) {
                return -1;
            }
        }
        return 0;
    }
}