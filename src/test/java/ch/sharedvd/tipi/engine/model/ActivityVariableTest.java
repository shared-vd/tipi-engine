package ch.sharedvd.tipi.engine.model;

import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.common.AbstractTipiPersistenceTest;
import ch.sharedvd.tipi.engine.infos.TipiActivityInfos;
import ch.sharedvd.tipi.engine.utils.InputStreamHolder;
import org.junit.Test;

import java.io.InputStream;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class ActivityVariableTest extends AbstractTipiPersistenceTest {

    @Test
    public void putGetVariables() throws Exception {

        final VariableMap vars = new VariableMap();
        vars.put("int", 42);
        vars.put("long", 24L);
        vars.put("regdate", LocalDate.of(2001, 2, 3));
        vars.put("str", "Une string");

        InputStream is = getClass().getResourceAsStream("/ActivityVariableTest/inputStreamVariable.txt");
        vars.put("file", new InputStreamHolder(is));

        final long pid = tipiFacade.launch(ActivityVariableProcess.meta, vars);
        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(100);
        }
        final TipiActivityInfos infos = tipiQueryFacade.getActivityInfos(pid);
        assertEquals(ActivityState.FINISHED, infos.getState());
    }

}
