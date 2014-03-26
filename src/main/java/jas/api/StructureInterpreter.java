package jas.api;

import java.util.Collection;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.SpawnListEntry;

/**
 * Used to add Support for Biome Structure Spawns. Instance must be registered by subscribing to the
 * {@link CompatibilityRegistrationEvent}. StructureHandler and Configuration will be automatically generated if registered
 * properly.
 */
public interface StructureInterpreter {

    /**
     * Returns a Collection of Objects used as Keys for representing seperate spawnlists.
     * 
     * This function is used to obtain all valid keys. It should not change after startup or between calls.
     * 
     * @return Collection of all Keys used for {@link getStructureSpawnList}. Return empty list if none.
     */
    public abstract Collection<String> getStructureKeys();

    /**
     * Functions as default map between SpawnLists and Structure Keys from {@link #getStructureKeys}.
     * 
     * Note the function is called obtain default spawnlist which are later customized via user input.
     * 
     * @param structureKey Object Key From {@link getStructureKeys}
     * @return Collection of Valid Entity Classes. Return empty list if none.
     */
    public abstract Collection<SpawnListEntry> getStructureSpawnList(String structureKey);

    /**
     * Evaluates if provided location contains Structure. If valid, should return a valid String key
     * 
     * Returned string key should be one returned via {@link getStructureKeys}.
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
