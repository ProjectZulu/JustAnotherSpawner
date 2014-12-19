package jas.legacy.spawner;

import jas.legacy.spawner.creature.handler.LivingGroupRegistry;
import jas.legacy.spawner.creature.type.CreatureType;
import jas.legacy.spawner.creature.type.CreatureTypeRegistry;
import jas.modern.BiomeBlacklist;
import jas.modern.JustAnotherSpawner;
import jas.modern.profile.TAGProfile;

import java.util.Iterator;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

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
            if (TAGProfile.worldSettings() == null
                    || TAGProfile.worldSettings().livingHandlerRegistry() == null) {
                return;
            }
            CreatureTypeRegistry creatureTypeRegistry = TAGProfile.worldSettings().creatureTypeRegistry();
            Iterator<CreatureType> iterator = creatureTypeRegistry.getCreatureTypes();
            BiomeGenBase spawnBiome = event.world.getBiomeGenForCoords(k + 16, l + 16);

            if (spawnBiome == null || blacklist.isBlacklisted(spawnBiome)) {
                return;
            }
            if (JustAnotherSpawner.globalSettings().disabledVanillaChunkSpawning) {
                event.setResult(Result.DENY);
            }
            while (iterator.hasNext()) {
                CreatureType creatureType = iterator.next();
                if (creatureType.chunkSpawnChance > 0.0f) {
                    LivingGroupRegistry livingGroupRegistry = TAGProfile.worldSettings().livingGroupRegistry();
                    CustomSpawner.performWorldGenSpawning(event.world, creatureType, livingGroupRegistry, spawnBiome,
                            k + 8, l + 8, 16, 16, event.rand);
                }
            }
        }
    }
}
