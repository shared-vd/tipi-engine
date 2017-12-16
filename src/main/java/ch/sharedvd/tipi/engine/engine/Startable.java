package ch.sharedvd.tipi.engine.engine;

public interface Startable {

	public void start() throws Exception;
	public void stop() throws Exception;
	public void destroy() throws Exception;

}
