package jas.spawner.modern.spawner;

import jas.common.JustAnotherSpawner;
import jas.common.global.BiomeBlacklist;
import jas.spawner.modern.MVELProfile;
import jas.spawner.modern.spawner.creature.handler.LivingHandlerRegistry;
import jas.spawner.modern.spawner.creature.type.CreatureType;
import jas.spawner.modern.spawner.creature.type.CreatureTypeRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;

public class ChunkSpawner {

	private BiomeBlacklist blacklist;

	public ChunkSpawner(BiomeBlacklist blacklist) {
		this.blacklist = blacklist;
	}

	private static class ChunkSpawn {
		private final World world;
		private final String creatureTypeID;
		private final int x;
		private final int z;

		public ChunkSpawn(World world, String creatureTypeID, int x, int z) {
			this.world = world;
			this.creatureTypeID = creatureTypeID;
			this.x = x;
			this.z = z;
		}
	}

	private List<ChunkSpawn> chunkSpawnToBeProcessed = new ArrayList<ChunkSpawner.ChunkSpawn>();

	@SubscribeEvent
	public void performChunkSpawning(ServerTickEvent serverTickEvent) {
		if (serverTickEvent.phase.END == Phase.END) {
			List<ChunkSpawn> chunksToSpawn = new ArrayList<ChunkSpawner.ChunkSpawn>(chunkSpawnToBeProcessed);
			chunkSpawnToBeProcessed.clear();

			CreatureTypeRegistry creatureTypeRegistry = MVELProfile.worldSettings().creatureTypeRegistry();

			for (ChunkSpawn chunkSpawn : chunksToSpawn) {
				CreatureType creatureType = creatureTypeRegistry.getCreatureType(chunkSpawn.creatureTypeID);
				LivingHandlerRegistry livingHandlerRegistry = MVELProfile.worldSettings().livingHandlerRegistry();
				BiomeGenBase spawnBiome = chunkSpawn.world.getBiomeGenForCoords(chunkSpawn.x + 16, chunkSpawn.z + 16);
				CustomSpawner.performWorldGenSpawning(chunkSpawn.world, creatureType, livingHandlerRegistry,
						spawnBiome, chunkSpawn.x + 8, chunkSpawn.z + 8, 16, 16, chunkSpawn.world.rand);
			}
		}
	}

	@SubscribeEvent
	public void performChunkSpawning(PopulateChunkEvent.Populate event) {
		/* ICE Event Type is Selected as it is Fired Immediately After Vanilla Chunk Creature Generation */
		if (event.type == PopulateChunkEvent.Populate.EventType.ANIMALS
				&& event.world.getGameRules().getGameRuleBooleanValue("doCustomMobSpawning") && !event.world.isRemote) {
			int k = event.chunkX * 16;
			int l = event.chunkZ * 16;
			if (MVELProfile.worldSettings() == null || MVELProfile.worldSettings().livingHandlerRegistry() == null) {
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
					chunkSpawnToBeProcessed.add(new ChunkSpawn(event.world, creatureType.typeID, k, l));
				}
			}
		}
	}
}
