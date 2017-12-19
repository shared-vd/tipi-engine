package ch.sharedvd.tipi.engine.client;

import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.command.CommandConsumer;
import ch.sharedvd.tipi.engine.command.CommandService;
import ch.sharedvd.tipi.engine.command.MetaModelHelper;
import ch.sharedvd.tipi.engine.command.impl.AbortProcessCommand;
import ch.sharedvd.tipi.engine.command.impl.ColdRestartCommand;
import ch.sharedvd.tipi.engine.command.impl.RunExecutingActivitiesCommand;
import ch.sharedvd.tipi.engine.engine.ActivityRunningService;
import ch.sharedvd.tipi.engine.engine.TipiStarter;
import ch.sharedvd.tipi.engine.engine.TopProcessGroupManager;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class TipiFacadeImpl implements TipiFacade {

    private static final Logger log = LoggerFactory.getLogger(TipiFacadeImpl.class);

    @Autowired
    private CommandService commandService;

    @Autowired
    private TipiStarter starter;

    @Autowired
    private CommandConsumer commandConsumer;

    @Autowired
    private ActivityRunningService activityService;

    @Autowired
    private TopProcessGroupManager groupManager;

    @Override
    public void coldRestart() {
        commandConsumer.addCommand(new ColdRestartCommand());
    }

    @Override
    public boolean hasActivityPending() {
        return groupManager.hasActivityPending() || commandConsumer.hasCommandPending();
    }

    @Override
    public int getPendingCommandCount() {
        return commandConsumer.getPendingCommandCount();
    }

    @Override
    public boolean hasCommandPending() {
        return commandConsumer.hasCommandPending();
    }

    @Override
    public long launch(final TopProcessMetaModel meta, final VariableMap vars) {
        return activityService.launch(meta, vars);
    }

    @Override
    public long launch(final Class<? extends TopProcess> cls, final VariableMap vars) {
        return activityService.launch(cls, vars);
    }

    @Override
    public void resumeAllError() {
        activityService.resumeAllError();
    }

    @Override
    public void resumeErrors(String groupName) {
        activityService.resumeErrors(groupName);
    }

    @Override
    public void resumeAllSuspended() {
        activityService.resumeAllSuspended();
    }

    @Override
    public void resume(final long id, final VariableMap vars) {
        activityService.resume(id, vars);
    }

    @Override
    public boolean isRunning(final long pid) {
        return activityService.isRunning(pid);
    }

    @Override
    public boolean isProcessRunning(final long id) {
        return activityService.isProcessRunning(id);
    }

    @Override
    public boolean isProcessScheduled(long aid) {
        return activityService.isProcessScheduled(aid);
    }

    @Override
    public boolean isResumable(final long pid) {
        return activityService.isResumable(pid);
    }

    @Override
    public void abortProcess(final long pid, final boolean delete) {
        commandService.sendCommand(new AbortProcessCommand(pid, delete));
    }

    @Override
    public String getStringVariable(final long id, final String key) {
        return activityService.getStringVariable(id, key);
    }

    @Override
    public void restartGroup(String fqn, int nbMax, int priority) throws Exception {
        TopProcessMetaModel group = MetaModelHelper.getTopProcessMeta(fqn);
        groupManager.restart(group, nbMax, priority);

        commandService.sendCommand(new RunExecutingActivitiesCommand());
    }

    @Override
    public void startGroup(String fqn) throws Exception {
        TopProcessMetaModel group = MetaModelHelper.getTopProcessMeta(fqn);
        groupManager.start(group);

        commandService.sendCommand(new RunExecutingActivitiesCommand());
    }

    @Override
    public void stopGroup(String fqn) throws Exception {
        TopProcessMetaModel group = MetaModelHelper.getTopProcessMeta(fqn);
        groupManager.stop(group);
    }

    @Override
    public void startAllGroups() throws Exception {
        groupManager.startAllGroups();

        commandService.sendCommand(new RunExecutingActivitiesCommand());
    }

    @Override
    public void stopAllGroups() throws Exception {
        groupManager.stopAllGroups();
    }

    @Override
    public boolean isTipiStarted() throws Exception {
        return starter.isStarted();
    }

    @Override
    public void startTipi() throws Exception {
        starter.start();
    }

    @Override
    public void stopTipi() throws Exception {
        starter.stop();
    }

    @Override
    public void setMaxConnections(String aConnectionType, int aNbMaxConnections) {
        activityService.setMaxConnections(aConnectionType, aNbMaxConnections);
    }

    @Override
    public void setMaxConcurrentActivitiesForGroup(String aGroupName, int aNbMaxConnections) {
        activityService.setMaxConcurrentActivitiesForGroup(aGroupName, aNbMaxConnections);
    }

    @Override
    public void setPriorityForGroup(String aGroupName, int aPrio) {
        activityService.setPriorityForGroup(aGroupName, aPrio);
    }
}
