package ch.sharedvd.tipi.engine.engine;


public interface TipiStarter {
	
	void start() throws Exception;
	void stop() throws Exception;
	boolean isStarted() throws Exception;

}
