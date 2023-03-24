package pt.tecnico.distledger.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.UserServiceImpl;
import pt.tecnico.distledger.server.domain.AdminServiceImpl;
import pt.tecnico.distledger.server.domain.DistLedgerCrossServerServiceImpl;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import io.grpc.StatusRuntimeException;
import java.io.IOException;

public class ServerMain {

	/**
	 * Set flag to true to print debug messages.
	 * The flag can be set using the -Ddebug command line option.
	 */    
    private static boolean debugFlag = (System.getProperty("debug") != null);

	// To print debug messages
	public static void debug(String debugMessage) {
		if (debugFlag) {
			System.err.println("DEBUG: " + debugMessage);
		}
	}

    public static void main(String[] args) throws IOException, InterruptedException{

		boolean debugFlag = false;

        System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

        if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s port%n", ServerMain.class.getName());
			return;
		}

		//Check debug
		if(args.length >= 3) {
			if (args[2] == "-debug") {
				debugFlag = true;
			}
		}

		//information about this server (host, port, service, type)
        final int port = Integer.parseInt(args[0]);
		final String type = args[1];
		final String address = "localhost:" + port;
		final String service = "DistLedger";


		//Naming server address
		final String host = "localhost";
        final int namingServerPort = 5001;
        final String target = host + ":" + namingServerPort;

		//Create a stub for the naming server
		final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
		NamingServerServiceGrpc.NamingServerServiceBlockingStub stub = NamingServerServiceGrpc.newBlockingStub(channel);

		register(service,type,address,stub);

		ServerState serverState = new ServerState();

		//DistLedger server services
		final BindableService userImpl = new UserServiceImpl(serverState, debugFlag, type);
		final BindableService adminImpl = new AdminServiceImpl(serverState, debugFlag, type);
		final BindableService crossImpl = new DistLedgerCrossServerServiceImpl(serverState, debugFlag);

		//Add the services to the server
        Server server = ServerBuilder.forPort(port).addService(userImpl).addService(adminImpl).addService(crossImpl).build();

		//whenever the server shutdowns run:
		Runtime.getRuntime().addShutdownHook(new Thread(){
		@Override
		public void run()
		{
			try {
				//call the delete method that removes the server entry from the naming server
				delete(service,address,stub);
			} 
			catch (StatusRuntimeException e) {
				System.out.println("Error deleting server entry from Naming server: " + e.getLocalizedMessage());			
			}
			//shutdown the comunication channel with the naming server
			channel.shutdownNow();
		}
		});

		try {
			//start the server
			server.start();

			// Server threads are running in the background.
			System.out.println("Server started");

			// Do not exit the main thread. Wait until server is terminated.
			server.awaitTermination();
		}
		catch(Exception e) {
			e.printStackTrace();
			server.shutdownNow();
		}
    }

	//To register an entry on the naming server
	public static void register(String service, String type, String address, NamingServerServiceGrpc.NamingServerServiceBlockingStub stub) {

        try {
			//use the stub with the naming server to call the service register
            RegisterRequest registerRequest = RegisterRequest.newBuilder().setService(service).setType(type).setAddress(address).build();
            RegisterResponse registerResponse = stub.register(registerRequest);

        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }
    }

	//To delete an entry on the naming server
	public static void delete(String service, String address, NamingServerServiceGrpc.NamingServerServiceBlockingStub stub) {
		try {
			//use the stub with the naming server to call the service delete
            DeleteRequest deleteRequest = DeleteRequest.newBuilder().setService(service).setAddress(address).build();
            DeleteResponse deleteResponse = stub.delete(deleteRequest);

        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }
	}

}

