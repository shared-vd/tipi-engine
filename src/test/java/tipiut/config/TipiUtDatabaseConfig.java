package tipiut.config;

import ch.sharedvd.tipi.engine.tx.TxTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class TipiUtDatabaseConfig {

    @Bean
    public TxTemplate txTemplate(PlatformTransactionManager ptm) {
        return new TxTemplate(ptm);
    }
}
