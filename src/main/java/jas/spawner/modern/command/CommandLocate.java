package jas.spawner.modern.command;

import jas.spawner.modern.MVELProfile;
import jas.spawner.modern.spawner.CountInfo;
import jas.spawner.modern.spawner.CustomSpawner;
import jas.spawner.modern.spawner.creature.handler.LivingGroupRegistry;
import jas.spawner.modern.spawner.creature.handler.LivingHandler;
import jas.spawner.modern.spawner.creature.handler.LivingHandlerRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

public class CommandLocate extends CommandJasBase {
    public String getCommandName() {
        return "locate";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender) {
        return "commands.jaslocate.usage";
    }

    @Override
    public void process(ICommandSender commandSender, String[] stringArgs) {
        if (stringArgs.length >= 3) {
            throw new WrongUsageException("commands.jaslocate.usage", new Object[0]);
        }

        EntityPlayerMP targetPlayer;
        if (stringArgs.length == 2) {
            targetPlayer = getPlayer(commandSender, stringArgs[0]);
        } else {
            targetPlayer = getPlayer(commandSender, commandSender.getCommandSenderName());
        }

        String entityTarget = stringArgs.length == 0 ? "*" : stringArgs.length == 1 ? stringArgs[0] : stringArgs[1];

        StringBuilder countedContents = new StringBuilder();
        countedContents.append("Locations: ");
        boolean foundMatch = false;
        Iterator<Entity> iterator = CustomSpawner.spawnCounter.countLoadedEntities(targetPlayer.worldObj).iterator();
        CountInfo info = CustomSpawner.spawnCounter.countEntities(targetPlayer.worldObj);
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            LivingGroupRegistry groupRegistry = MVELProfile.worldSettings().livingGroupRegistry();
            LivingHandlerRegistry handlerRegistry = MVELProfile.worldSettings().livingHandlerRegistry();
			List<LivingHandler> livingHandlers = handlerRegistry.getLivingHandlers(groupRegistry.EntityClasstoJASName
					.get(entity.getClass()));
			for (LivingHandler livingHandler : livingHandlers) {
				if (livingHandler != null
						&& (entityTarget.equals("*") || livingHandler.creatureTypeID.equals(entityTarget))
						|| entityTarget.equals(EntityList.classToStringMapping.get(entity.getClass()))) {
					foundMatch = true;
					;
					boolean canDespawn = livingHandler.canDespawn((EntityLiving) entity, info);
					countedContents.append(canDespawn ? "\u00A7a" : "\u00A7c")
							.append(EntityList.classToStringMapping.get(entity.getClass())).append("\u00A7r[");
					countedContents.append("\u00A79").append((int) entity.posX).append("\u00A7r").append(",");
					countedContents.append("\u00A79").append((int) entity.posY).append("\u00A7r").append(",");
					countedContents.append("\u00A79").append((int) entity.posZ).append("\u00A7r");
					if (iterator.hasNext()) {
						countedContents.append("] ");
					}
					break;
				}
			}
        }

        if (!foundMatch) {
            throw new WrongUsageException("commands.jaslocate.typenotfound", new Object[0]);
        } else {
            commandSender.addChatMessage(new ChatComponentText(countedContents.toString()));
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
            addEntityTypes(tabCompletions);
            addPlayerUsernames(tabCompletions);
            addEntityNames(tabCompletions);
        } else if (stringArgs.length == 2) {
            addEntityTypes(tabCompletions);
            addEntityNames(tabCompletions);
        }

        if (!tabCompletions.isEmpty()) {
            return getStringsMatchingLastWord(stringArgs, tabCompletions);
        } else {
            return tabCompletions;
        }
    }
}