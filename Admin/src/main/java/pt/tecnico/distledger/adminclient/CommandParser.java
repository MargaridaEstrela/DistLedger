package pt.tecnico.distledger.adminclient;

import pt.tecnico.distledger.adminclient.grpc.AdminService;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.ResponseCode;

import java.util.Scanner;

public class CommandParser {

    // Private variables
    private static final String SPACE = " ";
    private static final String ACTIVATE = "activate";
    private static final String DEACTIVATE = "deactivate";
    private static final String GET_LEDGER_STATE = "getLedgerState";
    private static final String GOSSIP = "gossip";
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private final AdminService adminService;

    // Constructor
    public CommandParser(AdminService adminService) {
        this.adminService = adminService;
    }

    // To parse the command line
    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String cmd = line.split(SPACE)[0];

            switch (cmd) {
                case ACTIVATE:
                    this.activate(line);
                    break;

                case DEACTIVATE:
                    this.deactivate(line);
                    break;

                case GET_LEDGER_STATE:
                    this.dump(line);
                    break;

                case GOSSIP:
                    this.gossip(line);
                    break;

                case HELP:
                    this.printUsage();
                    break;

                case EXIT:
                    exit = true;
                    break;

                default:
                    break;
            }

        }
        scanner.close();
    }

    // To print the activate command usage
    private void activate(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1];

        ResponseCode code = this.adminService.activate(server);
        System.out.println(formatToString(code));
    }

    // To print the deactivate command usage
    private void deactivate(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1];

        ResponseCode code = this.adminService.deactivate(server);
        System.out.println(formatToString(code));
    }

    // To print the getLedgerState command usage
    private void dump(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1];
        this.adminService.dump(server);

        // Print being done in the AdminService
    }

    // @SuppressWarnings("unused")
    private void gossip(String line){
        String[] split = line.split(SPACE);

        if (split.length != 2){
            this.printUsage();
            return;
        }
        String server = split[1];
        ResponseCode code = this.adminService.gossip(server);
        System.out.println(formatToString(code));
    }

    // To convert the ResponseCode to a string
    public static String formatToString(ResponseCode code) {
        switch(code) {
            case OK : return "OK";
            case UNRECOGNIZED : return "UNRECOGNIZED ERROR";
        }

        return "UNAVAILABLE SERVER";
    }


    // To print the help command
    private void printUsage() {
        System.out.println("Usage:\n" +
                "- activate <server>\n" +
                "- deactivate <server>\n" +
                "- getLedgerState <server>\n" +
                "- gossip <server>\n" +
                "- exit\n");
    }

}
