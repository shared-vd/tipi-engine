package ch.vd.shared.tipi.engine.svc;

import ch.vd.shared.tipi.engine.action.ActivityFacade;
import ch.vd.shared.tipi.engine.client.VariableMap;
import ch.vd.shared.tipi.engine.command.MetaModelHelper;
import ch.vd.shared.tipi.engine.meta.ActivityMetaModel;
import ch.vd.shared.tipi.engine.model.*;
import ch.vd.shared.tipi.engine.repository.ActivityModelRepository;
import ch.vd.shared.tipi.engine.utils.Assert;
import ch.vd.shared.tipi.engine.utils.BlobFactory;
import ch.vd.shared.tipi.engine.utils.InputStreamHolder;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ActivityPersistenceService {

    @Autowired
    private ActivityModelRepository activityModelRepository;
    @Autowired
    private EntityManager em;

    public ActivityState getDbActivityState(long activityId) {
        ActivityModel db = activityModelRepository.findOne(activityId);
        return db.getState();
    }

    private ActivityModel persistModelFromMeta(final ActivityMetaModel meta, final boolean isProcess, final VariableMap vars) {
        ActivityModel model = MetaModelHelper.createModelFromMeta(meta, isProcess, vars, this);
        em.persist(model);
        return model;
    }

    public long addChildActivity(final ActivityMetaModel meta, final SubProcessModel parent, final Long previousId,
                                 final VariableMap vars, final String correlationId) {
        Assert.notNull(parent);
        Assert.notNull(meta);

        final ActivityModel act = persistModelFromMeta(meta, false, vars);
        act.setParent(parent);
        act.setProcess(parent.getProcessOrThis());
        act.setCorrelationId(correlationId);
        Assert.notNull(act.getProcess());

        // Previous?
        if (previousId != null) {
            final ActivityModel previous = activityModelRepository.findOne(previousId);
            Assert.notNull(previous);
            Assert.isEqual(parent, previous.getParent());
            act.setPrevious(previous);
        }
        return act.getId();
    }

    @SuppressWarnings("unchecked")
    public List<ActivityFacade> getChildren(long parentId) {
        List<ActivityFacade> activities = new ArrayList<ActivityFacade>();

        final List<ActivityModel> actis = activityModelRepository.findByParentId(parentId);
        for (ActivityModel a : actis) {
            activities.add(new ActivityFacade(a.getId(), this));
        }
        return activities;
    }

    public ActivityModel getModel(long id) {
        return activityModelRepository.findOne(id);
    }

    public void putVariables(ActivityModel aActivityModel, VariableMap vars) {
        if (vars != null) {
            for (String key : vars.keySet()) {
                putVariable(aActivityModel, key, vars.get(key));
            }
        }
    }

    public void putVariable(ActivityModel aActivityModel, String key, Object value) {
        Assert.notNull(key);
        Assert.notNull(value, "Missing value for key=" + key);

        final Variable<?> variable;
        if (value instanceof String) {
            variable = new StringVariable(key, (String) value);
        }
        else if (value instanceof LocalDate) {
            Assert.fail("");
            variable = null;
            //variable = new IntegerVariable(key, ((LocalDate) value).index());
        }
        else if (value instanceof Integer) {
            variable = new IntegerVariable(key, (Integer) value);
        }
        else if (value instanceof Long) {
            variable = new LongVariable(key, (Long) value);
        }
        else if (value instanceof Boolean) {
            variable = new BooleanVariable(key, (Boolean) value);
        }
        else if (value instanceof Timestamp) {
            variable = new TimestampVariable(key, (Timestamp) value);
        }
        else if (value instanceof InputStreamHolder) {
            variable = new InputStreamVariable(key, (InputStreamHolder) value, new BlobFactory((Session)em.getDelegate()));
        }
        else {
            variable = new SerializableVariable(key, (Serializable) value, new BlobFactory((Session)em.getDelegate()));
        }
        variable.setOwner(aActivityModel);
        aActivityModel.putVariable(variable);
    }
}
