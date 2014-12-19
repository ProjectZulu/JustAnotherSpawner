package jas.modern.command;

import jas.modern.profile.MVELProfile;
import jas.modern.spawner.CountInfo;
import jas.modern.spawner.CustomSpawner;
import jas.modern.spawner.Tags;
import jas.modern.spawner.biome.group.BiomeHelper;
import jas.modern.spawner.biome.structure.StructureHandler;
import jas.modern.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.modern.spawner.creature.entry.SpawnListEntry;
import jas.modern.spawner.creature.handler.LivingGroupRegistry;
import jas.modern.spawner.creature.handler.LivingHandler;
import jas.modern.spawner.creature.handler.LivingHandlerRegistry;
import jas.modern.spawner.creature.handler.LivingHelper;
import jas.modern.spawner.creature.type.CreatureType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.biome.BiomeGenBase;

public class CommandCanSpawnHere extends CommandJasBase {

    /*
     * Number of trials to run canSpawnHere to rule out random chance of success/failure.
     * 
     * This was chosen such that to give a ~95% accuracy to the minimum accepted deviation in chance which is 1%. Any
     * entity that spawns at least 1% of the time should be detected properly >=95% of the time.
     * 
     * CHANCE_OF_SUCCESS = 0.95 >= = 1-(1-TRIAL_CHANCE/100)^SIMULATION_TRIALS. TRIAL_CHANCE is chance out of 100 that
     * the entity would spawn.
     */
    private static final int SIMULATION_TRIALS = 300;

    public String getCommandName() {
        return "canspawnhere";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender) {
        return "commands.jascanspawnhere.usage";
    }

    @Override
    public void process(ICommandSender commandSender, String[] stringArgs) {
        if (stringArgs.length == 0 || stringArgs.length > 2) {
            throw new WrongUsageException("commands.jascanspawnhere.usage", new Object[0]);
        }

        EntityPlayer targetPlayer = stringArgs.length == 1 ? getPlayer(commandSender,
                commandSender.getCommandSenderName()) : getPlayer(commandSender, stringArgs[0]);
        String entityName = stringArgs.length == 1 ? stringArgs[0] : stringArgs[1];
        if (!isValidEntityName(entityName)) {
            throw new WrongUsageException("commands.jascanspawnhere.entitynotfound", new Object[0]);
        }

		EntityLiving entity = getTargetEntity(entityName, targetPlayer);
		LivingGroupRegistry groupRegistry = MVELProfile.worldSettings().livingGroupRegistry();
		LivingHandlerRegistry handlerRegistry = MVELProfile.worldSettings().livingHandlerRegistry();
		List<LivingHandler> livingHandlers = handlerRegistry.getLivingHandlers(groupRegistry.EntityClasstoJASName
				.get(entity.getClass()));
		if (livingHandlers.isEmpty()) {
			throw new WrongUsageException("commands.jascanspawnhere.entityhasnogroups", new Object[0]);
		}
		for (LivingHandler livingHandler : livingHandlers) {
			CreatureType livingType = MVELProfile.worldSettings().creatureTypeRegistry()
					.getCreatureType(livingHandler.creatureTypeID);
			if (livingType == null) {
				commandSender.addChatMessage(new ChatComponentText(String.format(
						"Entity %s is of type NONE and thus will never spawn.", entityName)));
				return;
			}
			CountInfo countInfo = CustomSpawner.determineCountInfo(entity.worldObj);

			/* Get local spawnlist. Reminder: Biomes are only used when a structure is absent or empty */
			boolean isBiome = false;
			List<SpawnListEntry> spawnlistentries = new ArrayList<SpawnListEntry>(3);
			String locationName = getMatchingStructureSpawnListEntries(livingHandler.livingID, entity, spawnlistentries);
			String structureName = locationName;
			if (spawnlistentries.isEmpty()) {
				isBiome = true;
				locationName = getMatchingBiomeSpawnListEntries(livingHandler.livingID, entity, livingType,
						spawnlistentries);
			}

			if (spawnlistentries.isEmpty()) {
				spawnlistentries.add(null);
			}

			StringBuilder resultMessage = new StringBuilder();
			if (livingHandlers.size() > 1) {
				resultMessage.append("{Group ").append(livingHandler.livingID).append(": ");
			}
			Iterator<SpawnListEntry> iterator = spawnlistentries.iterator();
			while (iterator.hasNext()) {
				SpawnListEntry spawnListEntry = iterator.next();
				if (spawnlistentries.size() > 1) {
					resultMessage.append("{");
				}
				resultMessage.append(
						canEntitySpawnHere(targetPlayer, entity, livingHandler, livingType, spawnListEntry, entityName,
								countInfo)).append(": ");
				resultMessage.append(canEntityTypeSpawnHere(targetPlayer, entity, livingType, countInfo)).append(" ");
				resultMessage.append(canLivingHandlerSpawnHere(targetPlayer, entity, livingHandler, countInfo)).append(
						" ");

				if (!isBiome) {
					resultMessage.append(canSpawnListSpawnHere(targetPlayer, entity, livingHandler, spawnListEntry,
							locationName, false, countInfo));
				} else {
					/* If structureName is !null a structure is present but the spawnlist was empty so default to biome */
					if (structureName != null) {
						resultMessage.append("\u00A7b").append("Empty S: ").append(structureName)
								.append(" spawnlist defaults to biome. ").append("\u00A7r");
					}
					resultMessage.append(canSpawnListSpawnHere(targetPlayer, entity, livingHandler, spawnListEntry,
							locationName, true, countInfo));
				}

				if (spawnlistentries.size() > 1) {
					resultMessage.append("}");
					if (iterator.hasNext()) {
						resultMessage.append(" ");
					}
				}
			}
			if (livingHandlers.size() > 1) {
				resultMessage.append("}");
			}
			commandSender.addChatMessage(new ChatComponentText(resultMessage.toString()));
		}
	}

