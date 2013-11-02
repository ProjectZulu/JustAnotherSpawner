package jas.common.spawner.creature.type;

import jas.common.spawner.biome.group.BiomeGroupRegistry;

import java.util.HashMap;

import net.minecraft.block.material.Material;

public class CreatureTypeBuilder {
    public final String typeID;
    public final int spawnRate;
    public final int maxNumberOfCreature;
    private float chunkSpawnChance;
    private Material spawnMedium;
    private String optionalParameters;

    private int defaultBiomeCap;
    private HashMap<Integer, Integer> biomeCaps;

    public CreatureTypeBuilder(String typeID, int spawnRate, int maxNumberOfCreature) {
        this.typeID = typeID;
        this.spawnRate = spawnRate;
        this.maxNumberOfCreature = maxNumberOfCreature;
        this.chunkSpawnChance = 0.0f;
        this.spawnMedium = Material.air;
        this.defaultBiomeCap = -1;
        this.biomeCaps = new HashMap<Integer, Integer>();
        this.optionalParameters = "{spawn:!solidside,1,0,[0/-1/0]:liquid,0:normal,0:normal,0,[0/1/0]:!opaque,0,[0/-1/0]}";
    }

    public float getChunkSpawnChance() {
        return chunkSpawnChance;
    }

    public CreatureTypeBuilder withChanceToChunkSpawn(float chunkSpawnChance) {
        this.chunkSpawnChance = chunkSpawnChance;
        return this;
    }

    public Material getSpawnMedium() {
        return spawnMedium;
    }

    public CreatureTypeBuilder insideMedium(Material spawnMedium) {
        this.spawnMedium = spawnMedium;
        return this;
    }

    public String getOptionalParameters() {
        return optionalParameters;
    }

    public CreatureTypeBuilder withOptionalParameters(String optionalParameters) {
        this.optionalParameters = optionalParameters;
        return this;
    }

    public int getDefaultBiomeCap() {
        return defaultBiomeCap;
    }

    public CreatureTypeBuilder withDefaultBiomeCap(int defaultBiomeCap) {
        this.defaultBiomeCap = defaultBiomeCap;
        return this;
    }

    public HashMap<Integer, Integer> getBiomeCaps() {
        return biomeCaps;
    }

    public CreatureTypeBuilder withBiomeCap(int biomeID, int dimension) {
        this.biomeCaps.put(biomeID, dimension);
        return this;
    }

    public CreatureTypeBuilder withBiomeCaps(HashMap<Integer, Integer> biomeCaps) {
        if (biomeCaps == null) {
            this.biomeCaps = new HashMap<Integer, Integer>();
        } else {
            this.biomeCaps.putAll(biomeCaps);
        }
        return this;
    }

    public CreatureType build(BiomeGroupRegistry biomeGroupRegistry) {
        return new CreatureType(biomeGroupRegistry, this);
    }
}
