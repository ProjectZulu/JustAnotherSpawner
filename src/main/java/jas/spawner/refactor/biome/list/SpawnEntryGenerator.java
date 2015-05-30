package jas.spawner.refactor.biome.list;

import jas.common.global.ImportedSpawnList;
import jas.spawner.modern.spawner.biome.group.BiomeHelper;
import jas.spawner.refactor.LivingHelper;
import jas.spawner.refactor.LivingTypeBuilder.LivingType;
import jas.spawner.refactor.LivingTypes;
import jas.spawner.refactor.biome.BiomeGroups;
import jas.spawner.refactor.biome.BiomeMappings;
import jas.spawner.refactor.entities.Group;
import jas.spawner.refactor.entities.LivingMappings;

import java.util.Collection;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;

public class SpawnEntryGenerator {
	private ImmutableListMultimap<String, Integer> pckgNameToBiomeID;
	private ImportedSpawnList importedSpawnList;
	private LivingTypes livingTypes;

	public SpawnEntryGenerator(ImportedSpawnList importedSpawnList, LivingTypes livingTypes) {
		this.importedSpawnList = importedSpawnList;
		this.livingTypes = livingTypes;
		/* Build Reverse Lookup */
		ArrayListMultimap<String, Integer> pckgNameToBiomeIDBuilder = ArrayListMultimap.create();
		for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
			if (biome != null) {
				pckgNameToBiomeIDBuilder.put(BiomeHelper.getPackageName(biome), biome.biomeID);
			}
		}
		pckgNameToBiomeID = ImmutableListMultimap.<String, Integer> builder().putAll(pckgNameToBiomeIDBuilder).build();
	}

	/**
	 * Create a default SpawnListEntry from the Provided BiomeMapping-LivingHandler pair.
	 */
	public SpawnListEntryBuilder generateSpawnListEntry(World world, String newBiomeMapping, String livingMapping,
			Optional<String> livingHandlerID, LivingMappings livingMappings, BiomeMappings biomeMappings) {
		String modID = LivingHelper.guessModID(livingHandlerID.isPresent() ? livingHandlerID.get() : "");
		String livingTypeID = guessCreatureTypeOfGroup(world, livingMappings, livingMapping);
		String livingExpression = "Builder().A(" + livingMapping + ")";
		String biomeExpression = "Builder().A(" + BiomeGroups.key.concat(newBiomeMapping) + ")";

		int[] stats = findVanillaSpawnListEntryData(newBiomeMapping, livingMapping, biomeMappings, livingMappings);
		SpawnListEntryBuilder builder = new SpawnListEntryBuilder(modID,
				livingHandlerID.isPresent() ? livingHandlerID.get() : null, livingTypeID, biomeExpression,
				livingExpression);
		builder.setWeight(stats[0]);
		builder.setPassivePackSize("3");
		builder.setChunkPackSize(new StringBuilder().append(stats[0]).append("+ util.rand(1 + ").append(stats[2])
				.append("-").append(stats[1]).append(")").toString());
		return builder;
	}

	/**
	 * Generates a SLE for provided BiomeGroup and LivingMapping
	 */
	public SpawnListEntryBuilder generateSpawnListEntry(World world, Group biomeGroup, String livingMapping,
			Optional<String> livingHandlerID, LivingMappings livingMappings, BiomeMappings biomeMappings) {
		String modID = LivingHelper.guessModID(livingHandlerID.isPresent() ? livingHandlerID.get() : "");
		String livingTypeID = guessCreatureTypeOfGroup(world, livingMappings, livingMapping);
		String livingExpression = "Builder().A(" + livingMapping + ")";
		String biomeExpression = "Builder().A(" + BiomeGroups.key.concat(biomeGroup.iD()) + ")";

		SpawnListEntryBuilder builder = new SpawnListEntryBuilder(modID,
				livingHandlerID.isPresent() ? livingHandlerID.get() : null, livingTypeID, biomeExpression,
				livingExpression);
		for (String biomeMapping : biomeGroup.results()) {
			int[] stats = findVanillaSpawnListEntryData(biomeMapping, livingMapping, biomeMappings, livingMappings);
			builder.setWeight(stats[0]);
			builder.setPassivePackSize("3");
			builder.setChunkPackSize(new StringBuilder().append(stats[0]).append("+ util.rand(1 + ").append(stats[2])
					.append("-").append(stats[1]).append(")").toString());
			return builder;
		}
		return builder;
	}

	private String guessCreatureTypeOfGroup(World world, LivingMappings mappings, String... entityJASNames) {
		/* Find entity and inquire as to type */
		for (String jasName : entityJASNames) {
			Class<? extends EntityLiving> livingClass = LivingHelper.getEntityfromJASName(jasName, mappings);
			EntityLiving creature = LivingHelper.createCreature(livingClass, world);
			for (EnumCreatureType type : EnumCreatureType.values()) {
				boolean isType = creature != null ? creature.isCreatureType(type, true) : type.getClass()
						.isAssignableFrom(livingClass);
				if (isType && livingTypes.getLivingType((type.toString())) != null) {
					return type.toString();
				}
			}
		}
		/* If entity doesn't have type, Search for matching spawnlist and assign type equivalent to Spawnlist */
		for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
			if (biome != null) {
				for (EnumCreatureType creatureType : EnumCreatureType.values()) {
					for (net.minecraft.world.biome.BiomeGenBase.SpawnListEntry entry : importedSpawnList
							.getSpawnableCreatureList(biome, creatureType)) {
						for (String jasName : entityJASNames) {
							Class<? extends EntityLiving> livingClass = LivingHelper.getEntityfromJASName(jasName,
									mappings);
							if (entry.entityClass.equals(livingClass)) {
								LivingType type = livingTypes.getLivingType(creatureType.toString());
								if (type != null) {
									return type.livingTypeID;
								}
							}
						}
					}
				}
			}
		}
		return LivingTypes.NONE;
	}

	/** Return default Stats if applicable: [SpawnWeight, MinChunkPackSize, MaxChunkPackSize] */
	private int[] findVanillaSpawnListEntryData(String locationMapping, String livingMapping,
			BiomeMappings locationMappings, LivingMappings livingMappings) {
		for (Integer biomeID : pckgNameToBiomeID.get(locationMappings.mappingToKey().get(locationMapping))) {
			Collection<net.minecraft.world.biome.BiomeGenBase.SpawnListEntry> spawnListEntries = importedSpawnList
					.getSpawnableCreatureList(biomeID);
			Class<? extends EntityLiving> livingClass = LivingHelper
					.getEntityfromJASName(livingMapping, livingMappings);
			for (net.minecraft.world.biome.BiomeGenBase.SpawnListEntry spawnListEntry : spawnListEntries) {
				if (spawnListEntry.entityClass.equals(livingClass)) {
					return new int[] { spawnListEntry.itemWeight, spawnListEntry.minGroupCount,
							spawnListEntry.maxGroupCount };
				}
			}
		}
		return new int[] { 0, 0, 4 };
	}
}
