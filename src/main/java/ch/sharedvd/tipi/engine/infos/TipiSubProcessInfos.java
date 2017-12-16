package ch.sharedvd.tipi.engine.infos;

import ch.sharedvd.tipi.engine.model.DbSubProcess;

import java.util.Date;

public class TipiSubProcessInfos extends TipiActivityInfos {

    private static final long serialVersionUID = 1L;

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

    public TipiSubProcessInfos(DbSubProcess model, boolean loadVariables) {
        super(model, loadVariables);

        dateStartTerminate = model.getDateStartTerminate();
        dateEndTerminate = model.getDateEndTerminate();
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

}
