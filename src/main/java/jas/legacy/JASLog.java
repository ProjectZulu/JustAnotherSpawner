package jas.legacy;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class JASLog {
    private static Logger myLog;
    private static JASLog jasLog;
    private static boolean isSetup;

    public static JASLog log() {
        return jasLog;
    }

    private class LogType {
        private final boolean isEnabled;

        public LogType() {
            this.isEnabled = false;
        }

        public LogType(boolean isEnabled) {
            this.isEnabled = isEnabled;
        }

        public boolean isEnabled() {
            return isEnabled;
        }
    }

    public final String FILE_VERSION = "1.0";
    private final LogType SPAWNING;
    private final LogType DEBUG;
    private final LogType SPAWNING_NAME;
    private final LogType SPAWNING_TYPE;
    private final LogType SPAWNING_POS;
    private final LogType SPAWNING_BIOME;

    public JASLog() {
        SPAWNING = new LogType(true);
        DEBUG = new LogType();
        SPAWNING_NAME = new LogType(true);
        SPAWNING_TYPE = new LogType(true);
        SPAWNING_POS = new LogType(true);
        SPAWNING_BIOME = new LogType(true);
    }

    public static void setLogger(JASLog log) {
        if (!isSetup) {
            JASLog.isSetup = true;
            JASLog.jasLog = log;
            JASLog.myLog = LogManager.getLogger("JustAnotherSpawner");
        }
    }

    public void log(Level level, String format, Object... data) {
        myLog.log(level, String.format(format, data));
    }

    public void info(String format, Object... data) {
        log(Level.INFO, format, data);
    }

    public void warning(String format, Object... data) {
        log(Level.WARN, format, data);
    }

    public void severe(String format, Object... data) {
        log(Level.ERROR, format, data);
    }

    public void debug(Level level, String format, Object... data) {
        if (DEBUG.isEnabled()) {
            log(level, format, data);
        }
    }

    public void log(LogType type, Level level, String format, Object... data) {
        if (type.isEnabled()) {
            log(level, format, data);
        }
    }

    public void logSpawn(boolean chunkSpawn, String entityName, String creatureType, int xCoord, int yCoord,
            int zCoord, String biomeName) {
        if (SPAWNING.isEnabled()) {
            StringBuilder sb = new StringBuilder(90);

            sb.append(chunkSpawn ? "Chunk spawning entity" : "Passive Spawning entity");

            if (SPAWNING_NAME.isEnabled()) {
                sb.append(" ").append(entityName);
            }

            if (SPAWNING_TYPE.isEnabled()) {
                sb.append(" of type ").append(creatureType);
            }

            if (SPAWNING_POS.isEnabled()) {
                sb.append(" at ").append(xCoord).append(", ").append(yCoord).append(", ").append(zCoord);
            }

            if (SPAWNING_BIOME.isEnabled()) {
                sb.append(" (").append(biomeName).append(")");
            }

            log(SPAWNING, Level.INFO, sb.toString());
        }
    }
}
