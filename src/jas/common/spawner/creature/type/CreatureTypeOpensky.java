package jas.common.spawner.creature.type;

import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class CreatureTypeOpensky extends CreatureType {

    public CreatureTypeOpensky(String typeID, int maxNumberOfCreature, Material spawnMedium, int spawnRate,
            boolean chunkSpawning) {
        super(typeID, maxNumberOfCreature, spawnMedium, spawnRate, chunkSpawning);
    }

    @Override
    protected CreatureType constructInstance(String typeID, int maxNumberOfCreature, Material spawnMedium,
            int spawnRate, boolean chunkSpawning) {
        return new CreatureTypeOpensky(typeID, maxNumberOfCreature, spawnMedium, spawnRate, chunkSpawning);
    }

    @Override
    public boolean canSpawnAtLocation(World world, int xCoord, int yCoord, int zCoord) {
        if (!world.canBlockSeeTheSky(xCoord, yCoord, zCoord)) {
            return false;
        }
        return super.canSpawnAtLocation(world, xCoord, yCoord, zCoord);
    }
}
