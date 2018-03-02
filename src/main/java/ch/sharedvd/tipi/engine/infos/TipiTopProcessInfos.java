package ch.sharedvd.tipi.engine.infos;

import ch.sharedvd.tipi.engine.action.UnknownProcess;
import ch.sharedvd.tipi.engine.meta.MetaModelHelper;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbSubProcess;
import ch.sharedvd.tipi.engine.utils.Assert;

import java.util.Date;

public class TipiTopProcessInfos extends TipiActivityInfos {

    private int priority = 0;
    private int nbThreads = 0;

    private int nbActivitesTotal = 0;
    private int nbActivitesInitial = 0;
    private int nbActivitesExecuting = 0;
    private int nbActivitesRetry = 0;
    private int nbActivitesRequestEndExecution = 0;
    private int nbActivitesFinished = 0;
    private int nbActivitesAborted = 0;
    private int nbActivitesError = 0;
    private int nbActivitesSuspended = 0;
    private int nbActivitesWaiting = 0;

    public Date dateStartTerminate;
    public Date dateEndTerminate;

    public TipiTopProcessInfos(DbSubProcess model, String description, boolean loadVariables) {
        super(model, description, loadVariables);

        dateStartTerminate = model.getDateStartTerminate();
        dateEndTerminate = model.getDateEndTerminate();

        final TopProcessMetaModel meta = MetaModelHelper.getTopProcessMeta(model.getFqn());
        if (null != meta) { // ça arrive dans les tests...
            priority = meta.getPriority();
            nbThreads = meta.getNbMaxConcurrent();
            if (meta.getClazz().equals(UnknownProcess.class)) {
                setSimpleName("Unknown: " + getSimpleName());
                setProcessFqn("Unknown: " + getProcessFqn());
            }
        }
    }

    public int getPriority() {
        return priority;
    }

    public int getNbThreads() {
        return nbThreads;
    }

    public int getNbActivitesInitial() {
        return nbActivitesInitial;
    }

    public void incNbActivitesInitial(int nbActivitesInitial) {
        this.nbActivitesInitial += nbActivitesInitial;
    }

    public int getNbActivitesExecuting() {
        return nbActivitesExecuting;
    }

    public void incNbActivitesExecuting(int nbActivitesExecuting) {
        this.nbActivitesExecuting += nbActivitesExecuting;
    }

    public int getNbActivitesRetry() {
        return nbActivitesRetry;
    }

    public void incNbActivitesRetry(int nbActivites) {
        this.nbActivitesRetry += nbActivites;
    }

    public int getNbActivitesRequestEndExecution() {
        return nbActivitesRequestEndExecution;
    }

    public void incNbActivitesRequestEndExecution(int nbActivitesRequestEndExecution) {
        this.nbActivitesRequestEndExecution += nbActivitesRequestEndExecution;
    }

    public int getNbActivitesFinished() {
        return nbActivitesFinished;
    }

    public void incNbActivitesFinished(int nbActivitesFinished) {
        this.nbActivitesFinished += nbActivitesFinished;
    }

    public int getNbActivitesAborted() {
        return nbActivitesAborted;
    }

    public void incNbActivitesAborted(int nbActivitesAborted) {
        this.nbActivitesAborted += nbActivitesAborted;
    }

    public int getNbActivitesError() {
        return nbActivitesError;
    }

    public void incNbActivitesError(int nbActivitesError) {
        this.nbActivitesError += nbActivitesError;
    }

    public int getNbActivitesSuspended() {
        return nbActivitesSuspended;
    }

    public void incNbActivitesSuspended(int nbActivitesSuspended) {
        this.nbActivitesSuspended += nbActivitesSuspended;
    }

    public int getNbActivitesWaiting() {
        return nbActivitesWaiting;
    }

    public void incNbActivitesWaiting(int nbActivitesWaiting) {
        this.nbActivitesWaiting += nbActivitesWaiting;
    }

    public int getNbActivitesTotal() {
        return nbActivitesTotal;
    }

    public void incNbActivitesTotal(int nbActivitesTotal) {
        this.nbActivitesTotal += nbActivitesTotal;
    }

    public Date getDateStartTerminate() {
        return dateStartTerminate;
    }

    public Date getDateEndTerminate() {
        return dateEndTerminate;
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
