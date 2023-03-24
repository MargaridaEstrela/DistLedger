package pt.tecnico.distledger.namingserver;

public class ServerEntry {

    // Private variables
    private String host;
    private String type;
    private String port;

    // Constructor
    public ServerEntry(String host, String type, String port) {
        this.host = host;
        this.type = type;
        this.port = port;
    }

    // To get the host
    public String getHost() {
        return host;
    }

    // To get the port
    public String getPort() {
        return port;
    }

    // To get the type
    public String getType() {
        return type;
    }

    // To string representation
    public String toString() {
        return "host: " + host + " type: " + type + " port: " + port;
    }
}
