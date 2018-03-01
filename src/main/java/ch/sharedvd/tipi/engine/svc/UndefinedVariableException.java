package ch.sharedvd.tipi.engine.svc;

public class UndefinedVariableException extends RuntimeException {

    public UndefinedVariableException(String key) {
        super("Unknwon variable: "+key);
    }
}
