package pt.tecnico.distledger.userclient.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.BalanceRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.BalanceResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.CreateAccountRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.CreateAccountResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.DeleteAccountRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.DeleteAccountResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.TransferToRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.TransferToResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.ResponseCode;

import io.grpc.StatusRuntimeException;

import java.util.ArrayList;
import java.util.List;

public class UserService {

    /*
     * TODO: The gRPC client-side logic should be here.
     * This should include a method that builds a channel and stub,
     * as well as individual methods for each remote operation of this service.
     */

    private UserServiceGrpc.UserServiceBlockingStub stub;

    public UserService(UserServiceGrpc.UserServiceBlockingStub stub) {
        this.stub = stub;
    }

    public void createAccount(String server, String username) {

        ResponseCode res_code;

        try {
            CreateAccountRequest createAccRequest = CreateAccountRequest.newBuilder().setUserId(username).build();
            CreateAccountResponse createAccResponse = this.stub.createAccount(createAccRequest);

            // get response code
            System.out.println(createAccResponse.getCode());
            

        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }

    }

    public void deleteAccount(String server, String username) {

        ResponseCode res_code;

        try {
            DeleteAccountRequest deleteAccRequest = DeleteAccountRequest.newBuilder().setUserId(username).build();
            DeleteAccountResponse deleteAccResponse = this.stub.deleteAccount(deleteAccRequest);

            // get response code
            System.out.println(deleteAccResponse.getCode());

        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }
    }

    public void balance(String server, String username) {

        int balance = -1;
        ResponseCode res_code = ResponseCode.UNRECOGNIZED;

        try {
            BalanceRequest balanceRequest = BalanceRequest.newBuilder().setUserId(username).build();
            BalanceResponse balanceResponse = this.stub.balance(balanceRequest);

            // get response code
            res_code = balanceResponse.getCode();
            System.out.println(res_code);

            if (res_code == ResponseCode.OK) {
                System.out.println(balanceResponse.getValue());
            }

        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
            e.getStatus().getDescription()); 
        }
    }

    public void transferTo(String server, String from, String dest, Integer amount) {
        
        ResponseCode res_code;

        try {
            TransferToRequest transferToRequest = TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(dest).setAmount(amount).build();
            TransferToResponse transferToResponse = this.stub.transferTo(transferToRequest);

            System.out.println(transferToResponse.getCode());

            // get response code

        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
            e.getStatus().getDescription()); 
        }
    }
}
