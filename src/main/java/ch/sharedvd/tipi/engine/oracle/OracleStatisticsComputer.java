package ch.sharedvd.tipi.engine.oracle;

import ch.sharedvd.tipi.engine.utils.Assert;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;

public class OracleStatisticsComputer {

    protected DataSource dataSource;

    public void setDataSource(DataSource aDataSource) {
        dataSource = aDataSource;
    }

    public void recalculateIndexes(Object o, Object z) {
        Assert.fail("");
    }

    /**
     * Calcul les statistiques pour le schéma donné. Si le schéma est <code>null</code>, le schéma par
     * défaut pour l'utilisateur donné par la data source est utilisé.
     * <p>
     * NE PEUT PAS ETRE APPELLE DEPUIS UNE TRANSACTION. Depuis une transaction, il faut utiliser
     * computeStatisticsForSchemaInNewThread.
     *
     * @param aSchemaName le nom du schéma ou <code>null</code> pour le schéma par défaut de l'utilisateur.
     */
    public void computeStatisticsForSchema(final String aSchemaName) {
        computeStatistics(aSchemaName, null);
    }

    /**
     * Calcul les statistiques pour la table donnée du schéma donné. Si le schéma est <code>null</code>,
     * le schéma par défaut pour l'utilisateur donné par la data source est utilisé.
     * <p>
     * NE PEUT PAS ETRE APPELLE DEPUIS UNE TRANSACTION. Depuis une transaction, il faut utiliser
     * computeStatisticsForTableInNewThread.
     *
     * @param aSchemaName le nom du schéma ou <code>null</code> pour le schéma par défaut de l'utilisateur.
     * @param aTableName  le nom de la table.
     */
    public void computeStatisticsForTable(final String aSchemaName, final String aTableName) {
        if (aTableName == null) {
            throw new IllegalArgumentException();
        }
        computeStatistics(aSchemaName, aTableName);
    }

    /**
     * Calcul les statistiques pour le schéma donné. Si le schéma est <code>null</code>, le schéma par
     * défaut pour l'utilisateur donné par la data source est utilisé.
     * <p>
     * Lance un thread séparé pour faire le calcul pour s'affranchir de la transaction courante et attend
     * la fin de l'exécution avant de retourner à l'appellant (d'où la possible InterruptedException).
     * Hors transaction, on peut utiliser computeStatisticsForSchema.
     *
     * @param aSchemaName le nom du schéma ou <code>null</code> pour le schéma par défaut de l'utilisateur.
     * @throws InterruptedException
     */
    public void computeStatisticsForSchemaInNewThread(final String aSchemaName) throws InterruptedException {
        ThreadComputer thread = new ThreadComputer(aSchemaName, null);
        thread.start();
        thread.join();
    }

    /**
     * Calcul les statistiques pour la table donnée du schéma donné. Si le schéma est <code>null</code>,
     * le schéma par défaut pour l'utilisateur donné par la data source est utilisé.
     * <p>
     * Lance un thread séparé pour faire le calcul pour s'affranchir de la transaction courante et attend
     * la fin de l'exécution avant de retourner à l'appellant (d'où la possible InterruptedException).
     * Hors transaction, on peut utiliser computeStatisticsForTable.
     *
     * @param aSchemaName le nom du schéma ou <code>null</code> pour le schéma par défaut de l'utilisateur.
     * @param aTableName  le nom de la table.
     * @throws InterruptedException
     */
    public void computeStatisticsForTableInNewThread(final String aSchemaName, final String aTableName) throws InterruptedException {
        if (aTableName == null) {
            throw new IllegalArgumentException();
        }
        ThreadComputer thread = new ThreadComputer(aSchemaName, aTableName);
        thread.start();
        thread.join();
    }

    private void computeStatistics(final String aSchemaName, final String aTableName) {
        final JdbcTemplate template = new JdbcTemplate(dataSource);
        template.execute(new ConnectionCallback<Object>() {
            @Override
            public Object doInConnection(Connection conn) throws SQLException, DataAccessException {
                String queryPattern = "'{' call DBMS_STATS.gather_{0}_stats({1}) '}'";
                String query;
                if (null != aTableName) {
                    query = MessageFormat.format(queryPattern, "table", "?,?");
                } else {
                    query = MessageFormat.format(queryPattern, "schema", "?");
                }

                CallableStatement stmt = conn.prepareCall(query);
                stmt.setString(1, aSchemaName);
                if (null != aTableName) {
                    stmt.setString(2, aTableName);
                }

                stmt.execute();
                stmt.close();

                return null;
            }
        });
    }

    private class ThreadComputer extends Thread {

        private String schema;
        private String table;

        public ThreadComputer(String aSchema, String aTable) {
            schema = aSchema;
            table = aTable;
        }

        @Override
        public void run() {
            computeStatistics(schema, table);
        }
    }
}
