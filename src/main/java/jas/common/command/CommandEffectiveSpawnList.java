package jas.common.command;

import jas.common.BiomeBlacklist;
import jas.common.JustAnotherSpawner;
import jas.common.spawner.CountInfo;
import jas.common.spawner.CountInfo.ChunkStat;
import jas.common.spawner.EntityCounter;
import jas.common.spawner.Tags;
import jas.common.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.common.spawner.creature.entry.SpawnListEntry;
import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.type.CreatureType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import cpw.mods.fml.common.eventhandler.Event.Result;

public class CommandEffectiveSpawnList extends CommandJasBase {

	private BiomeBlacklist biomeBlacklist;

	public CommandEffectiveSpawnList(BiomeBlacklist biomeBlacklist) {
		this.biomeBlacklist = biomeBlacklist;
	}

	public String getCommandName() {
		return "effectivespawnlist";
	}

	/**
	 * Return the required permission level for this command.
	 */
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "commands.effectivespawnlist.usage";
	}

	@Override
	public void process(ICommandSender commandSender, String[] stringArgs) {
		// /jas effectivespawnlist [0]<Player=CommanderSender> [0]<CreatureType=*> [1]<ChunkDistance=8> [2]<Trials=100>
		// /jas effectivespawnlist [0]<HorizontalRange> [2]<VerticalRange>
		// /jas effectivespawnlist [0]<HorizontalRange>
		// /jas effectivespawnlist (HorizontalRange=60) [1]<VerticalRange> [2]<trials
		// 0
		// 1
		// 2
		// 3
		// 4
		//
		if (stringArgs.length > 3) {
			throw new WrongUsageException("commands.jascanspawnhere.usage", new Object[0]);
		}

		EntityPlayer targetPlayer = getPlayer(commandSender, commandSender.getCommandSenderName());
		//		getPlayer(commandSender, stringArgs[0])
		String desiredCreatureType = "*";
		int chunkDistace = 8;
		int cycles = 5;
		
		HashMap<ChunkCoordIntPair, ChunkStat> eligibleChunksForSpawning = determineChunksForSpawnering(targetPlayer,
				JustAnotherSpawner.globalSettings().chunkSpawnDistance);

		Iterator<CreatureType> typeIterator = JustAnotherSpawner.worldSettings().creatureTypeRegistry()
				.getCreatureTypes();
		CountInfo reportCount = new CountInfo(eligibleChunksForSpawning, new EntityCounter(), new EntityCounter());
		StringBuilder resultMessage = new StringBuilder();
		resultMessage.append("SpawnList after ").append(cycles).append(" cycles.");
		while (typeIterator.hasNext()) {
			CreatureType creatureType = typeIterator.next();
			if (!desiredCreatureType.equals("*") && !desiredCreatureType.equals(creatureType.typeID)) {
				continue;
			}
			CountInfo countInfo = new CountInfo(eligibleChunksForSpawning, new EntityCounter(), new EntityCounter());
			LivingHandlerRegistry livingHandlerRegistry = JustAnotherSpawner.worldSettings().livingHandlerRegistry();
			BiomeSpawnListRegistry biomeSpawnListRegistry = JustAnotherSpawner.worldSettings().biomeSpawnListRegistry();
			attemptSpawnCreaturesInChunks(cycles, targetPlayer.worldObj, JustAnotherSpawner.worldSettings()
					.livingHandlerRegistry(), JustAnotherSpawner.worldSettings().biomeSpawnListRegistry(),
					creatureType, biomeBlacklist, countInfo, reportCount);

			float totalCount = 0;
			for (String key : reportCount.getGlobalEntityClassCountKeysSet()) {
				int count = reportCount.getGlobalEntityClassCount(key);
				totalCount += count;
			}
			resultMessage.append(" TotalSpawned of ").append(creatureType.typeID);
			resultMessage.append(" is ").append((int)totalCount).append(". ");
			for (String key : reportCount.getGlobalEntityClassCountKeysSet()) {
				float count = reportCount.getGlobalEntityClassCount(key);
				resultMessage.append(" ").append(key).append(": ").append((int)count);
				resultMessage.append("[").append((int)(count / totalCount)).append("%]");
			}
		}
		commandSender.addChatMessage(new ChatComponentText(resultMessage.toString()));
	}

	private final HashMap<ChunkCoordIntPair, ChunkStat> determineChunksForSpawnering(EntityPlayer entityplayer,
			int chunkDistance) {
		HashMap<ChunkCoordIntPair, ChunkStat> eligibleChunksForSpawning = new HashMap<ChunkCoordIntPair, ChunkStat>();
		int posX = MathHelper.floor_double(entityplayer.posX / 16.0D);
		int posZ = MathHelper.floor_double(entityplayer.posZ / 16.0D);

		for (int xOffset = -chunkDistance; xOffset <= chunkDistance; ++xOffset) {
			for (int zOffset = -chunkDistance; zOffset <= chunkDistance; ++zOffset) {
				boolean flag3 = xOffset == -chunkDistance || xOffset == chunkDistance || zOffset == -chunkDistance
						|| zOffset == chunkDistance;
				ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(xOffset + posX, zOffset + posZ);
				ChunkStat chunkStat = new ChunkStat(flag3);
				eligibleChunksForSpawning.put(chunkcoordintpair, chunkStat);
			}
		}
		return eligibleChunksForSpawning;
	}

	/**
	 * Performs Actual Creature Spawning inside eligibleChunks. {@link determineChunksForSpawnering} needs to be run to
	 * populate eligibleChunksForSpawning with spawnable chunks
	 * 
	 * @param creatureType CreatureType spawnList that is being Spawned
	 */
	private void attemptSpawnCreaturesInChunks(int maxCycles, World worldServer,
			LivingHandlerRegistry livingHandlerRegistry, BiomeSpawnListRegistry biomeSpawnListRegistry,
			CreatureType creatureType, BiomeBlacklist blacklist, CountInfo countInfo, CountInfo reportCount) {
		ChunkCoordinates serverOriginPoint = worldServer.getSpawnPoint();
		List<ChunkCoordIntPair> eligibleChunksForSpawning = new ArrayList<ChunkCoordIntPair>(
				countInfo.eligibleChunkLocations());
		for (int cycle = 0; cycle < maxCycles; cycle++) {
			Collections.shuffle(eligibleChunksForSpawning);
			for (ChunkCoordIntPair chunkCoord : eligibleChunksForSpawning) {
				ChunkStat chunkStat = countInfo.getChunkStat(chunkCoord);
				if (chunkStat.isEdge) {
					continue;
				}
				// TODO: CreatureType.passiveSpawnAttempts
				countInfo.resetEntitiesSpawnedThisLoop();
				final int entityTypeCap = creatureType.maxNumberOfCreature * eligibleChunksForSpawning.size() / 256;
				for (int numLocAttempts = 0; numLocAttempts < creatureType.iterationsPerChunk; ++numLocAttempts) {
					IEntityLivingData entitylivingdata = null;
					ChunkPosition startSpawningPoint = creatureType.getRandomSpawningPointInChunk(worldServer,
							chunkCoord.chunkXPos, chunkCoord.chunkZPos);
					SpawnListEntry spawnlistentry = biomeSpawnListRegistry.getSpawnListEntryToSpawn(worldServer,
							creatureType, startSpawningPoint.chunkPosX, startSpawningPoint.chunkPosY,
							startSpawningPoint.chunkPosZ);
					if (spawnlistentry == null) {
						continue;
					}
					Tags tags = new Tags(worldServer, countInfo, startSpawningPoint.chunkPosX,
							startSpawningPoint.chunkPosY, startSpawningPoint.chunkPosZ);
					Class<? extends EntityLiving> livingToSpawn = livingHandlerRegistry.getRandomEntity(
							spawnlistentry.livingGroupID, worldServer.rand, tags);
					if (livingToSpawn == null) {
						continue;
					}
					LivingHandler handler = livingHandlerRegistry.getLivingHandler(spawnlistentry.livingGroupID);
					countInfo.resetEntitiesPackCount();
					for (int numEntAttempts = 0; numEntAttempts < creatureType.iterationsPerPack; ++numEntAttempts) {
						// Randomized on Each Attempt, but horizontally to allow a 'Pack' to spawn near each other
						final int horVar = 6;
						final int verVar = 1;
						ChunkPosition spawningPoint = new ChunkPosition(startSpawningPoint.chunkPosX
								+ worldServer.rand.nextInt(horVar) - worldServer.rand.nextInt(horVar),
								startSpawningPoint.chunkPosY + worldServer.rand.nextInt(verVar)
										- worldServer.rand.nextInt(verVar), startSpawningPoint.chunkPosZ
										+ worldServer.rand.nextInt(horVar) - worldServer.rand.nextInt(horVar));
						// Biome BlackList
						if (blacklist.isBlacklisted(worldServer.getBiomeGenForCoords(spawningPoint.chunkPosX,
								spawningPoint.chunkPosY))) {
							continue;
						}

						// CreatureType
						if (!creatureType.canSpawnHere(worldServer, countInfo, spawningPoint)) {
							continue;
						}

						// LivingCap and PackSize
						{
							int globalEntityClassCount = countInfo.getGlobalEntityClassCount(livingToSpawn);
							int livingCap = handler.getLivingCap();

							if (livingCap > 0 && globalEntityClassCount >= livingCap) {
								spawnlistentry = null;
								continue;
							}
							if (countInfo.getEntitiesSpawnedThisLoop() >= spawnlistentry.packSize) {
								continue;
							}
						}

						/* Spawn is Centered Version of blockSpawn such that entity is not placed in Corner */
						float spawnX = spawningPoint.chunkPosX + 0.5F;
						float spawnY = spawningPoint.chunkPosY;
						float spawnZ = spawningPoint.chunkPosZ + 0.5F;
						EntityLiving entityliving;
						try {
							entityliving = livingToSpawn.getConstructor(new Class[] { World.class }).newInstance(
									new Object[] { worldServer });
						} catch (Exception exception) {
							exception.printStackTrace();
							return;
						}
						entityliving.setLocationAndAngles(spawnX, spawnY, spawnZ,
								worldServer.rand.nextFloat() * 360.0F, 0.0F);

						Result canSpawn = ForgeEventFactory.canEntitySpawn(entityliving, worldServer, spawnX, spawnY,
								spawnZ);
						if (canSpawn == Result.ALLOW
								|| (canSpawn == Result.DEFAULT && spawnlistentry.getLivingHandler().getCanSpawnHere(
										entityliving, spawnlistentry, countInfo))) {
							reportCount.countSpawn(entityliving, creatureType.typeID);
						}
					}
				}
			}
		}
	}

	/**
	 * Adds the strings available in this command to the given list of tab completion options.
	 */
	@Override
	public List<String> getTabCompletions(ICommandSender commandSender, String[] stringArgs) {
		throw new UnsupportedOperationException();
		// stringArgs = correctedParseArgs(stringArgs, false);
		// List<String> tabCompletions = new ArrayList<String>();
		// if (stringArgs.length == 1) {
		// addPlayerUsernames(tabCompletions);
		// addEntityNames(tabCompletions);
		// } else if (stringArgs.length == 2) {
		// addEntityNames(tabCompletions);
		// }
		//
		// if (!tabCompletions.isEmpty()) {
		// return getStringsMatchingLastWord(stringArgs, tabCompletions);
		// } else {
		// return tabCompletions;
		// }
	}
}
