package jas.common.command;

import jas.common.JustAnotherSpawner;
import jas.common.spawner.biome.group.BiomeHelper;
import jas.common.spawner.biome.structure.BiomeHandler;
import jas.common.spawner.creature.entry.SpawnListEntry;
import jas.common.spawner.creature.type.CreatureType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.biome.BiomeGenBase;

public class CommandListSpawns extends CommandJasBase {
    public String getCommandName() {
        return "listspawns";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender) {
        return "commands.jaslistspawns.usage";
    }

    /**
     * Command stringArgs :
     * 
     * /jaslistspawns <targetPlayer> <EntityType> --OUTPUT--> For Biome Player is in: <entityTypeName>: <spawnEntry1>,
     * <spawnEntry2>
     */
    @Override
    public void process(ICommandSender commandSender, String[] stringArgs) {
        if (stringArgs.length >= 4) {
            throw new WrongUsageException("commands.jaslistspawns.usage", new Object[0]);
        }

        String targetBiomeStructure;
        String entityCategName;
        boolean expandedEntries = stringArgs.length == 3 ? stringArgs[2].equalsIgnoreCase("true") : false;
        if (stringArgs.length == 0) {
            EntityPlayerMP targetPlayer = getPlayer(commandSender, commandSender.getCommandSenderName());
            targetBiomeStructure = getTargetAtPlayer(targetPlayer);
            entityCategName = "*";
        } else if (stringArgs.length == 1) {
            EntityPlayerMP targetPlayer = getPlayer(commandSender, commandSender.getCommandSenderName());
            targetBiomeStructure = getTargetAtPlayer(targetPlayer);
            entityCategName = stringArgs[0];
        } else {
            try {
                EntityPlayerMP targetPlayer = getPlayer(commandSender, stringArgs[0]);
                targetBiomeStructure = getTargetAtPlayer(targetPlayer);
            } catch (Exception e) {
                targetBiomeStructure = stringArgs[0];
            }
            entityCategName = stringArgs[1];
        }

        boolean isStructure = isStructureName(targetBiomeStructure);
        if (!isStructure) {
            if (!isBiomeName(targetBiomeStructure)) {
                throw new WrongUsageException("commands.jaslistspawns.invalidtarget", new Object[0]);
            }
        }

        if (isStructure) {
            commandSender.sendChatToPlayer(new ChatMessageComponent().addText(getStructureSpawnList(
                    targetBiomeStructure, entityCategName, expandedEntries)));
        } else {
            commandSender.sendChatToPlayer(new ChatMessageComponent().addText(getBiomeSpawnList(
                    targetBiomeStructure, entityCategName, expandedEntries)));
        }
    }

    private String getTargetAtPlayer(EntityPlayerMP player) {
        Iterator<BiomeHandler> iterator = JustAnotherSpawner.worldSettings().biomeHandlerRegistry().getHandlers();
        while (iterator.hasNext()) {
            BiomeHandler handler = iterator.next();
            String target = handler.getStructure(player.worldObj, (int) player.posX, (int) player.posY,
                    (int) player.posZ);
            if (target != null) {
                return target;
            }
        }
        return BiomeHelper.getPackageName(player.worldObj.getBiomeGenForCoords((int) player.posX, (int) player.posZ));
    }

    private boolean isBiomeName(String arg) {
        if (arg == null || arg.trim().equals("")) {
            return false;
        }

        for (int i = 0; i < BiomeGenBase.biomeList.length; i++) {
            BiomeGenBase biome = BiomeGenBase.biomeList[i];
            if (biome != null && arg.equals(BiomeHelper.getPackageName(biome))) {
                return true;
            }
        }

        return false;
    }

    private String getBiomeSpawnList(String biomeStructureName, String entityCategName, boolean expandedEntries) {
        StringBuilder biomeContents = new StringBuilder();
        biomeContents.append("Biome ");
        biomeContents.append(biomeStructureName);
        biomeContents.append(" contains entries:");

        boolean structureMatch = false;
        boolean entityMatch = false;
        Iterator<CreatureType> iterator = JustAnotherSpawner.worldSettings().creatureTypeRegistry().getCreatureTypes();
        while (iterator.hasNext()) {
            CreatureType entityType = iterator.next();
            if (entityCategName.equals("*") || entityType.typeID.equalsIgnoreCase(entityCategName)) {
                if (!structureMatch) {
                    structureMatch = true;
                    biomeContents.append(" ");
                } else {
                    biomeContents.append(", ");
                }
                biomeContents.append("\u00A71").append(entityType.typeID).append("\u00A7r| ");
                Iterator<SpawnListEntry> spawnListIterator = entityType.getSpawnList(biomeStructureName).iterator();
                while (spawnListIterator.hasNext()) {
                    entityMatch = true;
                    SpawnListEntry entry = spawnListIterator.next();
                    biomeContents.append(EntityList.classToStringMapping.get(entry.livingClass)).append("[\u00A74")
                            .append(entry.itemWeight).append("\u00A7r");
                    if (expandedEntries) {
                        biomeContents.append("/").append(entry.packSize).append("/").append(entry.minChunkPack)
                                .append("/").append(entry.maxChunkPack);
                    }
                    biomeContents.append("]");
                    if (spawnListIterator.hasNext()) {
                        biomeContents.append(", ");
                    }
                }
            }
        }

        if (!structureMatch) {
            throw new WrongUsageException("commands.jaslistspawns.biomenotfound", new Object[0]);
        } else if (!entityMatch) {
//            biomeContents.append("No Entries Found");
        }
        return biomeContents.toString();
    }

