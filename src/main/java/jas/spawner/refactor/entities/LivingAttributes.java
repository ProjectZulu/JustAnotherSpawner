package jas.spawner.refactor.entities;

import jas.spawner.refactor.configsloader.ConfigLoader;
import jas.spawner.refactor.configsloader.EntityGroupingLoader;
import jas.spawner.refactor.entities.Group.Groups;
import jas.spawner.refactor.entities.LivingGroupBuilder.LivingGroup;

import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import com.google.common.collect.ImmutableMap;

public class LivingAttributes implements Groups {
	private ImmutableMap<String, LivingGroup> iDToAttribute;

	@Override
	public String key() {
		return "A.";
	}

	@Override
	public ImmutableMap<String, LivingGroup> iDToGroup() {
		return iDToAttribute;
	}

	public LivingAttributes(ConfigLoader loader, Mappings mappings) {
		loadFromConfig(loader, mappings);
	}

	private void loadFromConfig(ConfigLoader loader, Mappings mappings) {
		EntityGroupingLoader savedStats = loader.livingGroupLoader.saveObject;
		ImmutableMapGroupsBuilder<LivingGroupBuilder> attributeGroups = new ImmutableMapGroupsBuilder<LivingGroupBuilder>(
				"A|");
		if (savedStats.configNameToAttributeGroups.isPresent()) {
			Collection<TreeMap<String, LivingGroupBuilder>> mapOfGroups = savedStats.configNameToAttributeGroups.get()
					.values();
			for (TreeMap<String, LivingGroupBuilder> treeMap : mapOfGroups) {
				for (LivingGroupBuilder attributeGroup : treeMap.values()) {
					if (!"".equals(attributeGroup.groupID)) {
						attributeGroups.addGroup(attributeGroup);
					}
				}
			}
		}

		List<LivingGroupBuilder> sortedAttributes = ListContentGroup.Sorter.getSortedGroups(attributeGroups);
		ImmutableMapGroupsBuilder<LivingGroup> iDToAttributeBuilder = new ImmutableMapGroupsBuilder("A|");
		for (LivingGroupBuilder livingGroup : sortedAttributes) {
			LivingGroup group = livingGroup.build(mappings, iDToAttributeBuilder);
			iDToAttributeBuilder.addGroup(group);
		}
		this.iDToAttribute = iDToAttributeBuilder.build();
	}
}
