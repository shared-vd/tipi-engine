package ch.sharedvd.tipi.engine.model;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "TP_ACTIVITY")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("activity")
@org.hibernate.annotations.Table(appliesTo = "TP_ACTIVITY", indexes = {
        @Index(name = "TP_ACT_REQEND_STATE_IDX", columnNames = {"STATE", "REQUEST_END_EXECUTION"}),
        @Index(name = "TP_ACT_DTYPE_IDX", columnNames = "DTYPE")
})
public class DbActivity extends DbBaseEntity {

    private static final long serialVersionUID = -1L;

    private String fqn; // Le nom de l'activité
    private ActivityState state; // L'état de l'activité

    private DbTopProcess process;
    private DbSubProcess parent;
    private DbActivity previous;

    private String processName;

    private Map<String, DbVariable<?>> variables = new HashMap<String, DbVariable<?>>();

    private Date dateStartExecute; // La date de début d'execute() de l'activité
    private Date dateEndExecute; // la date de fin d'execute() de l'activité
    private Date dateEndActivity; // la date ou l'activité est passé à l'état FINISHED
    private String correlationId; // Espace pour stocker un id permettant une corrélation quelconque avec cette activité
    private boolean requestEndExecution = false;
    private int nbRetryDone = 0;

    private String callstack;


