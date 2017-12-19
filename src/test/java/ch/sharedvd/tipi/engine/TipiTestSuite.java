package ch.sharedvd.tipi.engine;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

// Cette classe est utile pour permettre de démarrer plusieurs tests avec le meme context spring
@RunWith(Suite.class)
@SuiteClasses(value = {
/*
		PersistenceTest.class,
		ProcessWithoutChildrenTest.class,
		ActivityStateChangeServiceTest.class,
		TipiColdStarterTest.class,
		ResumingTest.class,
		ShutdownTest.class,
		MaxConcurrentTest.class,
		ActivityRunner_SubProcess_Test.class,
		StopStartGroupTest.class,
		ActivityGetVariableTest.class,
		MasseTest.class,
		SessionClearTest.class,
		ExclusiveTest.class,
		ParentChildTest.class,
		VariableScopeTest.class,
		ActivityRunner_Activity_Test.class,
		NbRetryTest.class
*/
})
public class TipiTestSuite {
}
