package ch.sharedvd.tipi.engine.common;


public abstract class TipiTestingConstants {

    // src/main/resources
    public static final String MODEL = "classpath:spring-tipi-model.xml";
    public static final String PERSISTENCE = "classpath:spring-tipi-persistence.xml";
    public static final String ENGINE = "classpath:spring-tipi-engine.xml";
    public static final String DEFAULT_CONFIG = "classpath:spring-tipi-default-config.xml";

    // src/test/resources/ut
    public static final String TESTING = "classpath:spring-tipi-testing.xml";

    // src/test/resources/ut
    public static final String UT_COMMON = "classpath:ut/spring-tipiut-common.xml";
    public static final String UT_DATASOURCE = "classpath:ut/spring-tipiut-datasource.xml";
    public static final String UT_ENGINE = "classpath:ut/spring-tipiut-engine.xml";

}
