package jas.spawner.modern.eventspawn;

import jas.common.JASLog;
import jas.spawner.modern.spawner.creature.handler.LivingGroupRegistry;

import java.util.Random;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class SingleSpawnBuilder implements SpawnBuilder {
	private String entityMapping;
	private double spawnPosX;
	private double spawnPosY;
	private double spawnPosZ;

	public SingleSpawnBuilder(String entityMapping, double spawnPosX, double spawnPosY, double spawnPosZ) {
		this.entityMapping = entityMapping;
		this.spawnPosX = spawnPosX;
		this.spawnPosY = spawnPosY;
		this.spawnPosZ = spawnPosZ;
	}

	public SpawnBuilder offset(int radius) {
		Random rand = new Random();
		this.spawnPosX = spawnPosX + rand.nextInt(radius) - rand.nextInt(radius);
		this.spawnPosZ = spawnPosZ + rand.nextInt(radius) - rand.nextInt(radius);
		return this;
	}

	public SpawnBuilder offset(double spawnPosX, double spawnPosY, double spawnPosZ) {
		this.spawnPosX += spawnPosX;
		this.spawnPosY += spawnPosY;
		this.spawnPosZ += spawnPosZ;
		return this;
	}

	public SpawnBuilder alsoSpawn(String entityMapping, double offsetX, double offsetY, double offsetZ) {
		MultiSpawnBuilder builder = new MultiSpawnBuilder(this.entityMapping, spawnPosX, spawnPosY, spawnPosZ);
		builder.alsoSpawn(entityMapping, offsetX, offsetY, offsetZ);
		return builder;
	}

	public void spawn(World world, LivingGroupRegistry groupRegistry) {
		Class<? extends EntityLiving> livingToSpawn = groupRegistry.JASNametoEntityClass.get(entityMapping);
		if (livingToSpawn == null) {
			JASLog.log().severe("SpawnBuilder Error. Provided mappings %s does not correspond to a valid JAS Mapping.",
					entityMapping);
			return;
		}
		EntityLiving entityliving;
		try {
			entityliving = livingToSpawn.getConstructor(new Class[] { World.class })
					.newInstance(new Object[] { world });
		} catch (Exception exception) {
			exception.printStackTrace();
			return;
		}
		entityliving.setLocationAndAngles(spawnPosX, spawnPosY, spawnPosZ, world.rand.nextFloat() * 360.0F, 0.0F);
		world.spawnEntityInWorld(entityliving);
	}
}
