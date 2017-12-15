package ch.sharedvd.tipi.engine.meta;

import ch.sharedvd.tipi.engine.action.SubProcess;
import org.springframework.util.Assert;

public class SubProcessMetaModel extends ActivityMetaModel {

    private static final long serialVersionUID = 1L;

    public SubProcessMetaModel(Class<?> clazz) {
        super(clazz);
        Assert.isTrue(SubProcess.class.isAssignableFrom(clazz));
    }

    public SubProcessMetaModel(Class<?> clazz, String[] aUsedConnections, String descr) {
        super(clazz, aUsedConnections, descr);
        Assert.isTrue(SubProcess.class.isAssignableFrom(clazz));
    }
}
