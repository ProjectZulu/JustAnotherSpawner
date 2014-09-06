package jas.common.spawner.creature.type;

import jas.common.JASLog;
import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.biome.group.BiomeHelper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.material.Material;
import net.minecraft.world.biome.BiomeGenBase;

import com.google.common.collect.Multimap;

public class CreatureTypeBuilder {
	public final String typeID;
	public final int spawnRate;
	public final int maxNumberOfCreature;
	private float chunkSpawnChance;
	private String spawnMedium;
	private String spawnExpression;

	private int defaultBiomeCap;
	private HashMap<String, Integer> biomeMappingToCap;
	private int iterationsPerChunk;
	private int iterationsPerPack;

	public CreatureTypeBuilder() {
		this(null, 1, 50);
	}

	public CreatureTypeBuilder(String typeID, int spawnRate, int maxNumberOfCreature) {
		this.typeID = typeID;
		this.spawnRate = spawnRate;
		this.maxNumberOfCreature = maxNumberOfCreature;
		this.chunkSpawnChance = 0.0f;
		this.spawnMedium = "air";
		this.defaultBiomeCap = -1;
		this.biomeMappingToCap = new HashMap<String, Integer>();
		this.spawnExpression = "!solidside(1,{0,0,0},{0,-1,0})&&liquid({0,0,0},{0,0,0})&&normal({0,0,0},{0,0,0})&&normal({0,0,0},{0,1,0})&&!opaque({0,0,0},{0,-1,0})";
		this.iterationsPerChunk = 3;
		this.iterationsPerPack = 4;
	}

	public CreatureTypeBuilder(CreatureType creatureType) {
		this.typeID = creatureType.typeID;
		this.spawnRate = creatureType.spawnRate;
		this.maxNumberOfCreature = creatureType.maxNumberOfCreature;
		this.chunkSpawnChance = creatureType.chunkSpawnChance;
		if (creatureType.spawnMedium == Material.water) {
			this.spawnMedium = "water";
		} else {
			this.spawnMedium = "air";
		}
		this.defaultBiomeCap = creatureType.defaultBiomeCap;
		this.biomeMappingToCap = CreatureTypeBuilder.capMapBiomeIdToMapping(creatureType.biomeCaps,
				creatureType.biomeGroupRegistry.biomePckgToMapping());
		this.spawnExpression = creatureType.spawnExpression;
		this.iterationsPerChunk = creatureType.iterationsPerChunk;
		this.iterationsPerPack = creatureType.iterationsPerPack;
	}

	public float getChunkSpawnChance() {
		return chunkSpawnChance;
	}

	public CreatureTypeBuilder withChanceToChunkSpawn(float chunkSpawnChance) {
		this.chunkSpawnChance = chunkSpawnChance;
		return this;
	}

	public Material getSpawnMedium() {
		if (spawnMedium.equalsIgnoreCase("water")) {
			return Material.water;
		} else {
			return Material.air;
		}
	}

	public String getRawSpawnMedium() {
		return spawnMedium;
	}

	public CreatureTypeBuilder setRawMedium(String spawnMedium) {
		if (spawnMedium.equalsIgnoreCase("water")) {
			this.spawnMedium = "water";
		} else {
			this.spawnMedium = "air";
		}
		return this;
	}

	public CreatureTypeBuilder insideMedium(Material spawnMedium) {
		if (spawnMedium == Material.water) {
			this.spawnMedium = "water";
		} else {
			this.spawnMedium = "air";
		}
		return this;
	}

	public String getSpawnExpression() {
		return spawnExpression;
	}

	public CreatureTypeBuilder withSpawnExpression(String optionalParameters) {
		this.spawnExpression = optionalParameters;
		return this;
	}

	public CreatureTypeBuilder setIterationsPerChunk(int iterationsPerChunk) {
		this.iterationsPerChunk = iterationsPerChunk;
		return this;
	}

	public int getIterationsPerChunk() {
		return iterationsPerChunk;
	}
	
	public CreatureTypeBuilder setIterationsPerPack(int iterationsPerPack) {
		this.iterationsPerPack = iterationsPerPack;
		return this;
	}

	public int getIterationsPerPack() {
		return iterationsPerPack;
	}

	public int getDefaultBiomeCap() {
		return defaultBiomeCap;
	}

	public CreatureTypeBuilder withDefaultBiomeCap(int defaultBiomeCap) {
		this.defaultBiomeCap = defaultBiomeCap;
		return this;
	}

	public HashMap<String, Integer> getBiomeCaps() {
		return biomeMappingToCap;
	}

	public CreatureTypeBuilder withBiomeCap(String biomeMapping, int cap) {
		this.biomeMappingToCap.put(biomeMapping, cap);
		return this;
	}

	public CreatureTypeBuilder withBiomeCaps(HashMap<String, Integer> biomeCaps) {
		if (biomeCaps == null) {
			this.biomeMappingToCap = new HashMap<String, Integer>();
		} else {
			this.biomeMappingToCap.putAll(biomeCaps);
		}
		return this;
	}

	public CreatureType build(BiomeGroupRegistry biomeGroupRegistry) {
		if (typeID == null) {
			throw new IllegalArgumentException("Cannot build creature type with null typeId");
		}
		return new CreatureType(biomeGroupRegistry, this);
	}

	public static HashMap<String, Integer> capMapBiomeIdToMapping(Map<Integer, Integer> biomeCaps,
			Map<String, String> biomePackageToMapping) {
		HashMap<String, Integer> mappingBiomeCaps = new HashMap<String, Integer>();
		for (Entry<Integer, Integer> entry : biomeCaps.entrySet()) {
			String packageName = BiomeHelper.getPackageName(BiomeGenBase.getBiomeGenArray()[entry.getKey()]);
			String mappingName = biomePackageToMapping.get(packageName);
			mappingBiomeCaps.put(mappingName, entry.getValue());
		}
		return mappingBiomeCaps;
	}

	public static HashMap<Integer, Integer> capMapMappingToBiomeId(HashMap<String, Integer> biomeCaps,
			Map<String, String> biomeMappingToPackage, Multimap<String, Integer> pckgNameToBiomeID) {
		HashMap<Integer, Integer> biomeIDBiomeCaps = new HashMap<Integer, Integer>();
		for (Entry<String, Integer> entry : biomeCaps.entrySet()) {
			String packageName = biomeMappingToPackage.get(entry.getKey());
			if (packageName != null) {
				Collection<Integer> biomeIDs = pckgNameToBiomeID.get(packageName);
				for (Integer biomeID : biomeIDs) {
					biomeIDBiomeCaps.put(biomeID, entry.getValue());
				}
			} else {
				JASLog.log().severe("Error Parsing BiomeCap. %s is not a biome mapping.", entry.getKey());
			}
		}
		return biomeIDBiomeCaps;
	}
}
