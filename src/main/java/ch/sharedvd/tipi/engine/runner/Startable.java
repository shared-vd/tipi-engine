package ch.sharedvd.tipi.engine.runner;

public interface Startable {

	public void start() throws Exception;
	public void stop() throws Exception;
	public void destroy() throws Exception;

}
