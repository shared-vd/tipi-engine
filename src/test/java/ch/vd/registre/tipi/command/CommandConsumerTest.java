package ch.vd.registre.tipi.command;

import ch.sharedvd.tipi.engine.command.Command;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import org.junit.Test;

public class CommandConsumerTest extends TipiEngineTest {

    public static class ExceptionCommand extends Command {
        public int count = 0;

        @Override
        public void execute() {
            if (count == 0) {
                count++;
                throw new RuntimeException("Une exception de test");
            }
            count++;
        }
    }

    @Test
    public void onExceptionRetry() throws Exception {
        doWithLog4jBlocking("ch.vd.registre.tipi", () -> {
            ExceptionCommand cmd = new ExceptionCommand();
            commandConsumer.addCommand(cmd);
            while (cmd.count < 2) {
                Thread.sleep(200);
            }
            assertEquals(2, cmd.count);
            return null;
        });
    }

}
