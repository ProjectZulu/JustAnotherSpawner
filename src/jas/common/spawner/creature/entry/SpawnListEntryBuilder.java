package jas.common.spawner.creature.entry;

public class SpawnListEntryBuilder {
    private String livingGroupId;
    private String locationGroupId;
    private int weight;
    private int packSize;
    private int minChunkPack;
    private int maxChunkPack;
    private String optionalParameters;

    public SpawnListEntryBuilder() {
        this.livingGroupId = null;
        this.locationGroupId = null;
        this.weight = 0;
        this.packSize = 4;
        this.minChunkPack = 0;
        this.maxChunkPack = 4;
        this.optionalParameters = "";
    }

    public SpawnListEntryBuilder(String livingGroupId, String biomeGroupId) {
        this.livingGroupId = livingGroupId;
        this.locationGroupId = biomeGroupId;
        this.weight = 0;
        this.packSize = 4;
        this.minChunkPack = 0;
        this.maxChunkPack = 4;
        this.optionalParameters = "";
    }

    public SpawnListEntryBuilder(SpawnListEntry entry) {
        this.livingGroupId = entry.livingGroupID;
        this.locationGroupId = entry.pckgName;
        this.weight = entry.itemWeight;
        this.packSize = entry.packSize;
        this.minChunkPack = entry.minChunkPack;
        this.maxChunkPack = entry.maxChunkPack;
        this.optionalParameters = entry.optionalParameters;
    }

    public String getLivingGroupId() {
        return livingGroupId;
    }

    public SpawnListEntryBuilder setLivingGroupId(String livingGroupId) {
        this.livingGroupId = livingGroupId;
        return this;
    }

    public String getBiomeGroupId() {
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

    public String getOptionalParameters() {
        return optionalParameters;
    }

    public SpawnListEntryBuilder setOptionalParameters(String optionalParameters) {
        if (optionalParameters == null) {
            optionalParameters = "";
        }
        this.optionalParameters = optionalParameters;
        return this;
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
