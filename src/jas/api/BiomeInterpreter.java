package jas.api;

import java.util.Collection;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.SpawnListEntry;

/**
 * Used to add Support for Biome Structure Spawns. Instance must be registered in BiomeHandlerRegistry using
 * registerInterpreter(BiomeInterpreter biomeInterpreter) anytime before ServerStart. BiomeHandler and Configuration
 * will be automatically generated if registered properly.
 * 
 * Can and Should be Registered on Both Client and Server.
 */
public interface BiomeInterpreter {

    /**
     * Returns a Collection of Objects used as Keys for Determining the SpawnList to use
     * 
     * @return Collection of all Keys used for {@link getStructureSpawnList}. Return empty list if none.
     */
    public abstract Collection<String> getStructureKeys();

    /**
     * Returns Collection of Spawnable Entities for the Given Structure Key
     * 
     * @param structureKey Object Key From {@link getStructureKeys}
     * @return Collection of Valid Entity Classes. Return empty list if none.
     */
    public abstract Collection<SpawnListEntry> getStructureSpawnList(String structureKey);

    /**
     * Evaluates if Provided Location in Biome is
     * 
     * @param world World
     * @param xCoord X Coordinate Being Evaluated
     * @param yCoord Y Coordinate Being Evaluated
     * @param zCoord Z Coordinate Being Evaluated
     * @return Null if not valid structure, String Key for SpawnList if Valid
     */
    public abstract String areCoordsStructure(World world, int xCoord, int yCoord, int zCoord);

    /**
     * Checks if This Interpreter should apply to the Provided World-Biome. Called to determine if areCoordsStrucutre
     * should be called.
     * 
     * @param biomeGenBase Biome to Check
     * @return True if Interpreter should apply to biome
     */
    public abstract boolean shouldUseHandler(World world, BiomeGenBase biomeGenBase);
}
