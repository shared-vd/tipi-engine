package ch.vd.registre.testing;


import ch.vd.registre.testing.db.DatabaseTruncateHelper;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import javax.sql.DataSource;

public abstract class AbstractDatabaseTruncatingTest extends AbstractTxManagerTest {

    protected String getSessionFactoryName() {
        return "sessionFactory";
    }

    protected DataSource getDataSource() {
        return (DataSource) getHibernateConfiguration().getProperties().get(Environment.DATASOURCE);
    }

    protected Configuration getHibernateConfiguration() {
        final LocalSessionFactoryBean localSessionFactoryBean = getBean(LocalSessionFactoryBean.class, "&" + getSessionFactoryName());
        return localSessionFactoryBean.getConfiguration();
    }

    @Override
    protected void truncateDatabase() throws Exception {
        super.truncateDatabase();

        DatabaseTruncateHelper.truncateDatabase(getDataSource(), getHibernateConfiguration());
    }

}
