package jas.common.spawner;

import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.util.Iterator;

import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;

public class ChunkSpawner {
    @ForgeSubscribe
    public void performChunkSpawning(PopulateChunkEvent.Populate event) {

        /* ICE Event Type is Selected as it is Fired Immediately After Vanilla Chunk Creature Generation */
        if (event.type == PopulateChunkEvent.Populate.EventType.ICE
                && event.world.getGameRules().getGameRuleBooleanValue("doCustomMobSpawning")) {
            int k = event.chunkX * 16;
            int l = event.chunkZ * 16;
            Iterator<CreatureType> iterator = CreatureTypeRegistry.INSTANCE.getCreatureTypes();

            while (iterator.hasNext()) {
                CreatureType creatureType = iterator.next();
                if (creatureType.chunkSpawning) {
                    CustomSpawner.performWorldGenSpawning(event.world, creatureType,
                            event.world.getBiomeGenForCoords(k + 16, l + 16), k + 8, l + 8, 16, 16, event.world.rand);
                }
            }
        }
    }
}
