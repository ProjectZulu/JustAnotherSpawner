package jas.spawner.modern.command;

import jas.spawner.modern.MVELProfile;
import jas.spawner.modern.spawner.creature.type.CreatureType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public abstract class CommandJasBase extends CommandBase {

    @Override
    public final void processCommand(ICommandSender commandSender, String[] stringArgs) {
        process(commandSender, correctedParseArgs(stringArgs, true));
    }

    public abstract void process(ICommandSender commandSender, String[] stringArgs);

    @Override
    @SuppressWarnings("rawtypes")
    public final List addTabCompletionOptions(ICommandSender commandSender, String[] stringArgs) {
        return getTabCompletions(commandSender, correctedParseArgs(stringArgs, false));
    }

    public abstract List<String> getTabCompletions(ICommandSender commandSender, String[] stringArgs);

    protected String[] correctedParseArgs(String[] stringArgs, boolean removeQuotations) {
        ArrayList<String> endArgs = new ArrayList<String>();
        String intermediate = "";
        for (int i = 0; i < stringArgs.length; i++) {
            String arg = stringArgs[i];
            if (arg.startsWith("\"")) {
                intermediate = intermediate.concat(arg.substring(removeQuotations ? 1 : 0));
            } else if (arg.endsWith("\"")) {
                intermediate = intermediate.concat(" ").concat(
                        arg.substring(0, removeQuotations ? arg.length() - 1 : arg.length()));
                endArgs.add(intermediate);
                intermediate = "";
            } else if (!intermediate.equals("")) {
                intermediate = intermediate.concat(" ").concat(arg);
            } else {
                endArgs.add(arg);
            }
        }

        if (!intermediate.equals("")) {
            endArgs.add(intermediate);
        }
        return endArgs.toArray(new String[endArgs.size()]);
    }

    protected List<String> getStringsMatchingLastWord(String[] stringArgs, List<String> tabCompletions) {
        String lastArg = stringArgs[stringArgs.length - 1];
        ArrayList<String> arraylist = new ArrayList<String>();

        for (int j = 0; j < tabCompletions.size(); ++j) {
            String tabComplete = tabCompletions.get(j);

            if (doesStartWith(lastArg, tabComplete)) {
                tabComplete = tabComplete.replaceFirst(determineRemovalString(stringArgs), "");
                arraylist.add(tabComplete);
            }
        }
        return arraylist;
    }

    /**
     * String that must be removed from tabCompletion. Used to remove first characters from words that span multiple
     * arguments such as "TwilightForest.Hedge Spider" is actually []{"TwilightForest.Hedge,Spider"}
     */
    protected String determineRemovalString(String[] stringArgs) {
        String lastArg = stringArgs[stringArgs.length - 1];
        String[] spaceDelArgs;
        if (lastArg.endsWith(" ")) {
            String[] segments = lastArg.split(" ");
            spaceDelArgs = new String[segments.length + 1];
            for (int i = 0; i < spaceDelArgs.length - 1; i++) {
                spaceDelArgs[i] = segments[i];
            }
            spaceDelArgs[spaceDelArgs.length - 1] = "";
        } else {
            spaceDelArgs = lastArg.split(" ");
        }

        String removalString = "";
        for (int i = 0; i < spaceDelArgs.length - 1; i++) {
            removalString = removalString.concat(spaceDelArgs[i]).concat(" ");
        }
        return removalString;
    }

    /**
     * Returns true if the given substring is exactly equal to the start of the given string (case insensitive).
     */
    protected boolean doesStartWith(String wordFragment, String completeWord) {
        int fragmentOffset = wordFragment.startsWith("\"") ? 1 : 0;
        int completeOffset = completeWord.startsWith("\"") ? 1 : 0;
        return completeWord.regionMatches(true, completeOffset, wordFragment, fragmentOffset, wordFragment.length()
                - fragmentOffset);
    }

    /**
     * Helper used to add Entity names to tabCompletetion list. Names with spaces are surrounded with quotation marks.
     */
	public static void addEntityNames(List<String> tabCompletions) {
		for (String entityName : MVELProfile.worldSettings().livingGroupRegistry().JASNametoEntityClass.keySet()) {
			if (entityName.contains(" ")) {
				entityName = "\"".concat(entityName).concat("\"");
			}
			tabCompletions.add(entityName);
		}
	}

    /**
     * Helper used to add Entity category names to tabCompletetion list.
     */
    public static void addEntityTypes(List<String> tabCompletions) {
        Iterator<CreatureType> iterator = MVELProfile.worldSettings().creatureTypeRegistry().getCreatureTypes();
        while (iterator.hasNext()) {
            CreatureType entityType = iterator.next();
            tabCompletions.add(entityType.typeID);
        }
    }

    /**
     * Helper used to add logged in player usernames to tabCompletetion list.
     */
    public static void addPlayerUsernames(List<String> tabCompletions) {
        for (String username : MinecraftServer.getServer().getAllUsernames()) {
            tabCompletions.add(username);
        }
    }
}
