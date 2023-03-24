package pt.tecnico.distledger.userclient;

import pt.tecnico.distledger.userclient.grpc.UserService;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.ResponseCode;

import java.util.List;
import java.util.Scanner;

public class CommandParser {

    // private variables
    private static final String SPACE = " ";
    private static final String CREATE_ACCOUNT = "createAccount";
    private static final String DELETE_ACCOUNT = "deleteAccount";
    private static final String TRANSFER_TO = "transferTo";
    private static final String BALANCE = "balance";
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private final UserService userService;

    // Constructor
    public CommandParser(UserService userService) {
        this.userService = userService;
    }

    // To parse the command line
    void parseInput() {

        Scanner scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();
            String cmd = line.split(SPACE)[0];

            try{
                switch (cmd) {
                    case CREATE_ACCOUNT:
                        this.createAccount(line);
                        break;

                    case DELETE_ACCOUNT:
                        this.deleteAccount(line);
                        break;

                    case TRANSFER_TO:
                        this.transferTo(line);
                        break;

                    case BALANCE:
                        this.balance(line);
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
            catch (Exception e){
                System.err.println(e.getMessage());
            }
        }
        scanner.close();
    }

    // To print the create account command usage
    private void createAccount(String line){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }

        String server = split[1];
        String username = split[2];

        ResponseCode code = this.userService.createAccount(server, username);

        System.out.println(formatToString(code));
    }

    // To print the delete account command usage
    private void deleteAccount(String line){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String server = split[1];
        String username = split[2];

        ResponseCode code = this.userService.deleteAccount(server, username);

        System.out.println(formatToString(code));
    }

    // To print the get account balance command usage
    private void balance(String line){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String server = split[1];
        String username = split[2];

        List<Integer> res = this.userService.balance(server, username);

        // First element is the integer code of a ResponseCode object
        ResponseCode code = formatToCode(res.get(0));

        System.out.println(formatToString(code));

        // Only print the second element (balance) in case of ResponseCode == OK
        if (code == ResponseCode.OK){
            if(res.get(1) > 0){
                System.out.println(res.get(1));
            }
        }
    }

    // To print the transfer to command usage
    private void transferTo(String line){
        String[] split = line.split(SPACE);

        if (split.length != 5){
            this.printUsage();
            return;
        }
        
        String server = split[1];
        String from = split[2];
        String dest = split[3];
        Integer amount = Integer.valueOf(split[4]);

        ResponseCode code = this.userService.transferTo(server, from, dest, amount);

        System.out.println(formatToString(code));
    }

    // To convert the ResponseCode objetc to a String
    public static String formatToString(ResponseCode code) {
        switch(code) {
            case OK : return "OK";
            case NON_EXISTING_USER : return "NON EXISTING USER";
            case USER_ALREADY_EXISTS : return "USER ALREADY EXISTS";
            case AMOUNT_NOT_SUPORTED : return "AMOUNT NOT SUPPORTED";
            case UNRECOGNIZED : return "UNRECOGNIZED ERROR";
        }

        return "UNAVAILABLE SERVER";
    }

    // To convert an integer to a ResponseCode object
    public static ResponseCode formatToCode(int code) {
        switch(code) {
            case 0 : return ResponseCode.OK;
            case 1 : return ResponseCode.NON_EXISTING_USER;
            case 2 : return ResponseCode.USER_ALREADY_EXISTS;
            case 3 : return ResponseCode.AMOUNT_NOT_SUPORTED;
        }

        return ResponseCode.UNRECOGNIZED;
    } 

    // To print the help command usage
    private void printUsage() {
        System.out.println("Usage:\n" +
                        "- createAccount <server> <username>\n" +
                        "- deleteAccount <server> <username>\n" +
                        "- balance <server> <username>\n" +
                        "- transferTo <server> <username_from> <username_to> <amount>\n" +
                        "- exit\n");
    }
}
