package ch.sharedvd.tipi.engine.model;

import ch.sharedvd.tipi.engine.AbstractTipiPersistenceTest;
import ch.sharedvd.tipi.engine.client.VariableMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ActivityGetVariableTest extends AbstractTipiPersistenceTest {

    @Test
    public void getVariable() throws Exception {

        final long aId = txTemplate.txWith((s) -> {
            DbActivity model = new DbActivity();
            model.setFqn("act1");
            activityRepository.save(model);
            return model.getId();
        }).longValue();

        VariableMap vars = new VariableMap();
        vars.put("var", 42);
        vars.put("id", aId);
        final long pid = tipiFacade.launch(TstStoreNumberProcess.meta, vars);
        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(10);
        }
        assertEquals(42, TstStoreNumberProcess.number);

        txTemplate.txWithout((s) -> {
            DbTopProcess p = topProcessRepository.findOne(pid);
            assertEquals("TheResult", p.getVariable("result"));
            assertEquals("act1", p.getVariable("name"));
        });
    }

}
