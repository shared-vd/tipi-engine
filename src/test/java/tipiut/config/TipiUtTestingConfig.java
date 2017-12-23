package tipiut.config;

import ch.sharedvd.tipi.engine.runner.ConnectionCap;
import ch.sharedvd.tipi.engine.runner.ConnectionCapManager;
import ch.sharedvd.tipi.engine.runner.TipiStarter;
import ch.sharedvd.tipi.engine.runner.TipiStarterImpl;
import ch.sharedvd.tipi.engine.testing.TipiTestingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TipiUtTestingConfig {

    @Autowired
    private ConnectionCapManager connectionCapManager;

    @Bean
    public TipiTestingService tipiTestingService() {
        return new TipiTestingService();
    }

    @Bean
    public TipiStarter tipiStarter() {
        final TipiStarterImpl ts = new TipiStarterImpl();
        ts.setStartAtBoot(false); // Pas de AutoStart en UT
        return ts;
    }

    @Bean
    public ConnectionCap esbConnectionsCap() {
        ConnectionCap cap = new ConnectionCap();
        cap.setName("ESB");
        cap.setDescription("Limit on the ESB ActiveMQ connections");
        cap.setNbMaxConcurrent(1);
        cap.setDefault(false);
        cap.setConnectionCapManager(connectionCapManager);
        return cap;
    }

    @Bean
    public ConnectionCap wsConnectionsCap() {
        ConnectionCap cap = new ConnectionCap();
        cap.setName("WS");
        cap.setDescription("Limit on the WS connections");
        cap.setNbMaxConcurrent(1);
        cap.setDefault(false);
        cap.setConnectionCapManager(connectionCapManager);
        return cap;
    }

    @Bean
    public ConnectionCap hostDbConnectionsCap() {
        ConnectionCap cap = new ConnectionCap();
        cap.setName("MAINFRAME_DB");
        cap.setDescription("Limit on the Host DB connections");
        cap.setNbMaxConcurrent(1);
        cap.setDefault(false);
        cap.setConnectionCapManager(connectionCapManager);
        return cap;
    }
}
