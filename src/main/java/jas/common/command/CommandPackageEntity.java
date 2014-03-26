package jas.common.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ChatMessageComponent;

public class CommandPackageEntity extends CommandJasBase {
    public String getCommandName() {
        return "entitypackage";
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
    public void process(ICommandSender commandSender, String[] stringArgs) {
        if (stringArgs.length != 1) {
            throw new WrongUsageException("commands.jasentitypackage.usage", new Object[0]);
        }

        String name = stringArgs[0];
        Class<?> entityClass = (Class<?>) EntityList.stringToClassMapping.get(name);
        if (entityClass != null) {
            commandSender.sendChatToPlayer(new ChatMessageComponent().addText(name.concat(" package is ").concat(
                    entityClass.getName())));
        } else {
            throw new WrongUsageException("commands.jasentitypackage.typenotfound", new Object[0]);
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
            addEntityNames(tabCompletions);
        }

        if (!tabCompletions.isEmpty()) {
            return getStringsMatchingLastWord(stringArgs, tabCompletions);
        } else {
            return tabCompletions;
        }
    }
}