	private boolean isValidEntityName(String entityName) {
		LivingGroupRegistry livingGroupRegistry = MVELProfile.worldSettings().livingGroupRegistry();
		for (String mapping : livingGroupRegistry.JASNametoEntityClass.keySet()) {
			if (entityName.equals(mapping)) {
				return true;
			}
		}
		return false;
	}

    private EntityLiving getTargetEntity(String entityName, EntityPlayer targetPlayer) {
        EntityLiving entity;
        try {
        	LivingGroupRegistry livingGroupRegistry = MVELProfile.worldSettings().livingGroupRegistry();
        	
            @SuppressWarnings("unchecked")
            Class<? extends EntityLiving> entityClass = livingGroupRegistry.JASNametoEntityClass.get(entityName);
            entity = (EntityLiving) LivingHelper.instantiateEntity(entityClass, targetPlayer.worldObj);
        } catch (Exception exception) {
            throw new WrongUsageException("commands.jascanspawnhere.cannotinstantiateentity", new Object[0]);
        }
        entity.setLocationAndAngles(targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, targetPlayer.rotationYaw,
                targetPlayer.rotationPitch);
        return entity;
    }

    private String getMatchingStructureSpawnListEntries(String livingGroupID, EntityLiving entity,
            Collection<SpawnListEntry> matchingSpawnListEntries) {
        String structureName;
        for (StructureHandler StructureHandler : MVELProfile.worldSettings().structureHandlerRegistry()
                .handlers()) {
            structureName = StructureHandler.getStructure(entity.worldObj, (int) entity.posX, (int) entity.posY,
                    (int) entity.posZ);
            if (structureName != null) {
                for (String structureKey : StructureHandler.getStructureKeys()) {
                    if (structureName.equals(structureKey)) {
                        for (SpawnListEntry entry : StructureHandler.getStructureSpawnList(structureKey)) {
                            if (livingGroupID.equals(entry.livingGroupID)) {
                                matchingSpawnListEntries.add(entry);
                            }
                        }
                        return structureKey;
                    }
                }
            }
        }
        return null;
    }

    private String getMatchingBiomeSpawnListEntries(String livingGroupID, Entity entity, CreatureType livingType,
            Collection<SpawnListEntry> matchingSpawnListEntries) {
        BiomeGenBase biome = entity.worldObj.getBiomeGenForCoords((int) entity.posX, (int) entity.posZ);
        String packageBiome = BiomeHelper.getPackageName(biome);

        BiomeSpawnListRegistry biomeSpawnListRegistry = MVELProfile.worldSettings().biomeSpawnListRegistry();
        for (SpawnListEntry spawnListEntry : biomeSpawnListRegistry.getSpawnListFor(livingType.typeID, packageBiome)) {
            if (spawnListEntry.livingGroupID.equals(livingGroupID)) {
                matchingSpawnListEntries.add(spawnListEntry);
            }
        }
        String shortName = MVELProfile.worldSettings().biomeGroupRegistry().biomePckgToMapping().get(packageBiome);
        return shortName == null ? biome.biomeName : shortName;
    }

    private String canEntitySpawnHere(EntityPlayer targetPlayer, EntityLiving entity, LivingHandler livingHandler,
            CreatureType livingType, SpawnListEntry spawnListEntry, String entityName, CountInfo countInfo) {
        String failureMessage = "\u00A7c".concat(entityName).concat(" cannot spawn").concat("\u00A7r");
        /* Check Entity Type */
        {
            boolean tempSpawning = targetPlayer.preventEntitySpawning;
            targetPlayer.preventEntitySpawning = false;
			Tags tags = new Tags(entity.worldObj, countInfo, (int) entity.posX, (int) entity.posY, (int) entity.posZ);

			boolean canTypeSpawn = livingType.canSpawnAtLocation(entity.worldObj, tags, (int) entity.posX, (int) entity.posY,
					(int) entity.posZ);
            targetPlayer.preventEntitySpawning = tempSpawning;

            if (canTypeSpawn == false) {
                return failureMessage;
            }
        }

        boolean tempSpawning = targetPlayer.preventEntitySpawning;
        targetPlayer.preventEntitySpawning = false;
        boolean canSpawn = false;
        if (spawnListEntry != null) {
            for (int i = 0; i < SIMULATION_TRIALS; i++) {
                if (livingHandler.getCanSpawnHere(entity, spawnListEntry, countInfo)) {
                    canSpawn = true;
                    break;
                }
            }
        }
        targetPlayer.preventEntitySpawning = tempSpawning;
        if (canSpawn) {
			StringBuilder builder = new StringBuilder();
			builder.append("\u00A7a").append(entityName).append(" can spawn").append("\u00A7r");
			return builder.toString();
        } else {
            return failureMessage;
        }
    }

