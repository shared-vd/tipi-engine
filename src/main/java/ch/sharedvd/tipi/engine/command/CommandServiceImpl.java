package ch.sharedvd.tipi.engine.command;

import ch.sharedvd.tipi.engine.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class CommandServiceImpl implements CommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandServiceImpl.class);

    @Autowired
    private CommandConsumer consumer;

    private class CommandSynchronization extends TransactionSynchronizationAdapter {
        private final Command command;

        CommandSynchronization(Command c) {
            Assert.notNull(c, "Command can't be null");
            this.command = c;
        }

        @Override
        public void afterCompletion(int status) {
            if (TransactionSynchronization.STATUS_COMMITTED == status) {
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
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                final CommandSynchronization synchro = new CommandSynchronization(command);
                TransactionSynchronizationManager.registerSynchronization(synchro);
            } else {
                consumer.addCommand(command);
            }
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
