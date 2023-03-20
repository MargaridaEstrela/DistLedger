package pt.tecnico.distledger.namingserver;

import java.lang.WeakPairMap.Pair.Lookup;
import java.util.ArrayList;
import java.util.List;

import pt.tecnico.distledger.namingserver.NamingServer;
import pt.tecnico.distledger.namingserver.ServerEntry;
import pt.tecnico.distledger.namingserver.ServiceEntry;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.RegisterRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.RegisterResponse;

public class NamingServerServiceImpl {
    
    // Private variables
    private NamingServer namingServer;
    private boolean debugFlag;

    // Constructor
    public NamingServerServiceImpl(NamingServer namingServer, boolean debugFlag) {
        this.namingServer = namingServer;
        this.debugFlag = debugFlag;
    }

    //debug
    public void debug(String debugMessage) {
        if (this.debugFlag) {
            System.err.println("DEBUG: " + debugMessage);
        }
    } 

    public void register(String serviceName, String type, String port) {
        
        if (this.debugFlag) {
            debug("Register service: " + serviceName + ", " + type + ", " + port + "started");
        }
        
        RegisterResponse.Builder response = RegisterResponse.newBuilder();

        // Check if service name is already registered
        if (!namingServer.getServicesMap().containsKey(serviceName)) {
            ServerEntry serverEntry = new ServerEntry(serviceName, type, port);
            ServiceEntry serviceEntry = new ServiceEntry(serviceName);
            serviceEntry.addServerEntry(serverEntry);
            namingServer.addServerName(serviceName, serviceEntry);
            response.setStatus(RegisterResponse.Status.SUCCESS);
        } 
        else {
            System.err.println("ERROR: Not possible to register the server " + serviceName);
            response.setStatus(RegisterResponse.Status.FAILURE);
        }
    }

    public void lookup(String serviceName, String type) {
        if (this.debugFlag) {
            debug("Lookup service: " + serviceName + ", " + type + "started");
        }

        LookupResponse.Builder response = LookupResponse.newBuilder();

        List<String> result = new ArrayList<String>();

        // Given a service name and a type, return all the servers
        if (namingServer.getServicesMap().containsKey(serviceName)) {
            for (ServiceEntry serviceEntry : namingServer.getServicesMap().values()) {
                if (serviceEntry.getServiceName().equals(serviceName)) {
                    for (ServerEntry serverEntry : serviceEntry.getServiceEntriesList()) {
                        if (serverEntry.getType().equals(type)) {
                            result.add(serverEntry.getHost() + ":" + serverEntry.getPort());
                        }
                    }
                }
            }
        }

        response.addAllServers(result);
        response.setStatus(LookupResponse.Status.SUCCESS);
    }

}
