package ch.sharedvd.tipi.engine.command;

import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class CommandWrapper implements Comparable<CommandWrapper> {
	
	private long sendTime;
	private Command command;
	private int nbRetry = 0;
	
	public CommandWrapper(Command cmd) {
		Assert.notNull(cmd);
		this.command = cmd;
		sendTime = System.currentTimeMillis();
	}
	
	public long getElapsedTimeMillis() {
		return System.currentTimeMillis() - sendTime;
	}
	
	public Command getCommand() {
		return command;
	}

	public int getNbRetry() {
		return nbRetry;
	}
	public void incNbRetry() {
		this.nbRetry++;
	}

	@Override
	public String toString() {
		return "Command sent at "+ DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss").format(LocalDateTime.from(new Date(sendTime).toInstant()))+" : "+command;
	}

	@Override
	public int compareTo(CommandWrapper o) {
		return command.getPriority();
	}

}
