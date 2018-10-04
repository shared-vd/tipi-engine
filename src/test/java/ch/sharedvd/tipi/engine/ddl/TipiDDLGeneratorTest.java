package ch.sharedvd.tipi.engine.ddl;

import ch.sharedvd.tipi.engine.common.HibernateMetaDataHelper;
import ch.sharedvd.tipi.engine.model.DbActivity;
import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.dialect.PostgreSQL9Dialect;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;

public class TipiDDLGeneratorTest {

    private static final Logger log = LoggerFactory.getLogger(TipiDDLGeneratorTest.class);

    protected String getDialect() {
        return PGSQL_DIALECT;
    }

    protected String[] getHibernatePackagesToScan() {
        return new String[] { DbActivity.class.getPackage().getName() };
    }

    public static final String ORACLE_STD_DIALECT = Oracle10gDialect.class.getName();
    public static final String PGSQL_DIALECT = PostgreSQL9Dialect.class.getName();

    @Test
    @SuppressWarnings("unchecked")
    public void genddl() throws Exception {
        generateDDL();
        log.info("DDL is OK.");
    }

    private void generateDDL() throws Exception {
        final MetadataImplementor md = createMetaData();
        final SchemaExport export = new SchemaExport();

        // drop
        {
            export.setDelimiter(";");
            export.setOutputFile(getRefPath() + "/db-drop.sql");
            export.setFormat(true);
            export.execute(EnumSet.of(TargetType.SCRIPT), SchemaExport.Action.DROP, md);
        }
        // create
        {
            export.setDelimiter(";");
            export.setOutputFile(getRefPath() + "/db-create.sql");
            export.setFormat(true);
            export.execute(EnumSet.of(TargetType.SCRIPT), SchemaExport.Action.CREATE, md);
        }
    }

    private MetadataImplementor createMetaData() throws Exception {
        return HibernateMetaDataHelper.createMetadataImplementor(getDialect(), getHibernatePackagesToScan());
    }

    protected String getRefPath() { return "target"; };

}

