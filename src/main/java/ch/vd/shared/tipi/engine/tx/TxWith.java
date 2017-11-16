package ch.vd.shared.tipi.engine.tx;

public interface TxWith<T> {

    T execute() throws Exception;

}
