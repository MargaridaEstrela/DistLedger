package pt.tecnico.distledger.server.domain;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc.UserServiceImplBase;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.tecnico.distledger.server.domain.account.Account;
import static pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.ResponseCode.*;
import static io.grpc.Status.UNAVAILABLE;

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
    public static void debug(String debugMessage) {
        if (debugFlag) {
            System.err.println("DEBUG: " + debugMessage);
        }
    }

    @Override
    public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {

        if(debugFlag) {
            debu("balance Request started\n");
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
            debu("balance Request completed\n");
        }
    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {

        if(debugFlag) {
            debu("createAccount Request started\n");
        }

        if(!server.getActivated()) {
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        ResponseCode code = OK;

        //Check if account already exists
        if(server.existsAccount(request.getUserId())) {
            code = USER_ALREADY_EXISTS;
        }
        else {
            //Add/Create account
            server.addAccount(request.getUserId());
        }

        CreateAccountResponse response = CreateAccountResponse.newBuilder().setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            debu("createAccount Request completed\n");
        }
    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {

        if(debugFlag) {
            debu("deleteAccount Request started\n");
        }

        if(!server.getActivated()) {
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        ResponseCode code = OK;

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
            }
        }

        DeleteAccountResponse response = DeleteAccountResponse.newBuilder().setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            debu("deleteAccount Request completed\n");
        }
    }

    @Override
    public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {

        if(debugFlag) {
            debu("transferTo Request started\n");
        }

        if(!server.getActivated()) {
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        ResponseCode code = OK;

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
                }
            }
        }

        TransferToResponse response = TransferToResponse.newBuilder().setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            debu("transferTo Request completed\n");
        }
    }
}