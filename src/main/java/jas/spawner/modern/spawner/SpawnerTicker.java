package jas.spawner.modern.spawner;

import jas.common.global.BiomeBlacklist;
import jas.spawner.modern.MVELProfile;
import jas.spawner.modern.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.spawner.modern.spawner.creature.handler.LivingHandlerRegistry;
import jas.spawner.modern.spawner.creature.type.CreatureType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.relauncher.Side;

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
		/** Perform Spawning */
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
		MVELProfile.worldSettings().applyChanges();
	}

	private void performSpawningInWorld(WorldServer world) {
		if (!world.getGameRules().hasRule("doCustomMobSpawning")
				|| world.getGameRules().getGameRuleBooleanValue("doCustomMobSpawning")) {

			List<CreatureType> readyCreatureTypes = new ArrayList<CreatureType>();
			Iterator<CreatureType> typeIterator = MVELProfile.worldSettings().creatureTypeRegistry()
					.getCreatureTypes();
			while (typeIterator.hasNext()) {
				CreatureType creatureType = typeIterator.next();
				if (creatureType.isReady(world)) {
					readyCreatureTypes.add(creatureType);
				}
			}

			if (readyCreatureTypes.isEmpty()) {
				return;
			}
			CountInfo countInfo = CustomSpawner.spawnCounter.countEntities(world);
			for (CreatureType creatureType : readyCreatureTypes) {
				if (creatureType.isReady(world)) {
					LivingHandlerRegistry livingHandlerRegistry = MVELProfile.worldSettings()
							.livingHandlerRegistry();
					BiomeSpawnListRegistry biomeSpawnListRegistry = MVELProfile.worldSettings()
							.biomeSpawnListRegistry();
					CustomSpawner.spawnCreaturesInChunks(world, livingHandlerRegistry, biomeSpawnListRegistry,
							creatureType, blacklist, countInfo);
				}
			}
		}
	}
}
