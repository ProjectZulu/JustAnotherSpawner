package jas.spawner.refactor.entities;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;

//TODO: This needs to be implemented, was designed as a functional replacement to jas.spawner.refactor.entities.Group on 23Jan16
// along with GenericGroupsHelper and GenericParser
public interface GenericGroup<ID, CONTENT, RESULT> {
	// Unique Identifier separating this group from others
	public ID iD();

	// Configurable property
	public CONTENT content();

	// Unique Identifier separating this group from others
	public Set<RESULT> results();

	public interface Mappings<FMLKEY, MAPPING> {
		public Collection<MAPPING> newMappings();

		public BiMap<FMLKEY, MAPPING> keyToMapping();

		public BiMap<MAPPING, FMLKEY> mappingToKey();
	}

	public static interface MutableGenericGroup<ID, RESULT, CONTENT> extends GenericGroup<ID, RESULT, CONTENT> {
		public void setResults(Set<RESULT> results);

		public void setContents(CONTENT expression);
	}

	public static interface GenericGroups<PID, ID, CONTENT, RESULT, GROUP extends GenericGroup<ID, CONTENT, RESULT>> {
		// Unique Identifier for this group of groups
		public PID key();

		// Mapping from Group Unique ID to that respective Group
		public Map<ID, GROUP> iDToGroup();

		public interface ImmutableMapBuilder<PID, ID, CONTENT, RESULT, GROUP extends GenericGroup<ID, CONTENT, RESULT>>
				extends GenericGroups<PID, ID, CONTENT, RESULT, GROUP> {
			public PID key();

			public Map<ID, GROUP> iDToGroup();

			public void clear();

			public void removeGroup(GROUP group);

			public void addGroup(GROUP group);

			public ImmutableMap<ID, GROUP> buildIDToGroup();
		}
	}

	public static interface ReversibleGenericGroups<PID, ID, CONTENT, RESULT, GROUP extends GenericGroup<ID, CONTENT, RESULT>>
			extends GenericGroups<PID, ID, CONTENT, RESULT, GROUP> {
		// ResultKey to GenericGroupID
		public Multimap<RESULT, ID> mappingToID();

		public interface ImmutableMapBuilder<PID, ID, CONTENT, RESULT, GROUP extends GenericGroup<ID, CONTENT, RESULT>>
				extends GenericGroups.ImmutableMapBuilder<PID, ID, CONTENT, RESULT, GROUP> {
			public PID key();

			public Map<ID, GROUP> iDToGroup();

			public void clear();

			public void removeGroup(GROUP group);

			public void addGroup(GROUP group);

			public ImmutableMap<ID, GROUP> buildIDToGroup();

			public ImmutableListMultimap<RESULT, ID> buildMappingToGroupID();
		}
	}
}
