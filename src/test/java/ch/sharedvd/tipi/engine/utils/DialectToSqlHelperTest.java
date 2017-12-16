package ch.sharedvd.tipi.engine.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public class DialectToSqlHelperTest {

    private static final String oracleDialect = "ch.vd.shared.hibernate.config.OracleDialectWithNvarchar";
    private static final String pgsqlDialect = "org.hibernate.dialect.PostgreSQLDialect";

    private DialectToSqlHelper oracle;
    private DialectToSqlHelper pgsql;

    @Before
    public void onSetUp() throws Exception {

        oracle = new DialectToSqlHelper(oracleDialect);
        pgsql = new DialectToSqlHelper(pgsqlDialect);
    }

    @Test
    public void isOracle() {
        Assert.assertTrue(oracle.isOracle());
        Assert.assertFalse(pgsql.isOracle());
    }

    @Test
    public void isPostgresql() {
        Assert.assertFalse(oracle.isPostgresql());
        Assert.assertTrue(pgsql.isPostgresql());
    }

    @Test
    public void oracle_formatDateOnly() throws Exception {
        Assert.assertEquals("TO_DATE('2009-07-23', 'YYYY-MM-DD')", oracle.formatDateOnly(new Date(LocalDateTime.of(2009, 7, 23, 14, 55, 43).toEpochSecond(ZoneOffset.UTC))));
    }

    @Test
    public void pgsql_formatDateOnly() throws Exception {
        Assert.assertEquals("TO_DATE('2009-07-23', 'YYYY-MM-DD')", pgsql.formatDateOnly(new Date(LocalDateTime.of(2009, 7, 23, 14, 55, 43).toEpochSecond(ZoneOffset.UTC))));
    }

    @Test
    public void oracle_formatDateTime() throws Exception {
        Assert.assertEquals("TO_DATE('2009-07-23-14:55:43', 'YYYY-MM-DD-HH24:MI:SS')", oracle.formatDateTime(new Date(LocalDateTime.of(2009, 7, 23, 14, 55, 43).toEpochSecond(ZoneOffset.UTC))));
    }

    @Test
    public void pgsql_formatDateTime() throws Exception {
        Assert.assertEquals("TO_DATE('2009-07-23-14:55:43', 'YYYY-MM-DD-HH24:MI:SS')", pgsql.formatDateTime(new Date(LocalDateTime.of(2009, 7, 23, 14, 55, 43).toEpochSecond(ZoneOffset.UTC))));
    }

    @Test
    public void oracle_formatBoolean() throws Exception {
        Assert.assertEquals("1", oracle.formatBoolean(true));
        Assert.assertEquals("0", oracle.formatBoolean(false));
    }

    @Test
    public void pgsql_formatBoolean() throws Exception {
        Assert.assertEquals("true", pgsql.formatBoolean(true));
        Assert.assertEquals("false", pgsql.formatBoolean(false));
    }

}
