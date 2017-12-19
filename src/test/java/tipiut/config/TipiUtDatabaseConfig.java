package tipiut.config;

import ch.vd.registre.tipi.testing.TipiTestingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TipiUtDatabaseConfig {

    @Bean
    public TipiTestingService tipiTestingService() {
        return new TipiTestingService();
    }
}
