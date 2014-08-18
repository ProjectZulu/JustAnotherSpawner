package jas.common.command.mods;

import jas.common.JustAnotherSpawner;
import jas.common.command.CommandJasBase;
import jas.common.modification.ModAddBiomeGroup;
import jas.common.modification.ModAddCreatureType;
import jas.common.modification.ModRemoveBiomeGroup;
import jas.common.modification.ModRemoveCreatureType;
import jas.common.modification.ModUpdateBiomeGroup;
import jas.common.modification.ModUpdateCreatureType;
import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeBuilder;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

import com.google.common.base.Optional;

public class CommandModCreatureType extends CommandJasBase {

	public String getCommandName() {
		return "modcreaturetype";
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
		return "commands.modcreaturetype.usage";
	}

	@Override
	public void process(ICommandSender commandSender, String[] stringArgs) {
		if (stringArgs.length <= 1) {
			throw new WrongUsageException("commands.modcreaturetype.usage", new Object[0]);
		}
		// Format /jas modcreaturetype <add/remove/update> <creatureTypeId> <spawnRate> <maxNumberOfCreature>
		// <chunkSpawnChance> <spawnMedium> <tag>
		Ops operation = Ops.determineOp(stringArgs[0]);
		String creatureTypeId = stringArgs[1];

		if (creatureTypeId != null && !creatureTypeId.isEmpty()) {
			if (operation == Ops.REMOVE) {
				JustAnotherSpawner.worldSettings().addChange(new ModRemoveCreatureType(creatureTypeId));
			} else {
				if (stringArgs.length >= 7) {
					try {
						int spawnRate = Integer.parseInt(stringArgs[2]);
						int maxNumberOfCreature = Integer.parseInt(stringArgs[3]);
						int chunkSpawnChance = Integer.parseInt(stringArgs[4]);
						String spawnMedium = stringArgs[5];
						String tag = stringArgs[6];
						if (operation == Ops.ADD) {
							JustAnotherSpawner.worldSettings().addChange(
									new ModAddCreatureType(creatureTypeId, spawnRate, maxNumberOfCreature,
											chunkSpawnChance, spawnMedium, tag));
						} else if (operation == Ops.UPDATE) {
							JustAnotherSpawner.worldSettings().addChange(
									new ModUpdateCreatureType(creatureTypeId, spawnRate, maxNumberOfCreature,
											chunkSpawnChance, spawnMedium, tag));
						}
					} catch (Exception e) {
					}
				} else if (stringArgs.length >= 4) {
					try {
						int spawnRate = Integer.parseInt(stringArgs[2]);
						int maxNumberOfCreature = Integer.parseInt(stringArgs[3]);
						if (operation == Ops.ADD) {
							JustAnotherSpawner.worldSettings().addChange(
									new ModAddCreatureType(creatureTypeId, spawnRate, maxNumberOfCreature));
						} else if (operation == Ops.UPDATE) {
							JustAnotherSpawner.worldSettings().addChange(
									new ModUpdateCreatureType(creatureTypeId, spawnRate, maxNumberOfCreature));
						}
					} catch (NumberFormatException e) {
					}
				}
				if (operation == Ops.NONE) {
					throw new WrongUsageException("commands.modcreaturetype.operatorundefined", new Object[0]);
				} else {
					throw new WrongUsageException("commands.modcreaturetype.invalidformat", new Object[0]);
				}
			}
		} else {
			throw new WrongUsageException("commands.modcreaturetype.creaturetypeundefined", new Object[0]);
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
			// Ops operation = Ops.determineOp(stringArgs[0]);
			for (Ops operation : Ops.values()) {
				for (String matchingWord : operation.matchingWords) {
					tabCompletions.add(matchingWord);
				}
			}
			return tabCompletions;
		} else if (stringArgs.length == 2) {
			// String creatureTypeId = stringArgs[1];
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
			return tabCompletions; // BiomeGroupID can be anything when adding
		} else if (stringArgs.length == 6) {
			// String spawnMedium = stringArgs[5];
			tabCompletions.add("Water");
			tabCompletions.add("Air");
			return tabCompletions;
		} else {
			// int spawnRate = Integer.parseInt(stringArgs[2]);
			// int maxNumberOfCreature = Integer.parseInt(stringArgs[3]);
			// int chunkSpawnChance = Integer.parseInt(stringArgs[4]);
			// String tag = stringArgs[6];
			return tabCompletions;
		}
	}
}