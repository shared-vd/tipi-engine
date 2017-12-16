package ch.sharedvd.tipi.engine.command;

import ch.sharedvd.tipi.engine.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.transaction.*;

public class CommandServiceImpl implements CommandService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommandServiceImpl.class);

	@Autowired
	private CommandConsumer consumer;

	@Autowired
	@Qualifier("tipiTransactionManager")
	private TransactionManager txManager;

	private class CommandSynchronization implements Synchronization {

		private Command command;

		CommandSynchronization(Command c) {
			command = c;
		}

		@Override
		public void beforeCompletion() {
		}
		@Override
		public void afterCompletion(int aStatus) {
			if (Status.STATUS_COMMITTED == aStatus) {
				consumer.addCommand(command);
			}
		}
	}

	@Override
	public void removeCommandOfClass(Class<? extends Command> clazz) {
		consumer.removeCommandOfClass(clazz);
	}

	@Override
	public void sendCommand(Command command) {
		Assert.notNull(command);
		try {
			Transaction tx = txManager.getTransaction();
			if (tx != null) {
				CommandSynchronization synchro = new CommandSynchronization(command);
				tx.registerSynchronization(synchro);
			}
			else {
				consumer.addCommand(command);
			}
		}
		catch (RollbackException e) {
		}
		catch (RuntimeException e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
		catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

}
