package pt.tecnico.distledger.namingserver;

public class ServerEntry {

    // Private variables
    private String host;
    private String type;
    private String port;

    public ServerEntry(String host, String type, String port) {
        this.host = host;
        this.type = type;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getType() {
        return type;
    }

}
