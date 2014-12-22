package jas.spawner.legacy.command;

import jas.spawner.legacy.TAGProfile;
import jas.spawner.legacy.spawner.biome.group.BiomeHelper;
import jas.spawner.legacy.spawner.biome.structure.StructureHandler;
import jas.spawner.legacy.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.spawner.legacy.spawner.creature.entry.SpawnListEntry;
import jas.spawner.legacy.spawner.creature.handler.LivingGroupRegistry;
import jas.spawner.legacy.spawner.creature.handler.LivingHandler;
import jas.spawner.legacy.spawner.creature.handler.LivingHelper;
import jas.spawner.legacy.spawner.creature.type.CreatureType;

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
import net.minecraft.world.biome.BiomeGenBase;

import com.google.common.collect.ImmutableCollection;

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
        LivingGroupRegistry groupRegistry = TAGProfile.worldSettings().livingGroupRegistry();
        ImmutableCollection<String> groupIDs = groupRegistry.getGroupsWithEntity(groupRegistry.EntityClasstoJASName
                .get(entity.getClass()));
        if(groupIDs.isEmpty()) {
            throw new WrongUsageException("commands.jascanspawnhere.entityhasnogroups", new Object[0]);
        }
        for (String groupID : groupIDs) {
            LivingHandler livingHandler = TAGProfile.worldSettings().livingHandlerRegistry()
                    .getLivingHandler(groupID);
            CreatureType livingType = TAGProfile.worldSettings().creatureTypeRegistry()
                    .getCreatureType(livingHandler.creatureTypeID);
            if (livingType == null) {
                commandSender.addChatMessage(new ChatComponentText(String.format(
                        "Entity %s is of type NONE and thus will never spawn.", entityName)));
                return;
            }

            /* Get local spawnlist. Reminder: Biomes are only used when a structure is absent or empty */
            boolean isBiome = false;
            List<SpawnListEntry> spawnlistentries = new ArrayList<SpawnListEntry>(3);
            String locationName = getMatchingStructureSpawnListEntries(groupID, entity, spawnlistentries);
            String structureName = locationName;
            if (spawnlistentries.isEmpty()) {
                isBiome = true;
                locationName = getMatchingBiomeSpawnListEntries(groupID, entity, livingType, spawnlistentries);
            }

            if (spawnlistentries.isEmpty()) {
                spawnlistentries.add(null);
            }

            StringBuilder resultMessage = new StringBuilder();
            if (groupIDs.size() > 1) {
                resultMessage.append("{Group ").append(groupID).append(": ");
            }
            Iterator<SpawnListEntry> iterator = spawnlistentries.iterator();
            while (iterator.hasNext()) {
                SpawnListEntry spawnListEntry = iterator.next();
                if (spawnlistentries.size() > 1) {
                    resultMessage.append("{");
                }
                resultMessage
                        .append(canEntitySpawnHere(targetPlayer, entity, livingHandler, livingType, spawnListEntry,
                                entityName)).append(": ");
                resultMessage.append(canEntityTypeSpawnHere(targetPlayer, entity, livingType)).append(" ");
                resultMessage.append(canLivingHandlerSpawnHere(targetPlayer, entity, livingHandler)).append(" ");

                if (!isBiome) {
                    resultMessage.append(canSpawnListSpawnHere(targetPlayer, entity, livingHandler, spawnListEntry,
                            locationName, false));
                } else {
                    /* If structureName is !null a structure is present but the spawnlist was empty so default to biome */
                    if (structureName != null) {
                        resultMessage.append("\u00A7b").append("Empty S: ").append(structureName)
                                .append(" spawnlist defaults to biome. ").append("\u00A7r");
                    }
                    resultMessage.append(canSpawnListSpawnHere(targetPlayer, entity, livingHandler, spawnListEntry,
                            locationName, true));
                }

                if (spawnlistentries.size() > 1) {
                    resultMessage.append("}");
                    if (iterator.hasNext()) {
                        resultMessage.append(" ");
                    }
                }
            }
            if (groupIDs.size() > 1) {
                resultMessage.append("}");
            }
            commandSender.addChatMessage(new ChatComponentText(resultMessage.toString()));
        }
    }

	private boolean isValidEntityName(String entityName) {
		LivingGroupRegistry livingGroupRegistry = TAGProfile.worldSettings().livingGroupRegistry();
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
        	LivingGroupRegistry livingGroupRegistry = TAGProfile.worldSettings().livingGroupRegistry();
        	
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
        for (StructureHandler StructureHandler : TAGProfile.worldSettings().structureHandlerRegistry()
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

        BiomeSpawnListRegistry biomeSpawnListRegistry = TAGProfile.worldSettings().biomeSpawnListRegistry();
        for (SpawnListEntry spawnListEntry : biomeSpawnListRegistry.getSpawnListFor(livingType.typeID, packageBiome)) {
            if (spawnListEntry.livingGroupID.equals(livingGroupID)) {
                matchingSpawnListEntries.add(spawnListEntry);
            }
        }
        String shortName = TAGProfile.worldSettings().biomeGroupRegistry().biomePckgToMapping().get(packageBiome);
        return shortName == null ? biome.biomeName : shortName;
    }

    private String canEntitySpawnHere(EntityPlayer targetPlayer, EntityLiving entity, LivingHandler livingHandler,
            CreatureType livingType, SpawnListEntry spawnListEntry, String entityName) {
        String successMessage = "\u00A7a".concat(entityName).concat(" can spawn").concat("\u00A7r");
        String failureMessage = "\u00A7c".concat(entityName).concat(" cannot spawn").concat("\u00A7r");
        /* Check Entity Type */
        {
            boolean tempSpawning = targetPlayer.preventEntitySpawning;
            targetPlayer.preventEntitySpawning = false;
            boolean canTypeSpawn = livingType.canSpawnAtLocation(entity.worldObj, (int) entity.posX, (int) entity.posY,
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
                if (livingHandler.getCanSpawnHere(entity, spawnListEntry)) {
                    canSpawn = true;
                    break;
                }
            }
        }
        targetPlayer.preventEntitySpawning = tempSpawning;
        if (canSpawn) {
            return successMessage;
        } else {
            return failureMessage;
        }
    }

    private String canEntityTypeSpawnHere(EntityPlayer targetPlayer, EntityLiving entity, CreatureType livingType) {
        boolean tempSpawning = targetPlayer.preventEntitySpawning;
        targetPlayer.preventEntitySpawning = false;
        boolean canSpawn = false;
        for (int i = 0; i < SIMULATION_TRIALS; i++) {
            if (livingType.canSpawnAtLocation(entity.worldObj, (int) entity.posX, (int) entity.posY, (int) entity.posZ)) {
                canSpawn = true;
                break;
            }
        }
        targetPlayer.preventEntitySpawning = tempSpawning;

        if (canSpawn) {
            return "\u00A7a".concat(livingType.typeID).concat(" can spawn.").concat("\u00A7r");
        } else {
            return "\u00A7c".concat(livingType.typeID).concat(" cannot spawn.").concat("\u00A7r");
        }
    }

    private String canLivingHandlerSpawnHere(EntityPlayer targetPlayer, EntityLiving entity, LivingHandler livingHandler) {
        boolean tempSpawning = targetPlayer.preventEntitySpawning;
        targetPlayer.preventEntitySpawning = false;

        boolean canSpawn = false;
        for (int i = 0; i < SIMULATION_TRIALS; i++) {
            if (livingHandler.isValidLiving(entity)) {
                canSpawn = true;
                break;
            }
        }
        targetPlayer.preventEntitySpawning = tempSpawning;

        if (canSpawn) {
            return "\u00A7a".concat("Livinghandler can spawn.").concat("\u00A7r");
        } else {
            return "\u00A7c".concat("Livinghandler cannot spawn.").concat("\u00A7r");
        }
    }

    private String canSpawnListSpawnHere(EntityPlayer targetPlayer, EntityLiving entity, LivingHandler livingHandler,
            SpawnListEntry spawnListEntry, String locationName, boolean isBiome) {
        if (spawnListEntry == null) {
            return "\u00A7c".concat("Entity not in B:").concat(locationName).concat(" spawnlist").concat("\u00A7r");
        }

        if (!spawnListEntry.getOptionalSpawning().isOptionalEnabled()) {
            return "\u00A7a".concat("No ").concat(isBiome ? "B:" : "S:").concat(locationName).concat(" tags.")
                    .concat("\u00A7r");
        }

        boolean tempSpawning = targetPlayer.preventEntitySpawning;
        targetPlayer.preventEntitySpawning = false;
        boolean canSpawn = false;
        for (int i = 0; i < SIMULATION_TRIALS; i++) {
            if (livingHandler.isValidSpawnList(entity, spawnListEntry)) {
                canSpawn = true;
                break;
            }
        }
        targetPlayer.preventEntitySpawning = tempSpawning;

        if (canSpawn) {
            return "\u00A7a".concat("Can spawn in ").concat(isBiome ? "B:" : "S:").concat(locationName).concat(".")
                    .concat("\u00A7r");
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