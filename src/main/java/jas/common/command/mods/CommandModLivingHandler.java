package jas.common.command.mods;

import jas.common.JustAnotherSpawner;
import jas.common.command.CommandJasBase;
import jas.common.modification.ModAddLivingGroup;
import jas.common.modification.ModAddLivingHandler;
import jas.common.modification.ModRemoveLivingGroup;
import jas.common.modification.ModRemoveLivingHandler;
import jas.common.modification.ModUpdateLivingGroup;
import jas.common.modification.ModUpdateLivingHandler;
import jas.common.spawner.creature.handler.LivingGroupRegistry;
import jas.common.spawner.creature.handler.LivingGroupRegistry.LivingGroup;
import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

public class CommandModLivingHandler extends CommandJasBase {

	public String getCommandName() {
		return "modlivinghandler";
	}

	private enum Ops {
		// TODO Add RESET command to reset to default
		ADD("add", "a", "+"), REMOVE("remove", "rem", "r", "-"), UPDATE("update", "upd", "u", "->"), NONE("");
		public final String[] matchingWords; // Phrases that represent this
												// operation

		Ops(String... matchingWords) {
			this.matchingWords = matchingWords;
		}

		public static Ops determineOp(String potentialWord) {
			for (Ops op : Ops.values()) {
				for (String word : op.matchingWords) {
					if (word.equalsIgnoreCase(potentialWord)) {
						return op;
					}
				}
			}
			return NONE;
		}
	}

	/**
	 * Return the required permission level for this command.
	 */
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "commands.modlivinghandler.usage";
	}

	@Override
	public void process(ICommandSender commandSender, String[] stringArgs) {
		if (stringArgs.length <= 1) {
			throw new WrongUsageException("commands.modlivinghandler.usage", new Object[0]);
		}

		// Format /jas modlivinghandler <add/remove/update>, <groupID>, <creatureTypeId>, <shouldSpawn>, <tags>
		Ops operation = Ops.determineOp(stringArgs[0]);
		String handlerId = stringArgs[1];
		if (operation == Ops.NONE) {
			throw new WrongUsageException("commands.modlivinghandler.livinggroupoperatorundefined", new Object[0]);
		} else if (operation == Ops.REMOVE) {
			JustAnotherSpawner.worldSettings().addChange(new ModRemoveLivingHandler(handlerId));
		} else {
			String creatureTypeID = stringArgs[1];
			boolean shouldSpawn = Boolean.parseBoolean(stringArgs[1]);
			String optionalParameters = stringArgs[1];
			if (operation == Ops.ADD) {
				JustAnotherSpawner.worldSettings().addChange(
						new ModAddLivingHandler(handlerId, creatureTypeID, shouldSpawn, optionalParameters));
			} else if (operation == Ops.UPDATE) {
				JustAnotherSpawner.worldSettings().addChange(
						new ModUpdateLivingHandler(handlerId, creatureTypeID, shouldSpawn, optionalParameters));
			}
		}
		throw new WrongUsageException("commands.modcreaturetype.invalidformat", new Object[0]);
	}

	/**
	 * Adds the strings available in this command to the given list of tab completion options.
	 */
	@Override
	public List<String> getTabCompletions(ICommandSender commandSender, String[] stringArgs) {
		stringArgs = correctedParseArgs(stringArgs, false);
		List<String> tabCompletions = new ArrayList<String>();
		if (stringArgs.length == 1) {
			for (Ops operation : Ops.values()) {
				for (String matchingWord : operation.matchingWords) {
					tabCompletions.add(matchingWord);
				}
			}
			return tabCompletions;
		} else if (stringArgs.length == 2) {
			LivingHandlerRegistry registry = JustAnotherSpawner.worldSettings().livingHandlerRegistry();
			for (LivingHandler handler : registry.getLivingHandlers()) {
				String iD = handler.groupID;
				if (iD.contains(" ")) {
					tabCompletions.add("\"".concat(iD).concat("\""));
				} else {
					tabCompletions.add(iD);
				}
			}
			return tabCompletions;
		} else {
			CreatureTypeRegistry registry = JustAnotherSpawner.worldSettings().creatureTypeRegistry();
			Iterator<CreatureType> iterator = registry.getCreatureTypes();
			while (iterator.hasNext()) {
				CreatureType type = iterator.next();
				String iD = type.typeID;
				if (iD.contains(" ")) {
					tabCompletions.add("\"".concat(iD).concat("\""));
				} else {
					tabCompletions.add(iD);
				}
			}
			return tabCompletions;
		}
	}
}