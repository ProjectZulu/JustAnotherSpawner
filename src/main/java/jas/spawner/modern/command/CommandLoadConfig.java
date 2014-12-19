package jas.spawner.modern.command;

import jas.common.JustAnotherSpawner;
import jas.spawner.modern.MVELProfile;
import jas.spawner.modern.modification.ModLoadConfig;

import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

public class CommandLoadConfig extends CommandJasBase {
    public String getCommandName() {
        return "loadconfig";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender) {
        return "commands.jasloadconfig.usage";
    }

    @Override
    public void process(ICommandSender commandSender, String[] stringArgs) {
        if (stringArgs.length > 0) {
            throw new WrongUsageException("commands.jasloadconfig.usage", new Object[0]);
        }
        MVELProfile.worldSettings().addChange(new ModLoadConfig(JustAnotherSpawner.getModConfigDirectory(),
                MinecraftServer.getServer().worldServers[0]));
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    @Override
    public List<String> getTabCompletions(ICommandSender commandSender, String[] stringArgs) {
        return null;
    }
}