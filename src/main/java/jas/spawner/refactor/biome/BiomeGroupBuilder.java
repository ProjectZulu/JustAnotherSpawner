package jas.spawner.refactor.biome;

import jas.spawner.refactor.entities.Group.MutableContentGroup;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class BiomeGroupBuilder implements MutableContentGroup<String> {
	private String groupID;
	private String configName;
	private transient Set<String> pckgNames = new HashSet<String>();
	/** String Used to Build Group Content Names i.e. {desert,A|Forest,glacier} */
	private String expression;

	public BiomeGroupBuilder() {
		this.groupID = "";
		this.configName = "";
		this.expression = "";
	}

	public BiomeGroupBuilder(String groupID) {
		this.groupID = groupID;
		String[] parts = groupID.split("\\.");
		if (parts.length > 1) {
			this.configName = parts[0];
		} else {
			this.configName = "";
		}
		this.expression = "";
	}

	public BiomeGroupBuilder(String groupID, String configName, String expression) {
		this.groupID = groupID;
		this.configName = configName;
		this.expression = expression;
	}

	public BiomeGroupBuilder(BiomeGroup biomeGroup) {
		this.groupID = biomeGroup.groupID;
		this.configName = biomeGroup.configName;
		this.expression = biomeGroup.expression;
		this.pckgNames = biomeGroup.pckgNames;
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

	public static class BiomeGroup implements ContentGroup<String> {
		public final String groupID;
		public final String configName;
		private final ImmutableSet<String> pckgNames;
		/** String Used to Build Group Content Names i.e. {desert,A|Forest,glacier} */
		private final String expression;

		private BiomeGroup(BiomeGroupBuilder builder) {
			this.groupID = builder.getGroupID();
			this.configName = builder.getConfigName();
			this.pckgNames = ImmutableSet.<String> builder().addAll(builder.pckgNames).build();
			this.expression = builder.expression;
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
		public String content() {
			return expression;
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
	public String content() {
		return expression;
	}

	@Override
	public void setResults(Set<String> results) {
		this.pckgNames = results;
	}

	@Override
	public void setContents(String expression) {
		this.expression = expression;
	}
}
