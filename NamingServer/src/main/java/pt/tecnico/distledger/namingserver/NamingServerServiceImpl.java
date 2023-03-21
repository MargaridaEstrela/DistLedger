package pt.tecnico.distledger.namingserver;

import java.util.ArrayList;
import java.util.List;

import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.namingserver.NamingServer;
import pt.tecnico.distledger.namingserver.ServerEntry;
import pt.tecnico.distledger.namingserver.ServiceEntry;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.RegisterRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.RegisterResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupResponse;

import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc.NamingServerServiceImplBase;

public class NamingServerServiceImpl extends NamingServerServiceGrpc.NamingServerServiceImplBase {
    
    // Private variables
    private NamingServer namingServer;
    private boolean debugFlag;

    // Constructor
    public NamingServerServiceImpl(NamingServer namingServer) {
        this.namingServer = namingServer;
    }

    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        
        if (this.debugFlag) {
            namingServer.debug("Register service: started");
        }

        // Register a request to register
        RegisterResponse response;
        String serviceName = request.getService();
        String type = request.getType();
        String address = request.getAddress();

        // Check if service name is already registered
        if (!namingServer.getServicesMap().containsKey(serviceName)) {
            ServerEntry serverEntry = new ServerEntry(serviceName, type, address);
            ServiceEntry serviceEntry = new ServiceEntry(serviceName);
            serviceEntry.addServerEntry(serverEntry);
            namingServer.addServerName(serviceName, serviceEntry);
        } 

        response = RegisterResponse.getDefaultInstance();
		responseObserver.onNext(response);
		responseObserver.onCompleted();

        if (this.debugFlag) {
            namingServer.debug("Register service: ended");
        }
    }

    public void lookup(LookupRequest request, StreamObserver<LookupResponse> responseObserver) {
        if (this.debugFlag) {
            namingServer.debug("Received lookup request");
        }

        String serviceName = request.getServiceName();
        String type = request.getType();

        LookupResponse.Builder response = LookupResponse.newBuilder();
        String server;

        // Given a service name and a type, return all the servers
        if (namingServer.getServicesMap().containsKey(serviceName)) {
            for (ServiceEntry serviceEntry : namingServer.getServicesMap().values()) {
                if (serviceEntry.getServiceName().equals(serviceName)) {
                    for (ServerEntry serverEntry : serviceEntry.getServiceEntriesList()) {
                        if (serverEntry.getType().equals(type)) {
                            server = serverEntry.getHost() + ":" + serverEntry.getPort();
                            response.addServers(server);
                        }
                    }
                }
            }
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();

        if (this.debugFlag) {
            namingServer.debug("Ended lookup request");
        }

    }

}
