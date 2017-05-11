package jas.spawner.modern;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.Event.HasResult;
import jas.spawner.modern.spawner.CountInfo.ChunkStat;
import net.minecraft.world.ChunkCoordIntPair;

public class ForgeEvents extends Event {
	/**
	 * AddEligibleChunkForSpawning is fired when JAS counts entities in the world<br>
	 * and builds a list of candidate chunks for spawning new entities.<br>
	 * <br>
	 * {@link #chunkCoordIntPair} is the chunk being checked for eligibility.<br>
	 * {@link #chunkStat} contains detailed information about the chunk being checked.<br>
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
		public final ChunkCoordIntPair chunkCoordIntPair;
		public final ChunkStat chunkStat;
		
		public AddEligibleChunkForSpawning(ChunkCoordIntPair chunkCoordIntPair, ChunkStat chunkStat) {
			this.chunkCoordIntPair = chunkCoordIntPair;
			this.chunkStat = chunkStat;
		}
	}
}
