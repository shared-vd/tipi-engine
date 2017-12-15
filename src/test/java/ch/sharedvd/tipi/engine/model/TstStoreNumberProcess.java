package ch.sharedvd.tipi.engine.model;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import org.springframework.beans.factory.annotation.Autowired;

public class TstStoreNumberProcess extends TopProcess {

	public static RetryPolicy retry = new DefaultRetryPolicy(0);
	public static final TopProcessMetaModel meta = new TopProcessMetaModel(TstStoreNumberProcess.class, retry, 2, -1, 20, null) {
		@Override
		protected void init() {
			setDeleteWhenFinished(false);
		};
	};

	@Autowired
	private PersistenceContextService persist;

	public static int number = 0;

	public TstStoreNumberProcess() {
		super();
	}

	@Override
	protected ActivityResultContext execute() throws Exception {

		Integer var = getIntVariable("var");
		number = var;
		putVariable("result", "TheResult");

		Long pid = getLongVariable("id");
		ActivityModel model = persist.get(ActivityModel.class, pid);
		putVariable("name", model.getFqn());

		return new FinishedActivityResultContext();
	}

}
