package jas.common.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ChatMessageComponent;

public class CommandPackageEntity extends CommandBase {
    public String getCommandName() {
        return "jasentitypackage";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender) {
        return "commands.jasentitypackage.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] stringArgs) {
        if (stringArgs.length == 0) {
            throw new WrongUsageException("commands.jasentitypackage.usage", new Object[0]);
        }

        String name = "";
        for (String arg : stringArgs) {
            name = name.concat(arg);
        }

        Class<?> entityClass = (Class<?>) EntityList.stringToClassMapping.get(name);

        if (entityClass != null) {
            commandSender.sendChatToPlayer(new ChatMessageComponent().func_111079_a(name.concat(" package is ").concat(
                    entityClass.getName())));
        } else {
            throw new WrongUsageException("commands.jasentitypackage.typenotfound", new Object[0]);
        }
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List addTabCompletionOptions(ICommandSender commandSender, String[] stringArgs) {
        if (stringArgs.length == 1) {
            List<String> values = new ArrayList<String>();
            values.addAll(EntityList.classToStringMapping.values());
            return getListOfStringsMatchingLastWord(stringArgs, values.toArray(new String[values.size()]));
        } else {
            return null;
        }
    }
}