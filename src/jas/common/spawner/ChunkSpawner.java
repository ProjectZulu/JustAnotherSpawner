package jas.common.spawner;

import jas.common.BiomeBlacklist;
import jas.common.JustAnotherSpawner;
import jas.common.spawner.creature.handler.LivingGroupRegistry;
import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.util.Iterator;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;

public class ChunkSpawner {

    private BiomeBlacklist blacklist;

    public ChunkSpawner(BiomeBlacklist blacklist) {
        this.blacklist = blacklist;
    }

    @ForgeSubscribe
    public void performChunkSpawning(PopulateChunkEvent.Populate event) {

        /* ICE Event Type is Selected as it is Fired Immediately After Vanilla Chunk Creature Generation */
        if (event.type == PopulateChunkEvent.Populate.EventType.ICE
                && event.world.getGameRules().getGameRuleBooleanValue("doCustomMobSpawning")) {
            int k = event.chunkX * 16;
            int l = event.chunkZ * 16;
            if (JustAnotherSpawner.worldSettings() == null
                    || JustAnotherSpawner.worldSettings().livingHandlerRegistry() == null) {
                return;
            }
            CreatureTypeRegistry creatureTypeRegistry = JustAnotherSpawner.worldSettings().creatureTypeRegistry();
            Iterator<CreatureType> iterator = creatureTypeRegistry.getCreatureTypes();
            BiomeGenBase spawnBiome = event.world.getBiomeGenForCoords(k + 16, l + 16);

            if (spawnBiome == null || blacklist.isBlacklisted(spawnBiome)) {
                return;
            }

            while (iterator.hasNext()) {
                CreatureType creatureType = iterator.next();
                if (creatureType.chunkSpawning) {
                    LivingGroupRegistry livingGroupRegistry = JustAnotherSpawner.worldSettings().livingGroupRegistry();
                    CustomSpawner.performWorldGenSpawning(event.world, creatureType, livingGroupRegistry, spawnBiome,
                            k + 8, l + 8, 16, 16, event.rand);
                }
            }
        }
    }
}
