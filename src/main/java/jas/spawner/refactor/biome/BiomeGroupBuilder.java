package jas.spawner.refactor.biome;

import jas.spawner.refactor.entities.Group;
import jas.spawner.refactor.entities.Group.MutableGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class BiomeGroupBuilder implements MutableGroup {
	private String groupID;
	private String configName;
	private transient Set<String> pckgNames = new HashSet<String>();
	/** String Used to Build Group Content Names i.e. {desert,A|Forest,glacier} */
	private ArrayList<String> contents;

	public BiomeGroupBuilder() {
		this.groupID = "";
		this.configName = "";
		contents = new ArrayList<String>();
	}

	public BiomeGroupBuilder(String groupID) {
		this.groupID = groupID;
		String[] parts = groupID.split("\\.");
		if (parts.length > 1) {
			this.configName = parts[0];
		} else {
			this.configName = "";
		}
		contents = new ArrayList<String>();
	}

	public BiomeGroupBuilder(String groupID, String configName, ArrayList<String> contents) {
		this.groupID = groupID;
		this.configName = configName;
		this.contents = new ArrayList<String>(contents);
	}

	public BiomeGroup build() {
		return new BiomeGroup(this);
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public String getConfigName() {
		return configName;
	}

	public void setConfigName(String configName) {
		this.configName = configName;
	}

	public static class BiomeGroup implements Group {
		public final String groupID;
		public final String configName;
		private final ImmutableSet<String> pckgNames;
		/** String Used to Build Group Content Names i.e. {desert,A|Forest,glacier} */
		private final ImmutableList<String> contents;

		private BiomeGroup(BiomeGroupBuilder builder) {
			this.groupID = builder.getGroupID();
			this.configName = builder.getConfigName();
			this.pckgNames = ImmutableSet.<String> builder().addAll(builder.pckgNames).build();
			this.contents = ImmutableList.<String> builder().addAll(builder.contents).build();
		}

		@Override
		public String iD() {
			return groupID;
		}

		@Override
		public Set<String> results() {
			return pckgNames;
		}

		@Override
		public List<String> contents() {
			return contents;
		}
	}

	@Override
	public String iD() {
		return groupID;
	}

	@Override
	public Set<String> results() {
		return pckgNames;
	}

	@Override
	public List<String> contents() {
		return contents;
	}

	@Override
	public void setResults(Set<String> results) {
		this.pckgNames = results;
	}

	@Override
	public void setContents(List<String> contents) {
		this.contents = new ArrayList(contents);
	}
}
