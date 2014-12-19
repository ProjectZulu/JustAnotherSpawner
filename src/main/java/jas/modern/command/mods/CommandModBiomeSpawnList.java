package jas.modern.command.mods;

import jas.modern.JustAnotherSpawner;
import jas.modern.command.CommandJasBase;
import jas.modern.modification.ModAddSpawnListEntry;
import jas.modern.modification.ModRemoveSpawnListEntry;
import jas.modern.modification.ModUpdateSpawnListEntry;
import jas.modern.spawner.biome.group.BiomeGroupRegistry;
import jas.modern.spawner.creature.handler.LivingGroupRegistry;
import jas.modern.spawner.creature.handler.LivingHandler;
import jas.modern.spawner.creature.handler.LivingHandlerRegistry;
import jas.modern.spawner.creature.handler.LivingGroupRegistry.LivingGroup;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

public class CommandModBiomeSpawnList extends CommandJasBase {

	public String getCommandName() {
		return "modbiomespawnlist";
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
		return "commands.modbiomespawnlist.usage";
	}

	@Override
	public void process(ICommandSender commandSender, String[] stringArgs) {
		if (stringArgs.length <= 1) {
			throw new WrongUsageException("commands.modbiomespawnlist.usage", new Object[0]);
		}
		// Format /jas modbiomespawnlist <add/Upd/Rmv> <BiomeID><LivingID><Weight><Pack><Min><Max><Tags>
		Ops operation = Ops.determineOp(stringArgs[0]);
		String biomeGroupId = stringArgs[1];
		String livingGroupId = stringArgs[2];
		if (operation == Ops.NONE) {
			throw new WrongUsageException("commands.modbiomespawnlist.biomegroupoperatorundefined", new Object[0]);
		} else if (operation == Ops.REMOVE) {
			JustAnotherSpawner.worldSettings().addChange(new ModRemoveSpawnListEntry(livingGroupId, biomeGroupId));
			return;
		}

		int spawnWeight = Integer.parseInt(stringArgs[3]);
		int packSize = Integer.parseInt(stringArgs[4]);
		int minChunkPack = Integer.parseInt(stringArgs[5]);
		int maxChunkPack = Integer.parseInt(stringArgs[6]);
		String optionalParameters = stringArgs[7];
		if (operation == Ops.ADD) {
			JustAnotherSpawner.worldSettings().addChange(
					new ModAddSpawnListEntry(livingGroupId, biomeGroupId, spawnWeight, packSize, minChunkPack,
							maxChunkPack, optionalParameters));
		} else if (operation == Ops.UPDATE) {
			JustAnotherSpawner.worldSettings().addChange(
					new ModUpdateSpawnListEntry(livingGroupId, biomeGroupId, livingGroupId, biomeGroupId, spawnWeight,
							packSize, minChunkPack, maxChunkPack, optionalParameters));
		}
		throw new WrongUsageException("commands.modbiomespawnlist.biomegroupoperatorundefined", new Object[0]);
	}

	/**
	 * Adds the strings available in this command to the given list of tab completion options.
	 */
	@Override
	public List<String> getTabCompletions(ICommandSender commandSender, String[] stringArgs) {
		stringArgs = correctedParseArgs(stringArgs, false);
		// Format /jas modbiomespawnlist <add/Upd/Rmv> <BiomeID><LivingID><Weight><Pack><Min><Max><Tags>
		List<String> tabCompletions = new ArrayList<String>();
		if (stringArgs.length == 1) {
			for (Ops operation : Ops.values()) {
				for (String matchingWord : operation.matchingWords) {
					tabCompletions.add(matchingWord);
				}
			}
			return tabCompletions;
		} else if (stringArgs.length == 2) {
			BiomeGroupRegistry registry = JustAnotherSpawner.worldSettings().biomeGroupRegistry();
			for (String iD : registry.iDToGroup().keySet()) {
				if (iD.contains(" ")) {
					tabCompletions.add("\"".concat(iD).concat("\""));
				} else {
					tabCompletions.add(iD);
				}
			}
			return tabCompletions;
		} else if (stringArgs.length == 3) {
			LivingHandlerRegistry registry = JustAnotherSpawner.worldSettings().livingHandlerRegistry();
			for (LivingHandler group : registry.getLivingHandlers()) {
				String iD = group.livingID;
				if (iD.contains(" ")) {
					tabCompletions.add("\"".concat(iD).concat("\""));
				} else {
					tabCompletions.add(iD);
				}
			}
			return tabCompletions;
		} else {
			return tabCompletions;
		}
	}
}