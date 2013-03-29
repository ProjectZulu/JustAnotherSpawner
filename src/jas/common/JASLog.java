package jas.common;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JASLog {
    private static Logger myLog;
    private static boolean isSetup;
    private static boolean isDebug;

    public static void configureLogging(boolean isDebug) {
        if (!isSetup) {
            isSetup = true;
            myLog = Logger.getLogger("JAS");
            myLog.setParent(Logger.getLogger("ForgeModLoader"));
            JASLog.isDebug = isDebug;
        }
    }

    public static void log(Level level, String format, Object... data) {
        myLog.log(level, String.format(format, data));
    }

    public static void info(String format, Object... data) {
        log(Level.INFO, format, data);
    }

    public static void warning(String format, Object... data) {
        log(Level.WARNING, format, data);
    }
    
    public static void severe(String format, Object... data) {
        log(Level.SEVERE, format, data);
    }
    
    public static void debug(Level level, String format, Object... data) {
        if (isDebug) {
            log(level, format, data);
        }
    }
}
