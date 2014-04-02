package jas.common.spawner;

import jas.common.BiomeBlacklist;
import jas.common.JustAnotherSpawner;
import jas.common.spawner.CustomSpawner.ChunkStat;
import jas.common.spawner.creature.handler.LivingGroupRegistry;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.type.CreatureType;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;

public class SpawnerTicker {

    private BiomeBlacklist blacklist;

    public SpawnerTicker(BiomeBlacklist blacklist) {
        this.blacklist = blacklist;
    }

    @SubscribeEvent
    public void tickStart(WorldTickEvent event) {
        WorldServer world = (WorldServer) event.world;
        if (!world.getGameRules().hasRule("doCustomMobSpawning")
                || world.getGameRules().getGameRuleBooleanValue("doCustomMobSpawning")) {
            HashMap<ChunkCoordIntPair, ChunkStat> eligibleChunksForSpawning = CustomSpawner
                    .determineChunksForSpawnering(world, JustAnotherSpawner.globalSettings().chunkSpawnDistance);

            EntityCounter creatureTypeCount = new EntityCounter();
            EntityCounter creatureCount = new EntityCounter();
            CustomSpawner.countEntityInChunks(world, creatureTypeCount, creatureCount);

            Iterator<CreatureType> typeIterator = JustAnotherSpawner.worldSettings().creatureTypeRegistry()
                    .getCreatureTypes();
            while (typeIterator.hasNext()) {
                CreatureType creatureType = typeIterator.next();
                if (creatureType.isReady(world)) {
                    LivingHandlerRegistry livingHandlerRegistry = JustAnotherSpawner.worldSettings()
                            .livingHandlerRegistry();
                    LivingGroupRegistry livingGroupRegistry = JustAnotherSpawner.worldSettings().livingGroupRegistry();
                    CustomSpawner.spawnCreaturesInChunks(world, livingHandlerRegistry, livingGroupRegistry,
                            creatureType, eligibleChunksForSpawning, creatureTypeCount, creatureCount, blacklist);
                }
            }
        }
    }
}
