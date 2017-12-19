package ch.sharedvd.tipi.engine.model;

import ch.sharedvd.tipi.engine.AbstractTipiPersistenceTest;
import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.utils.InputStreamHolder;
import org.junit.Test;

import java.io.InputStream;
import java.time.LocalDate;

import static ch.sharedvd.tipi.engine.utils.Assert.fail;

public class ActivityVariableTest extends AbstractTipiPersistenceTest {

    @Test
    public void putGetVariables() throws Exception {

        final VariableMap vars = new VariableMap();
        vars.put("int", 42);
        vars.put("long", 24L);
        vars.put("regdate", LocalDate.of(2001, 2, 3));
        vars.put("str", "Une string");

        InputStream is = getClass().getResourceAsStream("inputStreamVariable.txt");
        vars.put("file", new InputStreamHolder(is));

        final long pid = tipiFacade.launch(ActivityVariableProcess.meta, vars);
        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(100);
        }
        fail("");
        //TipiActivityInfos infos = tipiFacade.getActivityInfos(pid);
        //assertEquals(ActivityState.FINISHED, infos.getState());
    }

}