    public DbActivity() {
        this.state = ActivityState.INITIAL;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_FK", nullable = true)
    @Index(name = "TP_ACT_PROCESS_FK_IDX")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public DbTopProcess getProcess() {
        return process;
    }

    public void setProcess(DbTopProcess process) {
        this.process = process;
        if (process != null) {
            setProcessName(process.getFqn());
        }
    }

    @Transient
    public DbTopProcess getProcessOrThis() {
        if (process == null) {
            // Si on n'a pas de process, on EST le process.
            return (DbTopProcess) this;
        }
        return process;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_FK", nullable = true)
    @Index(name = "TP_ACT_PARENT_FK_IDX")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public DbSubProcess getParent() {
        return parent;
    }

    public void setParent(DbSubProcess parent) {
        this.parent = parent;
    }

    @Column(name = "PROCESS_NAME")
    @Index(name = "TP_ACT_PROCESS_NAME_IDX")
    public String getProcessName() {
        if (this instanceof DbTopProcess) {
            // Si on n'a pas de process, on EST le process.
            return fqn;
        }
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PREVIOUS_FK", nullable = true)
    @Index(name = "TP_ACT_PREVIOUS_FK_IDX")
    @OnDelete(action = OnDeleteAction.CASCADE)
    public DbActivity getPrevious() {
        return previous;
    }

    public void setPrevious(DbActivity previous) {
        this.previous = previous;
    }

    @Column(name = "REQUEST_END_EXECUTION")
    @Index(name = "TP_ACTIVITY_REQUEST_END_EXEC")
    public boolean isRequestEndExecution() {
        return requestEndExecution;
    }

    public void setRequestEndExecution(boolean aRequestEndExecution) {
        this.requestEndExecution = aRequestEndExecution;
    }

    @Column(name = "NB_RETRY", nullable = false)
    public int getNbRetryDone() {
        return nbRetryDone;
    }

    public void setNbRetryDone(int nbRetryDone) {
        this.nbRetryDone = nbRetryDone;
    }

    @Column(name = "NAME", nullable = false)
    @Index(name = "TP_ACTIVITY_NAME_IDX")
    public String getFqn() {
        return fqn;
    }

    public void setFqn(String name) {
        this.fqn = name;
    }

    /**
     * La date quand l'activité commence a runner (début de la méthode execute())
     *
     * @return
     */
    @Column(name = "DATE_START_EXECUTE")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getDateStartExecute() {
        return dateStartExecute;
    }

    public void setDateStartExecute(Date aDateStartExecute) {
        this.dateStartExecute = aDateStartExecute;
    }

    /**
     * La date quand l'activité a terminée de runner (fin de la méthode execute())
     *
     * @return
     */
    @Column(name = "DATE_END_EXECUTE")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getDateEndExecute() {
        return dateEndExecute;
    }

    public void setDateEndExecute(Date aDateEndExecute) {
        this.dateEndExecute = aDateEndExecute;
    }

    /**
     * La date quand l'activité est complètement terminée (FINISHED)
     *
     * @return
     */
    @Column(name = "DATE_END_ACTIVITY")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getDateEndActivity() {
        return dateEndActivity;
    }

    public void setDateEndActivity(Date dateEndActivity) {
        this.dateEndActivity = dateEndActivity;
    }

    @Column(name = "STATE", nullable = false)
    @Enumerated(EnumType.STRING)
    @Index(name = "TP_ACTIVITY_STATE_IDX")
    public ActivityState getState() {
        return state;
    }

    public void setState(ActivityState state) {
        this.state = state;
    }

    @Column(name = "CORRELATION_ID")
    @Index(name = "TP_ACTIVITY_CORRELATION_IDX")
    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String aCorrelationId) {
        correlationId = aCorrelationId;
    }

    @Column(name = "CALLSTACK", length = 2000)
    public String getCallstack() {
        return callstack;
    }

    public void setCallstack(String cs) {
        if (cs != null && cs.length() > 2000) {
            this.callstack = cs.substring(0, 2000);
        } else {
            this.callstack = cs;
        }
    }

    @Transient
    public void setCallstack() {
    }

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "key")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Map<String, DbVariable<?>> getVariables() {
        return variables;
    }

    @SuppressWarnings("unused")
    private void setVariables(Map<String, DbVariable<?>> variables) {
        this.variables = variables;
    }

    // === Transient ===

    public void putVariable(DbVariable<?> aVar) {
        if (variables.get(aVar.getKey()) != null) {
            toString();
        }
        //Assert.isNull(variables.get(aVar.getKey()));
        variables.put(aVar.getKey(), aVar);
    }


    @Transient
    public void removeVariable(String key) {
        getVariables().size();
        getVariables().remove(key);
    }

    @Transient
    public Object getVariable(String key) {
        DbVariable<?> var = getVariables().get(key);
        if (var != null) {
            return var.getValue();
        }
        return null;
    }

    @Transient
    public Map<String, Object> getAllVariables() {
        final Map<String, Object> vars = new HashMap<String, Object>();

        for (String key : getVariables().keySet()) {
            Object value = getVariable(key);
            if (value != null) {
                vars.put(key, value);
            }
        }
        return vars;
    }

    @Transient
    public boolean containsVariable(String key) {
        return getVariables().containsKey(key);
    }

    @Transient
    public boolean isTerminated() {
        return isRequestEndExecution() == false
                &&
                ActivityState.FINISHED.equals(getState());
    }

    @Transient
    public boolean isAborted() {
        return isRequestEndExecution() == false
                &&
                ActivityState.ABORTED.equals(getState());
    }

    @Transient
    public boolean isTerminatedWithError() {
        return ActivityState.ERROR.equals(getState()) && isRequestEndExecution() == false;
    }

    @Transient
    public boolean isTerminatedSuspended() {
        return ActivityState.SUSPENDED.equals(getState()) && isRequestEndExecution() == false;
    }

    @Transient
    public boolean isResumable() {
        return (isTerminatedWithError() || isTerminatedSuspended());
    }

    @Transient
    public boolean isRepeatable() {
        return ActivityState.EXECUTING.equals(this.getState()) && !isRequestEndExecution();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("name=").append(fqn);
        str.append(",processName=").append(getProcessName());
        str.append(",parent=");
        if (parent != null) {
            str.append(parent.getId());
        } else {
            str.append("null");
        }
        str.append(",state=").append(getState());
        str.append(",reqEnd=").append(requestEndExecution);
        str.append(",retry=").append(getNbRetryDone());
        return super.toString(str.toString());
    }

    /**
     * Convert a package name with a class name
     * Exemple : ch.vd.rcpers.eve.svc.event.bd.BaseDeliveryTopProcess => BaseDeliveryTopProcess
     *
     * @return SimpleClassName
     */
    @Transient
    public String getSimpleName() {
        if (fqn.contains(".")) {
            String[] splitedName = fqn.split("\\.");
            int index = splitedName != null && splitedName.length > 0 ? splitedName.length - 1 : 0;
            String simpleName = splitedName[index];
            return simpleName;
        } else {
            return fqn;
        }
    }

}
