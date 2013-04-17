package jas.common.spawner.creature.type;

import net.minecraft.block.material.Material;
import net.minecraft.world.WorldServer;

public class CreatureTypeMonster extends CreatureType {

    public CreatureTypeMonster(String typeID, int maxNumberOfCreature, Material spawnMedium, int spawnRate,
            boolean chunkSpawning) {
        super(typeID, maxNumberOfCreature, spawnMedium, spawnRate, chunkSpawning);
    }

    @Override
    protected CreatureType constructInstance(String typeID, int maxNumberOfCreature, Material spawnMedium,
            int spawnRate, boolean chunkSpawning) {
        return new CreatureTypeMonster(typeID, maxNumberOfCreature, spawnMedium, spawnRate, chunkSpawning);
    }
    
    @Override
    public boolean isReady(WorldServer world) {
        return world.difficultySetting != 0 && super.isReady(world);
    }
}
