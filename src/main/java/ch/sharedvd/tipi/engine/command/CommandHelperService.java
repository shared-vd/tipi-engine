package ch.sharedvd.tipi.engine.command;

import ch.sharedvd.tipi.engine.action.Activity;
import ch.sharedvd.tipi.engine.action.ActivityFacade;
import ch.sharedvd.tipi.engine.action.SubProcess;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.meta.MetaModelHelper;
import ch.sharedvd.tipi.engine.meta.SubProcessMetaModel;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.repository.ActivityRepository;
import ch.sharedvd.tipi.engine.svc.ActivityPersisterService;
import ch.sharedvd.tipi.engine.utils.Assert;
import ch.sharedvd.tipi.engine.utils.BeanAutowirer;
import org.springframework.beans.factory.annotation.Autowired;

public class CommandHelperService {

	@Autowired
	private BeanAutowirer autowirer;
	@Autowired
	private ActivityRepository activityRepository;
	@Autowired
	private ActivityPersisterService activityHelper;

	public Activity createActivity(long actId) {
		DbActivity model = activityRepository.findById(actId).orElse(null);
		return createActivity(model);
	}
	public Activity createActivity(DbActivity model) {
		ActivityMetaModel meta = MetaModelHelper.createActivityMetaModel(model.getFqn());
		Activity act = meta.create();
		autowirer.autowire(act);
		act.setFacade(new ActivityFacade(model.getId(), activityHelper, activityRepository));

		verify(meta, act);
		return act;
	}

	private void verify(ActivityMetaModel meta, Activity act) {
		if (meta instanceof SubProcessMetaModel) {
			Assert.isTrue(act instanceof SubProcess);
		}
	}

}
