package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.adminclient.AdminClientMain;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.ResponseCode;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.ActivateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.ActivateResponse;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.DeactivateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.DeactivateResponse;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.getLedgerStateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.getLedgerStateResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.ArrayList;
import java.util.List;

public class AdminService {

    // Private variables
    private AdminServiceGrpc.AdminServiceBlockingStub stub;
    private ResponseCode code;

    // Constructor
    public AdminService(AdminServiceGrpc.AdminServiceBlockingStub stub) {
        this.stub = stub;
        this.code = ResponseCode.UNRECOGNIZED;
    }

    // Get the ResponseCode of a response.
    public ResponseCode getCode() {
        return code;
    }

    // Activate the AdminClient. Returns a ResponseCode.
    public ResponseCode activate(String server){
        try {
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(lookup("DistLedger", server).get(0)).usePlaintext().build();
            AdminServiceGrpc.AdminServiceBlockingStub stub = AdminServiceGrpc.newBlockingStub(channel);

            ActivateRequest activateRequest = ActivateRequest.newBuilder().build();
            ActivateResponse activateResponse = stub.activate(activateRequest);

            ResponseCode code = activateResponse.getCode();
            
            // Debug message
            if (code == ResponseCode.OK) {
                AdminClientMain.debug("Admin activated successfully");
            }
            return code;

        } catch (StatusRuntimeException e) {
            // Debug message
            AdminClientMain.debug("Server " + server + " is unreachable");

            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }

        // Return an error code
        return ResponseCode.UNRECOGNIZED;

    }

    // Deactivate the AdminClient. Returns a ResponseCode.
    public ResponseCode deactivate(String server){
        try {
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(lookup("DistLedger", server).get(0)).usePlaintext().build();
            AdminServiceGrpc.AdminServiceBlockingStub stub = AdminServiceGrpc.newBlockingStub(channel);

            DeactivateRequest deactivateRequest = DeactivateRequest.newBuilder().build();
            DeactivateResponse deactivateResponse = stub.deactivate(deactivateRequest);

            ResponseCode code = deactivateResponse.getCode();

            // Debug message
            if (code == ResponseCode.OK) {
                AdminClientMain.debug("Admin deactivated successfully");
            }

            return code;

        } catch (StatusRuntimeException e) {
            // Debug message
            AdminClientMain.debug("Server " + server + " is unreachable");

            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }
        
        // Return an error code
        return ResponseCode.UNRECOGNIZED;
    }

    public void dump(String server){
        try {
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(lookup("DistLedger", server).get(0)).usePlaintext().build();
            AdminServiceGrpc.AdminServiceBlockingStub stub = AdminServiceGrpc.newBlockingStub(channel);

            getLedgerStateRequest request = getLedgerStateRequest.newBuilder().build();
            getLedgerStateResponse response = stub.getLedgerState(request);

            ResponseCode code = response.getCode();
            LedgerState ledgerState = response.getLedgerState();

            // Debug message
            if (code == ResponseCode.OK) {
                AdminClientMain.debug("Dumped everything successfully");
                System.out.println("OK");
                System.out.println(ledgerState.toString());
            }    

        } catch (StatusRuntimeException e) {
            // Debug message
            AdminClientMain.debug("Server " + server + " is unreachable");

            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }
    }

    /*
     * To lookup all servers associated with a service. Returns the list of servers
     * for the type requested.
     * If none type had been requested, returns all the servers of the service. If
     * one of them doesn't exist, returns an empty list.
     */
    public List<String> lookup(String serviceName, String type) {

        List<String> res = new ArrayList<String>();

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
            AdminClientMain.debug("Server " + serviceName + " is unreachable");

            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }

        return res;

    }

}
