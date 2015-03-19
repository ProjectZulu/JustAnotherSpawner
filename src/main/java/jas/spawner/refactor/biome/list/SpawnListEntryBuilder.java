package jas.spawner.refactor.biome.list;

import jas.spawner.refactor.entities.Group.MutableContentGroup;
import jas.spawner.refactor.entities.ListContentGroup;
import jas.spawner.refactor.mvel.MVELExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class SpawnListEntryBuilder implements MutableContentGroup<String> {
	private transient Set<String> mappings;
	/** String Used to Build Mappings i.e. {desert,A|Forest,glacier} */
	private String contents;
	private String spawnListEntryID;

	private String livingHandlerID;
	private String livingTypeID;

	private String weight;
	private String passivePackSize;
	private String chunkPackSize; // [int] Replaces old min/max: returns the random result itself
	private String canSpawn;
	private String postSpawn;
	private String entityToSpawn;

	public SpawnListEntryBuilder() {
		this.setLivingHandlerID(null);
		this.setLivingTypeID(null);
		this.setWeight("0");
		this.setPassivePackSize("3");
		this.setChunkPackSize("0 + util.rand(1 + 4 - 0)");

		this.setCanSpawn("");
		this.setPostSpawn("");
		this.setEntityToSpawn("");
		this.setContents("");
		this.setResults(new HashSet<String>());
		this.recalculateEntryID();
	}

	public SpawnListEntryBuilder(String livingHandlerID, String livingTypeID, String biomeExpression,
			String entityExpression) {
		this.setLivingHandlerID(livingHandlerID);
		this.setLivingTypeID(livingTypeID);
		this.setWeight("0");
		this.setPassivePackSize("3");
		this.setChunkPackSize("0 + util.rand(1 + 4 - 0)");

		this.setCanSpawn("");
		this.setPostSpawn("");
		this.setEntityToSpawn(entityExpression);

		this.setContents(biomeExpression);
		this.setResults(new HashSet<String>());
		this.recalculateEntryID();
	}

	public SpawnListEntryBuilder(SpawnListEntry entry) {
		this.spawnListEntryID = entry.spawnListEntryID;
		this.setLivingHandlerID(entry.livingHandlerID);
		this.setLivingTypeID(entry.livingTypeID);
		this.setWeight(entry.weight.expression);
		this.setPassivePackSize(entry.passivePackSize.expression);
		this.setChunkPackSize(entry.chunkPackSize.expression);

		this.setCanSpawn(entry.canSpawn.expression);
		this.setPostSpawn(entry.postSpawn.expression);
		this.setEntityToSpawn(entry.entityToSpawn.expression);
		this.setContents(entry.contents);
		this.setResults(entry.mappings);
		this.recalculateEntryID();
	}

	private void recalculateEntryID() {
		spawnListEntryID = livingHandlerID + livingTypeID + contents.toString();
	}

	public static class SpawnListEntry implements ContentGroup<String> {
		public final String spawnListEntryID;
		private final transient ImmutableSet<String> mappings;
		/** String Used to Build Mappings i.e. {desert,A|Forest,glacier} */
		private final transient String contents;

		public final String livingHandlerID;
		public final String livingTypeID;

		public final MVELExpression<Integer> weight;
		public final MVELExpression<Integer> passivePackSize;
		public final MVELExpression<Integer> chunkPackSize;
		public final MVELExpression<Boolean> canSpawn;
		public final MVELExpression<Boolean> postSpawn;
		public final MVELExpression<EntitySpawn> entityToSpawn;

		private SpawnListEntry(SpawnListEntryBuilder builder) {
			this.spawnListEntryID = builder.spawnListEntryID;
			this.mappings = ImmutableSet.<String> builder().addAll(builder.results()).build();
			this.contents = builder.content();
			this.livingHandlerID = builder.spawnListEntryID;
			this.livingTypeID = builder.spawnListEntryID;
			this.weight = new MVELExpression<Integer>(builder.getWeight());
			this.passivePackSize = new MVELExpression<Integer>(builder.getPassivePackSize());
			this.chunkPackSize = new MVELExpression<Integer>(builder.getChunkPackSize());
			this.canSpawn = new MVELExpression<Boolean>(builder.getCanSpawn());
			this.postSpawn = new MVELExpression<Boolean>(builder.getPostSpawn());
			this.entityToSpawn = new MVELExpression<EntitySpawn>(builder.getEntityToSpawn());
		}

		@Override
		public String iD() {
			return spawnListEntryID;
		}

		@Override
		public Set<String> results() {
			return mappings;
		}

		@Override
		public String content() {
			return contents;
		}
	}

	public SpawnListEntry build() {
		return new SpawnListEntry(this);
	}

	@Override
	public String iD() {
		return spawnListEntryID;
	}

	public String getLivingHandlerID() {
		return livingHandlerID;
	}

	public SpawnListEntryBuilder setLivingHandlerID(String livingHandlerID) {
		this.livingHandlerID = livingHandlerID;
		recalculateEntryID();
		return this;
	}

	public String getLivingTypeID() {
		return livingTypeID;
	}

	public SpawnListEntryBuilder setLivingTypeID(String livingTypeID) {
		this.livingTypeID = livingTypeID;
		recalculateEntryID();
		return this;
	}

	public String getWeight() {
		return weight;
	}

	public SpawnListEntryBuilder setWeight(String weight) {
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

	public String getCanSpawn() {
		return canSpawn;
	}

	public SpawnListEntryBuilder setCanSpawn(String canSpawn) {
		this.canSpawn = canSpawn;
		return this;
	}

	public String getPostSpawn() {
		return postSpawn;
	}

	public SpawnListEntryBuilder setPostSpawn(String postSpawn) {
		this.postSpawn = postSpawn;
		return this;
	}

	public String getEntityToSpawn() {
		return entityToSpawn;
	}

	public SpawnListEntryBuilder setEntityToSpawn(String entityToSpawn) {
		this.entityToSpawn = entityToSpawn;
		return this;
	}

	@Override
	public Set<String> results() {
		return mappings;
	}

	@Override
	public String content() {
		return contents;
	}

	@Override
	public void setResults(Set<String> results) {
		this.mappings = new HashSet<String>(results);
	}

	@Override
	public void setContents(String contents) {
		this.contents = contents;
		recalculateEntryID();
	}
}
