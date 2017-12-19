package ch.vd.registre.tipi.command.stats;

import ch.sharedvd.tipi.engine.interceptor.TipiEngineInterceptor;

public class EngineInterceptorMock implements TipiEngineInterceptor {

    @Override
    public void onStartActivity(long id, String name) {
    }

    @Override
    public void onEndActivity(long id, String name) {
    }

    @Override
    public void onErrorActivity(long id, String name, Throwable exception) {
    }

    @Override
    public void afterConsumerStartActivity() {
    }
}
