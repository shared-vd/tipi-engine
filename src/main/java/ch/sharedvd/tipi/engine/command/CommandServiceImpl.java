package ch.sharedvd.tipi.engine.command;

import ch.sharedvd.tipi.engine.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.transaction.Status;

public class CommandServiceImpl implements CommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandServiceImpl.class);

    @Autowired
    private CommandConsumer consumer;

    private class CommandSynchronization implements TransactionSynchronization {
        private Command command;

        CommandSynchronization(Command c) {
            command = c;
        }

        @Override
        public void suspend() {
        }

        @Override
        public void resume() {
        }

        @Override
        public void flush() {
        }

        @Override
        public void beforeCommit(boolean readOnly) {
        }

        @Override
        public void beforeCompletion() {
        }

        @Override
        public void afterCommit() {
        }

        @Override
        public void afterCompletion(int status) {
            if (Status.STATUS_COMMITTED == 0) {
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
            CommandSynchronization synchro = new CommandSynchronization(command);
            TransactionSynchronizationManager.registerSynchronization(synchro);
            //consumer.addCommand(command);
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
