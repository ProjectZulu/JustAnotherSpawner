package jas.spawner.refactor;

import jas.spawner.modern.spawner.CountInfo;
import jas.spawner.modern.spawner.Tags;
import jas.spawner.refactor.ConfigLoader.ConfigLoader;
import jas.spawner.refactor.ConfigLoader.LivingTypeLoader;
import jas.spawner.refactor.ConfigLoader.ConfigLoader.LoadedFile;
import jas.spawner.refactor.mvel.MVELExpression;

import java.util.Collection;
import java.util.HashMap;

import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class LivingTypes {
	/* DEFAULT Types */
	public static final String NONE = "NONE";
	public static final String CREATURE = "CREATURE";
	public static final String MONSTER = "MONSTER";
	public static final String AMBIENT = "AMBIENT";
	public static final String WATERCREATURE = "WATERCREATURE";
	public static final String UNDERGROUND = "UNDERGROUND";
	public static final String OPENSKY = "OPENSKY";

	public static final ImmutableSet<String> defaultTypes;
	static {
		Builder<String> builder = ImmutableSet.<String> builder();
		builder.add(NONE).add(CREATURE).add(MONSTER).add(AMBIENT).add(WATERCREATURE).add(UNDERGROUND).add(OPENSKY);
		defaultTypes = builder.build();
	}

	private ImmutableMap<String, LivingType> types;

	public ImmutableMap<String, LivingType> types() {
		return types;
	}

	public LivingType getLivingType(String typeID) {
		return types.get(typeID.toUpperCase());
	}

	public LivingTypes() {
	}

	public void loadFromConfig(ConfigLoader loader) {
		Optional<Collection<LivingTypeBuilder>> read = loader.livingTypeLoader.saveObject.getTypes();

		HashMap<String, LivingTypeBuilder> readTypes = new HashMap<String, LivingTypeBuilder>();
		if (read.isPresent()) {
			for (LivingTypeBuilder creatureBuilder : read.get()) {
				if (creatureBuilder.livingTypeID != null) {
					readTypes.put(creatureBuilder.livingTypeID, creatureBuilder);
				}
			}
		} else {
			readTypes = new HashMap<String, LivingTypeBuilder>();
			LivingTypeBuilder monster = new LivingTypeBuilder(MONSTER, 70, 5, 0, "AIR",
					"||!liquid({0,0,0},{0,0,0})&&!liquid({0,0,0},{0,-1,0})&&normal({0,0,0},{0,1,0})");
			LivingTypeBuilder ambient = new LivingTypeBuilder(AMBIENT, 15, 5, 0, "AIR",
					"||!liquid({0,0,0},{0,0,0})&&!liquid({0,0,0},{0,-1,0})&&normal({0,0,0},{0,1,0})");
			LivingTypeBuilder watercreature = new LivingTypeBuilder(AMBIENT, 15, 5, 0, "WATER",
					"||!liquid({0,0,0},{0,0,0})&&!liquid({0,0,0},{0,-1,0})&&normal({0,0,0},{0,1,0})");
			LivingTypeBuilder underground = new LivingTypeBuilder(UNDERGROUND, 10, 5, 0, "AIR",
					"||!solidside(1,{0,0,0},{0,-1,0})&&liquid({0,0,0},{0,0,0})&&normal({0,0,0},{0,0,0})"
							+ "&&normal({0,0,0},{0,1,0})&&!opaque({0,0,0},{0,-1,0})&&sky()");
			LivingTypeBuilder opensky = new LivingTypeBuilder(OPENSKY, 10, 5, 0, "AIR",
					"||!solidside(1,{0,0,0},{0,-1,0})&&liquid({0,0,0},{0,0,0})&&normal({0,0,0},{0,0,0})"
							+ "&&normal({0,0,0},{0,1,0})&&!opaque({0,0,0},{0,-1,0})&&!sky()");
			LivingTypeBuilder creature = new LivingTypeBuilder(CREATURE, 10, 400, 0.1f, "AIR",
					"||!solidside(1,{0,0,0},{0,-1,0})&&liquid({0,0,0},{0,0,0})&&normal({0,0,0},{0,0,0})"
							+ "&&normal({0,0,0},{0,1,0})&&!opaque({0,0,0},{0,-1,0})&&!sky()");

			readTypes.put(monster.livingTypeID, monster);
			readTypes.put(ambient.livingTypeID, ambient);
			readTypes.put(opensky.livingTypeID, opensky);
			readTypes.put(creature.livingTypeID, creature);
			readTypes.put(underground.livingTypeID, underground);
			readTypes.put(watercreature.livingTypeID, watercreature);
		}
		ImmutableMap.Builder<String, LivingType> builder = ImmutableMap.<String, LivingType> builder();
		for (LivingTypeBuilder livingType : readTypes.values()) {
			builder.put(livingType.livingTypeID, livingType.build());
		}
		types = builder.build();
	}

	public void saveToConfig(ConfigLoader loader) {
		loader.livingTypeLoader = new LoadedFile(new LivingTypeLoader(this));
	}

	public static class LivingType {
		public final String livingTypeID;
		public final MVELExpression<Boolean> canSpawn;
		public final MVELExpression<Boolean> isReadyToPssve;
		public final MVELExpression<Boolean> isReadyToChnk;
		public final int iterationsPerChunk;
		public final int iterationsPerPack;

		public LivingType(LivingTypeBuilder builder) {
			this.livingTypeID = builder.livingTypeID;
			this.canSpawn = new MVELExpression<Boolean>(builder.canSpawn);
			this.isReadyToPssve = new MVELExpression<Boolean>(builder.getIsReadyToPssve());
			this.isReadyToChnk = new MVELExpression<Boolean>(builder.getIsReadyToCnk());
			this.iterationsPerChunk = builder.getIterationsPerChunk();
			this.iterationsPerPack = builder.getIterationsPerPack();
		}

		public boolean canSpawnAtLocation(World worldServer, CountInfo countInfo, ChunkPosition spawningPoint) {
			Tags tags = new Tags(worldServer, countInfo, spawningPoint.chunkPosX, spawningPoint.chunkPosY,
					spawningPoint.chunkPosZ);
			return canSpawn.evaluate(tags, "Error processing spawnExpression compiled expression for " + livingTypeID
					+ ": " + canSpawn.expression);
		}
	}

	public static class LivingTypeBuilder {
		public final String livingTypeID;
		private String canSpawn;
		private String isReadyToPssve;
		private String isReadyToCnk;
		private int iterationsPerChunk;
		private int iterationsPerPack;

		public LivingTypeBuilder(String livingTypeID, int maxSpawnable, int spawnRate, float chunkSpawnChance,
				String spawnMedium, String additionalCanSpawn) {
			this.livingTypeID = livingTypeID;
			this.canSpawn = "(count.globalType('" + livingTypeID + "') * 256 / count.chunks >= " + maxSpawnable
					+ " || !isSpawnMedium('" + spawnMedium + "'))" + additionalCanSpawn;
			this.isReadyToPssve = "world.totalTime() % " + spawnRate + " == 0";
			this.isReadyToCnk = "util.nextFloat() < " + chunkSpawnChance;
			this.iterationsPerChunk = 3;
			this.iterationsPerPack = 4;
		}

		public LivingTypeBuilder(String livingTypeID, String isReadyToPssve, String canSpawn) {
			this.livingTypeID = livingTypeID;
			this.canSpawn = canSpawn; // count.getGlobalEntityTypeCount('Creature') < 60 * #Players / 256 &&
										// isSpawnMedium('water')
			this.isReadyToPssve = isReadyToPssve;
			this.isReadyToCnk = "util.randFloat() < 0.0";
			this.iterationsPerChunk = 3;
			this.iterationsPerPack = 4;
		}

		public LivingTypeBuilder(LivingType type) {
			this.livingTypeID = type.livingTypeID;
			this.canSpawn = type.canSpawn.expression;
			this.isReadyToPssve = type.isReadyToPssve.expression;
			this.isReadyToCnk = type.isReadyToChnk.expression;
			this.iterationsPerChunk = type.iterationsPerChunk;
			this.iterationsPerPack = type.iterationsPerPack;
		}

		public String getSpawnExpression() {
			return canSpawn;
		}

		public void setSpawnExpression(String canSpawn) {
			this.canSpawn = canSpawn;
		}

		public String getIsReadyToPssve() {
			return isReadyToPssve;
		}

		public void setisReadyToPssve(String isReadyToPssve) {
			this.isReadyToPssve = isReadyToPssve;
		}

		public String getIsReadyToCnk() {
			return isReadyToCnk;
		}

		public void setIsReadyToCnk(String isReadyToCnk) {
			this.isReadyToCnk = isReadyToCnk;
		}

		public Integer getIterationsPerChunk() {
			return iterationsPerChunk;
		}

		public void setIterationsPerChunk(Integer iterationsPerChunk) {
			this.iterationsPerChunk = iterationsPerChunk;
		}

		public Integer getIterationsPerPack() {
			return iterationsPerPack;
		}

		public void setIterationsPerPack(Integer iterationsPerPack) {
			this.iterationsPerPack = iterationsPerPack;
		}

		public LivingType build() {
			return new LivingType(this);
		}
	}
}
