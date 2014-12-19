package jas.modern.command;

import jas.modern.BiomeBlacklist;
import jas.modern.JustAnotherSpawner;
import jas.modern.spawner.CountInfo;
import jas.modern.spawner.EntityCounter;
import jas.modern.spawner.Tags;
import jas.modern.spawner.CountInfo.ChunkStat;
import jas.modern.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.modern.spawner.creature.entry.SpawnListEntry;
import jas.modern.spawner.creature.handler.LivingHandler;
import jas.modern.spawner.creature.handler.LivingHandlerRegistry;
import jas.modern.spawner.creature.handler.parsing.ParsingHelper;
import jas.modern.spawner.creature.type.CreatureType;

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
		// /jas effectivespawnlist [0]<Player=CommanderSender> [1]<CreatureType=*> [2]<ChunkDistance=8> [3]<cycles=100>
		// /jas effectivespawnlist [0]<CreatureType> [1]<ChunkDistance> [2]<cycles>
		// /jas effectivespawnlist [0]<CreatureType> [1]<ChunkDistance>
		// /jas effectivespawnlist [0]<CreatureType>
				
		EntityPlayer targetPlayer = null;
		String desiredCreatureType = "*";
		int chunkDistace = 8;
		int cycles = 3;
		switch (stringArgs.length) {
		case 4:
			targetPlayer = getPlayer(commandSender, stringArgs[0]);
			desiredCreatureType = stringArgs[1];
			cycles = ParsingHelper.parseFilteredInteger(stringArgs[2], cycles, "Cycles");
			chunkDistace = ParsingHelper.parseFilteredInteger(stringArgs[3], chunkDistace, "Chunk Distance");
			break;

		// Fall through below is intentional
		case 3:
			chunkDistace = ParsingHelper.parseFilteredInteger(stringArgs[2], chunkDistace, "Chunk Distance");
		case 2:
			cycles = ParsingHelper.parseFilteredInteger(stringArgs[1], cycles, "Cycles");
		case 1:
			desiredCreatureType = stringArgs[0];
		case 0:
			targetPlayer = getPlayer(commandSender, commandSender.getCommandSenderName());
			break;
		default:
			throw new WrongUsageException("commands.jascanspawnhere.usage", new Object[0]);
		}
		HashMap<ChunkCoordIntPair, ChunkStat> eligibleChunksForSpawning = determineChunksForSpawnering(targetPlayer,
				JustAnotherSpawner.globalSettings().chunkSpawnDistance);

		Iterator<CreatureType> typeIterator = JustAnotherSpawner.worldSettings().creatureTypeRegistry()
				.getCreatureTypes();
		CountInfo reportCount = new CountInfo(eligibleChunksForSpawning, new EntityCounter(), new EntityCounter());
		StringBuilder resultMessage = new StringBuilder();
		resultMessage.append("SpawnList after ").append("\u00A7b").append(cycles).append(" cycles").append("\u00A7r");
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
			resultMessage.append(". \u00A7b").append(creatureType.typeID).append("\u00A7r");
			resultMessage.append(" spawned ").append("\u00A79").append((int) totalCount).append("\u00A7r").append(" {");
			for (String key : reportCount.getGlobalEntityClassCountKeysSet()) {
				float count = reportCount.getGlobalEntityClassCount(key);
				resultMessage.append(" \u00A7a").append(key).append("\u00A7r").append(": ");
				resultMessage.append("\u00A79").append((int) count).append("\u00A7r");
				resultMessage.append("[").append("\u00A79").append((int) (count / totalCount * 100)).append("\u00A7r")
						.append("%]");
			}
			resultMessage.append("}");
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
		stringArgs = correctedParseArgs(stringArgs, false);
		List<String> tabCompletions = new ArrayList<String>();
		if (stringArgs.length == 1) {
			addPlayerUsernames(tabCompletions);
			addEntityTypes(tabCompletions);
		} else if (stringArgs.length == 2) {
			// Two valid inputs are NUMBERS {i.e. ChunkDistance} or CREATURETYPE
			try {
				Integer.parseInt(stringArgs[1]);
			} catch (Exception e) {
				addEntityTypes(tabCompletions);
			}
		}

		if (!tabCompletions.isEmpty()) {
			return getStringsMatchingLastWord(stringArgs, tabCompletions);
		} else {
			return tabCompletions;
		}
	}
}
