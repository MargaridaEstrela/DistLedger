package pt.tecnico.distledger.server.domain;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc.AdminServiceImplBase;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;
import static pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.ResponseCode.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.*;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateResponse;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.*;
import pt.tecnico.distledger.server.domain.operation.*;
import io.grpc.StatusRuntimeException;

import java.util.List;
import java.util.ArrayList;

public class AdminServiceImpl extends AdminServiceImplBase {

    //Private variables
    private ServerState server;
    private boolean debugFlag;
    private String address;

    //Constructor
    public AdminServiceImpl(ServerState server, boolean debugFlag, String address) {
        this.server = server;
        this.debugFlag = debugFlag;
        this.address = address;
    }

    //debug
    public void debug(String debugMessage) {
        if (debugFlag) {
            System.err.println("DEBUG: " + debugMessage);
        }
    }

    @Override
    public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {

        if(debugFlag) {
            debug("activate Request started\n");
        }

        ResponseCode code = OK;

        //call the activate function
        server.activate();

        ActivateResponse response = ActivateResponse.newBuilder().setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            debug("activate Request completed\n");
        }
    }

    @Override
    public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {

        if(debugFlag) {
            debug("deactivate Request started\n");
        }

        ResponseCode code = OK;

        //call the deactivate function
        server.deactivate();

        DeactivateResponse response = DeactivateResponse.newBuilder().setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            debug("deactivate Request completed\n");
        }
    }

    @Override
    public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {

        if(debugFlag) {
            debug("gossip Request started\n");
        }

        ResponseCode code = OK;

        //TODO Sync here
        List<pt.tecnico.distledger.server.domain.operation.Operation> operations = server.getOperationsToGossip();
        List<Integer> replicaTS = server.getUnstables().getReplicaTS();
        List<String> servers = this.lookup();
        LedgerState.Builder ledger = LedgerState.newBuilder();

        for (pt.tecnico.distledger.server.domain.operation.Operation operation : operations) {
            pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Operation.Builder operationContract = DistLedgerCommonDefinitions.Operation.newBuilder();

            //Check the type of Operation
            if(operation.getType() == "CREATE") {
                operationContract.setType(OperationType.OP_CREATE_ACCOUNT);
            }
            else if(operation.getType() == "DELETE") {
                operationContract.setType(OperationType.OP_DELETE_ACCOUNT);
            }
            else if(operation.getType() == "TRANSFER") {
                operationContract.setType(OperationType.OP_TRANSFER_TO);
                TransferOp transferoperation = (TransferOp) operation;
                operationContract.setDestUserId(transferoperation.getDestAccount());
                operationContract.setAmount(transferoperation.getAmount());
            }
            else {
                operationContract.setType(OperationType.OP_UNSPECIFIED);
            }
            operationContract.setUserId(operation.getAccount());
            operationContract.addAllTS(operation.getTS());
            operationContract.addAllPrevTS(operation.getPrevTS());
            ledger.addLedger(operationContract.build());
        }

        PropagateStateRequest newRequest = PropagateStateRequest.newBuilder().setState(ledger).addAllReplicaTS(replicaTS).build();
        for (String address : servers) {
            if(!address.equals(this.address)) {
                final ManagedChannel channel = ManagedChannelBuilder.forTarget(address).usePlaintext().build();
                DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stub = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel);
                stub.propagateState(newRequest);
                channel.shutdown();
            }
        }
        GossipResponse response = GossipResponse.newBuilder().setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            debug("gossip Request completed\n");
        }
    }

    @Override
    public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {

        if(debugFlag) {
            debug("getLedgerState Request started\n");
        }

        ResponseCode code = OK;

        //ArrayList of operations from the server ledger
        List<pt.tecnico.distledger.server.domain.operation.Operation> operations = server.getLedger();

        LedgerState.Builder ledger = LedgerState.newBuilder();

        for (pt.tecnico.distledger.server.domain.operation.Operation operation : operations) {
            pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.Operation.Builder operationContract = DistLedgerCommonDefinitions.Operation.newBuilder();
            //Check the type of Operation
            if(operation.getType() == "CREATE") {
                operationContract.setType(OperationType.OP_CREATE_ACCOUNT);
            }
            else if(operation.getType() == "DELETE") {
                operationContract.setType(OperationType.OP_DELETE_ACCOUNT);
            }
            else if(operation.getType() == "TRANSFER") {
                operationContract.setType(OperationType.OP_TRANSFER_TO);
                TransferOp transferoperation = (TransferOp) operation;
                operationContract.setDestUserId(transferoperation.getDestAccount());
                operationContract.setAmount(transferoperation.getAmount());
            }
            else {
                operationContract.setType(OperationType.OP_UNSPECIFIED);
            }
            operationContract.setUserId(operation.getAccount());
            ledger.addLedger(operationContract.build());
        }
        
        getLedgerStateResponse response = getLedgerStateResponse.newBuilder().setLedgerState(ledger.build()).setCode(code).build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();

        if(debugFlag) {
            debug("getLedgerState Request completed\n");
        }
    }

    private List<String> lookup() {

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
}