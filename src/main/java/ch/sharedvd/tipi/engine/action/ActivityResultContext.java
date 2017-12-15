package ch.sharedvd.tipi.engine.action;

import ch.sharedvd.tipi.engine.model.ActivityState;

import java.io.Serializable;

public abstract class ActivityResultContext implements Serializable {

    private static final long serialVersionUID = -3357888087756294556L;

    public abstract ActivityState getState();

    public String infoMessage;

    public ActivityResultContext() {
    }

    public ActivityResultContext(String aInfoMessage) {
        setInfoMessage(aInfoMessage);
    }

    public String getInfoMessage() {
        return infoMessage;
    }

    public void setInfoMessage(String infoMessage) {
        this.infoMessage = infoMessage;
    }

}
