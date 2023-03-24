package pt.tecnico.distledger.namingserver;

import java.io.IOException;
import java.util.Map;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class NamingServer {
    /**
	 * Set flag to true to print debug messages.
	 * The flag can be set using the -Ddebug command line option.
	 */    
    private static boolean debugFlag = (System.getProperty("debug") != null);

    // Association of a server name with the corresponding ServiceEntry
    private Map<String, ServiceEntry> servicesMap;

    // To print debug messages
    public static void debug(String debugMessage) {
        if (debugFlag) {
            System.err.println("DEBUG: " + debugMessage);
        }
    }

    // Constructor
    public NamingServer() {
        servicesMap = new java.util.HashMap<String, ServiceEntry>();
    }

    // Get the servicesMap associated
    public Map<String, ServiceEntry> getServicesMap() {
        return servicesMap;
    }

    // Set a new service entry associated with the specified service name
    public void addServerName(String serviceName, ServiceEntry serviceEntry) {
        servicesMap.put(serviceName, serviceEntry);
    }

    // Get the service entry associated with the specified service name
    public ServiceEntry getServiceEntry(String serviceName) {
        return servicesMap.get(serviceName);
    }

    // Remove the service entry associated with the specified service name
    public void removeServerName(String serviceName) {
        servicesMap.remove(serviceName);
    }
 
    public static void main(String[] args) throws IOException, InterruptedException {

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        final int port = 5001;
        NamingServer namingServer = new NamingServer();
        if(args[0].equals("-debug")) {
            debugFlag = true;
        }

        
        final BindableService namingServerServiceImpl = new NamingServerServiceImpl(namingServer);

        // Create a new server to listen on port
        ServerBuilder serverBuilder = ServerBuilder.forPort(port);
        serverBuilder.addService(namingServerServiceImpl);
        
        Server server = serverBuilder.build();
    
        try {
            // Start the server
            server.start();
        } catch (IOException e) {
            System.err.println(e.getLocalizedMessage());
            return;
        }
    
        // Server threads are running in the background.
        System.out.println("Server started");
    
        try {
            // Do not exit the main thread. Wait until server is terminated.
            server.awaitTermination();
        } catch (InterruptedException e) {
            System.err.println(e.getLocalizedMessage());
        }

    }

}
