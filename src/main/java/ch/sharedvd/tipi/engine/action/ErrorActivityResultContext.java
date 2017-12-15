package ch.sharedvd.tipi.engine.action;

import ch.sharedvd.tipi.engine.model.ActivityState;

public class ErrorActivityResultContext extends ActivityResultContext {

    private static final long serialVersionUID = -8262083021099857343L;
    public String errorMessage;

    ErrorActivityResultContext() {
        this(null, null);
    }

    public ErrorActivityResultContext(String errorMessage) {
        this(errorMessage, null);
    }

    public ErrorActivityResultContext(String errorMessage, String aInfoMessage) {
        super(aInfoMessage);
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public ActivityState getState() {
        return ActivityState.ERROR;
    }

}
