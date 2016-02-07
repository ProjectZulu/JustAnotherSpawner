package jas.spawner.refactor.biome.list;

import jas.spawner.refactor.LivingTypes;
import jas.spawner.refactor.entities.GenericParser.ResultsBuilder;
import jas.spawner.refactor.mvel.MVELExpression;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class SpawnListEntryBuilder {
	public static final String defaultFileName = "vanilla";

	/** Mod this SpawnListEntry is associated with */
	private String modID;

	/** LivingType associated with this SLE */
	private HashSet<String> livingTypeIDs;
	/** Expression used to determine applicable LivingTypes i.e. {desert,A|Forest,glacier} */
	private String livingTypeContents;

	/** Expression that determines if Entity can spawn */
	private Optional<String> canSpawn;
	/** Expression called after entity has spawned; typically used to alter entity NBT data */
	private Optional<String> postSpawn;

	/** Biome/Structure Mappings this SpawnListEntry builder applies to */
	private transient Set<String> locMappings;
	/** Expression used to determine applicable entities i.e. {desert,A|Forest,glacier} */
	private String locContents;

	/** Entity Mappings this SpawnListEntry builder applies to */
	private transient Set<String> entityMappings;
	/** Expression used to determine applicable locations i.e. {desert,A|Forest,glacier} */
	private String entityContents;

	/** The LivingHandler associated with this SpawnListEntry */
	private Optional<String> livingHandlerID;

	/** Chance this SLE will be selected */
	private int weight;
	/** Expression to determine the maximum number of entities that can spawn in a passive spawn loop */
	private String passivePackSize;
	/** Expression to determine the maximum number of entities that can spawn in a chunk spawn loop */
	private String chunkPackSize; // [int] Replaces old min/max: returns the random result itself

	public SpawnListEntryBuilder() {
		this.setModID(defaultFileName);
		this.setLivingHandlerID(null);
		this.setLivingTypeIDs(null);
		this.setWeight(0);
		this.setPassivePackSize("3");
		this.setChunkPackSize("0 + util.rand(1 + 4 - 0)");

		this.setCanSpawn("");
		this.setPostSpawn("");
		this.setLocContents("");
		this.setLocResults(new HashSet<String>());
		this.setEntContents("");
		this.setEntResults(new HashSet<String>());
	}

	@Deprecated
	public SpawnListEntryBuilder(String fileName, String livingHandlerID, String livingTypeID, String locExpression) {
		this.setModID(fileName);
		HashSet<String> livingTypeIDs = new HashSet<String>();
		livingTypeIDs.add(livingTypeID);
		this.setLivingTypeIDs(livingTypeIDs);
		this.setLivingHandlerID(livingHandlerID);
		this.setWeight(0);
		this.setPassivePackSize("3");
		this.setChunkPackSize("0 + util.rand(1 + 4 - 0)");

		this.setCanSpawn("");
		this.setPostSpawn("");

		this.setLocContents(locExpression);
		this.setLocResults(new HashSet<String>());
		this.setEntContents("");
		this.setEntResults(new HashSet<String>());
	}

	// Commonly used constructor - should be a helper instead as it doesn't pertain to the class in a generic way
	public SpawnListEntryBuilder(String fileName, String livingHandlerID, String livingTypeExpression, String locExpression,
			String livExp) {
		this.setModID(fileName);
		this.setLivingTypeContents(livingTypeExpression);
		this.setLivingTypeIDs(new HashSet<String>());
		this.setLivingHandlerID(livingHandlerID);
		this.setWeight(0);
		this.setPassivePackSize("3");
		this.setChunkPackSize("0 + util.rand(1 + 4 - 0)");

		this.setCanSpawn("");
		this.setPostSpawn("");

		this.setLocContents(locExpression);
		this.setLocResults(new HashSet<String>());
		this.setEntContents(livExp);
		this.setEntResults(new HashSet<String>());
	}

	public SpawnListEntryBuilder(SpawnListEntry entry) {
		this.setModID(entry.modID);
		this.setLivingTypeIDs(new HashSet<String>(entry.livingTypeIDs));
		this.setLivingHandlerID(entry.livingHandlerID.isPresent() ? entry.livingHandlerID.get() : null);
		this.setWeight(entry.weight);
		this.setPassivePackSize(entry.passivePackSize.expression);
		this.setChunkPackSize(entry.chunkPackSize.expression);

		this.setCanSpawn(entry.canSpawn.isPresent() ? entry.canSpawn.get().expression : null);
		this.setPostSpawn(entry.postSpawn.isPresent() ? entry.postSpawn.get().expression : null);

		this.setLocContents(entry.locContents);
		this.setLocResults(entry.locMappings);
		this.setEntContents(entry.entityContents);
		this.setEntResults(new HashSet<String>(entry.entityMappings));
	}

	public static class SpawnListEntry {
		public final String modID;
		public final Optional<String> livingHandlerID;

		public final ImmutableSet<String> livingTypeIDs;
		public final transient String livingTypeContents;
		// public final transient MVELExpression<ResultsBuilder<String>> livingTypeContents;

		public final int weight;
		public final MVELExpression<Integer> passivePackSize;
		public final MVELExpression<Integer> chunkPackSize;
		public final Optional<MVELExpression<Boolean>> canSpawn;
		public final Optional<MVELExpression<Boolean>> postSpawn;

		/** Biome/Structure Mappings this SpawnListEntry builder applies to */
		public final transient ImmutableSet<String> locMappings;
		/** Expression used to determine applicable entities i.e. {desert,A|Forest,glacier} */
		public final transient String locContents;

		/** Entity Mappings this SpawnListEntry builder applies to */
		// Entries must be unique; List is for quick random access during spawn cycles
		public final transient ImmutableList<String> entityMappings;
		/** Expression used to determine applicable locations i.e. {desert,A|Forest,glacier} */
		public final transient String entityContents;

		private SpawnListEntry(SpawnListEntryBuilder builder) {
			this.modID = builder.modID;
			this.livingTypeContents = builder.livingTypeContents;
			this.livingTypeIDs = ImmutableSet.<String> builder().addAll(builder.livingTypeIDs).build();
			this.livingHandlerID = builder.livingHandlerID;
			this.weight = builder.getWeight();
			this.passivePackSize = new MVELExpression<Integer>(builder.getPassivePackSize());
			this.chunkPackSize = new MVELExpression<Integer>(builder.getChunkPackSize());
			if (builder.getCanSpawn().isPresent()) {
				this.canSpawn = Optional.of(new MVELExpression<Boolean>(builder.getCanSpawn().get()));
			} else {
				this.canSpawn = Optional.absent();
			}
			if (builder.getPostSpawn().isPresent()) {
				this.postSpawn = Optional.of(new MVELExpression<Boolean>(builder.getPostSpawn().get()));
			} else {
				this.postSpawn = Optional.absent();
			}

			this.locMappings = ImmutableSet.<String> builder().addAll(builder.getLocResults()).build();
			this.locContents = builder.getLocContent();

			this.entityMappings = ImmutableList.<String> builder().addAll(builder.getEntResults()).build();
			this.entityContents = builder.getEntContent();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((entityContents == null) ? 0 : entityContents.hashCode());
			result = prime * result + ((livingTypeIDs == null) ? 0 : livingTypeIDs.hashCode());
			result = prime * result + ((locContents == null) ? 0 : locContents.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SpawnListEntryBuilder other = (SpawnListEntryBuilder) obj;
			if (entityContents == null) {
				if (other.entityContents != null)
					return false;
			} else if (!entityContents.equals(other.entityContents))
				return false;
			if (livingTypeIDs == null) {
				if (other.livingTypeIDs != null)
					return false;
			} else if (!livingTypeIDs.equals(other.livingTypeIDs))
				return false;
			if (locContents == null) {
				if (other.locContents != null)
					return false;
			} else if (!locContents.equals(other.locContents))
				return false;
			return true;
		}
	}

	public SpawnListEntry build() {
		return new SpawnListEntry(this);
	}

	public SpawnListEntryBuilder setModID(String fileName) {
		if (fileName == null || fileName.trim().equals("")) {
			this.modID = SpawnListEntryBuilder.defaultFileName;
		} else {
			this.modID = fileName;
		}
		return this;
	}

	public String getModID() {
		return this.modID;
	}

	public Optional<String> getLivingHandlerID() {
		return livingHandlerID;
	}

	public SpawnListEntryBuilder setLivingHandlerID(String livingHandlerID) {
		if (livingHandlerID == null) {
			this.livingHandlerID = Optional.absent();
		} else {
			this.livingHandlerID = Optional.of(livingHandlerID);
		}
		return this;
	}

	public Set<String> getLivingTypeIDs() {
		return livingTypeIDs;
	}

	public SpawnListEntryBuilder setLivingTypeIDs(HashSet livingTypeIDs) {
		if (livingTypeIDs == null) {
			this.livingTypeIDs = new HashSet<String>();
		} else {
			this.livingTypeIDs = livingTypeIDs;
		}
		return this;
	}

	public String getLivingTypeContent() {
		return livingTypeContents;
	}

	public void setLivingTypeContents(String contents) {
		this.livingTypeContents = contents;
	}

	public int getWeight() {
		return weight;
	}

	public SpawnListEntryBuilder setWeight(int weight) {
		this.weight = weight;
		return this;
	}

	public String getPassivePackSize() {
		return passivePackSize;
	}

	public SpawnListEntryBuilder setPassivePackSize(String passivePackSize) {
		this.passivePackSize = passivePackSize;
		return this;
	}

	public String getChunkPackSize() {
		return chunkPackSize;
	}

	public SpawnListEntryBuilder setChunkPackSize(String chunkPackSize) {
		this.chunkPackSize = chunkPackSize;
		return this;
	}

	public Optional<String> getCanSpawn() {
		return canSpawn;
	}

	public SpawnListEntryBuilder setCanSpawn(String canSpawn) {
		if (canSpawn == null) {
			this.canSpawn = Optional.absent();
		} else {
			this.canSpawn = Optional.of(canSpawn);
		}
		return this;
	}

	public Optional<String> getPostSpawn() {
		return postSpawn;
	}

	public SpawnListEntryBuilder setPostSpawn(String postSpawn) {
		if (postSpawn == null) {
			this.postSpawn = Optional.absent();
		} else {
			this.postSpawn = Optional.of(postSpawn);
		}
		return this;
	}

	public Set<String> getLocResults() {
		return locMappings;
	}

	public String getLocContent() {
		return locContents;
	}

	public void setLocResults(Set<String> results) {
		this.locMappings = new HashSet<String>(results);
	}

	public void setLocContents(String contents) {
		this.locContents = contents;
	}

	public Set<String> getEntResults() {
		return entityMappings;
	}

	public String getEntContent() {
		return entityContents;
	}

	public void setEntResults(Set<String> results) {
		this.entityMappings = new HashSet<String>(results);
	}

	public void setEntContents(String contents) {
		this.entityContents = contents;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityContents == null) ? 0 : entityContents.hashCode());
		result = prime * result + ((livingTypeIDs == null) ? 0 : livingTypeIDs.hashCode());
		result = prime * result + ((locContents == null) ? 0 : locContents.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SpawnListEntryBuilder other = (SpawnListEntryBuilder) obj;
		if (entityContents == null) {
			if (other.entityContents != null)
				return false;
		} else if (!entityContents.equals(other.entityContents))
			return false;
		if (livingTypeIDs == null) {
			if (other.livingTypeIDs != null)
				return false;
		} else if (!livingTypeIDs.equals(other.livingTypeIDs))
			return false;
		if (locContents == null) {
			if (other.locContents != null)
				return false;
		} else if (!locContents.equals(other.locContents))
			return false;
		return true;
	}
}
