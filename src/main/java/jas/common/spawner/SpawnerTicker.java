package jas.common.spawner;

import jas.common.BiomeBlacklist;
import jas.common.JustAnotherSpawner;
import jas.common.spawner.CustomSpawner.ChunkStat;
import jas.common.spawner.creature.handler.LivingGroupRegistry;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.type.CreatureType;

import java.util.HashMap;
import java.util.Iterator;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.relauncher.Side;

public class SpawnerTicker {

    private BiomeBlacklist blacklist;

    public SpawnerTicker(BiomeBlacklist blacklist) {
        this.blacklist = blacklist;
    }

    @SubscribeEvent
	public void serverTick(ServerTickEvent event) {
		if (event.side != Side.SERVER || event.phase == Phase.END) {
			return;
		}
		MinecraftServer server = MinecraftServer.getServer();
		/** Perform Spawning*/
		Integer[] ids = DimensionManager.getIDs(server.getTickCounter() % 200 == 0);
		for (int x = 0; x < ids.length; x++) {
            int id = ids[x];
            long j = System.nanoTime();
            if (id == 0 || server.getAllowNether()) {
                WorldServer worldserver = DimensionManager.getWorld(id);
                performSpawningInWorld(worldserver);
            }
		}
		/** TODO: Perform Updates to WorldSettings Here */
		JustAnotherSpawner.worldSettings().applyChanges();
	}
    
	private void performSpawningInWorld(WorldServer world) {
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
