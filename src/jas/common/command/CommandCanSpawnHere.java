package jas.common.command;

import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.biome.group.BiomeHelper;
import jas.common.spawner.biome.structure.BiomeHandler;
import jas.common.spawner.biome.structure.BiomeHandlerRegistry;
import jas.common.spawner.creature.entry.SpawnListEntry;
import jas.common.spawner.creature.handler.CreatureHandlerRegistry;
import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.handler.LivingHelper;
import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.biome.BiomeGenBase;

public class CommandCanSpawnHere extends CommandJasBase {
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

        EntityPlayer targetPlayer = stringArgs.length == 1 ? func_82359_c(commandSender,
                commandSender.getCommandSenderName()) : func_82359_c(commandSender, stringArgs[0]);
        String entityName = stringArgs.length == 1 ? stringArgs[0] : stringArgs[1];
        if (!isValidEntityName(entityName)) {
            throw new WrongUsageException("commands.jascanspawnhere.entitynotfound", new Object[0]);
        }

        EntityLiving entity = getTargetEntity(entityName, targetPlayer);
        LivingHandler livingHandler = CreatureHandlerRegistry.INSTANCE.getLivingHandler(entity.getClass());

        CreatureType livingType = CreatureTypeRegistry.INSTANCE.getCreatureType(livingHandler.creatureTypeID);
        if (livingType == null) {
            commandSender.sendChatToPlayer(new ChatMessageComponent().func_111079_a(String.format(
                    "Entity %s is of type NONE and thus will never spawn.", entityName)));
            return;
        }

        /* Get local spawnlist. Reminder: Biomes are only used when a structure is absent or empty */
        boolean isBiome = false;
        List<SpawnListEntry> spawnlistentries = new ArrayList<SpawnListEntry>(3);
        String locationName = getMatchingStructureSpawnListEntries(entity, spawnlistentries);
        String structureName = locationName;
        if (spawnlistentries.isEmpty()) {
            isBiome = true;
            locationName = getMatchingBiomeSpawnListEntries(entity, livingType, spawnlistentries);
        }

        if (spawnlistentries.isEmpty()) {
            spawnlistentries.add(null);
        }

        StringBuilder resultMessage = new StringBuilder();
        Iterator<SpawnListEntry> iterator = spawnlistentries.iterator();
        while (iterator.hasNext()) {
            SpawnListEntry spawnListEntry = iterator.next();
            if (spawnlistentries.size() > 1) {
                resultMessage.append("{");
            }
            resultMessage.append(
                    canEntitySpawnHere(targetPlayer, entity, livingHandler, livingType, spawnListEntry, entityName))
                    .append(": ");
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
        commandSender.sendChatToPlayer(new ChatMessageComponent().func_111079_a(resultMessage.toString()));
    }

    private boolean isValidEntityName(String entityName) {
        for (Object object : EntityList.classToStringMapping.values()) {
            String mapping = (String) object;
            if (entityName.equals(mapping)) {
                return true;
            }
        }
        return false;
    }

    private EntityLiving getTargetEntity(String entityName, EntityPlayer targetPlayer) {
        EntityLiving entity;
        try {
            @SuppressWarnings("unchecked")
            Class<? extends EntityLiving> entityClass = (Class<? extends EntityLiving>) EntityList.stringToClassMapping
                    .get(entityName);
            entity = (EntityLiving) LivingHelper.instantiateEntity(entityClass, targetPlayer.worldObj);
        } catch (Exception exception) {
            throw new WrongUsageException("commands.jascanspawnhere.cannotinstantiateentity", new Object[0]);
        }
        entity.setLocationAndAngles(targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, targetPlayer.rotationYaw,
                targetPlayer.rotationPitch);
        return entity;
    }

    private String getMatchingStructureSpawnListEntries(EntityLiving entity,
            Collection<SpawnListEntry> matchingSpawnListEntries) {
        String structureName;
        for (BiomeHandler biomeHandler : BiomeHandlerRegistry.INSTANCE.handlers()) {
            structureName = biomeHandler.getStructure(entity.worldObj, (int) entity.posX, (int) entity.posY,
                    (int) entity.posZ);
            if (structureName != null) {
                for (String structureKey : biomeHandler.getStructureKeys()) {
                    if (structureName.equals(structureKey)) {
                        for (SpawnListEntry entry : biomeHandler.getStructureSpawnList(structureKey)) {
                            if (entity.getClass().equals(entry.getClass())) {
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

    private String getMatchingBiomeSpawnListEntries(EntityLiving entity, CreatureType livingType,
            Collection<SpawnListEntry> matchingSpawnListEntries) {
        BiomeGenBase biome = entity.worldObj.getBiomeGenForCoords((int) entity.posX, (int) entity.posZ);
        String packageBiome = BiomeHelper.getPackageName(biome);

        for (SpawnListEntry spawnListEntry : livingType.getSpawnList(packageBiome)) {
            if (spawnListEntry.livingClass.equals(entity.getClass())) {
                matchingSpawnListEntries.add(spawnListEntry);
            }
        }

        String shortName = BiomeGroupRegistry.INSTANCE.biomePckgToMapping.get(packageBiome);
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
        boolean canSpawn = livingHandler.getCanSpawnHere(entity, spawnListEntry);
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
        boolean canSpawn = livingType.canSpawnAtLocation(entity.worldObj, (int) entity.posX, (int) entity.posY,
                (int) entity.posZ);
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
        boolean canSpawn = livingHandler.isValidLiving(entity);
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
            return "\u00A7b".concat("Entity not in B:").concat(locationName).concat(" spawnlist").concat("\u00A7r");
        }

        if (!spawnListEntry.getOptionalSpawning().isOptionalEnabled()) {
            return "\u00A7b".concat("No ").concat(isBiome ? "B:" : "S:").concat(locationName).concat(" tags.")
                    .concat("\u00A7r");
        }

        boolean tempSpawning = targetPlayer.preventEntitySpawning;
        targetPlayer.preventEntitySpawning = false;
        boolean canSpawn = livingHandler.isValidSpawnList(entity, spawnListEntry);
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