package ch.sharedvd.tipi.engine.model;

import ch.vd.registre.base.date.RegDate;
import ch.vd.registre.tipi.client.TipiActivityInfos;
import ch.vd.registre.tipi.client.VariableMap;
import ch.vd.registre.tipi.common.TipiEngineTest;
import ch.vd.registre.tipi.utils.InputStreamHolder;
import org.junit.Test;

import java.io.InputStream;

public class ActivityVariableTest extends TipiEngineTest {

	@Test
	public void putGetVariables() throws Exception {

		final VariableMap vars = new VariableMap();
		vars.put("int", 42);
		vars.put("long", 24L);
		vars.put("regdate", RegDate.get(2001, 2, 3));
		vars.put("str", "Une string");

		InputStream is = getResourceInCurrentPackage("inputStreamVariable.txt");
		vars.put("file", new InputStreamHolder(is));

		final long pid = tipiFacade.launch(ActivityVariableProcess.meta, vars);
		while (tipiFacade.isRunning(pid)) {
			Thread.sleep(100);
		}

		TipiActivityInfos infos = tipiFacade.getActivityInfos(pid);
		assertEquals(ActivityState.FINISHED, infos.getState());
	}

}
