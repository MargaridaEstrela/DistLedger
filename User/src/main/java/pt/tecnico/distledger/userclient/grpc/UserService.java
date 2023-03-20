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
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import io.grpc.StatusRuntimeException;

import java.util.ArrayList;
import java.util.List;

public class UserService {

    // Private variables
    private UserServiceGrpc.UserServiceBlockingStub stub;
    private ResponseCode code;

    // Constructor
    public UserService(UserServiceGrpc.UserServiceBlockingStub stub) {
        this.stub = stub;
        code = ResponseCode.UNRECOGNIZED;
    }

    // Get the ResponseCode of a response.
    public ResponseCode get_code() {
        return code;
    }

    private String lookup(String service, String qualifier) {
        final String host = "localhost";
        final int namingServerPort = 5001;
        final String target = host + ":" + namingServerPort;
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        NamingServerServiceGrpc.NamingServerServiceBlockingStub stub2 = NamingServerServiceGrpc.newBlockingStub(channel);
        LookupRequest lookupRequest = LookupRequest.newBuilder().setType(qualifier).setService(service).build();
        LookupResponse lookupResponse = stub2.lookup(lookupRequest);
        return lookupResponse.getAddress();
    }

    // To create a new account. Returns a ResponseCode
    public ResponseCode createAccount(String server, String username) {

        try {
            CreateAccountRequest createAccRequest = CreateAccountRequest.newBuilder().setUserId(username).build();
            CreateAccountResponse createAccResponse = this.stub.createAccount(createAccRequest);

            ResponseCode code = createAccResponse.getCode();

            // Debug messages
            if (code == ResponseCode.OK) {
                UserClientMain.debug("User " + username + " created an account");
            } else if (code == ResponseCode.USER_ALREADY_EXISTS) {
                UserClientMain.debug("User " + username + " already exists");
            }

            return code;

        } catch (StatusRuntimeException e) {  
            // Debug message
            UserClientMain.debug("Server " + server + " is unreachable");
            
            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }

        // Return an error code
        return ResponseCode.UNRECOGNIZED;

    }

    // To delete the user account. Returns a ResponseCode
    public ResponseCode deleteAccount(String server, String username) {

        try {
            DeleteAccountRequest deleteAccRequest = DeleteAccountRequest.newBuilder().setUserId(username).build();
            DeleteAccountResponse deleteAccResponse = this.stub.deleteAccount(deleteAccRequest);

            ResponseCode code = deleteAccResponse.getCode();

            // Debug messages
            if (code == ResponseCode.OK) {
                UserClientMain.debug("User " + username + " deleted the account");
            } else if (code == ResponseCode.NON_EXISTING_USER) {
                UserClientMain.debug("User " + username + " doesn't exist");
            }

            return code;

        } catch (StatusRuntimeException e) {
            // Debug message
            UserClientMain.debug("Server " + server + " is unreachable");

            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }

        // Return an error code
        return ResponseCode.UNRECOGNIZED;
    }

    /**
     * To get the balance of a user. Returns a List with the respective number of the
     * ResponseCode and the balance of the user (in case ResponseCode != UNRECOGNIZED).
     */
    public List<Integer> balance(String server, String username) {

        List<Integer> res = new ArrayList<Integer>();

        try {
            BalanceRequest balanceRequest = BalanceRequest.newBuilder().setUserId(username).build();
            BalanceResponse balanceResponse = this.stub.balance(balanceRequest);

            ResponseCode code = balanceResponse.getCode();
            int balance = balanceResponse.getValue();

            // Add the ResponseCode and balance to the list
            res.add(code.getNumber());
            res.add(balance);

            // Debug message
            if (code == ResponseCode.NON_EXISTING_USER) {
                UserClientMain.debug("User " + username + "doesn't exist");
            }

            return res;

        } catch (StatusRuntimeException e) {
            // Debug message
            UserClientMain.debug("Server " + server + " is unreachable");

            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }

        // Add UNRECOGNIZED code value to list. Not needed to add balance because it is
        // not used in this case
        res.add(-1);

        return res;
    }

    // To transfer money from one account to another. Returns a ResponseCode
    public ResponseCode transferTo(String server, String from, String dest, Integer amount) {

        try {
            TransferToRequest transferToRequest = TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(dest)
                    .setAmount(amount).build();
            TransferToResponse transferToResponse = this.stub.transferTo(transferToRequest);

            ResponseCode code = transferToResponse.getCode();

            // Debug messages
            if (code == ResponseCode.OK) {
                UserClientMain.debug("User " + from + " transfered " + amount + " to user " + dest);
            } else if (code == ResponseCode.USER_ALREADY_EXISTS) {
                UserClientMain.debug("Something wrong with users");
            } else if (code == ResponseCode.AMOUNT_NOT_SUPORTED) {
                UserClientMain.debug("User " + from + " doesn't have enough amount");
            }

            // Return an error code
            return code;

        } catch (StatusRuntimeException e) {
            // Debug message
            UserClientMain.debug("Server " + server + " is unreachable");

            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }

        return ResponseCode.UNRECOGNIZED;
    }
}