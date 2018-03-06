package ch.sharedvd.tipi.engine.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tipi")
public class TipiProperties {

    private boolean startAtBoot = true;

    public boolean isStartAtBoot() {
        return startAtBoot;
    }
    public void setStartAtBoot(boolean startAtBoot) {
        this.startAtBoot = startAtBoot;
    }
}
