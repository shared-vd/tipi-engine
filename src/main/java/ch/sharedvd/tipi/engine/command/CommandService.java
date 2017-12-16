package ch.sharedvd.tipi.engine.command;

public interface CommandService {

	void sendCommand(Command c);
	
	void removeCommandOfClass(Class<? extends Command> clazz);
}
