package jas.common.command;

import jas.common.spawner.biome.group.BiomeHelper;
import jas.common.spawner.creature.entry.SpawnListEntry;
import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.world.World;

public class CommandCanSpawnHere extends CommandJasBase {
    public String getCommandName() {
        return "jascanspawnhere";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender) {
        return "commands.jascanspawnhere.usage";
    }

    @Override
    public void process(ICommandSender commandSender, String[] stringArgs) {
        if (stringArgs.length == 0 || stringArgs.length >= 3) {
            throw new WrongUsageException("commands.jascanspawnhere.usage", new Object[0]);
        }

        EntityPlayerMP targetPlayer = func_82359_c(commandSender, stringArgs[0]);
        String biomePckgName = BiomeHelper.getPackageName(targetPlayer.worldObj.getBiomeGenForCoords(
                (int) targetPlayer.posX, (int) targetPlayer.posZ));
        String entityName = stringArgs[1];

        if (!isValidEntityName(entityName)) {
            throw new WrongUsageException("commands.jascanspawnhere.entitynotfound", new Object[0]);
        }

        boolean foundMatch = false;
        Iterator<CreatureType> iterator = CreatureTypeRegistry.INSTANCE.getCreatureTypes();
        while (iterator.hasNext()) {
            CreatureType entityType = iterator.next();
            for (SpawnListEntry entry : entityType.getSpawnList(biomePckgName)) {
                if (entityName.equalsIgnoreCase((String) EntityList.classToStringMapping.get(entry.livingClass))) {
                    foundMatch = true;
                    EntityLiving entityliving;
                    try {
                        entityliving = entry.getLivingHandler().entityClass.getConstructor(new Class[] { World.class })
                                .newInstance(new Object[] { targetPlayer.worldObj });
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        return;
                    }

                    if (entityliving == null) {
                        throw new WrongUsageException("commands.jascanspawnhere.invalidinvocation", new Object[0]);
                    }

                    entityliving.setLocationAndAngles(targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ,
                            targetPlayer.rotationYaw, targetPlayer.rotationPitch);

                    boolean tempPreventEntitySpawning = targetPlayer.preventEntitySpawning;
                    targetPlayer.preventEntitySpawning = false;
                    boolean typeCanSpawnHere = entityType.canSpawnAtLocation(targetPlayer.worldObj,
                            (int) targetPlayer.posX, (int) targetPlayer.posY, (int) targetPlayer.posZ);
                    boolean entityCanSpawnHere = entry.getLivingHandler().getCanSpawnHere(entityliving, entry);
                    targetPlayer.preventEntitySpawning = tempPreventEntitySpawning;

                    StringBuilder resultMessage = new StringBuilder();
                    resultMessage.append(typeCanSpawnHere ? "\u00A72" : "\u00A74").append(entityType.typeID)
                            .append(" type ").append(typeCanSpawnHere ? "can" : "cannot").append(" spawn here. ");
                    resultMessage.append(entityCanSpawnHere ? "\u00A72" : "\u00A74").append(entityName)
                            .append(" entity ").append(entityCanSpawnHere ? "can" : "cannot").append(" spawn here.");
                    commandSender.sendChatToPlayer(new ChatMessageComponent().func_111079_a(resultMessage.toString()));
                    return;
                }
            }

        }

        if (!foundMatch) {
            commandSender.sendChatToPlayer(new ChatMessageComponent().func_111079_a("\u00A76".concat(entityName)
                    .concat(" does not exist in the spawnlist of ".concat(biomePckgName).concat("."))));
        }
    }

    private boolean isValidEntityName(String entityName) {
        for (Object object : EntityList.classToStringMapping.values()) {
            String mapping = (String) object;
            if (entityName.equals(mapping)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds the strings available in this command to the given list of tab completion options.
     */
    @Override
    public List<String> getTabCompletions(ICommandSender commandSender, String[] stringArgs) {
        stringArgs = correctedParseArgs(stringArgs, false);
        List<String> tabCompletions = new ArrayList<String>();
        if (stringArgs.length == 1) {
            addPlayerUsernames(tabCompletions);
        } else if (stringArgs.length == 2) {
            addEntityNames(tabCompletions);
        }

        if (!tabCompletions.isEmpty()) {
            return getStringsMatchingLastWord(stringArgs, tabCompletions);
        } else {
            return tabCompletions;
        }
    }
}