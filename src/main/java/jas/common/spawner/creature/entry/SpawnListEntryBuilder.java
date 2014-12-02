package jas.common.spawner.creature.entry;

import java.util.ArrayList;
import java.util.HashSet;

import jas.common.spawner.creature.handler.LivingHandlerBuilder;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import com.google.common.base.Optional;

public class SpawnListEntryBuilder {
    private String livingGroupId;
    private String locationGroupId; // BiomeGroupId or StructureGroupId
    private int weight;
    private int packSize;
    private int minChunkPack;
    private int maxChunkPack;
    
	private String spawnExpression;
	private String postspawnExpression;
	private Optional<Operand> spawnOperand;
    
    public SpawnListEntryBuilder() {
        this.livingGroupId = null;
        this.locationGroupId = null;
        this.weight = 0;
        this.packSize = 4;
        this.minChunkPack = 0;
        this.maxChunkPack = 4;
        this.setSpawnExpression("", Optional.<Operand> absent());
		this.setPostSpawnExpression("");
    }

    public SpawnListEntryBuilder(String livingGroupId, String biomeGroupId) {
        this.livingGroupId = livingGroupId;
        this.locationGroupId = biomeGroupId;
        this.weight = 0;
        this.packSize = 4;
        this.minChunkPack = 0;
        this.maxChunkPack = 4;
		this.setSpawnExpression("", Optional.<Operand> absent());
		this.setPostSpawnExpression("");
    }

    public SpawnListEntryBuilder(SpawnListEntry entry) {
        this.livingGroupId = entry.livingGroupID;
        this.locationGroupId = entry.locationGroup;
        this.weight = entry.itemWeight;
        this.packSize = entry.packSize;
        this.minChunkPack = entry.minChunkPack;
        this.maxChunkPack = entry.maxChunkPack;
		this.setSpawnExpression(entry.spawnExpression, entry.spawnOperand);
		this.setPostSpawnExpression(entry.postspawnExpression);
    }

    public String getLivingGroupId() {
        return livingGroupId;
    }

    public SpawnListEntryBuilder setLivingGroupId(String livingGroupId) {
        this.livingGroupId = livingGroupId;
        return this;
    }

    public String getLocationGroupId() {
        return locationGroupId;
    }

    public SpawnListEntryBuilder setBiomeGroupId(String biomeGroupId) {
        this.locationGroupId = biomeGroupId;
        return this;
    }

    public int getWeight() {
        return weight;
    }

    public SpawnListEntryBuilder setWeight(int weight) {
        this.weight = weight;
        return this;
    }

    public int getPackSize() {
        return packSize;
    }

    public SpawnListEntryBuilder setPackSize(int packSize) {
        this.packSize = packSize;
        return this;
    }

    public int getMinChunkPack() {
        return minChunkPack;
    }

    public SpawnListEntryBuilder setMinChunkPack(int minChunkPack) {
        this.minChunkPack = minChunkPack;
        return this;
    }

    public int getMaxChunkPack() {
        return maxChunkPack;
    }

    public SpawnListEntryBuilder setMaxChunkPack(int maxChunkPack) {
        this.maxChunkPack = maxChunkPack;
        return this;
    }
    
	public SpawnListEntryBuilder setSpawnExpression(String optionalParameters, Optional<Operand> spawnOperand) {
		if (optionalParameters == null || optionalParameters.trim().equals("")) {
			this.spawnExpression = "";
			this.spawnOperand = Optional.absent();
		} else {
			if (!spawnOperand.isPresent()) {
				spawnOperand = Optional.of(Operand.OR);
			}
			this.spawnExpression = optionalParameters;
			this.spawnOperand = spawnOperand;
		}
		return this;
	}

	public String getSpawnExpression() {
		return spawnExpression;
	}

	public Optional<Operand> getSpawnOperand() {
		return spawnOperand;
	}

	public SpawnListEntryBuilder setPostSpawnExpression(String optionalParameters) {
		if (optionalParameters == null) {
			optionalParameters = "";
		}
		this.postspawnExpression = optionalParameters;
		return this;
	}

	public String getPostSpawnExpression() {
		return postspawnExpression;
	}
    
    public SpawnListEntry build() {
        if (livingGroupId == null || livingGroupId.trim().equals("")) {
            throw new IllegalArgumentException("LivingGroupID cannot be " + livingGroupId != null ? "empty." : "null.");
        }

        if (locationGroupId == null || locationGroupId.trim().equals("")) {
            throw new IllegalArgumentException("BiomeGroupID cannot be " + locationGroupId != null ? "empty." : "null.");
        }
        return new SpawnListEntry(this);
    }
}
