package pt.tecnico.distledger.namingserver;

import java.util.ArrayList;
import java.util.List;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.RegisterRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.RegisterResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.DeleteRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.DeleteResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import static io.grpc.Status.ALREADY_EXISTS;

public class NamingServerServiceImpl extends NamingServerServiceGrpc.NamingServerServiceImplBase {

    // Private variables
    private NamingServer namingServer;

    // Constructor
    public NamingServerServiceImpl(NamingServer namingServer) {
        this.namingServer = namingServer;
    }

    // To register a request to the NamingCerver service with a given
    // RegisterRequest
    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {

        // Debug message
        NamingServer.debug("Register service: started");
        
        ServiceEntry serviceEntry;

        // Register a request to register
        RegisterResponse response;
        String serviceName = request.getService();
        String type = request.getType();
        String[] address = request.getAddress().split(":");
        synchronized(namingServer) {
            // Check if service name is already registered
            if (!namingServer.getServicesMap().containsKey(serviceName)) {
                serviceEntry = new ServiceEntry(serviceName);
            } else {
                serviceEntry = namingServer.getServicesMap().get(serviceName);
            }

            for (ServerEntry serverEntry : serviceEntry.getServiceEntriesList()) {
                if (serverEntry.getPort().equals(address[1]) && serverEntry.getHost().equals(address[0])) {
                    responseObserver.onError(ALREADY_EXISTS.withDescription("Server already exists").asRuntimeException());
                    return;
                }
            }

            // Create the service entry and register
            ServerEntry serverEntry = new ServerEntry(address[0], type, address[1]);
            serviceEntry.addServerEntry(serverEntry);
            namingServer.addServerName(serviceName, serviceEntry);
        }


        response = RegisterResponse.getDefaultInstance();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        // Debug message
        NamingServer.debug("Register service: ended");
    }

    // To lookup to a service name with a specified type. Returns null if no such
    // service, otherwise returns the list of services with the same type. If case of none 
    // corresponding type, returns all the services
    @Override
    public void lookup(LookupRequest request, StreamObserver<LookupResponse> responseObserver) {
        // Debug message
        NamingServer.debug("Received lookup request");

        String serviceName = request.getServiceName();
        String type = request.getType();

        LookupResponse.Builder response = LookupResponse.newBuilder();
        String server;

        synchronized(namingServer) {
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
                        // If no servers match the type, return all the servers
                        if (response.getServersList().size() == 0) {
                            for (ServerEntry serverEntry : serviceEntry.getServiceEntriesList()) {
                                server = serverEntry.getHost() + ":" + serverEntry.getPort();
                                response.addServers(server);
                            }
                        }
                    }
                }
            }
        }

        responseObserver.onNext(response.build());
        responseObserver.onCompleted();

        // Debug message
        NamingServer.debug("Ended lookup request");

    }

    // To delete a server from the naming server list
    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver) {

        // Debug message
        NamingServer.debug("Received delete request");

        List<ServerEntry> toDelete = new ArrayList<ServerEntry>();
        DeleteResponse response;
        String serviceName = request.getService();
        String[] address = request.getAddress().split(":");

        synchronized(namingServer) {
            // Colect server to be deleted into a list
            if (namingServer.getServicesMap().containsKey(serviceName)) {
                for (ServerEntry serverEntry : namingServer.getServicesMap().get(serviceName).getServiceEntriesList()) {
                    if (serverEntry.getHost().equals(address[0]) && serverEntry.getPort().equals(address[1])) {
                        toDelete.add(serverEntry);
                    }
                }
            }

            // Remove servers in the list
            toDelete.forEach(serverEntry -> namingServer.getServicesMap().get(serviceName).getServiceEntriesList()
                    .remove(serverEntry));
            if (namingServer.getServicesMap().get(serviceName).getServiceEntriesList().size() == 0) {
                namingServer.getServicesMap().remove(serviceName);
            }
        }

        response = DeleteResponse.getDefaultInstance();
        responseObserver.onNext(response);
        responseObserver.onCompleted();

        // Debug message
        NamingServer.debug("Ended delete request");
    }
}
