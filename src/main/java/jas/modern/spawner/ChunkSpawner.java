package jas.modern.spawner;

import jas.modern.BiomeBlacklist;
import jas.modern.JustAnotherSpawner;
import jas.modern.profile.MVELProfile;
import jas.modern.spawner.creature.handler.LivingHandlerRegistry;
import jas.modern.spawner.creature.type.CreatureType;
import jas.modern.spawner.creature.type.CreatureTypeRegistry;

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
            if (MVELProfile.worldSettings() == null
                    || MVELProfile.worldSettings().livingHandlerRegistry() == null) {
                return;
            }
            CreatureTypeRegistry creatureTypeRegistry = MVELProfile.worldSettings().creatureTypeRegistry();
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
                    LivingHandlerRegistry livingHandlerRegistry = MVELProfile.worldSettings().livingHandlerRegistry();
                    CustomSpawner.performWorldGenSpawning(event.world, creatureType, livingHandlerRegistry, spawnBiome,
                            k + 8, l + 8, 16, 16, event.rand);
                }
            }
        }
    }
}
