package jas.spawner.refactor;

import jas.common.global.BiomeBlacklist;
import jas.common.helper.MVELHelper;
import jas.spawner.modern.spawner.CountInfo;
import jas.spawner.refactor.LivingTypeBuilder.LivingType;
import jas.spawner.refactor.LivingTypeSpawnTriggerBuilder.SpawnProcess;
import jas.spawner.refactor.LivingTypeSpawnTriggerBuilder.LivingTypeSpawnTrigger;
import jas.spawner.refactor.LivingTypeSpawnTriggerBuilder.SPAWNER;
import jas.spawner.refactor.LivingTypeSpawnTriggerBuilder.TRIGGER;
import jas.spawner.refactor.SpawnerHelper.Counter;
import jas.spawner.refactor.SpawnerHelper.SpawnerLogic;
import jas.spawner.refactor.mvel.MVELExpression;
import jas.spawner.refactor.spawning.WorldSpawningLogic;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.relauncher.Side;

public class SpawnerTicker {
	private BiomeBlacklist blacklist;
	private EnumMap<SPAWNER, SpawnerLogic> spawners;

	public SpawnerTicker(BiomeBlacklist blacklist) {
		this.blacklist = blacklist;
		this.spawners = new EnumMap(SPAWNER.class);
		for (SPAWNER spawner : SPAWNER.values()) {
			switch (spawner) {
			case CHUNK:
				spawners.put(SPAWNER.CHUNK, new WorldSpawningLogic());
				break;
			case PLAYER:
				spawners.put(SPAWNER.PLAYER, new WorldSpawningLogic());
				break;
			case WORLD:
				spawners.put(SPAWNER.WORLD, new WorldSpawningLogic());
				break;
			}
		}
	}

	// Dimension, LivingType to ActiveSpawn
	// What if multiple chunk spawns happen? i.e. 2 Chunk Spawns + Block Break
	// List that allows == priority to be multiple times
	private Table<Integer, String, List<SpawnProcess>> activeSpawnProcesses;

	public synchronized void addActiveSpawn(World world, SpawnProcess activeSpawn) {
		addActiveSpawn(world.provider.dimensionId, activeSpawn);
	}

	public synchronized void addActiveSpawn(Integer dimension, SpawnProcess activeSpawn) {
		List<SpawnProcess> currentlyActive = activeSpawnProcesses.get(dimension, activeSpawn.livingType());
		if (currentlyActive == null || currentlyActive.isEmpty()
				|| currentlyActive.get(0).priority() < activeSpawn.priority()) {
			List<SpawnProcess> newList = new ArrayList();
			newList.add(activeSpawn);
			activeSpawnProcesses.put(dimension, activeSpawn.livingType(), newList);
		} else if (currentlyActive.get(0).priority() == activeSpawn.priority()) {
			currentlyActive.add(activeSpawn);
		}
	}

	public synchronized void performActiveSpawns(World world) {
		Iterator<Cell<Integer, String, List<SpawnProcess>>> iterator = activeSpawnProcesses.cellSet().iterator();
		while (iterator.hasNext()) {
			Cell<Integer, String, List<SpawnProcess>> cell = iterator.next();
			for (SpawnProcess activeSpawn : cell.getValue()) {
				if (activeSpawn.performSpawnCycle(world)) {
					SpawnerLogic spawnLogic = spawners.get(activeSpawn.spawner());
					Counter counter = spawnLogic.counter(world);
					counter.countEntities(world);
				}
				if (activeSpawn.isFinished(world)) {
					iterator.remove();
				}
				activeSpawn.incremenetDuration();
			}
		}
	}

	@SubscribeEvent
	public void serverTick(ServerTickEvent event) {
		if (event.side != Side.SERVER || event.phase == Phase.END) {
			return;
		}
		MinecraftServer server = MinecraftServer.getServer();
		Integer[] ids = DimensionManager.getIDs(false);

		// PassiveTrigger: Triggered Every X Ticks; increase if performance is an issue
		// passive spawns that want to tick faster should create a ActiveSpawns that persists and is valid every TICK
		if (server.getTickCounter() % 1 == 0) {
			LivingTypes types = null;
			Set<Entry<String, ImmutableList<LivingTypeSpawnTrigger>>> triggerable = types
					.getTriggerableTypes(TRIGGER.PASSIVE);
			for (Entry<String, ImmutableList<LivingTypeSpawnTrigger>> entry : triggerable) {
				String livingTypeID = entry.getKey();
				for (LivingTypeSpawnTrigger trigger : entry.getValue()) {

					// THID NEEDS TO BE DONE FOR EVERY DIMENSION; THIS FEELS BAD, THERE MUST BE A BETTER WAY
					// I think it is acceptable? Maybe make it process every 20 ticks or so

					// I think it is acceptable: Every ForEach Type (~7), ForEach Trigger(~1/2), ForEach Dim (~3-6,
					// Mystcraft??) == 7*2*6=84 MVEL expressions evaluated shouldn't cause issues,
					for (int x = 0; x < ids.length; x++) {
						int id = ids[x];
						if (id == 0 || server.getAllowNether()) {
							Optional<Boolean> result = MVELExpression.execute(trigger.isTriggered, new Object(),
									"Error evaluating canBeTriggered expression " + trigger.isTriggered.expression
											+ " of LivingType " + livingTypeID);
							if (result.isPresent() && result.get()) {
								List<SpawnProcess> currentlyActiveSpawns = activeSpawnProcesses.get(id, livingTypeID);
								if (currentlyActiveSpawns == null || currentlyActiveSpawns.isEmpty()
										|| currentlyActiveSpawns.get(0).priority() <= trigger.triggerPriority) {
									// ActiveSpawn passiveActiveSpawn = new passiveActiveSpawn(trigger)
									// addActiveSpawn(id, activeSpawn);
								}
							}
						}
					}
				}
			}
		}

		/** Perform Spawning */
		for (int x = 0; x < ids.length; x++) {
			int id = ids[x];
			long j = System.nanoTime();
			if (id == 0 || server.getAllowNether()) {
				WorldServer worldserver = DimensionManager.getWorld(id);
				// performSpawningInWorld(worldserver);
				if (!worldserver.getGameRules().hasRule("doCustomMobSpawning")
						|| worldserver.getGameRules().getGameRuleBooleanValue("doCustomMobSpawning")) {
					performActiveSpawns(worldserver);
				}
			}
		}
		/** TODO: Perform Updates to WorldSettings Here */
		// MVELProfile.worldSettings().applyChanges();
	}

	// Alternative Spawner; spawn per player: allow acces to player specific Scoreboard:objectives.
	@Deprecated
	private void performSpawningInWorld(WorldServer world) {
		if (!world.getGameRules().hasRule("doCustomMobSpawning")
				|| world.getGameRules().getGameRuleBooleanValue("doCustomMobSpawning")) {

			List<LivingType> readyLivingTypes = new ArrayList<LivingType>();
			for (LivingType livingType : ExperimentalProfile.worldSettings().getSpawnSettings(world).livingTypes()
					.types().values()) {
				// TODO CreateContext for isReady
				// Tags tags = new Tags(world, countInfo, posX, posY, posZ);
				// Without POS, what tags are ACTUALLY useful?
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
			CountInfo countInfo = SpawnerHelper.counter.countEntities(world);
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
