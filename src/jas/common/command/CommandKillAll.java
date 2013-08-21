package jas.common.command;

import jas.common.spawner.EntityCounter;
import jas.common.spawner.EntityCounter.CountableInt;
import jas.common.spawner.creature.handler.CreatureHandlerRegistry;
import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;

public class CommandKillAll extends CommandBase {
    public String getCommandName() {
        return "jaskillall";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender) {
        return "commands.jaskillall.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] stringArgs) {
        if (stringArgs.length >= 2) {
            throw new WrongUsageException("commands.jaskillall.usage", new Object[0]);
        }

        EntityPlayerMP targetPlayer;
        if (stringArgs.length > 0) {
            targetPlayer = func_82359_c(commandSender, stringArgs[0]);
        } else {
            targetPlayer = func_82359_c(commandSender, commandSender.getCommandSenderName());
        }

        String entityCategName = stringArgs.length == 0 ? "*" : stringArgs.length == 1 ? stringArgs[0] : stringArgs[1];

        EntityCounter deathCount = new EntityCounter();
        int totalDeaths = 0;
        @SuppressWarnings("unchecked")
        Iterator<Entity> iterator = targetPlayer.worldObj.getLoadedEntityList().iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            LivingHandler handler = CreatureHandlerRegistry.INSTANCE.getLivingHandler(entity.getClass());
            if (handler != null && (entityCategName.equals("*") || handler.creatureTypeID.equals(entityCategName))) {
                if (!handler.creatureTypeID.equals(CreatureTypeRegistry.NONE) || entity instanceof EntityLiving) {
                    entity.setDead();
                    deathCount.incrementOrPutIfAbsent(handler.creatureTypeID, 0);
                    totalDeaths++;
                }
            }
        }

        StringBuilder deathMessage = new StringBuilder();
        deathMessage.append("Total Deaths ").append(totalDeaths).append(": ");
        boolean first = true;
        for (Entry<String, CountableInt> entry : deathCount.countingHash.entrySet()) {
            if (first) {
                first = false;
            } else {
                deathMessage.append(", ");
            }
            deathMessage.append(entry.getValue().get()).append("-").append(entry.getKey());
        }
        commandSender.sendChatToPlayer(new ChatMessageComponent().func_111079_a(deathMessage.toString()));
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    @Override
    @SuppressWarnings("rawtypes")
    public List addTabCompletionOptions(ICommandSender commandSender, String[] stringArgs) {
        if (stringArgs.length == 1) {
            List<String> values = new ArrayList<String>();
            Iterator<CreatureType> iterator = CreatureTypeRegistry.INSTANCE.getCreatureTypes();
            while (iterator.hasNext()) {
                CreatureType creatureType = iterator.next();
                values.add(creatureType.typeID);
            }

            String[] combinedValues = new String[values.size() + MinecraftServer.getServer().getAllUsernames().length];
            int index = 0;
            for (String username : MinecraftServer.getServer().getAllUsernames()) {
                combinedValues[index] = username;
                index++;
            }

            for (String typeName : values) {
                combinedValues[index] = typeName;
                index++;
            }

            return getListOfStringsMatchingLastWord(stringArgs, combinedValues);
        } else if (stringArgs.length == 2) {
            List<String> values = new ArrayList<String>();
            Iterator<CreatureType> iterator = CreatureTypeRegistry.INSTANCE.getCreatureTypes();
            while (iterator.hasNext()) {
                CreatureType entityType = iterator.next();
                values.add(entityType.typeID);
            }
            return getListOfStringsMatchingLastWord(stringArgs, values.toArray(new String[values.size()]));
        } else if (stringArgs.length == 3) {
            return getListOfStringsMatchingLastWord(stringArgs, new String[] { "true", "false" });
        } else {
            return null;
        }
    }
}