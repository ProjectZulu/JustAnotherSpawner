package jas.spawner.refactor;

import jas.common.global.BiomeBlacklist;
import jas.common.helper.MVELHelper;
import jas.spawner.modern.spawner.CountInfo;
import jas.spawner.refactor.LivingTypeBuilder.LivingType;

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
		// MVELProfile.worldSettings().applyChanges();
	}

	private void performSpawningInWorld(WorldServer world) {
		if (!world.getGameRules().hasRule("doCustomMobSpawning")
				|| world.getGameRules().getGameRuleBooleanValue("doCustomMobSpawning")) {

			List<LivingType> readyLivingTypes = new ArrayList<LivingType>();
			Iterator<LivingType> typeIterator = ExperimentalProfile.worldSettings().getSpawnSettings(world)
					.livingTypes().types().values().iterator();
			while (typeIterator.hasNext()) {
				LivingType livingType = typeIterator.next();
				Object context = null; // creatureType.isReady(world)
				if (MVELHelper.executeExpression(livingType.isReadyToChnk.compiled.get(), context,
						"Error processing isReady compiled expression for " + livingType.livingTypeID + ": "
								+ livingType.isReadyToChnk.expression)) {
					readyLivingTypes.add(livingType);
				}
			}

			if (readyLivingTypes.isEmpty()) {
				return;
			}
			CountInfo countInfo = SpawnerLogic.counter.countEntities(world);
			for (LivingType livingType : readyLivingTypes) {
				// Spawn entities

				// LivingHandlerRegistry livingHandlerRegistry =
				// MVELProfile.worldSettings().livingHandlerRegistry();
				// BiomeSpawnListRegistry biomeSpawnListRegistry = MVELProfile.worldSettings()
				// .biomeSpawnListRegistry();
				// CustomSpawner.spawnCreaturesInChunks(world, livingHandlerRegistry, biomeSpawnListRegistry,
				// creatureType, blacklist, countInfo);
			}
		}
	}

}
