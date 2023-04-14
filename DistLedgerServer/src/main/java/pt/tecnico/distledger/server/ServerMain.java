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
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.getLedgerStateRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.*;
import pt.tecnico.distledger.server.domain.operation.*;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import io.grpc.StatusRuntimeException;
import java.io.IOException;
import java.util.*;

public class ServerMain {

	/**
	 * Set flag to true to print debug messages.
	 * The flag can be set using the -Ddebug command line option.
	 */    
    private static boolean debugFlag = (System.getProperty("debug") != null);
	private static boolean running;

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
			if (args[2].equals("-debug")) {
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

		int serverNumber = register(service,type,address,stub);
		if(!running) {
			System.exit(1);
		}

		ServerState serverState = new ServerState(serverNumber);
		List<pt.tecnico.distledger.server.domain.operation.Operation> ledger = new ArrayList<pt.tecnico.distledger.server.domain.operation.Operation>();
		if(serverNumber>2) {
			boolean success = false;
			List<String> addresses = lookup();
			for(String adr : addresses) {
				final ManagedChannel channel2 = ManagedChannelBuilder.forTarget(adr).usePlaintext().build();
				DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub2 = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel2);
				GetFullStateRequest request = GetFullStateRequest.newBuilder().build();
				try{
					GetFullStateResponse response = stub2.getFullState(request);
					response.getState().getLedgerList().forEach(op -> addOperation(ledger, op));
					serverState.gossip(ledger, new ArrayList<Integer>(response.getReplicaTSList()));
					channel2.shutdown();
					success = true;
				}
				catch(StatusRuntimeException e) {
					channel2.shutdown();
				}
			}
			if(!success) {
				System.exit(1);
			}
		}

		//DistLedger server services
		final BindableService userImpl = new UserServiceImpl(serverState, debugFlag);
		final BindableService adminImpl = new AdminServiceImpl(serverState, debugFlag, address);
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
	public static Integer register(String service, String type, String address, NamingServerServiceGrpc.NamingServerServiceBlockingStub stub) {

        try {
			//use the stub with the naming server to call the service register
            RegisterRequest registerRequest = RegisterRequest.newBuilder().setService(service).setType(type).setAddress(address).build();
            RegisterResponse registerResponse = stub.register(registerRequest);
			running = true;
			return registerResponse.getServerNumber();
        } catch (StatusRuntimeException e) {
			running = false;
            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
			return -1;
        }
    }

	//To delete an entry on the naming server
	public static void delete(String service, String address, NamingServerServiceGrpc.NamingServerServiceBlockingStub stub) {
		try {
			if(running) {
				//use the stub with the naming server to call the service delete
				DeleteRequest deleteRequest = DeleteRequest.newBuilder().setService(service).setAddress(address).build();
				DeleteResponse deleteResponse = stub.delete(deleteRequest);
			}

        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }
	}

	static private List<String> lookup() {

        List<String> res = new ArrayList<String>();

        //Definition of the service and server type to find
        String serviceName = "DistLedger";

        try {
            //naming server address
            final String host = "localhost";
            final int namingServerPort = 5001;
            final String target = host + ":" + namingServerPort;

            //open a stub with the naming server
            final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            NamingServerServiceGrpc.NamingServerServiceBlockingStub stub2 = NamingServerServiceGrpc.newBlockingStub(channel);

            //Request the information of the server
            LookupRequest lookupRequest = LookupRequest.newBuilder().setServiceName(serviceName).build();
            LookupResponse lookupResponse = stub2.lookup(lookupRequest);
            
            //Create list with all the answers
            for (String server : lookupResponse.getServersList()) {
                res.add(server);
            }
            channel.shutdown();

        } catch (StatusRuntimeException e) {
            // Debug message
            debug("Server " + serviceName + " is unreachable");
            
            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }
        return res;
    }
	static private void addOperation(List<pt.tecnico.distledger.server.domain.operation.Operation> ledger, pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Operation op) {
        pt.tecnico.distledger.server.domain.operation.Operation operation = null;
        //get the type of operation and call the constructor
        if(op.getType() == OperationType.OP_TRANSFER_TO) {
            operation = new TransferOp(op.getUserId(), op.getDestUserId(), (int) op.getAmount(), new ArrayList<Integer>(op.getPrevTSList()), new ArrayList<Integer>(op.getTSList()));
        }
         else if(op.getType() == OperationType.OP_DELETE_ACCOUNT) {
             operation = new DeleteOp(op.getUserId(), new ArrayList<Integer>(op.getPrevTSList()), new ArrayList<Integer>(op.getTSList()));
        }
        else if(op.getType() == OperationType.OP_CREATE_ACCOUNT) {
            operation = new CreateOp(op.getUserId(), new ArrayList<Integer>(op.getPrevTSList()), new ArrayList<Integer>(op.getTSList()));
        }
        //Add the new operation to a list (ledger)
        ledger.add(operation);
    }

}

