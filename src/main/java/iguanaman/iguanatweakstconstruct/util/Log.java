package iguanaman.iguanatweakstconstruct.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public final class Log {

    private Log() {} // non-instantiable

    // initialized by preinit
    private static Logger logger;

    public static void init(Logger log) {
        logger = log;
    }

    public static void log(Level level, Object obj) {
        logger.log(level, String.valueOf(obj));
    }

    public static void info(Object obj) {
        log(Level.INFO, obj);
    }

    public static void warn(Object obj) {
        log(Level.WARN, obj);
    }

    public static void error(Object obj) {
        log(Level.ERROR, obj);
    }

    public static void debug(Object obj) {
        log(Level.DEBUG, obj);
    }

    public static void trace(Object obj) {
        log(Level.TRACE, obj);
    }
}
