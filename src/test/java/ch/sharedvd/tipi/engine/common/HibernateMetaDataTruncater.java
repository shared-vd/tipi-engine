package ch.sharedvd.tipi.engine.common;

import org.hibernate.boot.spi.MetadataImplementor;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class HibernateMetaDataTruncater {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateMetaDataTruncater.class);

    private DataSource dataSource;
    private MetadataImplementor metadata;

    public HibernateMetaDataTruncater(DataSource dataSource, MetadataImplementor configuration) {
        this.dataSource = dataSource;
        this.metadata = configuration;
    }

    public void truncate() throws Exception {
        final String[] tables = getTableNames(null, false);
        deleteFromTables(tables);
    }

    private String[] getTableNames(String aSchemaName, boolean reverse) {
        final ArrayList<String> t = new ArrayList<>();
        final Collection<Table> tables = metadata.collectTableMappings();
        for (Table table : tables) {
            if (table.isPhysicalTable()) {
                addTableName(metadata, t, table, aSchemaName);
            }
        }
        if (reverse) {
            Collections.reverse(t);
        }
        return t.toArray(new String[t.size()]);
    }

    @SuppressWarnings("unchecked")
    private void addTableName(MetadataImplementor metadata, ArrayList<String> tables, Table table, String aSchemaName) {
        String name = (null == aSchemaName) ? "" : aSchemaName + ".";
        name += table.getName();
        if (tables.contains(name)) {
            return;
        }

        final Collection<Table> ts = metadata.collectTableMappings();
        for (Table t : ts) {
            if (t.equals(table)) {
                continue;
            }
            Iterator<ForeignKey> relationships = t.getForeignKeyIterator();
            while (relationships.hasNext()) {
                ForeignKey fk = relationships.next();
                if (fk.getReferencedTable().equals(table)) {
                    addTableName(metadata, tables, fk.getTable(), aSchemaName);
                }
            }
        }
        tables.add(name);
    }

    private int deleteFromTables(String... tableNames) {
        JdbcTemplate simpleJdbcTemplate = new JdbcTemplate(dataSource);
        int totalRowCount = 0;
        for (String tableName : tableNames) {
            int rowCount = simpleJdbcTemplate.update("DELETE FROM " + tableName);
            totalRowCount += rowCount;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Deleted " + rowCount + " rows from table " + tableName);
            }
        }
        return totalRowCount;
    }
}
