package ch.sharedvd.tipi.engine.interceptor;

import org.springframework.beans.factory.annotation.Autowired;

public class TipiEngineInterceptorFacade {

    @Autowired(required = false)
    private TipiEngineInterceptor intf;

    public void onStartActivity(long id, String name) {
        if (intf != null) {
            try {
                intf.onStartActivity(id, name);
            } catch (RuntimeException re) {
                throw re;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void onEndActivity(long id, String name) {
        if (intf != null) {
            try {
                intf.onEndActivity(id, name);
            } catch (RuntimeException re) {
                throw re;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void onErrorActivity(long id, String name, Throwable exception) {
        if (intf != null) {
            try {
                intf.onErrorActivity(id, name, exception);
            } catch (RuntimeException re) {
                throw re;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void afterConsumerStartActivity() {
        if (intf != null) {
            try {
                intf.afterConsumerStartActivity();
            } catch (RuntimeException re) {
                throw re;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
