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
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.GossipRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.GossipResponse;
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
    private NamingServerServiceGrpc.NamingServerServiceBlockingStub stub;
    private ResponseCode code;

    // Constructor
    public AdminService(NamingServerServiceGrpc.NamingServerServiceBlockingStub stub) {
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

            channel.shutdownNow();

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

            channel.shutdownNow();

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

    // To get the content of the ledger
    public void dump(String server){
        try {
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(lookup("DistLedger", server).get(0)).usePlaintext().build();
            AdminServiceGrpc.AdminServiceBlockingStub stub = AdminServiceGrpc.newBlockingStub(channel);

            getLedgerStateRequest request = getLedgerStateRequest.newBuilder().build();
            getLedgerStateResponse response = stub.getLedgerState(request);

            channel.shutdownNow();

            ResponseCode code = response.getCode();

            // Debug message
            if (code == ResponseCode.OK) {
                AdminClientMain.debug("Dumped everything successfully");
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
            LookupRequest lookupRequest = LookupRequest.newBuilder().setServiceName(serviceName).setType(type).build();
            LookupResponse lookupResponse = stub.lookup(lookupRequest);

            
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

    public ResponseCode gossip(String server) {

        try {
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(lookup("DistLedger", server).get(0)).usePlaintext().build();
            AdminServiceGrpc.AdminServiceBlockingStub stub = AdminServiceGrpc.newBlockingStub(channel);

            GossipRequest gossipRequest = GossipRequest.newBuilder().build();
            GossipResponse gossipResponse = stub.gossip(gossipRequest);

            channel.shutdownNow();

            ResponseCode code = gossipResponse.getCode();

            // Debug message
            if (code == ResponseCode.OK) {
                AdminClientMain.debug("Dumped everything successfully");
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

}
