package ch.sharedvd.tipi.engine.utils;

import org.springframework.util.Assert;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Permet de formatter les statements SQL suivant le Dialect hibernate utilisé
 * Ce travail pourrait probablement être fait avec le Dialect hibernate directement
 * mais son utilisation est peu claire...
 * <p>
 * A remplacer 12c4
 *
 * @author jec
 */
public class DialectToSqlHelper {

    private String dialect;

    public DialectToSqlHelper(String dialect) {
        this.dialect = dialect;
    }

    public String formatBoolean(boolean b) {
        if (isOracle()) {
            return b ? "1" : "0";
        } else if (isPostgresql()) {
            return b ? "true" : "false";
        }
        Assert.isTrue(false, "Dialect inconnu: " + dialect); // fail
        return null;
    }

    public String formatDateOnly(Date date) {
        if (isOracle() || isPostgresql()) {
            return "TO_DATE('" + dateToDashString(date) + "', 'YYYY-MM-DD')";
        }
        Assert.isTrue(false, "Dialect inconnu: " + dialect); // fail
        return null;
    }

    public String formatDateTime(Date date) throws Exception {
        if (isOracle() || isPostgresql()) {
            final String ISO_TIMSTAMP_FORMAT = "yyyy-MM-dd-HH:mm:ss";
            final SimpleDateFormat SDF_ISO_TIMSTAMP_FORMAT = new SimpleDateFormat(ISO_TIMSTAMP_FORMAT);
            final String dateStr = SDF_ISO_TIMSTAMP_FORMAT.format(date);
            return "TO_DATE('" + dateStr + "', 'YYYY-MM-DD-HH24:MI:SS')";
        }
        Assert.isTrue(false, "Dialect inconnu: " + dialect); // fail
        return null;
    }

    public boolean isOracle() {
        if (dialect.equals("ch.vd.shared.hibernate.config.OracleDialectWithNvarchar")) {
            return true;
        }
        return false;
    }

    public boolean isPostgresql() {
        if (dialect.equals("org.hibernate.dialect.PostgreSQLDialect")) {
            return true;
        }
        return false;
    }

    public static final String DATE_FORMAT_DASH = "yyyy-MM-dd";
    protected static final SimpleDateFormat SDF_DATE_FORMAT_DASH = new SimpleDateFormat(DATE_FORMAT_DASH);
    public static String dateToDashString(Date date) {
        return SDF_DATE_FORMAT_DASH.format(date);
    }

}
