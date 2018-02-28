package ch.sharedvd.tipi.engine.infos;

import ch.sharedvd.tipi.engine.action.UnknownProcess;
import ch.sharedvd.tipi.engine.command.MetaModelHelper;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbSubProcess;
import ch.sharedvd.tipi.engine.utils.Assert;

import java.util.Date;

public class TipiTopProcessInfos extends TipiActivityInfos {

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

    public Date dateStartTerminate;
    public Date dateEndTerminate;

    private long nbChildrenTotal = 0;
    private long nbChildrenInitial = 0;
    private long nbChildrenExecuting = 0;
    private long nbChildrenRetry = 0;
    private long nbChildrenRequestEndExecution = 0;
    private long nbChildrenFinished = 0;
    private long nbChildrenError = 0;
    private long nbChildrenAborted = 0;
    private long nbChildrenSuspended = 0;
    private long nbChildrenWaiting = 0;

    public TipiTopProcessInfos(DbSubProcess model, boolean loadVariables) {
        super(model, loadVariables);

        dateStartTerminate = model.getDateStartTerminate();
        dateEndTerminate = model.getDateEndTerminate();

        final TopProcessMetaModel meta = MetaModelHelper.getTopProcessMeta(model.getFqn());
        if (null != meta) { // ça arrive dans les tests...
            priority = meta.getPriority();
            nbThreads = meta.getNbMaxConcurrent();
            if (meta.getClazz().equals(UnknownProcess.class)) {
                setNameOrProcessName("Unknown: " + getNameOrProcessName());
                setProcessName("Unknown: " + getProcessName());
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

    public Date getDateStartTerminate() {
        return dateStartTerminate;
    }

    public Date getDateEndTerminate() {
        return dateEndTerminate;
    }

    public long getNbChildrenInitial() {
        return nbChildrenInitial;
    }

    public void incNbChildrenInitial(Long nbChildrenInitial) {
        this.nbChildrenInitial += nbChildrenInitial;
    }

    public long getNbChildrenExecuting() {
        return nbChildrenExecuting;
    }

    public void incNbChildrenExecuting(Long nbChildrenWaiting) {
        this.nbChildrenWaiting += nbChildrenWaiting;
    }

    public long getNbChildrenRequestEndExecution() {
        return nbChildrenRequestEndExecution;
    }

    public void incNbChildrenRequestEndExecution(Long nbChildrenRequestEndExecution) {
        this.nbChildrenRequestEndExecution += nbChildrenRequestEndExecution;
    }

    public long getNbChildrenFinished() {
        return nbChildrenFinished;
    }

    public void incNbChildrenFinished(Long nbChildrenFinished) {
        this.nbChildrenFinished += nbChildrenFinished;
    }

    public long getNbChildrenAborted() {
        return nbChildrenAborted;
    }

    public void incNbChildrenAborted(Long nbChildrenAborted) {
        this.nbChildrenAborted += nbChildrenAborted;
    }

    public long getNbChildrenError() {
        return nbChildrenError;
    }

    public void incNbChildrenError(Long nbChildrenError) {
        this.nbChildrenError += nbChildrenError;
    }

    public long getNbChildrenSuspended() {
        return nbChildrenSuspended;
    }

    public void incNbChildrenSuspended(Long nbChildrenSuspended) {
        this.nbChildrenSuspended += nbChildrenSuspended;
    }

    public long getNbChildrenWaiting() {
        return nbChildrenWaiting;
    }

    public void incNbChildrenWaiting(Long nbChildrenWaiting) {
        this.nbChildrenWaiting += nbChildrenWaiting;
    }

    public long getNbChildrenTotal() {
        return nbChildrenTotal;
    }

    public void incNbChildrenTotal(Long nbChildrenTotal) {
        this.nbChildrenTotal += nbChildrenTotal;
    }

    public long getNbChildrenRetry() {
        return nbChildrenRetry;
    }

    public void incNbChildrenRetry(Long nbChildren) {
        this.nbChildrenRetry += nbChildren;
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
