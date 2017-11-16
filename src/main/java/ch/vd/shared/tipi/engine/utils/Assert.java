package ch.vd.shared.tipi.engine.utils;

import org.apache.commons.lang3.StringUtils;

public class Assert extends org.springframework.util.Assert {


    public static void isEqual(Object expected, Object actual) {
        isEqual(expected, actual, "");
    }
    public static void isEqual(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            String m = "Expected:" + expected + " but was " + actual;
            if (StringUtils.isNotBlank(message)) {
                m = message + " : "+m;
            }
            fail(m);
        }
    }

    public static void fail(String msg) {
        isTrue(false, msg);
    }

    public static void isFalse(boolean expr, String msg) {
        isTrue(!expr, msg);
    }
}
