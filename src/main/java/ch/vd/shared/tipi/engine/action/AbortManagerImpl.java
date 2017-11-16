package ch.vd.shared.tipi.engine.action;

import ch.vd.shared.tipi.engine.client.AbortException;
import ch.vd.shared.tipi.engine.client.AbortManager;
import org.springframework.util.Assert;

public class AbortManagerImpl implements AbortManager {

    private ActivityFacade facade;

    public AbortManagerImpl(ActivityFacade facade) {
        Assert.notNull(facade);
        this.facade = facade;
    }

    @Override
    public void testAbort() {
        if (Thread.currentThread().isInterrupted()) {
            throw new AbortException(AbortException.AbortType.INTERRUPTED);
        } else if (facade.isAborted()) {
            throw new AbortException(AbortException.AbortType.ABORTED);
        }
    }
}
