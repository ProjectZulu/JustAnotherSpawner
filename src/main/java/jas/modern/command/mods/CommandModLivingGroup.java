package jas.modern.command.mods;
//package jas.common.command.mods;
//
//import jas.common.JustAnotherSpawner;
//import jas.common.command.CommandJasBase;
//import jas.common.spawner.creature.handler.LivingGroupRegistry;
//import jas.common.spawner.creature.handler.LivingHandler;
//import jas.common.spawner.creature.handler.LivingHandlerRegistry;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import net.minecraft.command.ICommandSender;
//import net.minecraft.command.WrongUsageException;
//
//public class CommandModLivingGroup extends CommandJasBase {
//
//	public String getCommandName() {
//		return "modlivinggroup";
//	}
//
//	private enum Ops {
//		// TODO Add RESET command to reset to default
//		ADD("add", "a", "+"), REMOVE("remove", "rem", "r", "-"), UPDATE("update", "upd", "u", "->"), NONE("");
//		public final String[] matchingWords; // Phrases that represent this
//												// operation
//
//		Ops(String... matchingWords) {
//			this.matchingWords = matchingWords;
//		}
//
//		public static Ops determineOp(String potentialWord) {
//			for (Ops op : Ops.values()) {
//				for (String word : op.matchingWords) {
//					if (word.equalsIgnoreCase(potentialWord)) {
//						return op;
//					}
//				}
//			}
//			return NONE;
//		}
//	}
//
//	/**
//	 * Return the required permission level for this command.
//	 */
//	public int getRequiredPermissionLevel() {
//		return 2;
//	}
//
//	@Override
//	public String getCommandUsage(ICommandSender commandSender) {
//		return "commands.modlivinggroup.usage";
//	}
//
//	@Override
//	public void process(ICommandSender commandSender, String[] stringArgs) {
//		if (stringArgs.length <= 1) {
//			throw new WrongUsageException("commands.modlivinggroup.usage", new Object[0]);
//		}
//
//		// Format /jas modlivinggroup <add/remove/update> <livingGroupID>
//		// <mapping/A|attribute/B|group#1>,
//		Ops operation = Ops.determineOp(stringArgs[0]);
//		String livingGroupID = stringArgs[1];
//		ArrayList<String> groupContents = new ArrayList<String>();
//		for (int i = 2; i < stringArgs.length; i++) {
//			if (stringArgs[i] == null || stringArgs[i].trim().isEmpty()) {
//				continue;
//			}
//			groupContents.add(stringArgs[i]);
//		}
//		if (livingGroupID != null && !livingGroupID.isEmpty()) {
//			switch (operation) {
//			case ADD:
//				JustAnotherSpawner.worldSettings().addChange(new ModAddLivingGroup(livingGroupID, groupContents));
//				break;
//			case REMOVE:
//				JustAnotherSpawner.worldSettings().addChange(new ModRemoveLivingGroup(livingGroupID));
//				break;
//			case UPDATE:
//				JustAnotherSpawner.worldSettings().addChange(new ModUpdateLivingGroup(livingGroupID, groupContents));
//				break;
//			case NONE:
//				throw new WrongUsageException("commands.modlivinggroup.livinggroupoperatorundefined", new Object[0]);
//			}
//		} else {
//			throw new WrongUsageException("commands.modlivinggroup.livinggroupundefined", new Object[0]);
//		}
//	}
//
//	/**
//	 * Adds the strings available in this command to the given list of tab completion options.
//	 */
//	@Override
//	public List<String> getTabCompletions(ICommandSender commandSender, String[] stringArgs) {
//		stringArgs = correctedParseArgs(stringArgs, false);
//		List<String> tabCompletions = new ArrayList<String>();
//		if (stringArgs.length == 1) {
//			for (Ops operation : Ops.values()) {
//				for (String matchingWord : operation.matchingWords) {
//					tabCompletions.add(matchingWord);
//				}
//			}
//			return tabCompletions;
//		} else if (stringArgs.length == 2) {
//			LivingHandlerRegistry registry = JustAnotherSpawner.worldSettings().livingHandlerRegistry();
//			for (LivingHandler group : registry.getLivingHandlers()) {
//				String iD = group.livingID;
//				if (iD.contains(" ")) {
//					tabCompletions.add("\"".concat(iD).concat("\""));
//				} else {
//					tabCompletions.add(iD);
//				}
//			}
//			return tabCompletions; // livingGroupID can be anything when adding
//		} else {
//			LivingGroupRegistry registry = JustAnotherSpawner.worldSettings().livingGroupRegistry();
//			for (String mapping : registry.entityClasstoJASName().values()) {
//				if (mapping.contains(" ")) {
//					tabCompletions.add("\"".concat(mapping).concat("\""));
//				} else {
//					tabCompletions.add(mapping);
//				}
//			}
//			for (String iD : registry.iDToAttribute().keySet()) {
//				if (iD.contains(" ")) {
//					tabCompletions.add("\"".concat("A|").concat(iD).concat("\""));
//				} else {
//					tabCompletions.add(("A|").concat(iD));
//				}
//			}
//			LivingHandlerRegistry livingHandlerRegistry = JustAnotherSpawner.worldSettings().livingHandlerRegistry();
//			for (LivingHandler group : livingHandlerRegistry.getLivingHandlers()) {
//				String iD = group.livingID;
//				if (iD.contains(" ")) {
//					tabCompletions.add("\"".concat(iD).concat("\""));
//				} else {
//					tabCompletions.add(iD);
//				}
//			}
//			return tabCompletions;
//		}
//	}
//}