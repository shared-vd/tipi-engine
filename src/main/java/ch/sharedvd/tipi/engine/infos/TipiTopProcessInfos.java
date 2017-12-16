package ch.sharedvd.tipi.engine.infos;

import ch.sharedvd.tipi.engine.action.UnknownProcess;
import ch.sharedvd.tipi.engine.command.MetaModelHelper;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbSubProcess;
import ch.sharedvd.tipi.engine.utils.Assert;

public class TipiTopProcessInfos extends TipiSubProcessInfos {

    private static final long serialVersionUID = 1L;

    private int priority = 0;
    private int nbThreads = 0;

    private long nbActivitesTotal = 0;
    private long nbActivitesInitial = 0;
    private long nbActivitesExecuting = 0;
    private long nbActivitesRetry = 0;
    private long nbActivitesRequestEndExecution = 0;
    private long nbActivitesFinished = 0;
    private long nbActivitesAborted = 0;
    private long nbActivitesError = 0;
    private long nbActivitesSuspended = 0;
    private long nbActivitesWaiting = 0;

    public TipiTopProcessInfos(DbSubProcess model, boolean loadVariables) {
        super(model, loadVariables);

        TopProcessMetaModel meta = MetaModelHelper.getTopProcessMeta(model.getFqn());
        if (null != meta) { // ça arrive dans les tests...
            priority = meta.getPriority();
            nbThreads = meta.getNbMaxConcurrent();
            if (meta.getClazz().equals(UnknownProcess.class)) {
                nameOrProcessName = "Unknown: " + nameOrProcessName;
                processName = "Unknown: " + processName;
            }
        }
    }

    public int getPriority() {
        return priority;
    }

    public int getNbThreads() {
        return nbThreads;
    }

    public long getNbActivitesInitial() {
        return nbActivitesInitial;
    }

    public void incNbActivitesInitial(Long nbActivitesInitial) {
        this.nbActivitesInitial += nbActivitesInitial;
    }

    public long getNbActivitesExecuting() {
        return nbActivitesExecuting;
    }

    public void incNbActivitesExecuting(Long nbActivitesExecuting) {
        this.nbActivitesExecuting += nbActivitesExecuting;
    }

    public long getNbActivitesRetry() {
        return nbActivitesRetry;
    }

    public void incNbActivitesRetry(Long nbActivites) {
        this.nbActivitesRetry += nbActivites;
    }

    public long getNbActivitesRequestEndExecution() {
        return nbActivitesRequestEndExecution;
    }

    public void incNbActivitesRequestEndExecution(Long nbActivitesRequestEndExecution) {
        this.nbActivitesRequestEndExecution += nbActivitesRequestEndExecution;
    }

    public long getNbActivitesFinished() {
        return nbActivitesFinished;
    }

    public void incNbActivitesFinished(Long nbActivitesFinished) {
        this.nbActivitesFinished += nbActivitesFinished;
    }

    public long getNbActivitesAborted() {
        return nbActivitesAborted;
    }

    public void incNbActivitesAborted(Long nbActivitesAborted) {
        this.nbActivitesAborted += nbActivitesAborted;
    }

    public long getNbActivitesError() {
        return nbActivitesError;
    }

    public void incNbActivitesError(Long nbActivitesError) {
        this.nbActivitesError += nbActivitesError;
    }

    public long getNbActivitesSuspended() {
        return nbActivitesSuspended;
    }

    public void incNbActivitesSuspended(Long nbActivitesSuspended) {
        this.nbActivitesSuspended += nbActivitesSuspended;
    }

    public long getNbActivitesWaiting() {
        return nbActivitesWaiting;
    }

    public void incNbActivitesWaiting(Long nbActivitesWaiting) {
        this.nbActivitesWaiting += nbActivitesWaiting;
    }

    public long getNbActivitesTotal() {
        return nbActivitesTotal;
    }

    public void incNbActivitesTotal(Long nbActivitesTotal) {
        this.nbActivitesTotal += nbActivitesTotal;
    }


    public void incActivitiesFromState(ActivityState aState, boolean aIsRequestEndExecution, int nbRetry) {
        ++nbActivitesTotal;
        if (nbRetry > 0) {
            ++nbActivitesRetry;
        }
        if (aIsRequestEndExecution) {
            ++nbActivitesRequestEndExecution;
        }
        switch (aState) {
            case INITIAL:
                ++nbActivitesInitial;
                break;
            case EXECUTING:
                ++nbActivitesExecuting;
                break;
            case ABORTED:
                ++nbActivitesAborted;
                break;
            case FINISHED:
                ++nbActivitesFinished;
                break;
            case ERROR:
                ++nbActivitesError;
                break;
            case WAIT_ON_CHILDREN:
                ++nbActivitesWaiting;
                break;
            case SUSPENDED:
                ++nbActivitesSuspended;
                break;
            default:
                Assert.fail("Not impl");
                break;
        }
    }
}
