package jas.refactor.entities;

import jas.common.JASLog;
import jas.common.TopologicalSort;
import jas.common.TopologicalSort.DirectedGraph;
import jas.common.TopologicalSortingException;
import jas.common.math.SetAlgebra;
import jas.common.math.SetAlgebra.OPERATION;
import jas.refactor.ConfigLoader.ConfigLoader;
import jas.refactor.ConfigLoader.EntityGroupingLoader;
import jas.refactor.biome.BiomeGroupBuilder;
import jas.refactor.entities.Group.Groups;
import jas.refactor.entities.LivingGroupBuilder.LivingGroup;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.toposort.ModSortingException.SortingExceptionData;

public class LivingAttributes implements Groups {
	private ImmutableMap<String, LivingGroup> iDToAttribute;

	@Override
	public String key() {
		return "A|";
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

		List<LivingGroupBuilder> sortedAttributes = Group.Sorter.getSortedGroups(attributeGroups);
		ImmutableMapGroupsBuilder<LivingGroup> iDToAttributeBuilder = new ImmutableMapGroupsBuilder("A|");
		for (LivingGroupBuilder livingGroup : sortedAttributes) {
			LivingGroup group = livingGroup.build(mappings, iDToAttributeBuilder);
			iDToAttributeBuilder.addGroup(group);
		}
		this.iDToAttribute = iDToAttributeBuilder.build();
	}
}
