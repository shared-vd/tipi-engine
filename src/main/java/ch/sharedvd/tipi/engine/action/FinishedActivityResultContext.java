package ch.sharedvd.tipi.engine.action;

import ch.sharedvd.tipi.engine.model.ActivityState;

public class FinishedActivityResultContext extends ActivityResultContext {
    private static final long serialVersionUID = -7062351930399590505L;

    public FinishedActivityResultContext() {
        this(null);
    }

    public FinishedActivityResultContext(String aInfoMessage) {
        super(aInfoMessage);
    }

    @Override
    public ActivityState getState() {
        return ActivityState.FINISHED;
    }

}
