package ch.vd.shared.tipi.engine.action;

import ch.vd.shared.tipi.engine.model.ActivityState;

public class SuspendedActivityResultContext extends ActivityResultContext {

    private static final long serialVersionUID = 7198688453192293685L;

    private String correlationId = null;

    SuspendedActivityResultContext() {
    }

    public SuspendedActivityResultContext(String aCorrelationId) {
        this(aCorrelationId, null);
    }

    public SuspendedActivityResultContext(String aCorrelationId, String aInfoMessage) {
        super(aInfoMessage);
        correlationId = aCorrelationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    void setCorrelationId(String aCorrelationId) {
        correlationId = aCorrelationId;
    }

    @Override
    public ActivityState getState() {
        return ActivityState.SUSPENDED;
    }

}
