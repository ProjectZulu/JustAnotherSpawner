package jas.refactor.entities;

import jas.common.JASLog;
import jas.common.TopologicalSort;
import jas.common.TopologicalSort.DirectedGraph;
import jas.common.TopologicalSortingException;
import jas.common.math.SetAlgebra;
import jas.common.math.SetAlgebra.OPERATION;
import jas.refactor.ConfigLoader.ConfigLoader;
import jas.refactor.ConfigLoader.EntityGroupingLoader;
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
	public ImmutableMap<String, LivingGroup> iDToAttribute() {
		return iDToAttribute;
	}

	public LivingAttributes(ConfigLoader loader, Mappings mappings) {
		loadFromConfig(loader, mappings);
	}

	private void loadFromConfig(ConfigLoader loader, Mappings mappings) {
		EntityGroupingLoader savedStats = loader.livingGroupLoader.saveObject;
		Set<LivingGroupBuilder> attributeGroups = new HashSet<LivingGroupBuilder>();
		if (savedStats.configNameToAttributeGroups.isPresent()) {
			Collection<TreeMap<String, LivingGroupBuilder>> mapOfGroups = savedStats.configNameToAttributeGroups.get()
					.values();
			for (TreeMap<String, LivingGroupBuilder> treeMap : mapOfGroups) {
				for (LivingGroupBuilder attributeGroup : treeMap.values()) {
					if (!"".equals(attributeGroup.groupID)) {
						attributeGroups.add(attributeGroup);
					}
				}
			}
		}
		List<LivingGroupBuilder> sortedAttributes = getSortedGroups(attributeGroups);
		ImmutableMapGroupsBuilder<LivingGroup> iDToAttributeBuilder = new ImmutableMapGroupsBuilder();
		for (LivingGroupBuilder livingGroup : sortedAttributes) {
			LivingGroup group = livingGroup.build(mappings, iDToAttributeBuilder);
			iDToAttributeBuilder.addGroup(group);
		}
		this.iDToAttribute = iDToAttributeBuilder.build();
	}

	private List<LivingGroupBuilder> getSortedGroups(Collection<LivingGroupBuilder> livingGroups) {
		/* Evaluate each group, ensuring entries are valid mappings or Groups and */
		DirectedGraph<LivingGroupBuilder> groupGraph = new DirectedGraph<LivingGroupBuilder>();
		for (LivingGroupBuilder livingGroup : livingGroups) {
			groupGraph.addNode(livingGroup);
		}
		for (LivingGroupBuilder currentGroup : livingGroups) {
			for (String contentComponent : currentGroup.contents) {
				for (LivingGroupBuilder possibleGroup : livingGroups) {
					// Reminder: substring(2) is to remove mandatory A| and G| for groups
					if (contentComponent.substring(2).equals(possibleGroup.groupID)) {
						groupGraph.addEdge(possibleGroup, currentGroup);
					}
				}
			}
		}

		List<LivingGroupBuilder> sortedList;
		try {
			sortedList = TopologicalSort.topologicalSort(groupGraph);
		} catch (TopologicalSortingException sortException) {
			SortingExceptionData<LivingGroup> exceptionData = sortException.getExceptionData();
			JASLog.log().severe(
					"A circular reference was detected when processing entity groups. Groups in the cycle were: ");
			int i = 1;
			for (LivingGroup invalidGroups : exceptionData.getVisitedNodes()) {
				JASLog.log().severe("Group %s: %s containing %s", i++, invalidGroups.groupID,
						invalidGroups.contents().toString());
			}
			throw sortException;
		}
		return sortedList;
	}

	/**
	 * Evaluate build instructions (i.e. A|allbiomes,&Jungle) of group and evalute them into jasNames
	 */
	private void parseGroupContents(LivingGroup livingGroup, Mappings mappings) {
		/* Evaluate contents and fill in jasNames */
		for (String contentComponent : livingGroup.contents) {
			OPERATION operation;
			if (contentComponent.startsWith("-")) {
				contentComponent = contentComponent.substring(1);
				operation = OPERATION.COMPLEMENT;
			} else if (contentComponent.startsWith("&")) {
				contentComponent = contentComponent.substring(1);
				operation = OPERATION.INTERSECT;
			} else {
				operation = OPERATION.UNION;
				if (contentComponent.startsWith("+")) {
					contentComponent = contentComponent.substring(1);
				}
			}

			if (contentComponent.startsWith("A|")) {
				LivingGroup groupToAdd = iDToAttribute.get(contentComponent.substring(2));
				if (groupToAdd != null) {
					SetAlgebra.operate(livingGroup.entityJASNames, groupToAdd.entityJASNames, operation);
					continue;
				}
			} else if (mappings.jASNametoEntityClass().containsKey(contentComponent)) {
				SetAlgebra.operate(livingGroup.entityJASNames, Sets.newHashSet(contentComponent), operation);
				continue;
			}
			JASLog.log().severe("Error processing %s content from %s. The component %s does not exist.",
					livingGroup.groupID, livingGroup.contents().toString(), contentComponent);
		}
	}
}
