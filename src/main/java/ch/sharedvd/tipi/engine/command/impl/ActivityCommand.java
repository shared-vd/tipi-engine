package ch.sharedvd.tipi.engine.command.impl;

import ch.sharedvd.tipi.engine.command.Command;
import ch.sharedvd.tipi.engine.command.MetaModelHelper;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.utils.Assert;

public abstract class ActivityCommand extends Command {

	private long activityId;
	private DbActivity model;

	public ActivityCommand(long id) {
		activityId = id;
	}

	long getActivityId() {
		return activityId;
	}

	protected TopProcessMetaModel getTopProcess() {
		Assert.notNull(getModel(), "ActivityID: "+activityId);
		Assert.notNull(getModel().getProcessOrThis(), "ActivityID: "+activityId);
		Assert.notNull(getModel().getProcessOrThis().getFqn(), "ActivityID: "+activityId);
		Assert.isFalse(getModel().getProcessOrThis().getFqn().isEmpty(), "ActivityID: "+activityId);

		TopProcessMetaModel p = MetaModelHelper.getTopProcessMeta(getModel().getProcessOrThis().getFqn());
		Assert.notNull(p);
		return p;
	}
	protected ActivityMetaModel getMeta() {
		Assert.notNull(getModel(), "ActivityID: "+activityId);
		Assert.notNull(getModel().getFqn(), "ActivityID: "+activityId);
		Assert.isFalse(getModel().getFqn().isEmpty(), "ActivityID: "+activityId);
		ActivityMetaModel mm = MetaModelHelper.getMeta(getModel().getFqn());
		Assert.notNull(mm);
		return mm;
	}
	protected TopProcessMetaModel getTopProcMeta() {
		ActivityMetaModel mm = MetaModelHelper.getMeta(getModel().getFqn());
		TopProcessMetaModel sub = null;
		if (mm instanceof TopProcessMetaModel) {
			sub = (TopProcessMetaModel)mm;
		}
		return sub;
	}

	protected DbActivity getModel() {
		if (model == null) {
			model = activityRepository.findOne(getActivityId());
		}
		return model;
	}

	protected TopProcessGroupLauncher getLauncher() {
		return getLauncher(getTopProcess());
	}

	protected void runActivity() {
		runActivity(getModel(), getMeta(), getTopProcess());
	}

	@Override
	public String toString() {
		StringBuilder msg = new StringBuilder();
		msg.append(super.toString()).append(" ActivityId: ").append(getActivityId());
		// Ne pas utilisé getModel(): activityRepository peut être null dans certains tests unitaires...
		if (null != model) {
			msg.append(" ").append(model.getFqn());
		}
		return msg.toString();
	}

}
