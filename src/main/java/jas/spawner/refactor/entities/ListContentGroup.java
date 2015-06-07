package jas.spawner.refactor.entities;

import jas.common.JASLog;
import jas.common.helper.sort.TopologicalSort;
import jas.common.helper.sort.TopologicalSort.DirectedGraph;
import jas.common.helper.sort.TopologicalSortingException;
import jas.spawner.modern.math.SetAlgebra;
import jas.spawner.modern.math.SetAlgebra.OPERATION;
import jas.spawner.refactor.entities.Group.ContentGroup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraftforge.fml.common.toposort.ModSortingException.SortingExceptionData;

import com.google.common.collect.Sets;

/** Groups composed of a Mapping Object where each is defined by a unique String */
public interface ListContentGroup extends ContentGroup<List<String>> {
	public String iD();

	public Set<String> results();

	public List<String> content();

	public static class Parser {

		/**
		 * @param mappings: Base Unit of composition for a group
		 * @param keyToMap Prefix key To corresponding groups i.e. A| -> LivingAttributes
		 */
		public static void parseGroupContents(MutableContentGroup<List<String>> mutableGroup, Mappings mappings,
				Groups<? extends ContentGroup<List<String>>>... maps) {
			/* Evaluate contents and fill in jasNames */
			for (String contentComponent : mutableGroup.content()) {
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
				Set<String> results = new HashSet<String>(mutableGroup.results());
				if (mappings.mappingToKey().containsKey(contentComponent)) {
					SetAlgebra.operate(results, Sets.newHashSet(contentComponent), operation);
				} else {
					boolean foundMatch = false;
					for (Groups<? extends ContentGroup<List<String>>> map : maps) {
						if (contentComponent.startsWith(map.key())) {
							ContentGroup<List<String>> groupToAdd = map.iDToGroup().get(
									contentComponent.substring(map.key().length()));
							if (groupToAdd != null) {
								SetAlgebra.operate(mutableGroup.results(), groupToAdd.results(), operation);
								foundMatch = true;
								break;
							}
						}
					}

					if (!foundMatch) {
						JASLog.log().severe("Error processing %s content from %s. The component %s does not exist.",
								mutableGroup.iD(), mutableGroup.content().toString(), contentComponent);
					}
				}
				mutableGroup.setResults(results);
			}
		}
	}

	// TODO: This could (should?) be more generic? i.e. interace Sortable
	public static class Sorter {

		public static <T extends ListContentGroup> List<T> getSortedGroups(Groups<T> groups) {
			/* Evaluate each group, ensuring entries are valid mappings or Groups and */
			DirectedGraph<T> groupGraph = new DirectedGraph<T>();
			for (T livingGroup : groups.iDToGroup().values()) {
				groupGraph.addNode(livingGroup);
			}
			for (T currentGroup : groups.iDToGroup().values()) {
				for (String contentComponent : currentGroup.content()) {
					for (T possibleGroup : groups.iDToGroup().values()) {
						if (contentComponent.startsWith(groups.key())
								&& contentComponent.substring(groups.key().length()).equals(possibleGroup.iD())) {
							groupGraph.addEdge(possibleGroup, currentGroup);
						}
					}
				}
			}

			List<T> sortedList;
			try {
				sortedList = TopologicalSort.topologicalSort(groupGraph);
			} catch (TopologicalSortingException sortException) {
				SortingExceptionData<T> exceptionData = sortException.getExceptionData();
				JASLog.log().severe(
						"A circular reference was detected when processing entity groups. Groups in the cycle were: ");
				int i = 1;
				for (T invalidGroups : exceptionData.getVisitedNodes()) {
					JASLog.log().severe("Group %s: %s containing %s", i++, invalidGroups.iD(),
							invalidGroups.content().toString());
				}
				throw sortException;
			}
			return sortedList;
		}
	}
}
