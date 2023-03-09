package pt.tecnico.distledger.userclient;

import pt.tecnico.distledger.userclient.grpc.UserService;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.ResponseCode;

import java.util.List;
import java.util.Scanner;

public class CommandParser {

    private static final String SPACE = " ";
    private static final String CREATE_ACCOUNT = "createAccount";
    private static final String DELETE_ACCOUNT = "deleteAccount";
    private static final String TRANSFER_TO = "transferTo";
    private static final String BALANCE = "balance";
    private static final String HELP = "help";
    private static final String EXIT = "exit";

    private final UserService userService;


    public CommandParser(UserService userService) {
        this.userService = userService;
    }

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
    }

    private void createAccount(String line){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }

        String server = split[1];
        String username = split[2];

        ResponseCode res_code = userService.createAccount(server, username);

        System.out.println(formatToString(res_code));
    }

    private void deleteAccount(String line){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String server = split[1];
        String username = split[2];

        ResponseCode res_code = userService.deleteAccount(server, username);

        System.out.println(formatToString(res_code));
    }


    private void balance(String line){
        String[] split = line.split(SPACE);

        if (split.length != 3){
            this.printUsage();
            return;
        }
        String server = split[1];
        String username = split[2];

        List<Integer> res = userService.balance(server, username);

        System.out.println(formatToCode(res.get(0)));

        int balance = res.get(1);
        if (balance > 0 ) {
            System.out.println(res.get(1));
        }
    }

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

        ResponseCode res_code = userService.transferTo(server, from, dest, amount);

        System.out.println(formatToString(res_code));
    }

    public static String formatToString(ResponseCode code) {
        switch(code) {
            case OK : return "OK";
            case NON_EXISTING_USER : return "NON EXISTINGUSER";
            case USER_ALREADY_EXISTS : return "USER ALREADY EXISTS";
            case AMOUNT_NOT_SUPORTED : return "AMOUNT NOT SUPPORTED";
        }

        return "UNKNOWN ERROR";
    }

    public static ResponseCode formatToCode(int code) {
        switch(code) {
            case 0 : return ResponseCode.OK;
            case 1 : return ResponseCode.NON_EXISTING_USER;
            case 2 : return ResponseCode.USER_ALREADY_EXISTS;
            case 3 : return ResponseCode.AMOUNT_NOT_SUPORTED;
        }

        return ResponseCode.UNRECOGNIZED;
    } 

    private void printUsage() {
        System.out.println("Usage:\n" +
                        "- createAccount <server> <username>\n" +
                        "- deleteAccount <server> <username>\n" +
                        "- balance <server> <username>\n" +
                        "- transferTo <server> <username_from> <username_to> <amount>\n" +
                        "- exit\n");
    }
}
