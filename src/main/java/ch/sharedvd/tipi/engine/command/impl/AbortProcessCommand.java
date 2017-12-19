package ch.sharedvd.tipi.engine.command.impl;

import ch.sharedvd.tipi.engine.runner.ActivityRunningService;
import ch.sharedvd.tipi.engine.runner.TopProcessGroupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class AbortProcessCommand extends ActivityCommand {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(AbortProcessCommand.class);

    @Autowired
    private ActivityRunningService activityService;
    @Autowired
    private TopProcessGroupManager manager;

    private boolean delete;

    public AbortProcessCommand(long pid, final boolean delete) {
        super(pid);
        this.delete = delete;
    }

    @Override
    public void execute() {

        boolean wasDeleted = false;
        if (delete) {
            wasDeleted = activityService.deleteProcess(getActivityId());
        }

        if (!wasDeleted) {
            // On a pas pu deleter -> abort
            activityService.abortProcess(getActivityId());
        }

        // Flush tous les cache
        manager.clearCaches();
    }

    // On se débrouille pour les transactions.
    // Il faut être clever pour pas rester bloqué sur des setState()
    @Override
    public boolean needTransaction() {
        return false;
    }

}
