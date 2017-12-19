package ch.sharedvd.tipi.engine.query;

import ch.sharedvd.tipi.engine.model.ActivityState;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class TipiCriteria implements Serializable {

    private static final long serialVersionUID = 7854878213663622228L;

    private Long id;
    private Long processId;
    private Long parentId;
    private String nameOrProcessName = null;
    private ComparatorOperator operatorForNameOrProcessName = ComparatorOperator.END_WITH;
    private String variableName;
    private Object variableValue;
    private Boolean demandeFinExecution = null;
    private ActivityState[] statesSelectionnes;
    private String idCorrelation;
    private ComparatorOperator operatorForIdCorrelation = ComparatorOperator.EQ;
    private boolean onlyTopProcesses;

    public TipiCriteria() {
        reset();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processusId) {
        this.processId = processusId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long aParentId) {
        parentId = aParentId;
    }

    public String getNameOrProcessName() {
        return nameOrProcessName;
    }

    public void setNameOrProcessName(String nameOrProcessName) {
        this.nameOrProcessName = nameOrProcessName;
    }

    public ComparatorOperator getOperatorForIdCorrelation() {
        return operatorForIdCorrelation;
    }

    public void setOperatorForIdCorrelation(
            ComparatorOperator operatorForIdCorrelation) {
        this.operatorForIdCorrelation = operatorForIdCorrelation;
    }

    public ComparatorOperator getOperatorForNameOrProcessName() {
        return operatorForNameOrProcessName;
    }

    public void setOperatorForNameOrProcessName(ComparatorOperator operatorForNameOrProcessName) {
        this.operatorForNameOrProcessName = operatorForNameOrProcessName;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public Object getVariableValue() {
        return variableValue;
    }

    public void setVariableValue(Object variableValue) {
        this.variableValue = variableValue;
    }

    public Boolean getDemandeFinExecution() {
        return demandeFinExecution;
    }

    public void setDemandeFinExecution(Boolean demandeFinExecution) {
        this.demandeFinExecution = demandeFinExecution;
    }

    public ActivityState[] getStatesSelectionnes() {
        return statesSelectionnes;
    }

    public void setStatesSelectionnes(ActivityState[] statesSelectionnes) {
        this.statesSelectionnes = copyArrayWithoutNullValues(statesSelectionnes, ActivityState.class);
    }

    public String getIdCorrelation() {
        return idCorrelation;
    }

    public void setIdCorrelation(String idCorrelation) {
        this.idCorrelation = idCorrelation;
    }

    public ActivityState getInitialState() {
        return ActivityState.INITIAL;
    }

    public ActivityState getExecutingState() {
        return ActivityState.EXECUTING;
    }

    public ActivityState getFinishedState() {
        return ActivityState.FINISHED;
    }

    public ActivityState getAbortedState() {
        return ActivityState.ABORTED;
    }

    public ActivityState getErrorState() {
        return ActivityState.ERROR;
    }

    public ActivityState getSuspendedState() {
        return ActivityState.SUSPENDED;
    }


    public void setFiltreState(ActivityState filtreState) {
        if (filtreState != null) {
            statesSelectionnes = new ActivityState[]{filtreState};
        } else {
            statesSelectionnes = null;
        }
    }


    public void reset() {
        id = null;
        processId = null;
        parentId = null;
        nameOrProcessName = null;
        variableName = null;
        variableValue = null;
        demandeFinExecution = null;
        statesSelectionnes = null;
        idCorrelation = null;
        operatorForNameOrProcessName = ComparatorOperator.END_WITH;
        operatorForIdCorrelation = ComparatorOperator.EQ;
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] copyArrayWithoutNullValues(T[] array, Class<T> entityClass) {
        if (array != null) {
            List<T> result = new ArrayList<T>();
            for (T value : array) {
                if (value != null) {
                    result.add(value);
                }
            }
            T[] resultAsArray = (T[]) Array.newInstance(entityClass, result.size());
            return result.toArray(resultAsArray);
        }
        return null;
    }

    public boolean isOnlyTopProcesses() {
        return onlyTopProcesses;
    }

    public void setOnlyTopProcesses(boolean onlyTopProcesses) {
        this.onlyTopProcesses = onlyTopProcesses;
    }

}
