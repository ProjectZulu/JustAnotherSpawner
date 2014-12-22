package jas.spawner.legacy.spawner.creature.entry;

import jas.spawner.legacy.TAGProfile;
import jas.spawner.legacy.spawner.creature.handler.LivingHandler;
import jas.spawner.legacy.spawner.creature.handler.parsing.keys.Key;
import jas.spawner.legacy.spawner.creature.handler.parsing.settings.OptionalSettingsPostSpawning;
import jas.spawner.legacy.spawner.creature.handler.parsing.settings.OptionalSettingsSpawnListSpawning;
import jas.spawner.modern.DefaultProps;

import java.util.Locale;

import net.minecraft.util.WeightedRandom;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

/**
 * Every SpawnListEntry is assumed Unique for a EntityLivingClass given biome Spawn.
 * 
 * It should be noted that Technically, f(Class, Biome, CreatureType) --> SpawnList, but since f(Class) --> CreatureType
 * then f(Class, Biome) --> SpawnList
 */
// TODO: Large Constructor could probably use Factory / Or Split packSize into Its Own Immutable Class
public class SpawnListEntry extends WeightedRandom.Item {
    public final String livingGroupID;
    public final int packSize;
    /* Refers to BiomeGroup or StructureGroup */
    public final String locationGroup;
    public final int minChunkPack;
    public final int maxChunkPack;
    public final String optionalParameters;
    protected OptionalSettingsSpawnListSpawning spawning;
    protected OptionalSettingsPostSpawning postspawning;

    public OptionalSettingsSpawnListSpawning getOptionalSpawning() {
        return spawning;
    }

    public OptionalSettingsPostSpawning getOptionalPostSpawning() {
        return postspawning;
    }

    public static final String SpawnListCategoryComment = "Editable Format: SpawnWeight" + DefaultProps.DELIMETER
            + "SpawnPackSize" + DefaultProps.DELIMETER + "MinChunkPackSize" + DefaultProps.DELIMETER
            + "MaxChunkPackSize";

    public SpawnListEntry(SpawnListEntryBuilder builder) {
        super(builder.getWeight());
        this.livingGroupID = builder.getLivingGroupId();
        this.packSize = builder.getPackSize();
        this.locationGroup = builder.getLocationGroupId();
        this.minChunkPack = builder.getMinChunkPack();
        this.maxChunkPack = builder.getMaxChunkPack();
        this.optionalParameters = builder.getOptionalParameters();

        for (String string : optionalParameters.split("\\{")) {
            String parsed = string.replace("}", "");
            String titletag = parsed.split("\\:", 2)[0].toLowerCase();
            if (Key.spawn.keyParser.isMatch(titletag)) {
                spawning = new OptionalSettingsSpawnListSpawning(parsed);
            } else if (Key.postspawn.keyParser.isMatch(titletag)) {
                postspawning = new OptionalSettingsPostSpawning(parsed);
            }
        }
        spawning = spawning == null ? new OptionalSettingsSpawnListSpawning("") : spawning;
        postspawning = postspawning == null ? new OptionalSettingsPostSpawning("") : postspawning;
    }

    // TODO: Remove This. Hidden static dependency bad. Unnecessary. Alternatively, pass in livingHandlerRegistry
    public LivingHandler getLivingHandler() {
        return TAGProfile.worldSettings().livingHandlerRegistry().getLivingHandler(livingGroupID);
    }

    public static void setupConfigCategory(Configuration config) {
        ConfigCategory category = config.getCategory("CreatureSettings.SpawnListEntry".toLowerCase(Locale.ENGLISH));
        category.setComment(SpawnListEntry.SpawnListCategoryComment);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((locationGroup == null) ? 0 : locationGroup.hashCode());
        result = prime * result + ((livingGroupID == null) ? 0 : livingGroupID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other == null || getClass() != other.getClass()) {
            return false;
        }

        SpawnListEntry otherEntry = (SpawnListEntry) other;
        return locationGroup.equals(otherEntry.locationGroup) && livingGroupID.equals(otherEntry.livingGroupID);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[SpawnListEntry: ID ").append(livingGroupID).append(",");
        sb.append(" Biome ").append(locationGroup).append(",");
        sb.append(" Stats ").append(itemWeight).append("/").append(packSize).append("/").append(minChunkPack)
                .append("/").append(maxChunkPack).append(",");
        sb.append(" Tags ").append(optionalParameters).append("]");
        return sb.toString();
    }
}
