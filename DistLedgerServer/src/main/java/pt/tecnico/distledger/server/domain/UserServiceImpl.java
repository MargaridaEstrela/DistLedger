package pt.tecnico.distledger.server.domain;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc.UserServiceImplBase;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.*;
import pt.tecnico.distledger.server.domain.account.Account;

public class UserServiceImpl extends UserServiceImplBase {

    private ServerState server = new ServerState();

    @Override
    public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {

        BalanceResponse response = BalanceResponse.newBuilder().setValue(server.getAccount(request.getUserId()).getMoney()).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();
    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {

        Account account = new Account(request.getUserId(),0);
        //TODO Mover account create to server state
        server.addAccount(account);

        CreateAccountResponse response = CreateAccountResponse.newBuilder().build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();
    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {

        server.removeAccount(request.getUserId());

        DeleteAccountResponse response = DeleteAccountResponse.newBuilder().build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();
    }

    @Override
    public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {

        Account account_from = server.getAccount(request.getAccountFrom());
        Account account_to = server.getAccount(request.getAccountTo());

        server.transferTo(account_from, account_to, request.getAmount());

        TransferToResponse response = TransferToResponse.newBuilder().build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();
    }
}