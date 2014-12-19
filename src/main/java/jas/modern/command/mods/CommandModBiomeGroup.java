package jas.modern.command.mods;

import jas.modern.command.CommandJasBase;
import jas.modern.modification.ModAddBiomeGroup;
import jas.modern.modification.ModRemoveBiomeGroup;
import jas.modern.modification.ModUpdateBiomeGroup;
import jas.modern.profile.MVELProfile;
import jas.modern.spawner.biome.group.BiomeGroupRegistry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;

public class CommandModBiomeGroup extends CommandJasBase {

	public String getCommandName() {
		return "modbiomegroup";
	}

	private enum Ops {
		//TODO Add RESET command to reset to default
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
		return "commands.modbiomegroup.usage";
	}

	@Override
	public void process(ICommandSender commandSender, String[] stringArgs) {
		if (stringArgs.length <= 1) {
			throw new WrongUsageException("commands.modbiomegroup.usage", new Object[0]);
		}

		// Format /jas addbgroup <add/remove/update> <biomeGroupID>
		// <mapping/A|attribute/B|group#1>,
		Ops operation = Ops.determineOp(stringArgs[0]);
		String biomeGroupID = stringArgs[1];
		ArrayList<String> groupContents = new ArrayList<String>();
		for (int i = 2; i < stringArgs.length; i++) {
			if (stringArgs[i] == null || stringArgs[i].trim().isEmpty()) {
				continue;
			}
			groupContents.add(stringArgs[i]);
		}
		if (biomeGroupID != null && !biomeGroupID.isEmpty()) {
			switch (operation) {
			case ADD:
				MVELProfile.worldSettings().addChange(new ModAddBiomeGroup(biomeGroupID, groupContents));
				break;
			case REMOVE:
				MVELProfile.worldSettings().addChange(new ModRemoveBiomeGroup(biomeGroupID));
				break;
			case UPDATE:
				MVELProfile.worldSettings().addChange(new ModUpdateBiomeGroup(biomeGroupID, groupContents));
				break;
			case NONE:
				throw new WrongUsageException("commands.modbiomegroup.biomegroupoperatorundefined", new Object[0]);
			}
		} else {
			throw new WrongUsageException("commands.modbiomegroup.biomegroupundefined", new Object[0]);
		}
	}

	/**
	 * Adds the strings available in this command to the given list of tab
	 * completion options.
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
			BiomeGroupRegistry registry = MVELProfile.worldSettings().biomeGroupRegistry();
			for (String iD : registry.iDToGroup().keySet()) {
				if (iD.contains(" ")) {
					tabCompletions.add("\"".concat(iD).concat("\""));
				} else {
					tabCompletions.add(iD);
				}
			}
			return tabCompletions; // BiomeGroupID can be anything when adding
		} else {
			BiomeGroupRegistry registry = MVELProfile.worldSettings().biomeGroupRegistry();
			for (String mapping : registry.biomeMappingToPckg().keySet()) {
				if (mapping.contains(" ")) {
					tabCompletions.add("\"".concat(mapping).concat("\""));
				} else {
					tabCompletions.add(mapping);
				}
			}
			for (String iD : registry.iDToAttribute().keySet()) {
				if (iD.contains(" ")) {
					tabCompletions.add("\"".concat("A|").concat(iD).concat("\""));
				} else {
					tabCompletions.add(("A|").concat(iD));
				}
			}
			for (String iD : registry.iDToGroup().keySet()) {
				if (iD.contains(" ")) {
					tabCompletions.add("\"".concat("B|").concat(iD).concat("\""));
				} else {
					tabCompletions.add(("B|").concat(iD));
				}
			}
			return tabCompletions;
		}
	}
}
