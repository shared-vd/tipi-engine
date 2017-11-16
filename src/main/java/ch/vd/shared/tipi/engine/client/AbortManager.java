package ch.vd.shared.tipi.engine.client;


import org.aspectj.bridge.AbortException;

public interface AbortManager {

	/**
	 * @throws AbortException si un abort est effectué. NE PAS CATCHER pour que cette exception soit traitée
	 * proprement par le TiPi engine.
	 */
	void testAbort() throws AbortException;

}
