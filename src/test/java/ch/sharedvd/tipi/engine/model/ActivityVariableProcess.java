package ch.sharedvd.tipi.engine.model;

import ch.vd.registre.base.date.RegDate;
import ch.vd.registre.base.utils.Assert;
import ch.vd.registre.tipi.action.ActivityResultContext;
import ch.vd.registre.tipi.action.FinishedActivityResultContext;
import ch.vd.registre.tipi.action.TopProcess;
import ch.vd.registre.tipi.common.TipiEngineTest;
import ch.vd.registre.tipi.meta.TopProcessMetaModel;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;

public class ActivityVariableProcess extends TopProcess {

	public static final TopProcessMetaModel meta = new TopProcessMetaModel(ActivityVariableProcess.class, TipiEngineTest.defaultRetry, 10, -1, 10, null) {
		@Override
		protected void init() {
			setDeleteWhenFinished(false);
		};
	};

	@Override
	protected ActivityResultContext execute() throws Exception {

		Assert.isEqual(42, getIntVariable("int"));
		Assert.isEqual(24L, getLongVariable("long"));
		Assert.isEqual("Une string", getStringVariable("str"));
		Assert.isEqual(20010203, getIntVariable("regdate"));
		Assert.isNull(getRegDateVariable("regdate2"));
		Assert.isEqual(RegDate.get(2001, 2, 3), getRegDateVariable("regdate"));

		InputStream inputStream = getInputStreamVariable("file");
		Assert.isEqual("test\n", IOUtils.toString(inputStream));

		return new FinishedActivityResultContext();
	}

}
