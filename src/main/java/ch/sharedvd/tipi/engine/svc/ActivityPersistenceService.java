package ch.sharedvd.tipi.engine.svc;

import ch.sharedvd.tipi.engine.action.ActivityFacade;
import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.command.MetaModelHelper;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.model.*;
import ch.sharedvd.tipi.engine.repository.ActivityRepository;
import ch.sharedvd.tipi.engine.utils.Assert;
import ch.sharedvd.tipi.engine.utils.BlobFactory;
import ch.sharedvd.tipi.engine.utils.InputStreamHolder;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ActivityPersistenceService {

    @Autowired
    private ActivityRepository activityModelRepository;
    @Autowired
    private EntityManager em;

    public ActivityState getDbActivityState(long activityId) {
        DbActivity db = activityModelRepository.findOne(activityId);
        return db.getState();
    }

    private DbActivity persistModelFromMeta(final ActivityMetaModel meta, final boolean isProcess, final VariableMap vars) {
        DbActivity model = MetaModelHelper.createModelFromMeta(meta, isProcess, vars, this);
        em.persist(model);
        return model;
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
            final DbActivity previous = activityModelRepository.findOne(previousId);
            Assert.notNull(previous);
            Assert.isEqual(parent, previous.getParent());
            act.setPrevious(previous);
        }
        return act.getId();
    }

    @SuppressWarnings("unchecked")
    public List<ActivityFacade> getChildren(long parentId) {
        List<ActivityFacade> activities = new ArrayList<ActivityFacade>();

        final List<DbActivity> actis = activityModelRepository.findByParentId(parentId);
        for (DbActivity a : actis) {
            activities.add(new ActivityFacade(a.getId(), this));
        }
        return activities;
    }

    public DbActivity getModel(long id) {
        return activityModelRepository.findOne(id);
    }

    public void putVariables(DbActivity aActivityModel, VariableMap vars) {
        if (vars != null) {
            for (String key : vars.keySet()) {
                putVariable(aActivityModel, key, vars.get(key));
            }
        }
    }

    public void putVariable(DbActivity aActivityModel, String key, Object value) {
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
        variable.setOwner(aActivityModel);
        aActivityModel.putVariable(variable);
    }
}
