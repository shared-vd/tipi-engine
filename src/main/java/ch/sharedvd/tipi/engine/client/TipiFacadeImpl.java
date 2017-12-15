package ch.sharedvd.tipi.engine.client;

import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TipiFacadeImpl implements TipiFacade {

    @Override
    public long launch(TopProcessMetaModel meta, VariableMap vars) {
        return 0;
    }

    @Override
    public long launch(Class<? extends TopProcess> cls, VariableMap vars) {
        return 0;
    }

    @Override
    public void resume(long id, VariableMap vars) {

    }

    @Override
    public void resumeAllError() {

    }

    @Override
    public void resumeErrors(String groupName) {

    }

    @Override
    public void resumeAllSuspended() {

    }

    @Override
    public boolean hasActivityPending() {
        return false;
    }

    @Override
    public boolean hasCommandPending() {
        return false;
    }

    @Override
    public int getPendingCommandCount() {
        return 0;
    }

    @Override
    public boolean isRunning(long pid) {
        return false;
    }

    @Override
    public boolean isProcessRunning(long id) {
        return false;
    }

    @Override
    public boolean isProcessScheduled(long aid) {
        return false;
    }

    @Override
    public boolean isResumable(long pid) {
        return false;
    }

    @Override
    public void abortProcess(long topPid, boolean delete) {

    }

    @Override
    public String getStringVariable(long id, String key) {
        return null;
    }

    @Override
    public void startTipi() throws Exception {

    }

    @Override
    public void stopTipi() throws Exception {

    }

    @Override
    public boolean isTipiStarted() throws Exception {
        return false;
    }

    @Override
    public List<Long> getActivitiesForCorrelationId(String aCorrelationId) {
        return null;
    }

    @Override
    public void startAllGroups() throws Exception {

    }

    @Override
    public void stopAllGroups() throws Exception {

    }

    @Override
    public void startGroup(String fqn) throws Exception {

    }

    @Override
    public void restartGroup(String fqn, int nbMax, int priority) throws Exception {

    }

    @Override
    public void stopGroup(String fqn) throws Exception {

    }

    @Override
    public void setMaxConnections(String aConnectionType, int aNbMaxConnections) {

    }

    @Override
    public void setMaxConcurrentActivitiesForGroup(String aGroupName, int aNbMaxConnections) {

    }

    @Override
    public void setPriorityForGroup(String aGroupName, int aPrio) {

    }

    @Override
    public void coldRestart() {

    }
}
