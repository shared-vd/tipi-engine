package ch.sharedvd.tipi.engine.model;

import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import org.junit.Assert;
import org.junit.Test;

public class ActivityGetVariableTest extends TipiEngineTest {

    @Test
    public void getVariable() throws Exception {
        final long aId = txTemplate.txWith(s -> {
            DbActivity model = new DbActivity();
            model.setFqn("act1");
            model.setProcessName("act1");
            activityRepository.save(model);
            return model.getId();
        });

        VariableMap vars = new VariableMap();
        vars.put("var", 42);
        vars.put("id", aId);
        final long pid = tipiFacade.launch(TstStoreNumberProcess.meta, vars);
        waitWhileRunning(pid, 5000);
        Assert.assertEquals(42, TstStoreNumberProcess.number);

        txTemplate.txWithout((s) -> {
            DbTopProcess p = topProcessRepository.findById(pid).orElse(null);
            Assert.assertEquals("TheResult", p.getVariable("result"));
            Assert.assertEquals("act1", p.getVariable("name"));
        });
    }

}
