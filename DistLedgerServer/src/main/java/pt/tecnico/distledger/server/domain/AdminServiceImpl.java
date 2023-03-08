package pt.tecnico.distledger.server.domain;

import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc.AdminServiceImplBase;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.*;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.*;

import java.util.ArrayList;
import java.util.List;

public class AdminServiceImpl extends AdminServiceImplBase {

    private ServerState server;

    public AdminServiceImpl(ServerState server) {
        this.server = server;
    }

    @Override
    public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {

        server.activate();

        ActivateResponse response = ActivateResponse.newBuilder().build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();
    }

    @Override
    public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {

        server.deactivate();

        DeactivateResponse response = DeactivateResponse.newBuilder().build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();
    }

    @Override
    public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {

        //TODO 3ª fase

        GossipResponse response = GossipResponse.newBuilder().build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();
    }

    @Override
    public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {

        

        getLedgerStateResponse response = getLedgerStateResponse.newBuilder().build();

        responseObserver.onNext(response);
            
        responseObserver.onCompleted();
    }
}