package tipi.config;

import ch.sharedvd.tipi.engine.command.CommandConsumer;
import ch.sharedvd.tipi.engine.command.CommandHelperService;
import ch.sharedvd.tipi.engine.command.CommandService;
import ch.sharedvd.tipi.engine.command.CommandServiceImpl;
import ch.sharedvd.tipi.engine.engine.*;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.repository.ActivityRepository;
import ch.sharedvd.tipi.engine.svc.ActivityPersistenceService;
import ch.sharedvd.tipi.engine.utils.BeanAutowirer;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan("ch.sharedvd.tipi.engine")
@EntityScan(basePackageClasses = DbActivity.class)
@EnableJpaRepositories(basePackageClasses = ActivityRepository.class)
public class TipiAutoConfiguration {

    @Bean
    public ConnectionCap defaultCap() {
        ConnectionCap cap = new ConnectionCap();
        cap.setName("DataSource");
        cap.setDescription("Limit on the datasource connections");
        cap.setNbMaxConcurrent(10);
        cap.setDefault(true);
        cap.setManager(connectionCapManager());
        return cap;
    }

    @Bean
    public CommandHelperService commandHelperService() {
        return new CommandHelperService();
    }

    @Bean
    public TipiStarter tipiStarter() {
        return new TipiStarterImpl();
    }

    @Bean
    public BeanAutowirer beanAutowirer() {
        return new BeanAutowirer();
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
    public ActivityServiceImpl activityService() {
        return new ActivityServiceImpl();
    }

    @Bean
    public ActivityPersistenceService activityPersistenceService() {
        return new ActivityPersistenceService();
    }

    @Bean
    public ConnectionCapManager connectionCapManager() {
        return new ConnectionCapManager();
    }
}
