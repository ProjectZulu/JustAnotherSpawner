package jas.modern.command;

import jas.modern.JustAnotherSpawner;
import jas.modern.profile.MVELProfile;
import jas.modern.spawner.CountInfo.ChunkStat;
import jas.modern.spawner.CustomSpawner;
import jas.modern.spawner.EntityCounter;
import jas.modern.spawner.EntityCounter.CountableInt;
import jas.modern.spawner.creature.type.CreatureType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class CommandCountCap extends CommandJasBase {

    public String getCommandName() {
        return "countcap";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender par1ICommandSender) {
        return "commands.jascountcap.usage";
    }

    /**
     * Command stringArgs :
     * 
     * /jascountcap <targetPlayer> <EntityType> --OUTPUT--> For Dimension Player is in: Count of <EntityType> is <XX>
     * out of <EntityTypeCap>
     * 
     * /jascountcap <EntityType> --OUTPUT--> New Line for Each Dimension of <EntityType> is <XX> out of <EntityTypeCap>
     */
    @Override
    public void process(ICommandSender commandSender, String[] stringArgs) {
        if (stringArgs.length >= 3) {
            throw new WrongUsageException("commands.jascountcap.usage", new Object[0]);
        }

        World targetWorld = null;
        if (stringArgs.length > 0) {
            targetWorld = getTargetWorld(stringArgs[0], commandSender);
        }

        if (targetWorld == null) {
            targetWorld = getPlayer(commandSender, commandSender.getCommandSenderName()).worldObj;
        }

        String typeName = stringArgs.length == 0 ? "*" : stringArgs[stringArgs.length == 1 ? 0 : 1];
        World[] worlds = stringArgs.length == 2 && stringArgs[0].equals("*") ? MinecraftServer.getServer().worldServers
                : new World[] { targetWorld };
        for (int i = 0; i < worlds.length; i++) {
            if (worlds[i] == null) {
                continue;
            }
            World world = worlds[i];
            HashMap<ChunkCoordIntPair, ChunkStat> eligibleChunksForSpawning = CustomSpawner.determineChunksForSpawnering(
                    world, JustAnotherSpawner.globalSettings().chunkSpawnDistance);
            EntityCounter creatureTypeCount = new EntityCounter();
            EntityCounter creatureCount = new EntityCounter();
            CustomSpawner.countEntityInChunks(world, creatureTypeCount, creatureCount);

            Iterator<CreatureType> iterator = MVELProfile.worldSettings().creatureTypeRegistry()
                    .getCreatureTypes();
            StringBuilder worldTypeContents = new StringBuilder();
            worldTypeContents.append("Results World (");
            worldTypeContents.append(world.provider.getDimensionName());
            worldTypeContents.append("|");
            worldTypeContents.append(world.provider.dimensionId);
            worldTypeContents.append(")");

            boolean foundMatch = false;
            while (iterator.hasNext()) {
                CreatureType entityType = iterator.next();
                if (typeName.equals("*") || entityType.typeID.equalsIgnoreCase(typeName)) {
                    CountableInt counter = creatureTypeCount.getOrPutIfAbsent(entityType.typeID, 0);
                    if (!foundMatch) {
                        foundMatch = true;
                        worldTypeContents.append(" ");
                    } else {
                        worldTypeContents.append(", ");
                    }
                    int entityTypeCap = entityType.maxNumberOfCreature * eligibleChunksForSpawning.size() / 256;
                    worldTypeContents.append("\u00A7r").append(entityType.typeID).append(":")
                            .append(counter.get() >= entityTypeCap ? "\u00A74" : "\u00A72").append(counter.get())
                            .append("\u00A7r").append("/").append(entityTypeCap);
                }
            }

            if (!foundMatch) {
                throw new WrongUsageException("commands.jascountcap.typenotfound", new Object[0]);
            } else {
                commandSender.addChatMessage(new ChatComponentText(worldTypeContents.toString()));
            }
        }
    }

    private World getTargetWorld(String arg, ICommandSender commandSender) {
        try {
            EntityPlayerMP targetPlayer = getPlayer(commandSender, arg);
            return targetPlayer.worldObj;
        } catch (Exception e) {

        }

        try {
            int targetDim = parseInt(commandSender, arg);
            for (WorldServer world : MinecraftServer.getServer().worldServers) {
                if (targetDim == world.provider.dimensionId) {
                    return world;
                }
            }
        } catch (NumberInvalidException e) {

        }
        return null;
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    @Override
    public List<String> getTabCompletions(ICommandSender commandSender, String[] stringArgs) {
        stringArgs = correctedParseArgs(stringArgs, false);
        List<String> tabCompletions = new ArrayList<String>();
        if (stringArgs.length == 1) {
            addEntityTypes(tabCompletions);
            addPlayerUsernames(tabCompletions);
        } else if (stringArgs.length == 2) {
            addEntityTypes(tabCompletions);
        }

        if (!tabCompletions.isEmpty()) {
            return getStringsMatchingLastWord(stringArgs, tabCompletions);
        } else {
            return tabCompletions;
        }
    }
}
