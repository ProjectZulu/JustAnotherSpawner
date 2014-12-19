package jas.legacy.spawner;

import jas.legacy.spawner.CustomSpawner.ChunkStat;
import jas.legacy.spawner.creature.handler.LivingGroupRegistry;
import jas.legacy.spawner.creature.handler.LivingHandlerRegistry;
import jas.legacy.spawner.creature.type.CreatureType;
import jas.modern.BiomeBlacklist;
import jas.modern.JustAnotherSpawner;
import jas.modern.profile.TAGProfile;

import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.relauncher.Side;

public class SpawnerTicker {

    private BiomeBlacklist blacklist;

    public SpawnerTicker(BiomeBlacklist blacklist) {
        this.blacklist = blacklist;
    }

    @SubscribeEvent
    public void tickStart(WorldTickEvent event) {
        if (event.side != Side.SERVER || event.phase != Phase.END) {
            return;
        }
        WorldServer world = (WorldServer) event.world;
        if (!world.getGameRules().hasRule("doCustomMobSpawning")
                || world.getGameRules().getGameRuleBooleanValue("doCustomMobSpawning")) {
            HashMap<ChunkCoordIntPair, ChunkStat> eligibleChunksForSpawning = CustomSpawner
                    .determineChunksForSpawnering(world, JustAnotherSpawner.globalSettings().chunkSpawnDistance);

            EntityCounter creatureTypeCount = new EntityCounter();
            EntityCounter creatureCount = new EntityCounter();
            CustomSpawner.countEntityInChunks(world, creatureTypeCount, creatureCount);

            Iterator<CreatureType> typeIterator = TAGProfile.worldSettings().creatureTypeRegistry()
                    .getCreatureTypes();
            while (typeIterator.hasNext()) {
                CreatureType creatureType = typeIterator.next();
                if (creatureType.isReady(world)) {
                    LivingHandlerRegistry livingHandlerRegistry = TAGProfile.worldSettings()
                            .livingHandlerRegistry();
                    LivingGroupRegistry livingGroupRegistry = TAGProfile.worldSettings().livingGroupRegistry();
                    CustomSpawner.spawnCreaturesInChunks(world, livingHandlerRegistry, livingGroupRegistry,
                            creatureType, eligibleChunksForSpawning, creatureTypeCount, creatureCount, blacklist);
                }
            }
        }
    }
}
