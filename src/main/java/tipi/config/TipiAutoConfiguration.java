package tipi.config;

import ch.sharedvd.tipi.engine.command.CommandConsumer;
import ch.sharedvd.tipi.engine.engine.*;
import ch.sharedvd.tipi.engine.model.DbActivity;
import ch.sharedvd.tipi.engine.repository.ActivityRepository;
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
    public ActivityServiceImpl activityService() {
        return new ActivityServiceImpl();
    }

    @Bean
    public ConnectionCapManager connectionCapManager() {
        return new ConnectionCapManager();
    }
}
