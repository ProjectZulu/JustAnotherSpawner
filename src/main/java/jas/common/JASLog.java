package jas.common;

import java.util.ArrayList;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.world.ChunkPosition;

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

        private LogType(boolean isEnabled) {
            this.isEnabled = isEnabled;
        }

        public boolean isEnabled() {
            return isEnabled;
        }
    }

    public final String FILE_VERSION = "1.0";
    private final LogType SPAWNING;
    private final LogType DEBUG;
    private final LogType SETUP_SPAWNLISTENTRY;
    private final LogType SPAWNING_NAME;
    private final LogType SPAWNING_TYPE;
    private final LogType SPAWNING_POS;
    private final LogType SPAWNING_BIOME;
    private final LogType SPAWNING_NEARBY_BLOCKS;

    public JASLog() {
        SPAWNING = new LogType(true);
        DEBUG = new LogType();
        SPAWNING_NAME = new LogType(true);
        SPAWNING_TYPE = new LogType(true);
        SPAWNING_POS = new LogType(true);
        SPAWNING_BIOME = new LogType(true);
        SETUP_SPAWNLISTENTRY = new LogType(true);
        SPAWNING_NEARBY_BLOCKS = new LogType(false);
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
    
	public void logSpawnListEntry(String livingGroupID, String LocationID, boolean success, String detail) {
		if (SETUP_SPAWNLISTENTRY.isEnabled) {
			if (success) {
				JASLog.log().info("Adding SpawnListEntry %s to BiomeGroup %s %s", livingGroupID, detail, LocationID);
			} else {
				JASLog.log().debug(Level.INFO, "Not adding generated SpawnListEntry of %s to %s %s",
						livingGroupID, LocationID, detail);
			}
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
    
    // Accessor created due to performance cost of checking nearby blocks if logging is not required
    public boolean isLogNearbyBlocksEnabled(){
    	return SPAWNING_NEARBY_BLOCKS.isEnabled;
    }
      
    public void logSpawn(boolean chunkSpawn, String entityName, String creatureType, int xCoord, int yCoord,
            int zCoord, String biomeName, ArrayList<Integer> nearbyX, ArrayList<Integer> nearbyY, ArrayList<Integer> nearbyZ, ArrayList<String> nearbyBlocks) {
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
            if(SPAWNING_NEARBY_BLOCKS.isEnabled) {
                if(nearbyX.size() != nearbyY.size() || nearbyY.size() != nearbyZ.size() || nearbyZ.size() != nearbyBlocks.size()) {
                    jasLog.severe("Error writing nearbyBlocks to log, unequal data [%s, %s, %s, %s]", nearbyX.size(), nearbyY.size(), nearbyZ.size(), nearbyBlocks.size());
                } else {
                    sb.append(": Nearby blocks were");
                    for (int i = 0; i < nearbyBlocks.size(); i++) {
                        if (i!=0) {
                            sb.append(",");
                        }
                        sb.append(" [");
                        sb.append(nearbyX.get(i)).append(", ");
                        sb.append(nearbyY.get(i)).append(", ");
                        sb.append(nearbyZ.get(i)).append(", ");
                        sb.append(nearbyBlocks.get(i)).append("]");
                    }
                }
            }
            log(SPAWNING, Level.INFO, sb.toString());
        }
    }
}
