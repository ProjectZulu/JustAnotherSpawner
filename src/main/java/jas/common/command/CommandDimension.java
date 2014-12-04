package jas.common.command;

import jas.common.JustAnotherSpawner;
import jas.common.spawner.CountInfo;
import jas.common.spawner.CustomSpawner;
import jas.common.spawner.EntityCounter;
import jas.common.spawner.EntityCounter.CountableInt;
import jas.common.spawner.creature.handler.LivingGroupRegistry;
import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.type.CreatureType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

public class CommandDimension extends CommandJasBase {

	public String getCommandName() {
		return "dimension";
	}

	/**
	 * Return the required permission level for this command.
	 */
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "commands.jasdimension.usage";
	}

	@Override
	public void process(ICommandSender commandSender, String[] stringArgs) {
		if (stringArgs.length > 0) {
			throw new WrongUsageException("commands.jasdimension.usage", new Object[0]);
		}

		StringBuilder builder = new StringBuilder();
		builder.append("Current Dimension is ").append(commandSender.getEntityWorld().provider.dimensionId);
		commandSender.addChatMessage(new ChatComponentText(builder.toString()));
	}

	/**
	 * Adds the strings available in this command to the given list of tab completion options.
	 */
	@Override
	public List<String> getTabCompletions(ICommandSender commandSender, String[] stringArgs) {
		return Collections.emptyList();
	}
}
