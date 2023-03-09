package pt.tecnico.distledger.server.domain;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc.UserServiceImplBase;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import static pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.ResponseCode.*;
import pt.tecnico.distledger.server.domain.account.Account;
import static io.grpc.Status.UNAVAILABLE;

public class UserServiceImpl extends UserServiceImplBase {

    private ServerState server;

    private boolean debugFlag;

    public UserServiceImpl(ServerState server, boolean debugFlag) {
        this.server = server;
        this.debugFlag = debugFlag;
    }

    @Override
    public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {

        if(debugFlag) {
            System.err.println("[DEBUG: balance Request started]\n");
        }

        if(!server.getActivated()) {
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        ResponseCode code = OK;
        BalanceResponse.Builder response = BalanceResponse.newBuilder();

        Account account = server.getAccount(request.getUserId());

        if(account == null) {
            code = NON_EXISTING_USER;
        }
        else {
            response.setValue(account.getMoney());
        }

        responseObserver.onNext(response.setCode(code).build());
            
        responseObserver.onCompleted();

        if(debugFlag) {
            System.err.println("[DEBUG: balance Request completed]\n");
        }
    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {

        if(debugFlag) {
            System.err.println("[DEBUG: createAccount Request started]\n");
        }

        if(!server.getActivated()) {
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        ResponseCode code = OK;

        if(server.getAccount(request.getUserId()) != null) {
            code = USER_ALREADY_EXISTS;
        }
        else {
            Account account = new Account(request.getUserId(),0);
            server.addAccount(account);
        }

        CreateAccountResponse response = CreateAccountResponse.newBuilder().setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            System.err.println("[DEBUG: createAccount Request completed]\n");
        }
    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {

        if(debugFlag) {
            System.err.println("[DEBUG: deleteAccount Request started]\n");
        }

        if(!server.getActivated()) {
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        ResponseCode code = OK;

        Account account = server.getAccount(request.getUserId());

        if(account == null) {
            code = NON_EXISTING_USER;
        }
        else if(account.getMoney() > 0) {
            code = AMOUNT_NOT_SUPORTED;
        }
        else {
            server.removeAccount(request.getUserId());
        }

        DeleteAccountResponse response = DeleteAccountResponse.newBuilder().setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            System.err.println("[DEBUG: deleteAccount Request completed]\n");
        }
    }

    @Override
    public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {

        if(debugFlag) {
            System.err.println("[DEBUG: transferTo Request started]\n");
        }

        if(!server.getActivated()) {
            responseObserver.onError(UNAVAILABLE.withDescription("Server is Unavailable").asRuntimeException());
            return;
        }

        ResponseCode code = OK;

        Account account_from = server.getAccount(request.getAccountFrom());
        Account account_to = server.getAccount(request.getAccountTo());

        if(account_from == null || account_to == null) {
            code = NON_EXISTING_USER;
        }
        else {
            if(account_from.getMoney() < request.getAmount()) {
                code = AMOUNT_NOT_SUPORTED;
            }

            else {
                server.transferTo(account_from, account_to, request.getAmount());
            }
        }

        TransferToResponse response = TransferToResponse.newBuilder().setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            System.err.println("[DEBUG: transferTo Request completed]\n");
        }
    }
}