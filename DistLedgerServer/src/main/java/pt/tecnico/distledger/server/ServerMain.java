package pt.tecnico.distledger.server;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.domain.UserServiceImpl;
import pt.tecnico.distledger.server.domain.AdminServiceImpl;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.*;
import pt.ulisboa.tecnico.distledger.contract.namingserver.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import io.grpc.StatusRuntimeException;
import java.io.IOException;

public class ServerMain {

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

		if(args.length >= 3) {
			if (args[2] == "-debug") {
				debugFlag = true;
			}
		}

        final int port = Integer.parseInt(args[0]);
		final String type = args[1];
		final String address = "localhost:" + port;


		final String host = "localhost";
        final int namingServerPort = 5001;
        final String target = host + ":" + namingServerPort;
		final String service = "DistLedger";

		final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

		NamingServerServiceGrpc.NamingServerServiceBlockingStub stub = NamingServerServiceGrpc.newBlockingStub(channel);

		register(service,type,address,stub);

		channel.shutdownNow();


		ServerState serverState = new ServerState();

		final BindableService userImpl = new UserServiceImpl(serverState, debugFlag);
		final BindableService adminImpl = new AdminServiceImpl(serverState, debugFlag);

        Server server = ServerBuilder.forPort(port).addService(userImpl).addService(adminImpl).build();

        server.start();

		// Server threads are running in the background.
		System.out.println("Server started");

		// Do not exit the main thread. Wait until server is terminated.
		server.awaitTermination();
    }

	public static void register(String service, String type, String address, NamingServerServiceGrpc.NamingServerServiceBlockingStub stub) {

        try {
			
            RegisterRequest registerRequest = RegisterRequest.newBuilder().setService(service).setType(type).setAddress(address).build();
            RegisterResponse RegisterResponse = stub.register(registerRequest);

        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }
    }

}

