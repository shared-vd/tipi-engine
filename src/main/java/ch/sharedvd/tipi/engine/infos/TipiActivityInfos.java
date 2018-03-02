package ch.sharedvd.tipi.engine.infos;

import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbSubProcess;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import ch.sharedvd.tipi.engine.utils.QuantityFormatter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;


public class TipiActivityInfos implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String type;
    private String simpleName;
    private long processId;
    private String processName;
    private long parentId;
    private String parentName;
    private String description;
    private boolean requestEndExecution;

    private Date dateCreation;
    private Date dateStartExecute;
    private Date dateEndExecute;
    private Date dateEndActivity;
    private ActivityState state;
    private boolean terminated;
    private String correlationId;

    private Map<String, Object> variables;
    private String callstack;

    public TipiActivityInfos(DbActivity db, String description, boolean loadVariables) {

        // On doit résoudre tous les champs -> LazyInit
        id = db.getId();
        if (db instanceof DbTopProcess) {
            type = "Process";
        } else if (db instanceof DbSubProcess) {
            type = "Sous-process";
        } else if (db instanceof DbActivity) {
            type = "Activité";
        } else {
            type = "Inconnu";
        }
        simpleName = db.getSimpleName();
        this.description = description;
        processId = db.getProcessOrThis().getId();
        processName = db.getProcessOrThis().getSimpleName();
        if (null != db.getParent()) {
            parentId = db.getParent().getId();
            parentName = db.getParent().getFqn();
        }
        requestEndExecution = db.isRequestEndExecution();

        dateCreation = db.getCreationDate();
        dateStartExecute = db.getDateStartExecute();
        dateEndExecute = db.getDateEndExecute();
        dateEndActivity = db.getDateEndActivity();

        state = db.getState();
        terminated = db.isTerminated();
        correlationId = db.getCorrelationId();
        callstack = db.getCallstack();
        if (loadVariables) {
            variables = db.getAllVariables();
        }
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getProcessName() {
        return processName;
    }
    // package
    void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getSimpleName() {
        return simpleName;
    }
    // package
    void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getCallstack() {
        return callstack;
    }

    public long getProcessId() {
        return processId;
    }

    public long getParentId() {
        return parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isRequestEndExecution() {
        return requestEndExecution;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public Date getDateStartExecute() {
        return dateStartExecute;
    }

    public Date getDateEndExecute() {
        return dateEndExecute;
    }
    public void setDateEndExecute(Date date) {
        this.dateEndExecute = date;
    }

    public Date getDateEndActivity() {
        return dateEndActivity;
    }

    public ActivityState getState() {
        return state;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    /**
     * Retourne la durée d'exécution
     *
     * @return
     */
    public String getExecutionTime() {
        if (dateStartExecute != null) {
            final Date dateTo;
            if (dateEndActivity == null) {
                dateTo = new Date();
            } else {
                dateTo = dateEndActivity;
            }

            long diffInMillis = (dateTo.getTime() - dateStartExecute.getTime());
            final String timeFormat = QuantityFormatter.formatMillis(diffInMillis);
            return timeFormat;
        }
        return "0s";
    }

    @Override
    public String toString() {
        return "id=" + id + " name=" + simpleName + " state=" + state;
    }
}
