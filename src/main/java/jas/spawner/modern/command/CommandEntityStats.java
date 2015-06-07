package jas.spawner.modern.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class CommandEntityStats extends CommandJasBase {

	public String getCommandName() {
		return "entitystats";
	}

	private static class StatsTabulator {
		public float chunksMean;
		public float chunksMedian;
		public float chunksMode;

		public float distanceMean;
		public float distanceMedian;
		public float distanceMode;

		public StatsTabulator(StatsCounter counter) {
			chunksMean = mean(counter.entitiesInChunks);
			chunksMedian = median(counter.entitiesInChunks);
			chunksMode = mode(counter.entitiesInChunks);

			distanceMean = mean(counter.entitiesDistances);
			distanceMedian = median(counter.entitiesDistances);
			distanceMode = mode(counter.entitiesDistances);
		}
		
		private float mean(List<Float> numbers) {
			float sum = 0;
			for (Float number : numbers) {
				sum += number;
			}
			return sum / numbers.size();
		}
		
		private float mode(List<Float> numbers) {
			Collections.sort(numbers);

			Integer longestRepeated = null;
			float longestValue = 0;

			int currentRepeated = 0;
			int currentValue = 0;

			for (Float number : numbers) {
				int value = (int) (number * 10000); // 10000 Chosen for 4 sig digits of accuracy
				if (currentValue == value) {
					currentRepeated++;
					if (longestRepeated == null || longestRepeated < currentRepeated) {
						longestValue = currentValue / 10000;
						longestRepeated = currentRepeated;
					}
				} else {
					currentRepeated = 0;
					currentValue = value;
				}
			}
			return longestRepeated == 1 ? -1 : longestValue;
		}
		
		private float median(List<Float> numbers) {
			Collections.sort(numbers);
			return numbers.get(numbers.size() / 2);
		}
	}

	private static class StatsCounter {
		private final List<Float> entitiesInChunks = new ArrayList<Float>();
		private final List<Float> entitiesDistances = new ArrayList<Float>();
		private final static int maxDistance = 1000000;

		public void addEntitiesFromChunk(Collection<Entity> collection) {
			entitiesInChunks.add((float)collection.size());
			for (Entity entity : collection) {
				World world = entity.worldObj;
				Entity closestEntity = world.getClosestPlayerToEntity(entity, maxDistance);
				entitiesDistances.add(closestEntity != null ? entity.getDistanceToEntity(closestEntity)
						: maxDistance);
			}
		}
	}
	
	/**
	 * Return the required permission level for this command.
	 */
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "commands.jasentitystats.usage";
	}

	@Override
	public void process(ICommandSender commandSender, String[] stringArgs) throws CommandException {
		if (stringArgs.length > 0) {
			throw new WrongUsageException("commands.jasentitystats.usage", new Object[0]);
		}

		World world = commandSender.getEntityWorld();
		
		Multimap<ChunkCoordIntPair, Entity> entitiesPerChunk = ArrayListMultimap.create();

		for (Object object : world.getLoadedEntityList()) {
			Entity entity = (Entity) object;
			entitiesPerChunk.put(
					new ChunkCoordIntPair(MathHelper.floor_double(entity.posX / 16.0D), MathHelper
							.floor_double(entity.posZ / 16.0D)), entity);
		}
		
		StatsCounter counter = new StatsCounter();
		for (ChunkCoordIntPair chunkCoord : entitiesPerChunk.keySet()) {
			counter.addEntitiesFromChunk(entitiesPerChunk.get(chunkCoord));
		}
		
		StatsTabulator tabulator = new StatsTabulator(counter);
		
		StringBuilder disMsg = new StringBuilder();
		disMsg.append("Entity Density: ");
		disMsg.append("Mean [").append(String.format("%.2f", tabulator.chunksMean)).append("], ");
		disMsg.append("Median [").append(String.format("%.2f", tabulator.chunksMedian)).append("], ");
		disMsg.append("Mode [").append(String.format("%.2f", tabulator.chunksMode)).append("]");
		commandSender.addChatMessage(new ChatComponentText(disMsg.toString()));

		disMsg = new StringBuilder();
		disMsg.append("Distance To Player: ");
		disMsg.append("Mean [").append(String.format("%.2f", tabulator.distanceMean)).append("], ");
		disMsg.append("Median [").append(String.format("%.2f", tabulator.distanceMedian)).append("], ");
		disMsg.append("Mode [").append(String.format("%.2f", tabulator.distanceMode)).append("]");
		commandSender.addChatMessage(new ChatComponentText(disMsg.toString()));
	}

	/**
	 * Adds the strings available in this command to the given list of tab completion options.
	 */
	@Override
	public List<String> getTabCompletions(ICommandSender commandSender, String[] stringArgs, BlockPos blockPos) {
		return Collections.emptyList();
	}
}