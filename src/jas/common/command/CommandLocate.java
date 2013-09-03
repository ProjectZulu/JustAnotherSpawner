package jas.common.command;

import jas.common.JustAnotherSpawner;
import jas.common.spawner.creature.handler.LivingHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatMessageComponent;

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
            targetPlayer = func_82359_c(commandSender, stringArgs[0]);
        } else {
            targetPlayer = func_82359_c(commandSender, commandSender.getCommandSenderName());
        }

        String entityTarget = stringArgs.length == 0 ? "*" : stringArgs.length == 1 ? stringArgs[0] : stringArgs[1];

        StringBuilder countedContents = new StringBuilder();
        countedContents.append("Locations: ");
        boolean foundMatch = false;
        @SuppressWarnings("unchecked")
        Iterator<Entity> iterator = targetPlayer.worldObj.getLoadedEntityList().iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            LivingHandler handler = JustAnotherSpawner.worldSettings().creatureHandlerRegistry()
                    .getLivingHandler(entity.getClass());
            if (handler != null && (entityTarget.equals("*") || handler.creatureTypeID.equals(entityTarget))
                    || entityTarget.equals(EntityList.classToStringMapping.get(entity.getClass()))) {
                foundMatch = true;
                boolean canDespawn = handler.canDespawn((EntityLiving) entity);
                countedContents.append(canDespawn ? "\u00A7a" : "\u00A7c")
                        .append(EntityList.classToStringMapping.get(entity.getClass())).append("\u00A7r[");
                countedContents.append("\u00A79").append((int) entity.posX).append("\u00A7r").append(",");
                countedContents.append("\u00A79").append((int) entity.posY).append("\u00A7r").append(",");
                countedContents.append("\u00A79").append((int) entity.posZ).append("\u00A7r");
                if (iterator.hasNext()) {
                    countedContents.append("] ");
                }
            }
        }

        if (!foundMatch) {
            throw new WrongUsageException("commands.jaslocate.typenotfound", new Object[0]);
        } else {
            commandSender.sendChatToPlayer(new ChatMessageComponent().func_111079_a(countedContents.toString()));
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