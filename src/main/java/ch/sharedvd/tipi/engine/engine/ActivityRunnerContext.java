package ch.sharedvd.tipi.engine.engine;

import ch.sharedvd.tipi.engine.command.CommandHelperService;
import ch.sharedvd.tipi.engine.command.CommandService;
import ch.sharedvd.tipi.engine.repository.ActivityRepository;
import ch.sharedvd.tipi.engine.runner.TopProcessGroupLauncher;
import org.springframework.transaction.PlatformTransactionManager;

public class ActivityRunnerContext {

	public final ActivityRepository activityRepository;
	public final CommandHelperService commandHelperService;
	public final CommandService commandService;
	public final TopProcessGroupManager groupManager;
	public final PlatformTransactionManager txManager;
	public final TopProcessGroupLauncher launcher;
	
	public ActivityRunnerContext(ActivityRepository persist, CommandHelperService commandHelperService, CommandService commandService, TopProcessGroupManager groupManager, PlatformTransactionManager txManager, TopProcessGroupLauncher launcher) {
		this.activityRepository = persist;
		this.commandHelperService = commandHelperService;
		this.commandService = commandService;
		this.groupManager = groupManager;
		this.txManager = txManager;
		this.launcher = launcher;
	}

}
