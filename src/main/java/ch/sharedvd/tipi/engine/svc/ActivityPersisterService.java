package ch.sharedvd.tipi.engine.svc;

import ch.sharedvd.tipi.engine.action.ActivityFacade;
import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.meta.MetaModelHelper;
import ch.sharedvd.tipi.engine.meta.SubProcessMetaModel;
import ch.sharedvd.tipi.engine.meta.VariableDescription;
import ch.sharedvd.tipi.engine.model.*;
import ch.sharedvd.tipi.engine.repository.ActivityRepository;
import ch.sharedvd.tipi.engine.utils.Assert;
import ch.sharedvd.tipi.engine.utils.BlobFactory;
import ch.sharedvd.tipi.engine.utils.InputStreamHolder;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ActivityPersisterService {

    @Autowired
    private ActivityRepository activityRepository;
    @Autowired
    private EntityManager em;

    private DbActivity createModelFromMeta(final ActivityMetaModel meta, final boolean isProcess, final VariableMap vars) {
        Assert.notNull(meta);

        DbActivity a = null;
        if (isProcess) {
            a = new DbTopProcess();
        } else if (meta instanceof SubProcessMetaModel) {
            a = new DbSubProcess();
        } else {
            a = new DbActivity();
        }
        a.setFqn(meta.getFQN());
        a.setProcessName(meta.getFQN()); // sera overridé par setProcess() appelé sur Activity
        this.putVariables(a, meta, vars);

        return a;
    }

    public DbActivity persistModelFromMeta(final ActivityMetaModel meta, final boolean isProcess, final VariableMap vars) {
        final DbActivity model = createModelFromMeta(meta, isProcess, vars);
        final DbActivity a = activityRepository.save(model);
        // flush so the insert fail fast if bad
        activityRepository.flush();
        return a;
    }

    public long addChildActivity(final ActivityMetaModel meta, final DbSubProcess parent, final Long previousId,
                                 final VariableMap vars, final String correlationId) {
        Assert.notNull(parent);
        Assert.notNull(meta);

        final DbActivity act = persistModelFromMeta(meta, false, vars);
        act.setParent(parent);
        act.setProcess(parent.getProcessOrThis());
        act.setCorrelationId(correlationId);
        Assert.notNull(act.getProcess());

        // Previous?
        if (previousId != null) {
            final DbActivity previous = activityRepository.findById(previousId).orElse(null);
            Assert.notNull(previous);
            Assert.isEqual(parent, previous.getParent());
            act.setPrevious(previous);
        }
        return act.getId();
    }

    @SuppressWarnings("unchecked")
    public List<ActivityFacade> getChildren(long parentId) {
        List<ActivityFacade> activities = new ArrayList<ActivityFacade>();

        final List<DbActivity> actis = activityRepository.findByParentId(parentId);
        for (DbActivity a : actis) {
            activities.add(new ActivityFacade(a.getId(), this, activityRepository));
        }
        return activities;
    }

    public DbActivity getModel(long id) {
        return activityRepository.findById(id).orElse(null);
    }

    public void putVariables(DbActivity aDbActivity, final ActivityMetaModel meta, VariableMap vars) {
        if (vars != null) {
            for (String key : vars.keySet()) {
                final Serializable value = vars.get(key);
                this.putVariable(aDbActivity, meta, key, value);
            }
        }
    }

    public void putVariable(DbActivity aDbActivity, String key, Serializable value) {
        final ActivityMetaModel meta = MetaModelHelper.createActivityMetaModel(aDbActivity.getFqn());
        putVariable(aDbActivity, meta, key, value);
    }
    public void putVariable(DbActivity aDbActivity, final ActivityMetaModel meta, String key, Serializable value) {
        Assert.notNull(key);
        Assert.notNull(value, "Missing value for key=" + key);

        final VariableDescription vd = meta.getVariable(key);
        if (vd != null) {
            if (vd.getVariableType().isCompatible(value)) {
                reallyPutVariable(aDbActivity, key, value);
            } else {
                throw new WrongTypeVariableException(key, vd.getVariableType().getClazz(), value.getClass());
            }
        } else {
            throw new UndefinedVariableException(key);
        }
    }

    private void reallyPutVariable(DbActivity aDbActivity, String key, Serializable value) {
        Assert.notNull(key);
        Assert.notNull(value, "Missing value for key=" + key);

        final DbVariable<?> variable;
        if (value instanceof String) {
            variable = new DbStringVariable(key, (String) value);
        } else if (value instanceof LocalDate) {
            final LocalDate date = (LocalDate) value;
            final String str = DateTimeFormatter.ofPattern("YYYYMMdd").format(date);
            Integer i = Integer.parseInt(str);
            variable = new DbIntegerVariable(key, i);
        } else if (value instanceof Integer) {
            variable = new DbIntegerVariable(key, (Integer) value);
        } else if (value instanceof Long) {
            variable = new DbLongVariable(key, (Long) value);
        } else if (value instanceof Boolean) {
            variable = new DbBooleanVariable(key, (Boolean) value);
        } else if (value instanceof Timestamp) {
            variable = new DbTimestampVariable(key, (Timestamp) value);
        } else if (value instanceof InputStreamHolder) {
            variable = new DbInputStreamVariable(key, (InputStreamHolder) value, new BlobFactory((Session) em.getDelegate()));
        } else {
            variable = new DbSerializableVariable(key, (Serializable) value, new BlobFactory((Session) em.getDelegate()));
        }
        variable.setOwner(aDbActivity);
        aDbActivity.putVariable(variable);
    }
}