    private boolean isStructureName(String arg) {
        if (arg == null || arg.trim().equals("")) {
            return false;
        }

        Iterator<BiomeHandler> iterator = JustAnotherSpawner.worldSettings().biomeHandlerRegistry().getHandlers();
        while (iterator.hasNext()) {
            BiomeHandler type = iterator.next();
            for (String structureKey : type.getStructureKeys()) {
                if (arg.equals(structureKey)) {
                    return true;
                }
            }
        }

        return false;
    }

    private String getStructureSpawnList(String structureName, String entityCateg, boolean expandedEntries) {
        StringBuilder biomeContents = new StringBuilder();
        biomeContents.append("Structure ");
        biomeContents.append(structureName);
        biomeContents.append(" contains entries:");

        boolean structureMatch = false;
        boolean entityMatch = false;
        Iterator<BiomeHandler> iterator = JustAnotherSpawner.worldSettings().biomeHandlerRegistry().getHandlers();
        while (iterator.hasNext()) {
            BiomeHandler handler = iterator.next();
            for (String structureKey : handler.getStructureKeys()) {
                if (structureName.equals(structureKey)) {
                    structureMatch = true;
                    Iterator<SpawnListEntry> spawnListIterator = handler.getStructureSpawnList(structureKey).iterator();
                    while (spawnListIterator.hasNext()) {
                        SpawnListEntry spawnEntry = spawnListIterator.next();
                        if (spawnEntry.getLivingHandler() == null) {
                            continue;
                        }
                        String entityType = spawnEntry.getLivingHandler().creatureTypeID;
                        if (entityCateg.equals("*") || entityType.equals(entityCateg)) {
                            if (!entityMatch) {
                                entityMatch = true;
                                biomeContents.append(" ");
                            } else {
                                biomeContents.append(", ");
                            }

                            biomeContents.append(EntityList.classToStringMapping.get(spawnEntry.livingClass))
                                    .append("[\u00A74").append(spawnEntry.itemWeight).append("\u00A7r");
                            if (expandedEntries) {
                                biomeContents.append("/").append(spawnEntry.packSize).append("/")
                                        .append(spawnEntry.minChunkPack).append("/").append(spawnEntry.maxChunkPack);
                            }
                            biomeContents.append("]");
                            if (spawnListIterator.hasNext()) {
                                biomeContents.append(", ");
                            }
                        }
                    }
                }
            }
        }

        if (!structureMatch) {
            throw new WrongUsageException("commands.jaslistspawns.structurennotfound", new Object[0]);
        } else if (!entityMatch) {
            biomeContents.append(" No Entries Found");
        }
        return biomeContents.toString();
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
            addPackageBiomeNames(tabCompletions);
            addStructureNames(tabCompletions);
            addEntityTypes(tabCompletions);
        } else if (stringArgs.length == 2) {
            addEntityTypes(tabCompletions);
        } else if (stringArgs.length == 3) {
            tabCompletions.add("true");
            tabCompletions.add("false");
        }

        if (!tabCompletions.isEmpty()) {
            return getStringsMatchingLastWord(stringArgs, tabCompletions);
        } else {
            return tabCompletions;
        }
    }

    private void addPackageBiomeNames(List<String> tabCompletions) {
        for (BiomeGenBase biome : BiomeGenBase.biomeList) {
            if (biome != null) {
                tabCompletions.add(BiomeHelper.getPackageName(biome));
            }
        }
    }

    private void addStructureNames(List<String> tabCompletions) {
        Iterator<BiomeHandler> iterator = JustAnotherSpawner.worldSettings().biomeHandlerRegistry().getHandlers();
        while (iterator.hasNext()) {
            BiomeHandler handler = iterator.next();
            for (String structureKey : handler.getStructureKeys()) {
                if (structureKey != null) {
                    tabCompletions.add(structureKey);
                }
            }
        }
    }
}
