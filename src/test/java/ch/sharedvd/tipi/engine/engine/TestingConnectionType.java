package ch.vd.registre.tipi.engine;

import org.junit.Ignore;

@Ignore
public enum TestingConnectionType {

    DB_ORACLE("Base de données principale (Oracle)"),
    ESB("Bus d'entreprise (ESB)"),
    DB_HOST("Base de données Host (DB2)"),
    DB_ORACLE_PROD("Base de données de production (Oracle)"),
    UPI_WS("Web service de l'UPI");

    private String description;

    private TestingConnectionType(String aDescription) {
        description = aDescription;
    }

    public String getDescription() {
        return description;
    }
}
