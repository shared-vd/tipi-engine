package ch.sharedvd.tipi.engine.action;

import ch.sharedvd.tipi.engine.client.VariableMap;
import ch.sharedvd.tipi.engine.command.MetaModelHelper;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.model.ActivityState;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.model.DbSubProcess;
import ch.sharedvd.tipi.engine.model.DbTopProcess;
import ch.sharedvd.tipi.engine.svc.ActivityPersisterService;
import ch.sharedvd.tipi.engine.utils.Assert;

import java.io.Serializable;
import java.util.List;

public class ActivityFacade {

    private ActivityPersisterService activityPersisterService;
    private long activityId;

    public ActivityFacade(long activityId, ActivityPersisterService manager) {
        Assert.notNull(activityId);
        Assert.notNull(manager);
        this.activityId = activityId;
        this.activityPersisterService = manager;
    }

    public long getId() {
        return getModel().getId();
    }

    public String getName() {
        return getModel().getFqn();
    }

    public Object getVariable(String key) {
        return getVariable(getModel(), key);
    }

    public Object getVariable(long id, String key) {
        DbActivity m = activityPersisterService.getModel(id);
        return getVariable(m, key);
    }

    public List<ActivityFacade> getChildren() {
        return activityPersisterService.getChildren(getModel().getId());
    }

    public void putVariable(String key, Serializable value) {
        activityPersisterService.putVariable(activityPersisterService.getModel(activityId), key, value);
    }

    public long addChildActivity(final ActivityMetaModel meta, Long previousId, VariableMap vars) {
        final DbSubProcess parent = (DbSubProcess) getModel();
        return activityPersisterService.addChildActivity(meta, parent, previousId, vars, null);
    }

    public long addChildActivity(final Class<? extends Activity> clazz, Long previousId, VariableMap vars) {
        final DbSubProcess parent = (DbSubProcess) getModel();
        return activityPersisterService.addChildActivity(MetaModelHelper.getActivityMetaModel(clazz), parent, previousId, vars, null);
    }

    public long addChildActivity(final Class<? extends Activity> clazz, Long previousId, VariableMap vars, String correlationId) {
        final DbSubProcess parent = (DbSubProcess) getModel();
        final ActivityMetaModel meta = MetaModelHelper.getActivityMetaModel(clazz);
        return activityPersisterService.addChildActivity(meta, parent, previousId, vars, correlationId);
    }

    public boolean isAborted() {
        final DbActivity m = getModel();

        final DbTopProcess p;
        if (m.getProcess() != null) {
            p = m.getProcess();
        } else {
            p = (DbTopProcess) m;
        }
        Assert.notNull(p);
        ActivityState dbState = activityPersisterService.getDbActivityState(p.getId());
        return dbState == ActivityState.ABORTED;
    }


    // --- private ---

    private DbActivity getModel() {
        return activityPersisterService.getModel(activityId);
    }

    private Object getVariable(DbActivity m, String key) {
        // D'abord chez soi
        Object o = m.getVariable(key);

        // Ensuite dans le previous
        if (o == null && m.getPrevious() != null) {
            o = m.getPrevious().getVariable(key);
        }

        // Ensuite dans le parent
        if (o == null) {
            o = getVariableFromParent(m.getParent(), key);
        }
        return o;
    }

    private Object getVariableFromParent(DbActivity parent, String key) {
        if (parent != null) {
            Object o = parent.getVariable(key);
            if (o == null) {
                return getVariableFromParent(parent.getParent(), key);
            }
            return o;
        }
        return null;
    }

}
