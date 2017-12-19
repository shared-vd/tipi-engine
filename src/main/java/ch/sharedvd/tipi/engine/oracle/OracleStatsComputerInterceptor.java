package ch.sharedvd.tipi.engine.oracle;

import ch.sharedvd.tipi.engine.interceptor.TipiEngineInterceptor;
import ch.sharedvd.tipi.engine.utils.QuantityFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class OracleStatsComputerInterceptor implements TipiEngineInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(OracleStatsComputerInterceptor.class);

    private static final int ORACLE_STATS_COMPUTE_INTERVAL_IN_SEC = 900; // 15 minutes * 60 secs
    private int oracleStatsComputeInterval = 1000 * ORACLE_STATS_COMPUTE_INTERVAL_IN_SEC;
    private long lastOracleStatsComputeTimestamp = 0;

    @Autowired
    private OracleStatisticsComputer oracleStatisticsComputer;

    private OracleStatsThread oracleStatisticsComputerThread;

    @Override
    public void onStartActivity(long id, String name) {
    }

    @Override
    public void onErrorActivity(long id, String name, Throwable exception) {
    }

    @Override
    public void onEndActivity(long id, String name) {
    }

    @Override
    public void afterConsumerStartActivity() throws Exception {
        long now = System.currentTimeMillis();

        // Si c'est la première fois, on calcule pas
        if (lastOracleStatsComputeTimestamp == 0) {
            lastOracleStatsComputeTimestamp = now;
        } else if ((lastOracleStatsComputeTimestamp + oracleStatsComputeInterval) <= now) {

            if (oracleStatisticsComputerThread != null) {
                if (!oracleStatisticsComputerThread.isAlive()) {
                    LOGGER.debug("Join du thread de calcul des stats Oracle");
                    oracleStatisticsComputerThread.join();
                    oracleStatisticsComputerThread = null;
                } else {
                    LOGGER.info("Attention! Le Thread de calcul des stats Oracle est toujours actif.");
                }
            }

            if (oracleStatisticsComputerThread == null) {
                LOGGER.debug("Démarrage du thread de calcul des stats Oracle");
                oracleStatisticsComputerThread = new OracleStatsThread();
                oracleStatisticsComputerThread.start();
            }
        }
    }

    private class OracleStatsThread extends Thread {

        @Override
        public void run() {
            final long begin = System.currentTimeMillis();

            lastOracleStatsComputeTimestamp = begin;

            oracleStatisticsComputer.computeStatisticsForTable(null, "TP_ACTIVITY");
            oracleStatisticsComputer.computeStatisticsForTable(null, "TP_VARIABLE");
            oracleStatisticsComputer.recalculateIndexes(null, "TP_ACTIVITY");

            final long diff = System.currentTimeMillis() - begin;
            LOGGER.info("Tipi: fin du calcul des stats/indexes Oracle en " + QuantityFormatter.formatMillis(diff));
        }
    }

    /**
     * Donne l'intervalle minimum entre deux calculs des statistiques d'Oracle en secondes.
     *
     * @param aOracleStatsComputeIntervalInSec
     */
    public void setOracleStatsComputeInterval(int aOracleStatsComputeIntervalInSec) {
        oracleStatsComputeInterval = aOracleStatsComputeIntervalInSec;
    }
}
