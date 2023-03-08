package pt.tecnico.distledger.userclient.grpc;

import pt.tecnico.distledger.userclient.UserClientMain;
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
    private ResponseCode code;

    public UserService(UserServiceGrpc.UserServiceBlockingStub stub) {
        this.stub = stub;
        code = ResponseCode.UNRECOGNIZED;
    }

    public ResponseCode get_code() { return code; }

    public void set_code(ResponseCode code) { this.code = code; }
    
    public ResponseCode createAccount(String server, String username) {

        try {
            CreateAccountRequest createAccRequest = CreateAccountRequest.newBuilder().setUserId(username).build();
            CreateAccountResponse createAccResponse = this.stub.createAccount(createAccRequest);

            ResponseCode code = createAccResponse.getCode();

            if (code == ResponseCode.OK) {
               UserClientMain.debug("User " + username + " created an account"); 
            } else if (code == ResponseCode.USER_ALREADY_EXISTS) {
                UserClientMain.debug("User " + username + " already exists"); 
            }
            return code;
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }

        return ResponseCode.UNRECOGNIZED;

    }

    public ResponseCode deleteAccount(String server, String username) {

        try {
            DeleteAccountRequest deleteAccRequest = DeleteAccountRequest.newBuilder().setUserId(username).build();
            DeleteAccountResponse deleteAccResponse = this.stub.deleteAccount(deleteAccRequest);

             ResponseCode code = deleteAccResponse.getCode();

            if (code == ResponseCode.OK) {
               UserClientMain.debug("User " + username + " deleted the account"); 
            } else if (code == ResponseCode.NON_EXISTING_USER) {
                UserClientMain.debug("User " + username + " doesn't exist"); 
            }
            return code;
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }

        return ResponseCode.UNRECOGNIZED;
    }

    public List<Integer> balance(String server, String username) {

        int balance = -1;
        List<Integer> res = new ArrayList<Integer>();

        try {
            BalanceRequest balanceRequest = BalanceRequest.newBuilder().setUserId(username).build();
            BalanceResponse balanceResponse = this.stub.balance(balanceRequest);

            ResponseCode code = balanceResponse.getCode();
            balance = balanceResponse.getValue();

            res.add(code.getNumber());
            res.add(balance);

            if (code == ResponseCode.NON_EXISTING_USER) {
                 UserClientMain.debug("User " + username + "doesn't exist"); 
            }

             return res;

        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
            e.getStatus().getDescription()); 
        }

        res.add(ResponseCode.UNRECOGNIZED.getNumber());
        res.add(balance);

        return res;
    }

    public ResponseCode transferTo(String server, String from, String dest, Integer amount) {
        
        try {
            TransferToRequest transferToRequest = TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(dest).setAmount(0).build();
            TransferToResponse transferToResponse = this.stub.transferTo(transferToRequest);

            ResponseCode code = transferToResponse.getCode();

            if (code == ResponseCode.OK) {
               UserClientMain.debug("User " + from + " transfered " + amount + " to user " + dest); 
            } else if (code == ResponseCode.NON_EXISTING_USER) {
                UserClientMain.debug("Something wrong with users"); 
            } else if (code == ResponseCode.AMOUNT_NOT_SUPORTED) {
                UserClientMain.debug("User " + from + " doesn't have enought money"); 
            }
            return code;
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
            e.getStatus().getDescription()); 
        }

        return ResponseCode.UNRECOGNIZED;
    }
}
