package ch.sharedvd.tipi.engine.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.sharedvd.tipi.engine.model.DbActivity.*;

@Entity
@NamedQueries({
        @NamedQuery(name = "DbActivity.findExecutingActivities", query = FIND_EXEC_ACTIVITIES),
        @NamedQuery(name = "DbActivity.findChildren", query = FIND_CHILDREN),
        @NamedQuery(name = "DbActivity.findByGroupAndState", query = FIND_GROUP_STATE),
        @NamedQuery(name = "DbActivity.findByState", query = FIND_BY_STATE)
})
@Table(name = "TP_ACTIVITY", indexes = {
        @Index(name = "TP_ACT_REQEND_STATE_IDX", columnList = "STATE,REQUEST_END_EXECUTION"),
        @Index(name = "TP_ACT_DTYPE_IDX", columnList = "DTYPE"),
        @Index(name = "TP_ACT_PARENT_FK_IDX", columnList = "PARENT_FK"),
        @Index(name = "TP_ACT_PROCESS_FK_IDX", columnList = "PROCESS_FK"),
        @Index(name = "TP_ACT_FQN_IDX", columnList = "FQN"),
        @Index(name = "TP_ACTIVITY_CORRELATION_IDX", columnList = "CORRELATION_ID"),
        @Index(name = "TP_ACTIVITY_STATE_IDX", columnList = "STATE"),
        @Index(name = "TP_ACTIVITY_REQUEST_END_EXEC", columnList = "REQUEST_END_EXECUTION"),
        @Index(name = "TP_ACT_PREVIOUS_FK_IDX", columnList = "PREVIOUS_FK")
})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
@DiscriminatorValue("activity")
public class DbActivity extends DbBaseEntity {

    @Column(name = "FQN", nullable = false)
    private String fqn; // Le nom de l'activité
    @Column(name = "PROCESS_NAME", nullable = false)
    private String processName; // Le nom du process ou FQN si on est le process

    @Column(name = "STATE", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActivityState state; // L'état de l'activité

    // Utile seulement pour la requete:
    //    ActivityRepository.findTopProcessNamesByStateAndReqEnd
    @OneToMany(mappedBy = "process")
    private List<DbActivity> children;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_FK", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DbTopProcess process;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_FK", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DbSubProcess parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PREVIOUS_FK", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DbActivity previous;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKey(name = "key")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Map<String, DbVariable<?>> variables = new HashMap<String, DbVariable<?>>();

    @Column(name = "DATE_START_EXECUTE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateStartExecute; // La date de début d'execute() de l'activité

    @Column(name = "DATE_END_EXECUTE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEndExecute; // la date de fin d'execute() de l'activité

    @Column(name = "DATE_END_ACTIVITY")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEndActivity; // la date ou l'activité est passé à l'état FINISHED

    @Column(name = "CORRELATION_ID")
    private String correlationId; // Espace pour stocker un id permettant une corrélation quelconque avec cette activité

    @Column(name = "REQUEST_END_EXECUTION", nullable = false)
    private boolean requestEndExecution = false;

    @Column(name = "NB_RETRY", nullable = false)
    private int nbRetryDone = 0;

    @Column(name = "CALLSTACK", length = 2000)
    private String callstack;


    public DbActivity() {
        this.state = ActivityState.INITIAL;
    }

    public DbTopProcess getProcess() {
        return process;
    }

    public void setProcess(DbTopProcess process) {
        this.process = process;
        if (process != null) {
            setProcessName(process.getFqn());
        }
    }

    public DbSubProcess getParent() {
        return parent;
    }

    public void setParent(DbSubProcess parent) {
        this.parent = parent;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public DbActivity getPrevious() {
        return previous;
    }

    public void setPrevious(DbActivity previous) {
        this.previous = previous;
    }

    public boolean isRequestEndExecution() {
        return requestEndExecution;
    }

    public void setRequestEndExecution(boolean aRequestEndExecution) {
        this.requestEndExecution = aRequestEndExecution;
    }

    public int getNbRetryDone() {
        return nbRetryDone;
    }

    public void setNbRetryDone(int nbRetryDone) {
        this.nbRetryDone = nbRetryDone;
    }

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
    public Date getDateEndActivity() {
        return dateEndActivity;
    }

    public void setDateEndActivity(Date dateEndActivity) {
        this.dateEndActivity = dateEndActivity;
    }

    public ActivityState getState() {
        return state;
    }

    public void setState(ActivityState state) {
        this.state = state;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String aCorrelationId) {
        correlationId = aCorrelationId;
    }

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

    /**
     * Convert a package name with a class name
     * Exemple : ch.vd.rcpers.eve.svc.event.bd.BaseDeliveryTopProcess - BaseDeliveryTopProcess
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

    @Transient
    public DbTopProcess getProcessOrThis() {
        if (process == null) {
            // Si on n'a pas de process, on EST le process.
            return (DbTopProcess) this;
        }
        return process;
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

    static final String FIND_BY_STATE =
            "select a from DbActivity a " +
                    "where a.requestEndExecution = false " +
                    "  and a.state = (?1)";

    static final String FIND_GROUP_STATE =
            "select a from DbActivity a join a.process p " +
                    "where p in (select pi from DbTopProcess pi where pi.fqn = (?1)) " +
                    "  and a.requestEndExecution = false " +
                    "  and a.state = (?2)";

    static final String FIND_CHILDREN = "from DbActivity a " +
            "where a.parent = (?1)";

    static final String FIND_EXEC_ACTIVITIES = "" +
            "select a from DbActivity a " +
            "where (a.process in (from DbTopProcess p where p.fqn = (?1)) " +
            "       or a in (from DbTopProcess p where p.fqn = (?1)) ) " +
            "   and a.state = ch.sharedvd.tipi.engine.model.ActivityState.EXECUTING " +
            "   and a.requestEndExecution = false " +
            "order by nbRetryDone asc, id asc";
}
