package pt.tecnico.distledger.namingserver;

import java.util.List;
import java.util.ArrayList;

public class ServiceEntry {

    private List<ServerEntry> serviceEntriesList;
    private final String serviceName;

    // Constructor
    public ServiceEntry(String serviceName) {
        this.serviceName = serviceName;
        this.serviceEntriesList = new ArrayList<ServerEntry>();
    }

    // To get the serviceName associated
    public String getServiceName() {
        return serviceName;
    }

    // To get the list of serverEntries
    public List<ServerEntry> getServiceEntriesList() {
        return serviceEntriesList;
    }

    // To add a serverEntry to the service list
    public void addServerEntry(ServerEntry serverEntry) {
        this.serviceEntriesList.add(serverEntry);
    }
}