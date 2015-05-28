package jas.spawner.refactor.entities;

import jas.spawner.refactor.entities.Group.MutableContentGroup;
import jas.spawner.refactor.mvel.MVELExpression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class LivingHandlerBuilder implements MutableContentGroup<List<String>> {
	public static final String defaultFileName = "vanilla";

	private String livingHandlerID;
	private String modID;

	/** Expression that determines if Entity can spawn */
	private Optional<String> canSpawn;
	/** Expression called after entity has spawned; typically used to alter entity NBT data */
	private Optional<String> postSpawn;
	private transient Set<String> results = new HashSet<String>();
	/** String Used to Build Group Content Names i.e. {spider,A|HOSTILE,sheep} */
	private ArrayList<String> contents;

	/** Chance this SLE will be selected */
	private Optional<String> weight;
	/** Expression to determine the maximum number of entities that can spawn in a passive spawn loop */
	private Optional<String> passivePackSize;
	/** Expression to determine the maximum number of entities that can spawn in a chunk spawn loop */
	private Optional<String> chunkPackSize; // [int] Replaces old min/max: returns the random result itself

	public LivingHandlerBuilder(String livingHandlerID) {
		if (livingHandlerID == null) {
			throw new IllegalArgumentException("LivingHandlerBuilder name cannot be null");
		}
		this.livingHandlerID = livingHandlerID;
		this.setModID(defaultFileName);
		this.setCanSpawn("!(modspawn || sp.clearBounding)");
		this.setPostSpawn(null);
		this.setWeight(null);
		this.setPassivePackSize(null);
		this.setChunkPackSize(null);
		// this.canDspwn = "false";
		// this.shouldInstantDspwn = "sp.plyrDist < 128";
		// this.dieOfAge = "!(ent.age > 600 && util.random(1+40/3,0,0))";
		// this.resetAge = "sp.plyrDist > 32";
	}

	public LivingHandlerBuilder(LivingHandler livingHandler) {
		this.livingHandlerID = livingHandler.livingHandlerID;
		this.setModID(livingHandler.modID);
		this.setCanSpawn(livingHandler.canSpawn.isPresent() ? livingHandler.canSpawn.get().expression : null);
		this.setPostSpawn(livingHandler.postSpawn.isPresent() ? livingHandler.postSpawn.get().expression : null);
		this.setWeight(livingHandler.weight.isPresent() ? livingHandler.weight.get().expression : null);
		this.setPassivePackSize(livingHandler.passivePacketSize.isPresent() ? livingHandler.passivePacketSize.get().expression
				: null);
		this.setPassivePackSize(livingHandler.passivePacketSize.isPresent() ? livingHandler.passivePacketSize.get().expression
				: null);
		this.setChunkPackSize(livingHandler.chunkPackSize.isPresent() ? livingHandler.chunkPackSize.get().expression
				: null);
	}

	public static class LivingHandler implements ListContentGroup {
		public final String livingHandlerID;
		public final String modID;
		public final Optional<MVELExpression<Boolean>> canSpawn;
		public final Optional<MVELExpression<Boolean>> postSpawn;
		public final Optional<MVELExpression<Integer>> weight;
		public final Optional<MVELExpression<Integer>> passivePacketSize;
		public final Optional<MVELExpression<Integer>> chunkPackSize;

		private final ImmutableSet<String> results;
		/** String Used to Build Group Content Names i.e. {spider,A|HOSTILE,sheep} */
		private final ImmutableList<String> contents;

		private LivingHandler(LivingHandlerBuilder builder) {
			this.livingHandlerID = builder.livingHandlerID;
			this.modID = builder.getModID();
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

			if (builder.getWeight().isPresent()) {
				this.weight = Optional.of(new MVELExpression<Integer>(builder.getWeight().get()));
			} else {
				this.weight = Optional.absent();
			}
			if (builder.getPassivePackSize().isPresent()) {
				this.passivePacketSize = Optional.of(new MVELExpression<Integer>(builder.getPassivePackSize().get()));
			} else {
				this.passivePacketSize = Optional.absent();
			}
			if (builder.getChunkPackSize().isPresent()) {
				this.chunkPackSize = Optional.of(new MVELExpression<Integer>(builder.getChunkPackSize().get()));
			} else {
				this.chunkPackSize = Optional.absent();
			}
			this.results = ImmutableSet.<String> builder().addAll(builder.results()).build();
			this.contents = ImmutableList.<String> builder().addAll(builder.results()).build();
		}

		@Override
		public String iD() {
			return livingHandlerID;
		}

		@Override
		public Set<String> results() {
			return results;
		}

		@Override
		public List<String> content() {
			return contents;
		}
	}

	public LivingHandler build() {
		return new LivingHandler(this);
	}

	public String getLivingHandlerID() {
		return livingHandlerID;
	}

	public String getModID() {
		return modID;
	}

	public LivingHandlerBuilder setModID(String modID) {
		if (modID == null || modID.trim().equals("")) {
			this.modID = defaultFileName;
		} else {
			this.modID = modID;
		}
		return this;
	}

	public Optional<String> getCanSpawn() {
		return canSpawn;
	}

	public LivingHandlerBuilder setCanSpawn(String canSpawn) {
		if (canSpawn == null || canSpawn.trim().equals("")) {
			this.canSpawn = Optional.absent();
		} else {
			this.canSpawn = Optional.of(canSpawn);
		}
		return this;
	}

	public Optional<String> getPostSpawn() {
		return postSpawn;
	}

	public LivingHandlerBuilder setPostSpawn(String postSpawn) {
		if (postSpawn == null || postSpawn.trim().equals("")) {
			this.postSpawn = Optional.absent();
		} else {
			this.postSpawn = Optional.of(postSpawn);
		}
		return this;
	}

	public Optional<String> getWeight() {
		return weight;
	}

	public LivingHandlerBuilder setWeight(String weight) {
		if (weight == null || weight.trim().equals("")) {
			this.weight = Optional.absent();
		} else {
			this.weight = Optional.of(weight);
		}
		return this;
	}

	public Optional<String> getPassivePackSize() {
		return passivePackSize;
	}

	public LivingHandlerBuilder setPassivePackSize(String passivePackSize) {
		if (passivePackSize == null || passivePackSize.trim().equals("")) {
			this.passivePackSize = Optional.absent();
		} else {
			this.passivePackSize = Optional.of(passivePackSize);
		}
		return this;
	}

	public Optional<String> getChunkPackSize() {
		return chunkPackSize;
	}

	public LivingHandlerBuilder setChunkPackSize(String chunkPackSize) {
		if (chunkPackSize == null || chunkPackSize.trim().equals("")) {
			this.chunkPackSize = Optional.absent();
		} else {
			this.chunkPackSize = Optional.of(chunkPackSize);
		}
		return this;
	}

	@Override
	public String iD() {
		return livingHandlerID;
	}

	@Override
	public Set<String> results() {
		return results;
	}

	@Override
	public List<String> content() {
		return contents;
	}

	@Override
	public void setResults(Set<String> results) {
		this.results = new HashSet<String>(results);
	}

	@Override
	public void setContents(List<String> contents) {
		this.contents = new ArrayList<String>(contents);
	}
}
