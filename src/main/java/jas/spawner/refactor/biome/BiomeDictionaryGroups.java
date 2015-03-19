package jas.spawner.refactor.biome;

import jas.spawner.modern.spawner.biome.group.BiomeHelper;
import jas.spawner.refactor.biome.BiomeGroupBuilder.BiomeGroup;
import jas.spawner.refactor.entities.Group;
import jas.spawner.refactor.entities.Group.Groups;
import jas.spawner.refactor.entities.Group.Parser.ExpressionContext;
import jas.spawner.refactor.entities.ImmutableMapGroupsBuilder;

import java.util.Map;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

public class BiomeDictionaryGroups implements Groups {
	private ImmutableMap<String, BiomeGroup> iDToGroup;
	private ImmutableListMultimap<String, String> mappingToGroupID;

	@Override
	public String key() {
		return "D.";
	}

	@Override
	public Map<String, BiomeGroup> iDToGroup() {
		return iDToGroup;
	}

	public BiomeDictionaryGroups(BiomeMappings biomeMappings) {
		loadFromConfig(biomeMappings);
	}

	private void loadFromConfig(BiomeMappings biomeMappings) {
		ImmutableMapGroupsBuilder<BiomeGroupBuilder> attributeGroups = new ImmutableMapGroupsBuilder<BiomeGroupBuilder>(
				"D|");

		/* Define Group for each BiomeDictionary Type */
		for (Type type : BiomeDictionary.Type.values()) {
			BiomeGroupBuilder attributeGroup = new BiomeGroupBuilder(type.toString());
			StringBuilder expressionBuilder = new StringBuilder();
			BiomeGenBase[] types = BiomeDictionary.getBiomesForType(type);
			if (types.length != 0) {
				expressionBuilder.append("Builder().");
				for (int i = 0; i < types.length; i++) {
					BiomeGenBase biome = types[i];
					expressionBuilder.append("A('")
							.append(biomeMappings.keyToMapping().get(BiomeHelper.getPackageName(biome))).append("')");
				}
			}
			attributeGroup.setContents(expressionBuilder.toString());
			attributeGroups.addGroup(attributeGroup);
		}

		ListMultimap<String, String> mappingToGroupIDBuilder = ArrayListMultimap.create();
		ExpressionContext context = new ExpressionContext(biomeMappings);
		ImmutableMapGroupsBuilder<BiomeGroup> attributeBuilder = new ImmutableMapGroupsBuilder<BiomeGroup>("D|");
		for (BiomeGroupBuilder biomeGroup : attributeGroups.iDToGroup().values()) {
			Group.Parser.parseGroupContents(biomeGroup, context);
			attributeBuilder.addGroup(biomeGroup.build());
			for (String pckgName : biomeGroup.results()) {
				mappingToGroupIDBuilder.get(pckgName).add(biomeGroup.iD());
			}
		}

		iDToGroup = attributeBuilder.build();
		mappingToGroupID = ImmutableListMultimap.<String, String> builder().putAll(mappingToGroupIDBuilder).build();
	}
}
