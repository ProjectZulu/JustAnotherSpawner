package jas.spawner.refactor.biome;

import jas.common.JASLog;
import jas.spawner.modern.spawner.biome.group.BiomeHelper;
import jas.spawner.refactor.configsloader.BiomeSettingsLoader;
import jas.spawner.refactor.configsloader.ConfigLoader;
import jas.spawner.refactor.entities.Mappings;

import java.util.Collection;
import java.util.HashSet;

import net.minecraft.world.biome.BiomeGenBase;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;

public class BiomeMappings implements Mappings<String, String> {
	private Collection<String> newMappings;
	private ImmutableBiMap<String, String> PckgToJASName;
	private ImmutableBiMap<String, String> JASNameToPckg;

	public BiomeMappings(ConfigLoader loader) {
		loadFromConfig(loader);
	}

	private void loadFromConfig(ConfigLoader loader) {
		BiomeSettingsLoader biomeLoader = loader.biomeGroupLoader.saveObject;
		newMappings = new HashSet<String>();
		HashBiMap<String, String> biomeMappingToPckgBuilder = HashBiMap.create();
		BiMap<String, String> biomePckgToMappingBuilder = biomeMappingToPckgBuilder.inverse();
		biomePckgToMappingBuilder.putAll(biomeLoader.biomeMappings);
		for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
			if (biome == null) {
				continue;
			}
			String packageName = BiomeHelper.getPackageName(biome);
			// Only look for mapping if we already don't have a one for this Biome
			if (!biomePckgToMappingBuilder.containsKey(packageName)) {
				String uniqueMapping = getUniqueMapping(biome, biomeMappingToPckgBuilder);
				biomePckgToMappingBuilder.put(packageName, uniqueMapping);
				newMappings.add(uniqueMapping);
			}
		}
		JASNameToPckg = ImmutableBiMap.<String, String> builder().putAll(biomeMappingToPckgBuilder).build();
		PckgToJASName = JASNameToPckg.inverse();
	}

	private String getUniqueMapping(BiomeGenBase biome, BiMap<String, String> biomeMappingToPckgBuilder) {
		String defaultMapping = biome.biomeName;
		int attempts = 0;
		while (defaultMapping == null || biomeMappingToPckgBuilder.containsKey(defaultMapping)) {
			defaultMapping = BiomeHelper.getShortPackageName(biome);
			if (attempts > 0) {
				// For multiple tries, concat the number of the attempts
				// to create a unique mapping... eventually ;)
				defaultMapping = defaultMapping + "_" + attempts;
			}
			attempts++;
		}
		if (attempts > 0) {
			JASLog.log().info("Duplicate mapping %s and was renamed to %s.", biome.biomeName, defaultMapping);
		}
		return defaultMapping;
	}

	@Override
	public Collection<String> newMappings() {
		return newMappings;
	}

	@Override
	public BiMap<String, String> keyToMapping() {
		return PckgToJASName;
	}

	@Override
	public BiMap<String, String> mappingToKey() {
		return JASNameToPckg;
	}
}
