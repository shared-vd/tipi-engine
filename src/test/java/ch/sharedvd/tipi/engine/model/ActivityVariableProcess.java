package ch.sharedvd.tipi.engine.model;

import ch.sharedvd.tipi.engine.action.ActivityResultContext;
import ch.sharedvd.tipi.engine.action.FinishedActivityResultContext;
import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;
import ch.sharedvd.tipi.engine.meta.VariableDescription;
import ch.sharedvd.tipi.engine.meta.VariableType;
import ch.sharedvd.tipi.engine.utils.Assert;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.time.LocalDate;

public class ActivityVariableProcess extends TopProcess {

    public static final TopProcessMetaModel meta = new TopProcessMetaModel(ActivityVariableProcess.class,
            new VariableDescription[]{
                    new VariableDescription("int", VariableType.Integer),
                    new VariableDescription("long", VariableType.Long),
                    new VariableDescription("regdate", VariableType.LocalDate),
                    new VariableDescription("str", VariableType.String),
                    new VariableDescription("file", VariableType.Serializable),
            }, null,
            10, -1, 10, null, true) {
        @Override
        protected void init() {
            setDeleteWhenFinished(false);
        }
    };

    @Override
    protected ActivityResultContext execute() throws Exception {

        Assert.isEqual(42, getIntVariable("int"));
        Assert.isEqual(24L, getLongVariable("long"));
        Assert.isEqual("Une string", getStringVariable("str"));
        Assert.isEqual(20010203, getIntVariable("regdate"));
        Assert.isNull(getLocalDateVariable("regdate2"));
        Assert.isEqual(LocalDate.of(2001, 2, 3), getLocalDateVariable("regdate"));

        InputStream inputStream = getInputStreamVariable("file");
        Assert.isEqual("test\n", IOUtils.toString(inputStream));

        return new FinishedActivityResultContext();
    }

}
