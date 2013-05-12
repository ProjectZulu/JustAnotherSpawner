package jas.common.spawner.creature.type;

import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class CreatureTypeOpensky extends CreatureType {
    public CreatureTypeOpensky(String typeID, int maxNumberOfCreature, Material spawnMedium, int spawnRate,
            boolean chunkSpawning, String optionalParameters) {
        super(typeID, maxNumberOfCreature, spawnMedium, spawnRate, chunkSpawning, optionalParameters);
    }

    @Override
    protected CreatureType constructInstance(String typeID, int maxNumberOfCreature, Material spawnMedium,
            int spawnRate, boolean chunkSpawning, String optionalParameters) {
        return new CreatureTypeOpensky(typeID, maxNumberOfCreature, spawnMedium, spawnRate, chunkSpawning,
                optionalParameters);
    }

    @Override
    public boolean canSpawnAtLocation(World world, int xCoord, int yCoord, int zCoord) {
        if (!world.canBlockSeeTheSky(xCoord, yCoord, zCoord)) {
            return false;
        }
        return super.canSpawnAtLocation(world, xCoord, yCoord, zCoord);
    }
}
