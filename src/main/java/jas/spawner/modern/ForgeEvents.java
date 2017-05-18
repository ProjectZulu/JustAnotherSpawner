package jas.spawner.modern;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.Event.HasResult;
import jas.spawner.modern.spawner.CountInfo.ChunkStat;
import jas.spawner.modern.spawner.creature.type.CreatureType;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class ForgeEvents extends Event {
	/**
	 * AddEligibleChunkForSpawning is fired when JAS counts entities in the world<br>
	 * and builds a list of candidate chunks for spawning new entities.<br>
	 * <br>
	 * Note that it is not possible to determine what is being spawned here - this<br>
	 * event is only useful for implementing zones that are completely excluded from JAS.<br>
	 * To filter based on creatureType, see {@link StartSpawnCreaturesInChunks}.<br>
	 * <br>
	 * {@link #world} is the world that is getting spawns.<br>
	 * {@link #chunkCoordIntPair} is the chunk being checked for eligibility.<br>
	 * {@link #chunkStat} contains statistics about the chunk being checked.<br>
	 * <br>
	 * This event is {@link Cancelable}. Canceling the event will stop the chunk from<br>
	 * being considered as an eligible chunk for any spawn event.<br>
	 * <br>
	 * This event does not have a result. {@link HasResult}<br>
	 * <br>
	 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
	 **/
	@Cancelable
	public static class AddEligibleChunkForSpawning extends ForgeEvents {
		public final World world;
		public final ChunkCoordIntPair chunkCoordIntPair;
		public final ChunkStat chunkStat;
		
		public AddEligibleChunkForSpawning(World world, ChunkCoordIntPair chunkCoordIntPair, ChunkStat chunkStat) {
			this.world = world;
			this.chunkCoordIntPair = chunkCoordIntPair;
			this.chunkStat = chunkStat;
		}
	}
	
	/**
	 * StartSpawnCreaturesInChunks is fired early in the JAS method responsible for<br>
	 * actually spawning the creatures. Unlike the AddEligibleChunkForSpawning event,<br>
	 * this one actually allows you to filter and cancel specific creature types - though<br>
	 * it's fired much more often.<br>
	 * <br>
	 * Note that this is fired after the count cap has been checked, but before all the<br>
	 * detailed location/position checks (e.g. if it's too close to the player, can't<br>
	 * fit, and so on).<br>
	 * <br>
	 * {@link #world} is the world that is getting spawns.<br>
	 * {@link #chunkCoordIntPair} is the chunk being checked for eligibility.<br>
	 * {@link #creatureType} is the current CreatureType spawn candidate.<br>
	 * <br>
	 * This event is {@link Cancelable}. Canceling the event will stop this CreatureType<br> 
	 * from being spawned in this chunk.<br>
	 * <br>
	 * This event does not have a result. {@link HasResult}<br>
	 * <br>
	 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
	 **/
	@Cancelable
	public static class StartSpawnCreaturesInChunks extends ForgeEvents {
		public final WorldServer world;
		public final CreatureType creatureType;
		public final ChunkCoordIntPair chunkCoordIntPair;
		public StartSpawnCreaturesInChunks(WorldServer world, CreatureType creatureType,
		
		ChunkCoordIntPair chunkCoordIntPair) {
			this.world = world;
			this.creatureType = creatureType;
			this.chunkCoordIntPair = chunkCoordIntPair;
		}
		
		
	}
}
