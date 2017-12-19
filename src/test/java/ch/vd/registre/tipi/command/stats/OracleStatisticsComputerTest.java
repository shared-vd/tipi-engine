package ch.vd.registre.tipi.command.stats;

import ch.sharedvd.tipi.engine.command.CommandConsumer;
import ch.sharedvd.tipi.engine.common.TipiEngineTest;
import ch.sharedvd.tipi.engine.oracle.OracleStatisticsComputer;
import ch.sharedvd.tipi.engine.oracle.OracleStatsComputerInterceptor;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

;

public class OracleStatisticsComputerTest extends TipiEngineTest {

    private static final Logger LOGGER = Logger.getLogger(OracleStatisticsComputerTest.class);

    @Autowired
    private CommandConsumer commandConsumer;

    @Autowired
    private OracleStatisticsComputer oracleStatisticsComputer;

    @Autowired
    private OracleStatsComputerInterceptor oracleInterceptor;

    @Autowired
    @Qualifier("hibernateDialect")
    private String hibernateDialect;

    @Autowired
    @Qualifier("dbSchema")
    private String schemaName;

    @Test
    public void computeStats_SchemaAndTable() throws Exception {
        if (hibernateDialect.equals("ch.vd.shared.hibernate.config.OracleDialectWithNvarchar")) {
            _computeStats(schemaName, "TP_ACTIVITY");
        } else {
            LOGGER.info("Dialect différent de Oracle: " + hibernateDialect);
        }
    }

    @Test
    public void computeStats_TableOnly() throws Exception {
        if (hibernateDialect.equals("ch.vd.shared.hibernate.config.OracleDialectWithNvarchar")) {
            _computeStats(null, "TP_ACTIVITY");
        } else {
            LOGGER.info("Dialect différent de Oracle: " + hibernateDialect);
        }
    }

    private void _computeStats(String schema, String table) throws Exception {
        Assert.assertNotNull(table);
        oracleStatisticsComputer.computeStatisticsForTable(schema, table);
        oracleStatisticsComputer.recalculateIndexes(schema, table);

        oracleInterceptor.setOracleStatsComputeInterval(2);

        // Le calcul se fait toutes les 2 secondes -> on va re-calculer
        createLaunchAndWaitEndProcess();

        // Le calcul se fait toutes les 2 secondes -> on va re-calculer
        createLaunchAndWaitEndProcess();
    }

    private void createLaunchAndWaitEndProcess() throws Exception {
        tipiFacade.launch(NullTestProcess.meta, null);
        while (commandConsumer.hasCommandPending()) {
            Thread.sleep(50);
        }
        // Wait at least the "OracleStatsComputeInterval"
        Thread.sleep(2001);
    }

}
