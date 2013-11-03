package jas.common.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

public final class CommandJAS extends CommandJasBase {

    private HashMap<String, CommandWrapper> commands = new HashMap<String, CommandWrapper>();

    public CommandJAS() {
        addCommand(new CommandCountCap());
        addCommand(new CommandListSpawns());
        addCommand(new CommandCanSpawnHere());
        addCommand(new CommandComposition());
        addCommand(new CommandKillAll());
        addCommand(new CommandLocate());
        addCommand(new CommandPackageEntity());
        addCommand(new CommandSaveConfig());
        addCommand(new CommandLoadConfig());
        addCommand(new CommandCanDespawnHere());
    }

    public void addCommand(CommandBase base) {
        if (base == null || base.getCommandName() == null || commands.containsKey(base.getCommandName())) {
            throw new IllegalArgumentException("Commands cannot be null");
        }
        commands.put(base.getCommandName(), new CommandWrapper(base));
    }

    private static class CommandWrapper {
        public final ICommand command;

        public CommandWrapper(ICommand command) {
            if (command == null || command.getCommandName() == null) {
                throw new IllegalArgumentException("Commands cannot be null");
            }
            this.command = command;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != getClass()) {
                return false;
            }
            CommandWrapper other = (CommandWrapper) obj;
            return command.getCommandName().equals(other.command.getCommandName());
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            return prime * 1 + command.getCommandName().hashCode();
        }
    }

    @Override
    public String getCommandName() {
        return "jas";
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender) {
        return "commands.jas.usage";
    }

    @Override
    public void process(ICommandSender commandSender, String[] stringArgs) {
        if (stringArgs.length == 0) {
            throw new WrongUsageException("commands.jas.usage", new Object[0]);
        }

        CommandWrapper command = commands.get(stringArgs[0]);
        if (command == null) {
            throw new WrongUsageException("commands.jas.unknowncommand", new Object[0]);
        }
        command.command.processCommand(commandSender, truncateArgs(stringArgs));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getTabCompletions(ICommandSender commandSender, String[] stringArgs) {
        if (stringArgs.length == 1) {
            return getStringsMatchingLastWord(stringArgs, new ArrayList<String>(commands.keySet()));
        } else if (stringArgs.length > 1) {
            CommandWrapper command = commands.get(stringArgs[0]);
            if (command != null) {
                return command.command.addTabCompletionOptions(commandSender, truncateArgs(stringArgs));
            }
        }
        return null;
    }

    /*
     * Used to remove the command name and give child commands only their respective arguments
     * 
     * i.e. User types /jas killall MONSTER, CommandJAS gets {killall MONSTER}, CommandKillAll gets {MONSTER}
     */
    private String[] truncateArgs(String[] stringArgs) {
        String[] args = new String[stringArgs.length - 1];
        for (int i = 0; i < args.length; i++) {
            args[i] = stringArgs[i + 1];
        }
        return args;
    }
}
