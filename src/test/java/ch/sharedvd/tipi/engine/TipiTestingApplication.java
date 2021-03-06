package ch.sharedvd.tipi.engine;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import tipi.config.TipiAutoConfiguration;

@SpringBootApplication
@Import({
        TipiAutoConfiguration.class
})
public class TipiTestingApplication {
}
