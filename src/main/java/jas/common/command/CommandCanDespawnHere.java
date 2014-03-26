package jas.common.command;

import jas.common.JustAnotherSpawner;
import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.handler.LivingHelper;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatMessageComponent;

public class CommandCanDespawnHere extends CommandJasBase {

    @Override
    public String getCommandName() {
        return "candespawnhere";
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender) {
        return "commands.candespawnhere.usage";
    }

    @Override
    public void process(ICommandSender commandSender, String[] stringArgs) {
        if (stringArgs.length == 0 || stringArgs.length > 2) {
            throw new WrongUsageException("commands.jascandespawnhere.usage", new Object[0]);
        }

        EntityPlayer targetPlayer = stringArgs.length == 1 ? getPlayer(commandSender,
                commandSender.getCommandSenderName()) : getPlayer(commandSender, stringArgs[0]);
        String entityName = stringArgs.length == 1 ? stringArgs[0] : stringArgs[1];
        if (!isValidEntityName(entityName)) {
            throw new WrongUsageException("commands.jascanspawnhere.entitynotfound", new Object[0]);
        }

        EntityLiving entity = getTargetEntity(entityName, targetPlayer);
        boolean canDespawn = canEntityDespawnHere(entity);
        StringBuilder resultMessage = new StringBuilder();
        if (canDespawn) {
            resultMessage.append("\u00A7a").append("Entity ").append(entityName).append(" can despawn here.");
        } else {
            resultMessage.append("\u00A7c").append("Entity ").append(entityName).append(" cannot despawn here.");
        }
        resultMessage.append("\u00A7r");
        commandSender.sendChatToPlayer(new ChatMessageComponent().addText(resultMessage.toString()));
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

    private boolean canEntityDespawnHere(EntityLiving entity) {
        LivingHandlerRegistry livingHandlerRegistry = JustAnotherSpawner.worldSettings().livingHandlerRegistry();
        List<LivingHandler> livingHandlers = livingHandlerRegistry.getLivingHandlers(entity.getClass());
        if (!livingHandlers.isEmpty()) {
            for (LivingHandler livingHandler : livingHandlers) {
                if (livingHandler.canDespawn(entity)) {
                    return true;
                }
            }
            return false;
        }
        return LivingHelper.canDespawn(entity);
    }

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
