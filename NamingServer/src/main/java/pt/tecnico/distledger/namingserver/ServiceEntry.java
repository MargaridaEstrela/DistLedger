package pt.tecnico.distledger.namingserver;

import java.util.List;
import java.util.ArrayList;

public class ServiceEntry {

    private List<ServerEntry> serviceEntriesList;
    private final String serviceName;

    public ServiceEntry(String serviceName) {
        this.serviceName = serviceName;
        this.serviceEntriesList = new ArrayList<ServerEntry>();
    }

    public String getServiceName() {
        return serviceName;
    }

    public List<ServerEntry> getServiceEntriesList() {
        return serviceEntriesList;
    }

    public void addServerEntry(ServerEntry serverEntry) {
        this.serviceEntriesList.add(serverEntry);
    }
}