package jas.common.spawner.creature.entry;

import jas.common.DefaultProps;
import jas.common.JustAnotherSpawner;
import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.handler.parsing.keys.Key;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettingsPostSpawning;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettingsSpawnListSpawning;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.io.Serializable;
import java.util.Locale;

import org.mvel2.MVEL;

import com.google.common.base.Optional;

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
    
	public final String spawnExpression;
	public final String postspawnExpression;
	public final Optional<Operand> spawnOperand;
	private Optional<Serializable> compSpawnExpression;
	private Optional<Serializable> compPostSpawnExpression;

	public Optional<Serializable> getOptionalSpawning() {
		return compSpawnExpression;
	}

	public Optional<Serializable> getOptionalPostSpawning() {
		return compPostSpawnExpression;
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
        this.spawnOperand = builder.getSpawnOperand();
		this.spawnExpression = builder.getSpawnExpression();
		this.postspawnExpression = builder.getPostSpawnExpression();
		this.compSpawnExpression = !spawnExpression.trim().equals("") ? Optional.of(MVEL
				.compileExpression(spawnExpression)) : Optional.<Serializable> absent();
		this.compPostSpawnExpression = !postspawnExpression.trim().equals("") ? Optional.of(MVEL
				.compileExpression(postspawnExpression)) : Optional.<Serializable> absent();
    }

    // TODO: Remove This. Hidden static dependency bad. Unnecessary. Alternatively, pass in livingHandlerRegistry
    public LivingHandler getLivingHandler() {
        return JustAnotherSpawner.worldSettings().livingHandlerRegistry().getLivingHandler(livingGroupID);
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
}
