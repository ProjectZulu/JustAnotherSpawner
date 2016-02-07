package jas.spawner.refactor.entities;

import jas.spawner.refactor.entities.GenericGroup.GenericGroups;
import jas.spawner.refactor.entities.GenericGroup.ReversibleGenericGroups;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

public class GenericGroupsHelper {

	public static class ImmutableMapBuilder<PID, ID, CONTENT, RESULT, GROUP extends GenericGroup<ID, CONTENT, RESULT>>
			implements GenericGroups.ImmutableMapBuilder<PID, ID, CONTENT, RESULT, GROUP> {
		private HashMap<ID, GROUP> iDToGroup = new HashMap<ID, GROUP>();
		private PID key;

		@Override
		public PID key() {
			return key;
		}

		public ImmutableMapBuilder(PID key) {
			this.key = key;
		}

		@Override
		public Map<ID, GROUP> iDToGroup() {
			return iDToGroup;
		}

		@Override
		public void clear() {
			iDToGroup.clear();
		}

		@Override
		public void removeGroup(GROUP group) {
			iDToGroup.remove(group.iD());
		}

		@Override
		public void addGroup(GROUP group) {
			iDToGroup.put(group.iD(), group);
		}

		@Override
		public ImmutableMap<ID, GROUP> buildIDToGroup() {
			return ImmutableMap.<ID, GROUP> builder().putAll(iDToGroup).build();
		}
	}

	public static class ReversibleImmutableMapBuilder<PID, ID, CONTENT, RESULT, GROUP extends GenericGroup<ID, CONTENT, RESULT>>
			implements ReversibleGenericGroups.ImmutableMapBuilder<PID, ID, CONTENT, RESULT, GROUP> {
		private HashMap<ID, GROUP> iDToGroup = new HashMap<ID, GROUP>();
		private ListMultimap<RESULT, ID> mappingToGroupIDBuilder = ArrayListMultimap.create();

		private PID key;

		@Override
		public PID key() {
			return key;
		}

		public ReversibleImmutableMapBuilder(PID key) {
			this.key = key;
		}

		@Override
		public Map<ID, GROUP> iDToGroup() {
			return iDToGroup;
		}

		@Override
		public void clear() {
			iDToGroup.clear();
			mappingToGroupIDBuilder.clear();
		}

		@Override
		public void removeGroup(GROUP group) {
			iDToGroup.remove(group.iD());
			for (RESULT mapping : group.results()) {
				mappingToGroupIDBuilder.get(mapping).remove(group.iD());
			}
		}

		@Override
		public void addGroup(GROUP group) {
			iDToGroup.put(group.iD(), group);
			for (RESULT mapping : group.results()) {
				mappingToGroupIDBuilder.get(mapping).add(group.iD());
			}
		}

		@Override
		public ImmutableMap<ID, GROUP> buildIDToGroup() {
			return ImmutableMap.<ID, GROUP> builder().putAll(iDToGroup).build();
		}

		@Override
		public ImmutableListMultimap<RESULT, ID> buildMappingToGroupID() {
			return ImmutableListMultimap.<RESULT, ID> builder().putAll(mappingToGroupIDBuilder).build();
		}
	}

}
