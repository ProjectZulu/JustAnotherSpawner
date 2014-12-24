package jas.spawner.refactor.biome;

import jas.common.global.ImportedSpawnList;
import jas.spawner.modern.spawner.biome.group.BiomeHelper;
import jas.spawner.modern.spawner.creature.handler.LivingHelper;
import jas.spawner.refactor.LivingHandlers;
import jas.spawner.refactor.LivingTypes;
import jas.spawner.refactor.LivingTypes.LivingType;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder;
import jas.spawner.refactor.entities.ListContentGroup;
import jas.spawner.refactor.entities.Group;
import jas.spawner.refactor.entities.LivingHandlerBuilder.LivingHandler;
import jas.spawner.refactor.entities.LivingMappings;

import java.util.Collection;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

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
	public SpawnListEntryBuilder generateSpawnListEntry(World world, String newBiomeMapping,
			LivingHandler livingHandler, LivingMappings livingMappings, BiomeMappings biomeMappings) {
		String livingHandlerID = livingHandler.livingHandlerID;
		String livingTypeID = guessCreatureTypeOfGroup(world, livingMappings, livingHandler.results());
		String biomeExpression = "Builder().A(" + BiomeGroups.key.concat(newBiomeMapping) + ")";
		int[] stats = findVanillaSpawnListEntryData(newBiomeMapping, livingHandler, biomeMappings, livingMappings);
		SpawnListEntryBuilder builder = new SpawnListEntryBuilder(livingHandlerID, livingTypeID, biomeExpression);
		builder.setWeight(Integer.toString(stats[0]));
		builder.setPassivePackSize("3");
		builder.setChunkPackSize(new StringBuilder().append(stats[0]).append("+ util.rand(1 + ").append(stats[2])
				.append("-").append(stats[1]).append(")").toString());
		builder.setEntityToSpawn(LivingHandlers.key.concat(livingHandler.iD()));
		return builder;
	}

	public SpawnListEntryBuilder generateSpawnListEntry(World world, Group newBiomeGroup, LivingHandler livingHandler,
			LivingMappings livingMappings, BiomeMappings biomeMappings) {
		String livingHandlerID = livingHandler.livingHandlerID;
		String livingTypeID = guessCreatureTypeOfGroup(world, livingMappings, livingHandler.results());
		String biomeExpression = "Builder().A(" + BiomeGroups.key.concat(newBiomeGroup.iD()) + ")";

		for (String biomeMapping : newBiomeGroup.results()) {
			int[] stats = findVanillaSpawnListEntryData(biomeMapping, livingHandler, biomeMappings, livingMappings);
			SpawnListEntryBuilder builder = new SpawnListEntryBuilder(livingHandlerID, livingTypeID, biomeExpression);
			builder.setWeight(Integer.toString(stats[0]));
			builder.setPassivePackSize("3");
			builder.setChunkPackSize(new StringBuilder().append(stats[0]).append("+ util.rand(1 + ").append(stats[2])
					.append("-").append(stats[1]).append(")").toString());
			builder.setEntityToSpawn(LivingHandlers.key.concat(livingHandler.iD()));
			return builder;
		}

		return new SpawnListEntryBuilder(livingHandlerID, livingTypeID, biomeExpression);
	}

	private String guessCreatureTypeOfGroup(World world, LivingMappings mappings, Collection<String> entityJASNames) {
		/* Find entity and inquire as to type */
		for (String jasName : entityJASNames) {
			Class<? extends EntityLiving> livingClass = mappings.mappingToKey().get(jasName);
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
							Class<? extends EntityLiving> livingClass = mappings.mappingToKey().get(jasName);
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
	private int[] findVanillaSpawnListEntryData(String newBiomeMapping, LivingHandler livingHandler,
			BiomeMappings biomeMappings, LivingMappings livingMappings) {
		String packString = biomeMappings.mappingToKey().get(newBiomeMapping);
		for (Integer biomeID : pckgNameToBiomeID.get(biomeMappings.mappingToKey().get(newBiomeMapping))) {
			Collection<net.minecraft.world.biome.BiomeGenBase.SpawnListEntry> spawnListEntries = importedSpawnList
					.getSpawnableCreatureList(biomeID);
			for (String jasName : livingHandler.results()) {
				Class<? extends EntityLiving> livingClass = livingMappings.mappingToKey().get(jasName);
				for (net.minecraft.world.biome.BiomeGenBase.SpawnListEntry spawnListEntry : spawnListEntries) {
					if (spawnListEntry.entityClass.equals(livingClass)) {
						return new int[] { spawnListEntry.itemWeight, spawnListEntry.minGroupCount,
								spawnListEntry.maxGroupCount };
					}
				}
			}
		}
		return new int[] { 0, 0, 4 };
	}

	private int[] findVanillaSpawnListEntryData(ListContentGroup biomeGroup, LivingHandler livingHandler,
			BiomeMappings biomeMappings, LivingMappings livingMappings) {
		for (String biomeMapping : biomeGroup.results()) {
			String packString = biomeMappings.mappingToKey().get(biomeMapping);
			for (Integer biomeID : pckgNameToBiomeID.get(biomeMappings.mappingToKey().get(biomeMapping))) {
				Collection<net.minecraft.world.biome.BiomeGenBase.SpawnListEntry> spawnListEntries = importedSpawnList
						.getSpawnableCreatureList(biomeID);
				for (String jasName : livingHandler.results()) {
					Class<? extends EntityLiving> livingClass = livingMappings.mappingToKey().get(jasName);
					for (net.minecraft.world.biome.BiomeGenBase.SpawnListEntry spawnListEntry : spawnListEntries) {
						if (spawnListEntry.entityClass.equals(livingClass)) {
							return new int[] { spawnListEntry.itemWeight, spawnListEntry.minGroupCount,
									spawnListEntry.maxGroupCount };
						}
					}
				}
			}
		}
		return new int[] { 0, 0, 4 };
	}
}
