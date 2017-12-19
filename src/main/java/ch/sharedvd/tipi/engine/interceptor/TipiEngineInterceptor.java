package ch.sharedvd.tipi.engine.interceptor;

/**
 * Intercepteur sur les start/stop/error des activités Tipi
 */
public interface TipiEngineInterceptor {

    /**
     * Appelé au démarrage de l'activité (Thread context de l'activité)
     */
    void onStartActivity(long id, String name) throws Exception;

    /**
     * Appelé lorsqu'une activité est en erreur (Thread context de l'activité)
     */
    void onErrorActivity(long id, String name, Throwable exception) throws Exception;

    /**
     * Appelé à l'arrêt de l'activité (Thread context de l'activité)
     */
    void onEndActivity(long id, String name) throws Exception;

    /**
     * Appelé par le CommandConsumer après le start d'activités (Thread context du CommandConsumer)
     */
    void afterConsumerStartActivity() throws Exception;

}
