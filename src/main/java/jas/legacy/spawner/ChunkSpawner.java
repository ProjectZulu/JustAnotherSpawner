package jas.legacy.spawner;

import jas.legacy.BiomeBlacklist;
import jas.legacy.LegacyJustAnotherSpawner;
import jas.legacy.spawner.creature.handler.LivingGroupRegistry;
import jas.legacy.spawner.creature.type.CreatureType;
import jas.legacy.spawner.creature.type.CreatureTypeRegistry;

import java.util.Iterator;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;

public class ChunkSpawner {

    private BiomeBlacklist blacklist;

    public ChunkSpawner(BiomeBlacklist blacklist) {
        this.blacklist = blacklist;
    }

    @SubscribeEvent
    public void performChunkSpawning(PopulateChunkEvent.Populate event) {

        /* ICE Event Type is Selected as it is Fired Immediately After Vanilla Chunk Creature Generation */
        if (event.type == PopulateChunkEvent.Populate.EventType.ANIMALS
                && event.world.getGameRules().getGameRuleBooleanValue("doCustomMobSpawning")) {
            int k = event.chunkX * 16;
            int l = event.chunkZ * 16;
            if (LegacyJustAnotherSpawner.worldSettings() == null
                    || LegacyJustAnotherSpawner.worldSettings().livingHandlerRegistry() == null) {
                return;
            }
            CreatureTypeRegistry creatureTypeRegistry = LegacyJustAnotherSpawner.worldSettings().creatureTypeRegistry();
            Iterator<CreatureType> iterator = creatureTypeRegistry.getCreatureTypes();
            BiomeGenBase spawnBiome = event.world.getBiomeGenForCoords(k + 16, l + 16);

            if (spawnBiome == null || blacklist.isBlacklisted(spawnBiome)) {
                return;
            }
            if (LegacyJustAnotherSpawner.globalSettings().disabledVanillaChunkSpawning) {
                event.setResult(Result.DENY);
            }
            while (iterator.hasNext()) {
                CreatureType creatureType = iterator.next();
                if (creatureType.chunkSpawnChance > 0.0f) {
                    LivingGroupRegistry livingGroupRegistry = LegacyJustAnotherSpawner.worldSettings().livingGroupRegistry();
                    CustomSpawner.performWorldGenSpawning(event.world, creatureType, livingGroupRegistry, spawnBiome,
                            k + 8, l + 8, 16, 16, event.rand);
                }
            }
        }
    }
}
