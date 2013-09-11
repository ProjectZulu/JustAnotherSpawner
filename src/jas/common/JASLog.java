package jas.common;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraftforge.common.Configuration;

public class JASLog {
    private static Logger myLog;
    private static boolean isSetup;

    // private static boolean isDebug;

    public enum LogType {
        SPAWNING(true), DEBUG(), SPAWNING_NAME(true), SPAWNING_TYPE(true), SPAWNING_POS(true), SPAWNING_BIOME(true);
        public final boolean defaultEnabled;
        private boolean isEnabled = false;

        public void setEnabled(boolean isEnabled) {
            this.isEnabled = isEnabled;
        }

        LogType() {
            defaultEnabled = false;
        }

        LogType(boolean defaultEnabled) {
            this.defaultEnabled = defaultEnabled;
        }

        public static boolean isEnabled(LogType logType) {
            return logType.isEnabled ? true : LogType.DEBUG.isEnabled;
        }
    }

    public static void configureLogging(File configDirectory) {
        if (!isSetup) {
            isSetup = true;
            myLog = Logger.getLogger("JAS");
            myLog.setParent(Logger.getLogger("ForgeModLoader"));
            Configuration config = new Configuration(new File(configDirectory, DefaultProps.MODDIR
                    + "GlobalProperties.cfg"));
            config.load();
            for (LogType type : LogType.values()) {
                if (type == LogType.DEBUG) {
                    type.setEnabled(config.get("Properties.Logging", type.toString() + " Logging", type.defaultEnabled,
                            "Master Switch For All Debug Printing").getBoolean(type.defaultEnabled));
                } else {
                    type.setEnabled(config.get("Properties.Logging", type.toString() + " Logging", type.defaultEnabled,
                            "Enables " + type + " Logging").getBoolean(type.defaultEnabled));
                }
            }
            config.save();
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
        if (LogType.isEnabled(LogType.DEBUG)) {
            log(level, format, data);
        }
    }

    public static void log(LogType type, Level level, String format, Object... data) {
        if (LogType.isEnabled(type)) {
            log(level, format, data);
        }
    }

    public static void logSpawn(boolean chunkSpawn, String entityName, String creatureType, int xCoord, int yCoord,
            int zCoord, String biomeName) {
        if (LogType.isEnabled(LogType.SPAWNING)) {
            StringBuilder sb = new StringBuilder(90);

            sb.append(chunkSpawn ? "Chunk spawning entity" : "Passive Spawning entity");

            if (LogType.isEnabled(LogType.SPAWNING_NAME)) {
                sb.append(" ").append(entityName);
            }

            if (LogType.isEnabled(LogType.SPAWNING_TYPE)) {
                sb.append(" of type ").append(creatureType);
            }

            if (LogType.isEnabled(LogType.SPAWNING_POS)) {
                sb.append(" at ").append(xCoord).append(", ").append(yCoord).append(", ").append(zCoord);
            }

            if (LogType.isEnabled(LogType.SPAWNING_BIOME)) {
                sb.append(" (").append(biomeName).append(")");
            }

            JASLog.log(LogType.SPAWNING, Level.INFO, sb.toString());
        }
    }
}
