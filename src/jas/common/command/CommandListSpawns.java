package jas.common.command;

import jas.common.spawner.biome.group.BiomeHelper;
import jas.common.spawner.creature.entry.SpawnListEntry;
import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;

public class CommandListSpawns extends CommandBase {
    public String getCommandName() {
        return "jaslistspawns";
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
    public void processCommand(ICommandSender commandSender, String[] stringArgs) {

        if (stringArgs.length == 0 || stringArgs.length >= 4) {
            throw new WrongUsageException("commands.jaslistspawns.usage", new Object[0]);
        }

        EntityPlayerMP targetPlayer = func_82359_c(commandSender, stringArgs[0]);
        String biomePckgName = BiomeHelper.getPackageName(targetPlayer.worldObj.getBiomeGenForCoords(
                (int) targetPlayer.posX, (int) targetPlayer.posZ));
        String entityCategName = stringArgs[1];
        boolean expandedEntries = stringArgs.length == 3 ? stringArgs[2].equalsIgnoreCase("true") : false;

        StringBuilder biomeContents = new StringBuilder();
        biomeContents.append("Biome ");
        biomeContents.append(biomePckgName);
        biomeContents.append(" contains entries:");

        boolean foundMatch = false;
        Iterator<CreatureType> iterator = CreatureTypeRegistry.INSTANCE.getCreatureTypes();
        while (iterator.hasNext()) {
            CreatureType entityType = iterator.next();
            if (entityCategName.equals("*") || entityType.typeID.equalsIgnoreCase(entityCategName)) {
                if (!foundMatch) {
                    foundMatch = true;
                    biomeContents.append(" ");
                } else {
                    biomeContents.append(", ");
                }
                biomeContents.append("§1").append(entityType.typeID).append("§r| ");
                Iterator<SpawnListEntry> spawnListIterator = entityType.getSpawnList(biomePckgName).iterator();
                while (spawnListIterator.hasNext()) {
                    SpawnListEntry entry = spawnListIterator.next();
                    biomeContents.append(EntityList.classToStringMapping.get(entry.livingClass)).append("[§4")
                            .append(entry.itemWeight).append("§r");
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

        if (!foundMatch) {
            throw new WrongUsageException("commands.jaslistspawns.typenotfound", new Object[0]);
        } else {
            commandSender.sendChatToPlayer(new ChatMessageComponent().func_111079_a(biomeContents.toString()));
        }
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public List addTabCompletionOptions(ICommandSender commandSender, String[] stringArgs) {
        if (stringArgs.length == 1) {
            return getListOfStringsMatchingLastWord(stringArgs, MinecraftServer.getServer().getAllUsernames());
        } else if (stringArgs.length == 2) {
            List<String> values = new ArrayList<String>();
            Iterator<CreatureType> iterator = CreatureTypeRegistry.INSTANCE.getCreatureTypes();
            while (iterator.hasNext()) {
                CreatureType entityType = iterator.next();
                values.add(entityType.typeID);
            }
            String[] arrayValues = values.toArray(new String[values.size()]);
            return getListOfStringsMatchingLastWord(stringArgs, arrayValues);
        } else if (stringArgs.length == 3) {
            return getListOfStringsMatchingLastWord(stringArgs, new String[] { "true", "false" });
        } else {
            return null;
        }
    }
}
