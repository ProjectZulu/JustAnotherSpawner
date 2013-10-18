package jas.common.spawner;

import jas.common.BiomeBlacklist;
import jas.common.JustAnotherSpawner;
import jas.common.spawner.creature.handler.LivingGroupRegistry;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.type.CreatureType;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.IScheduledTickHandler;
import cpw.mods.fml.common.TickType;

public class SpawnerTicker implements IScheduledTickHandler {

    private BiomeBlacklist blacklist;

    public SpawnerTicker(BiomeBlacklist blacklist) {
        this.blacklist = blacklist;
    }

    @Override
    public EnumSet<TickType> ticks() {
        return EnumSet.of(TickType.WORLD);
    }

    @Override
    public String getLabel() {
        return "jasSpawner";
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
        WorldServer world = (WorldServer) tickData[0];
        if (!world.getGameRules().hasRule("doCustomMobSpawning")
                || world.getGameRules().getGameRuleBooleanValue("doCustomMobSpawning")) {
            HashMap<ChunkCoordIntPair, Boolean> eligibleChunksForSpawning = CustomSpawner.determineChunksForSpawnering(
                    world, JustAnotherSpawner.globalSettings().chunkspawnDistance);

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

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {

    }

    @Override
    public int nextTickSpacing() {
        return JustAnotherSpawner.globalSettings().spawnerTickSpacing;
    }
}
