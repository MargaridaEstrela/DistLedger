package pt.tecnico.distledger.userclient;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.distledger.userclient.grpc.UserService;
import pt.ulisboa.tecnico.distledger.contract.namingserver.*;

public class UserClientMain {

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
    public static void main(String[] args) {

        System.out.println(UserClientMain.class.getSimpleName());

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        // check arguments
        if (args.length != 0 && args.length != 1) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: mvn exec:java -Dexec.args=<-debug>");
            return;
        } 

        if(args.length > 0) {
            if(args[0].equals("-debug")) {
                debugFlag = true;
            }
        }

        final String host = "localhost";
        final int port = 5001;
        final String target = host + ":" + port;
		debug("Target: " + target);

		// Channel is the abstraction to connect to a service endpoint.
		// Let us use plaintext communication because we do not have certificates.
		final ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

		// It is up to the client to determine whether to block the call.
		// Here we create a blocking stub, but an async stub,
		// or an async stub with Future are always possible.
		NamingServerServiceGrpc.NamingServerServiceBlockingStub stub = NamingServerServiceGrpc.newBlockingStub(channel);

        CommandParser parser = new CommandParser(new UserService(stub));
        parser.parseInput();

        // Close the channel
        channel.shutdownNow();
    }
}
