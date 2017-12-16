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

    public long id;
    public String type;
    public String nameOrProcessName;
    public long processId;
    public String processName;
    public long parentId;
    public String parentName;
    public String description;
    public boolean requestEndExecution;

    public Date dateCreation;
    public Date dateStartExecute;
    public Date dateEndExecute;
    public Date dateEndActivity;
    public ActivityState state;
    public boolean terminated;
    public String correlationId;

    public Map<String, Object> variables;
    public String callstack;

    public TipiActivityInfos(DbActivity model, boolean loadVariables) {

        // On doit résoudre tous les champs -> LazyInit
        id = model.getId();
        if (model instanceof DbTopProcess) {
            type = "Process";
        } else if (model instanceof DbSubProcess) {
            type = "Sous-process";
        } else if (model instanceof DbActivity) {
            type = "Activité";
        } else {
            type = "Inconnu";
        }
        nameOrProcessName = model.getSimpleName();
        {
            // a refaire ici la description de l'activity
        }
        processId = model.getProcessOrThis().getId();
        processName = model.getProcessOrThis().getSimpleName();
        if (null != model.getParent()) {
            parentId = model.getParent().getId();
            parentName = model.getParent().getFqn();
        }
        requestEndExecution = model.isRequestEndExecution();

        dateCreation = model.getCreationDate();
        dateStartExecute = model.getDateStartExecute();
        dateEndExecute = model.getDateEndExecute();
        dateEndActivity = model.getDateEndActivity();

        state = model.getState();
        terminated = model.isTerminated();
        correlationId = model.getCorrelationId();
        callstack = model.getCallstack();
        if (loadVariables) {
            variables = model.getAllVariables();
        }
    }

    public long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getNameOrProcessName() {
        return nameOrProcessName;
    }

    public String getCallstack() {
        return callstack;
    }

    public long getProcessId() {
        return processId;
    }

    public String getProcessName() {
        return processName;
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

    public String getSimpleProcessName() {
        return getProcessName();
    }

    @Override
    public String toString() {
        return "id=" + id + " name=" + nameOrProcessName + " state=" + state;
    }
}
