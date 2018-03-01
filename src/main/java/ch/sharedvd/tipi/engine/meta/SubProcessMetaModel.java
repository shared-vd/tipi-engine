package ch.sharedvd.tipi.engine.meta;

import ch.sharedvd.tipi.engine.action.SubProcess;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

public class SubProcessMetaModel extends ActivityMetaModel {

    private static final long serialVersionUID = 1L;

    public SubProcessMetaModel(Class<?> clazz) {
        super(clazz);
    }

    public SubProcessMetaModel(Class<?> clazz, VariableDescription[] vars) {
        super(clazz, Arrays.asList(vars), null, null);
    }

    public SubProcessMetaModel(Class<?> clazz, String descr) {
        super(clazz, null, null, descr);
    }

    public SubProcessMetaModel(Class<?> clazz, List<VariableDescription> variables, String[] aUsedConnections, String descr) {
        super(clazz, variables, aUsedConnections, descr);
        Assert.isTrue(SubProcess.class.isAssignableFrom(clazz));
    }
}
