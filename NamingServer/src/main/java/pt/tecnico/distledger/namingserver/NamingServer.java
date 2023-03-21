package pt.tecnico.distledger.namingserver;

import java.io.IOException;
import java.util.Map;
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

    public NamingServer() {
        servicesMap = new java.util.HashMap<String, ServiceEntry>();
    }

    public Map<String, ServiceEntry> getServicesMap() {
        return servicesMap;
    }

    public void addServerName(String serviceName, ServiceEntry serviceEntry) {
        servicesMap.put(serviceName, serviceEntry);
    }

    public ServiceEntry getServiceEntry(String serviceName) {
        return servicesMap.get(serviceName);
    }

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
        
        // Create a new server to listen on port
        ServerBuilder serverBuilder = ServerBuilder.forPort(port);
        
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
