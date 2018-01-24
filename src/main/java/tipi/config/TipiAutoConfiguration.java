package tipi.config;

import ch.sharedvd.tipi.engine.client.TipiFacade;
import ch.sharedvd.tipi.engine.client.TipiFacadeImpl;
import ch.sharedvd.tipi.engine.command.CommandConsumer;
import ch.sharedvd.tipi.engine.command.CommandHelperService;
import ch.sharedvd.tipi.engine.command.CommandService;
import ch.sharedvd.tipi.engine.command.CommandServiceImpl;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.query.ActivityQueryService;
import ch.sharedvd.tipi.engine.query.TipiQueryFacade;
import ch.sharedvd.tipi.engine.query.TipiQueryFacadeImpl;
import ch.sharedvd.tipi.engine.repository.ActivityRepository;
import ch.sharedvd.tipi.engine.runner.*;
import ch.sharedvd.tipi.engine.svc.ActivityPersisterService;
import ch.sharedvd.tipi.engine.utils.BeanAutowirer;
import ch.sharedvd.tipi.engine.utils.TixTemplate;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ComponentScan("ch.sharedvd.tipi.engine")
@EntityScan(basePackageClasses = DbActivity.class)
@EnableJpaRepositories(basePackageClasses = ActivityRepository.class)
public class TipiAutoConfiguration {

    @Bean
    public TixTemplate tixTemplate(PlatformTransactionManager ptm) {
        return new TixTemplate(ptm);
    }

    @Bean
    public BeanAutowirer beanAutowirer() {
        return new BeanAutowirer();
    }

    @Bean
    public TipiStarter tipiStarter() {
        return new TipiStarterImpl();
    }

    @Bean
    public TipiQueryFacade tipiQueryFacade() {
        return new TipiQueryFacadeImpl();
    }
    @Bean
    public TipiFacade tipiFacade() {
        return new TipiFacadeImpl();
    }

    @Bean
    public ActivityRunningService activityService() {
        return new ActivityRunningService();
    }

    @Bean
    public ActivityPersisterService activityPersistenceService() {
        return new ActivityPersisterService();
    }
    @Bean
    public ActivityQueryService activityQueryService() {
        return new ActivityQueryService();
    }

    @Bean
    public CommandHelperService commandHelperService() {
        return new CommandHelperService();
    }

    @Bean
    public TopProcessGroupManager activityGroupManager() {
        return new TopProcessGroupManager();
    }

    @Bean
    public CommandConsumer commandConsumer() {
        return new CommandConsumer();
    }
    @Bean
    public CommandService commandService() {
        return new CommandServiceImpl();
    }

    @Bean
    public ConnectionCapManager connectionCapManager() {
        return new ConnectionCapManager();
    }

    @Bean
    public ConnectionCap defaultCap() {
        ConnectionCap cap = new ConnectionCap();
        cap.setName("DataSource");
        cap.setDescription("Limit on the datasource connections");
        cap.setNbMaxConcurrent(10);
        cap.setDefault(true);
        cap.setConnectionCapManager(connectionCapManager());
        return cap;
    }
}
