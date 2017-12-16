package ch.sharedvd.tipi.engine.engine;

import ch.vd.registre.base.jpa.PersistenceContextService;
import ch.vd.registre.tipi.command.CommandHelperService;
import ch.vd.registre.tipi.command.CommandService;
import org.springframework.transaction.PlatformTransactionManager;

public class ActivityRunnerContext {

	public final PersistenceContextService persist;
	public final CommandHelperService commandHelperService;
	public final CommandService commandService;
	public final TopProcessGroupManager groupManager;
	public final PlatformTransactionManager txManager;
	public final TopProcessGroupLauncher launcher;
	
	public ActivityRunnerContext(PersistenceContextService persist, CommandHelperService commandHelperService, CommandService commandService, TopProcessGroupManager groupManager, PlatformTransactionManager txManager, TopProcessGroupLauncher launcher) {
		this.persist = persist;
		this.commandHelperService = commandHelperService;
		this.commandService = commandService;
		this.groupManager = groupManager;
		this.txManager = txManager;
		this.launcher = launcher;
	}

}
