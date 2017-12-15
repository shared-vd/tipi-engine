package ch.sharedvd.tipi.engine.tx;

public interface TxWith<T> {

    T execute() throws Exception;

}
