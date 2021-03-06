package ch.sharedvd.tipi.engine.engine;

public enum TestingConnectionType {

    ESB("Enterprise Service Bus (ESB)"),
    MAINFRAME_DB("IBM Mainframe DB"),
    WS("Web service (WS)");

    private String description;

    private TestingConnectionType(String aDescription) {
        description = aDescription;
    }

    public String getDescription() {
        return description;
    }
}
