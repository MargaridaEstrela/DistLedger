package pt.tecnico.distledger.adminclient.grpc;

import io.grpc.StatusRuntimeException;
import pt.tecnico.distledger.adminclient.AdminClientMain;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.ResponseCode;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.ActivateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.ActivateResponse;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.DeactivateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.DeactivateResponse;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.getLedgerStateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.getLedgerStateResponse;

public class AdminService {

    /* TODO: The gRPC client-side logic should be here.
        This should include a method that builds a channel and stub,
        as well as individual methods for each remote operation of this service. */

    private AdminServiceGrpc.AdminServiceBlockingStub stub;
    private ResponseCode code;

    public AdminService(AdminServiceGrpc.AdminServiceBlockingStub stub) {
        this.stub = stub;
        this.code = ResponseCode.UNRECOGNIZED;
    }

    public ResponseCode getCode() {
        return code;
    }

    public void set_code(ResponseCode code) {
        this.code = code;
    }

    public ResponseCode activate(String server){
        try {
            ActivateRequest activateRequest = ActivateRequest.newBuilder().build();
            ActivateResponse activateResponse = this.stub.activate(activateRequest);

            ResponseCode code = activateResponse.getCode();

            if (code == ResponseCode.OK) {
                AdminClientMain.debug("Admin activated successfully");
            }
            return code;
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }

        return ResponseCode.UNRECOGNIZED;

    }

    public ResponseCode deactivate(String server){
        try {
            DeactivateRequest deactivateRequest = DeactivateRequest.newBuilder().build();
            DeactivateResponse deactivateResponse = this.stub.deactivate(deactivateRequest);

            ResponseCode code = deactivateResponse.getCode();

            if (code == ResponseCode.OK) {
                AdminClientMain.debug("Admin deactivated successfully");
            }
            return code;
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }

        return ResponseCode.UNRECOGNIZED;
    }

    public void dump(String server){
        try {
            getLedgerStateRequest request = getLedgerStateRequest.newBuilder().build();
            getLedgerStateResponse response = this.stub.getLedgerState(request);

            ResponseCode code = response.getCode();
            LedgerState ledgerState = response.getLedgerState();

            if (code == ResponseCode.OK) {
                AdminClientMain.debug("Dumped everything successfully");
                System.out.println("OK");
                System.out.println(ledgerState.toString());
            }    
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
                    e.getStatus().getDescription());
        }
    }

}
