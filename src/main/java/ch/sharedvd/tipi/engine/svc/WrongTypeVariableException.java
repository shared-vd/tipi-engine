package ch.sharedvd.tipi.engine.svc;

public class WrongTypeVariableException extends RuntimeException {

    public WrongTypeVariableException(String key, Class exp, Class act) {
        super("Type for variable '"+key+"' is wrong. expected="+exp.getSimpleName()+" actual="+act.getSimpleName());
    }
}
