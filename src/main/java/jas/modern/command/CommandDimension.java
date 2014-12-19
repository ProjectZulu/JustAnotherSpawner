package jas.modern.command;

import jas.modern.JustAnotherSpawner;
import jas.modern.spawner.CountInfo;
import jas.modern.spawner.CustomSpawner;
import jas.modern.spawner.EntityCounter;
import jas.modern.spawner.EntityCounter.CountableInt;
import jas.modern.spawner.creature.handler.LivingGroupRegistry;
import jas.modern.spawner.creature.handler.LivingHandler;
import jas.modern.spawner.creature.handler.LivingHandlerRegistry;
import jas.modern.spawner.creature.type.CreatureType;

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
		builder.append("Current Dimension ID is ").append(commandSender.getEntityWorld().provider.dimensionId);
		builder.append(" aka ").append(commandSender.getEntityWorld().provider.getDimensionName());
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