    private String canEntityTypeSpawnHere(EntityPlayer targetPlayer, EntityLiving entity, CreatureType livingType, CountInfo countInfo) {
		Tags tags = new Tags(entity.worldObj, countInfo, (int) entity.posX, (int) entity.posY, (int) entity.posZ);

        boolean tempSpawning = targetPlayer.preventEntitySpawning;
        targetPlayer.preventEntitySpawning = false;
        boolean canSpawn = false;
        int successes = 0;
        int numTrials = 0;
		for (int i = 0; i < SIMULATION_TRIALS; i++) {
			if (livingType.canSpawnAtLocation(entity.worldObj, tags, (int) entity.posX, (int) entity.posY,
					(int) entity.posZ)) {
				canSpawn = true;
                successes++;
			}
            numTrials++;
		}
        targetPlayer.preventEntitySpawning = tempSpawning;

        if (canSpawn) {
			StringBuilder builder = new StringBuilder();
			builder.append("\u00A7a").append(livingType.typeID).append(" can spawn");
			if (successes != numTrials) {
				builder.append(" [").append(MathHelper.ceiling_float_int(successes / (float) numTrials)).append("%]");
			} else { 
				builder.append(".");
			}
			builder.append("\u00A7r");
			return builder.toString();
        } else {
            return "\u00A7c".concat(livingType.typeID).concat(" cannot spawn.").concat("\u00A7r");
        }
    }

    private String canLivingHandlerSpawnHere(EntityPlayer targetPlayer, EntityLiving entity, LivingHandler livingHandler, CountInfo countInfo) {
        boolean tempSpawning = targetPlayer.preventEntitySpawning;
        targetPlayer.preventEntitySpawning = false;

        boolean canSpawn = false;
        int successes = 0;
        int numTrials = 0;
        for (int i = 0; i < SIMULATION_TRIALS; i++) {
            if (livingHandler.isValidLiving(entity, countInfo)) {
                canSpawn = true;
                successes++;
            }
            numTrials++;
        }
        targetPlayer.preventEntitySpawning = tempSpawning;

        if (canSpawn) {
			StringBuilder builder = new StringBuilder();
			builder.append("\u00A7a").append("Livinghandler can spawn");
			if (successes != numTrials) {
				builder.append(" [").append(MathHelper.ceiling_float_int(successes / (float) numTrials)).append("%]");
			} else { 
				builder.append(".");
			}
			builder.append("\u00A7r");
			return builder.toString();
        } else {
            return "\u00A7c".concat("Livinghandler cannot spawn.").concat("\u00A7r");
        }
    }

    private String canSpawnListSpawnHere(EntityPlayer targetPlayer, EntityLiving entity, LivingHandler livingHandler,
            SpawnListEntry spawnListEntry, String locationName, boolean isBiome, CountInfo countInfo) {
        if (spawnListEntry == null) {
            return "\u00A7c".concat("Entity not in B:").concat(locationName).concat(" spawnlist").concat("\u00A7r");
        }

        if (!spawnListEntry.getOptionalSpawning().isPresent()) {
            return "\u00A7a".concat("No ").concat(isBiome ? "B:" : "S:").concat(locationName).concat(" tags.")
                    .concat("\u00A7r");
        }

        boolean tempSpawning = targetPlayer.preventEntitySpawning;
        targetPlayer.preventEntitySpawning = false;
        boolean canSpawn = false;
        int successes = 0;
        int numTrials = 0;
        for (int i = 0; i < SIMULATION_TRIALS; i++) {
            if (livingHandler.isValidSpawnList(entity, spawnListEntry, countInfo)) {
                canSpawn = true;
                successes++;
            }
            numTrials++;
        }
        targetPlayer.preventEntitySpawning = tempSpawning;

        if (canSpawn) {
			StringBuilder builder = new StringBuilder();
			builder.append("\u00A7a").append("Can spawn in ").append(isBiome ? "B:" : "S:").append(locationName);
			if (successes != numTrials) {
				builder.append(" [").append(MathHelper.ceiling_float_int(successes / (float) numTrials)).append("%]");
			} else { 
				builder.append(".");
			}
			builder.append("\u00A7r");
			return builder.toString();
        } else {
            return "\u00A7c".concat("Cannot spawn in ").concat(isBiome ? "B:" : "S:").concat(locationName).concat(".")
                    .concat("\u00A7r");
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
            addEntityNames(tabCompletions);
        } else if (stringArgs.length == 2) {
            addEntityNames(tabCompletions);
        }

        if (!tabCompletions.isEmpty()) {
            return getStringsMatchingLastWord(stringArgs, tabCompletions);
        } else {
            return tabCompletions;
        }
    }
}