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
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.DeleteRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.DeleteResponse;

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

    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        
        if (this.debugFlag) {
            namingServer.debug("Register service: started");
        }
        ServiceEntry serviceEntry;

        // Register a request to register
        RegisterResponse response;
        String serviceName = request.getService();
        String type = request.getType();
        String[] address = request.getAddress().split(":");

        // Check if service name is already registered
        if (!namingServer.getServicesMap().containsKey(serviceName)) {
            serviceEntry = new ServiceEntry(serviceName);
        }
        else {
            serviceEntry = namingServer.getServicesMap().get(serviceName);
        }
        ServerEntry serverEntry = new ServerEntry(address[0], type, address[1]);
        serviceEntry.addServerEntry(serverEntry);
        namingServer.addServerName(serviceName, serviceEntry);

        response = RegisterResponse.getDefaultInstance();
		responseObserver.onNext(response);
		responseObserver.onCompleted();

        if (this.debugFlag) {
            namingServer.debug("Register service: ended");
        }
    }

    @Override
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
                    if(response.getServersList().size() == 0) {
                        for (ServerEntry serverEntry : serviceEntry.getServiceEntriesList()) {
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

    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {

        System.out.println("hi\n");

        if (this.debugFlag) {
            namingServer.debug("Received delete request");
        }
        List<ServerEntry> toDelete = new ArrayList<ServerEntry>();
        DeleteResponse response;
        String serviceName = request.getService();
        String[] address = request.getAddress().split(":");

        // Check if service name is already registered
        if (namingServer.getServicesMap().containsKey(serviceName)) {
            for(ServerEntry serverEntry : namingServer.getServicesMap().get(serviceName).getServiceEntriesList()) {
                if(serverEntry.getHost().equals(address[0]) && serverEntry.getPort().equals(address[1])) {
                    toDelete.add(serverEntry);
                }
            }
        }

        toDelete.forEach(serverEntry -> namingServer.getServicesMap().get(serviceName).getServiceEntriesList().remove(serverEntry));
        if(namingServer.getServicesMap().get(serviceName).getServiceEntriesList().size() == 0) {
            namingServer.getServicesMap().remove(serviceName);
        }

        response = DeleteResponse.getDefaultInstance();
		responseObserver.onNext(response);
		responseObserver.onCompleted();

        if (this.debugFlag) {
            namingServer.debug("Ended delete request");
        }
    }

}
