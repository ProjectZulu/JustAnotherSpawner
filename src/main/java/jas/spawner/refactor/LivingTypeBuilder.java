package jas.spawner.refactor;

import jas.spawner.modern.spawner.CountInfo;
import jas.spawner.modern.spawner.Tags;
import jas.spawner.refactor.mvel.MVELExpression;

import java.util.List;

import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;

public class LivingTypeBuilder {
	public final String livingTypeID;
	private String isReadyToPssve;
	private String canSpawn;
	private String quickCheck;
	private int iterationsPerChunk;
	private int iterationsPerPack;
	private List<LivingTypeSpawnTriggerBuilder> triggers;
	
	@Deprecated
	private String isReadyToCnk;

	public LivingTypeBuilder(String livingTypeID, int maxSpawnable, int spawnRate, float chunkSpawnChance,
			String spawnMedium, String additionalCanSpawn) {
		this.livingTypeID = livingTypeID;
		this.quickCheck = "(count.globalType('" + livingTypeID + "') * 256 / count.chunks >= " + maxSpawnable;
		this.canSpawn = "(count.globalType('" + livingTypeID + "') * 256 / count.chunks >= " + maxSpawnable
				+ " || !isSpawnMedium('" + spawnMedium + "'))" + additionalCanSpawn;
		this.isReadyToPssve = "world.totalTime() % " + spawnRate + " == 0";
		this.isReadyToCnk = "util.nextFloat() < " + chunkSpawnChance;
		this.iterationsPerChunk = 3;
		this.iterationsPerPack = 4;
		LivingTypeSpawnTriggerBuilder builder = new LivingTypeSpawnTriggerBuilder(livingTypeID);
	}

	public LivingTypeBuilder(String livingTypeID, String isReadyToPssve, String quickCheck, String canSpawn) {
		this.livingTypeID = livingTypeID;
		this.quickCheck = quickCheck;
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
		this.quickCheck = type.quickCheck.expression;
		this.isReadyToPssve = type.isReadyToPssve.expression;
		this.isReadyToCnk = type.isReadyToChnk.expression;
		this.iterationsPerChunk = type.iterationsPerChunk;
		this.iterationsPerPack = type.iterationsPerPack;
	}

	public static class LivingType {
		public final String livingTypeID;
		public final MVELExpression<Boolean> isReadyToPssve;
		public final MVELExpression<Boolean> quickCheck;
		public final MVELExpression<Boolean> canSpawn;

		@Deprecated
		// Needs to be removed; ChunkSpawn is just going to be a separate Trigger. Types can select Passive,
		public final MVELExpression<Boolean> isReadyToChnk;
		public final int iterationsPerChunk;
		public final int iterationsPerPack;

		public LivingType(LivingTypeBuilder builder) {
			this.livingTypeID = builder.livingTypeID;
			this.canSpawn = new MVELExpression<Boolean>(builder.canSpawn);
			this.quickCheck = new MVELExpression<Boolean>(builder.quickCheck);
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

	public String getSpawnExpression() {
		return canSpawn;
	}

	public void setSpawnExpression(String canSpawn) {
		this.canSpawn = canSpawn;
	}

	public String getQuickCheckExpression() {
		return quickCheck;
	}

	public void setQuickCheckExpression(String quickCheck) {
		this.quickCheck = quickCheck;
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