package tipiut.config;

import ch.sharedvd.tipi.engine.runner.TipiStarter;
import ch.sharedvd.tipi.engine.runner.TipiStarterImpl;
import ch.sharedvd.tipi.engine.testing.TipiTestingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TipiUtDatabaseConfig {

    @Bean
    public TipiTestingService tipiTestingService() {
        return new TipiTestingService();
    }

    @Bean
    public TipiStarter tipiStarter() {
        final TipiStarterImpl ts = new TipiStarterImpl();
        ts.setAutostart(false); // Pas de AutoStart en UT
        return ts;
    }
}
