package ch.sharedvd.tipi.engine.model;

import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.common.AbstractTipiPersistenceTest;
import org.junit.Assert;
import org.junit.Test;

;

public class ActivityGetVariableTest extends AbstractTipiPersistenceTest {

    @Test
    public void getVariable() throws Exception {

        final long aId = txTemplate.txWith((s) -> {
            DbActivity model = new DbActivity();
            model.setFqn("act1");
            DbActivity a = activityRepository.save(model);
            return a.getId();
        }).longValue();

        VariableMap vars = new VariableMap();
        vars.put("var", 42);
        vars.put("id", aId);
        final long pid = tipiFacade.launch(TstStoreNumberProcess.meta, vars);
        while (tipiFacade.isRunning(pid)) {
            Thread.sleep(10);
        }
        Assert.assertEquals(42, TstStoreNumberProcess.number);

        txTemplate.txWithout((s) -> {
            DbTopProcess p = topProcessRepository.findOne(pid);
            Assert.assertEquals("TheResult", p.getVariable("result"));
            Assert.assertEquals("act1", p.getVariable("name"));
        });
    }

}
