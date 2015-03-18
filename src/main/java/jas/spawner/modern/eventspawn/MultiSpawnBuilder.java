package jas.spawner.modern.eventspawn;

import jas.common.JASLog;
import jas.spawner.modern.spawner.creature.handler.LivingGroupRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class MultiSpawnBuilder implements SpawnBuilder {
	public List<SpawnData> spawnData;
	public double groupPosX;
	public double groupPosY;
	public double groupPosZ;

	private static class SpawnData {
		public String entityMapping;
		public double spawnPosX;
		public double spawnPosY;
		public double spawnPosZ;

		public SpawnData(String entityMapping, double spawnPosX, double spawnPosY, double spawnPosZ) {
			this.entityMapping = entityMapping;
			this.spawnPosX = spawnPosX;
			this.spawnPosY = spawnPosY;
			this.spawnPosZ = spawnPosZ;
		}
	}

	public MultiSpawnBuilder(String entityMapping, double groupPosX, double groupPosY, double groupPosZ) {
		this.spawnData = new ArrayList<MultiSpawnBuilder.SpawnData>(4);
		spawnData.add(new SpawnData(entityMapping, 0, 0, 0));
		this.groupPosX = groupPosX;
		this.groupPosY = groupPosY;
		this.groupPosZ = groupPosZ;
	}

	public SpawnBuilder offset(int radius) {
		Random rand = new Random();
		groupPosX = groupPosX + rand.nextInt(radius) - rand.nextInt(radius);
		groupPosZ = groupPosZ + rand.nextInt(radius) - rand.nextInt(radius);
		return this;
	}

	public SpawnBuilder offset(double offsetX, double offsetY, double offsetZ) {
		this.groupPosX += offsetX;
		this.groupPosY += offsetY;
		this.groupPosZ += offsetZ;
		return this;
	}

	public SpawnBuilder offset(int element, int radius) {
		SpawnData data = spawnData.get(element);
		Random rand = new Random();
		data.spawnPosX = data.spawnPosX + rand.nextInt(radius) - rand.nextInt(radius);
		data.spawnPosZ = data.spawnPosZ + rand.nextInt(radius) - rand.nextInt(radius);
		return this;
	}

	public SpawnBuilder offset(String dataName, double offsetX, double offsetY, double offsetZ) {
		for (SpawnData data : spawnData) {
			if (data.entityMapping.equals(dataName)) {
				data.spawnPosX += offsetX;
				data.spawnPosY += offsetY;
				data.spawnPosZ += offsetZ;
			}
		}
		return this;
	}

	public SpawnBuilder alsoSpawn(String entityMapping, double offsetX, double offsetY, double offsetZ) {
		spawnData.add(new SpawnData(entityMapping, offsetX, offsetY, offsetZ));
		return this;
	}

	public void spawn(World world, LivingGroupRegistry groupRegistry) {
		for (SpawnData data : spawnData) {
			Class<? extends EntityLiving> livingToSpawn = groupRegistry.JASNametoEntityClass.get(data.entityMapping);
			if (livingToSpawn == null) {
				JASLog.log().severe(
						"SpawnBuilder Error. Provided mappings %s does not correspond to a valid JAS Mapping.",
						data.entityMapping);
				return;
			}
			EntityLiving entityliving;
			try {
				entityliving = livingToSpawn.getConstructor(new Class[] { World.class }).newInstance(
						new Object[] { world });
			} catch (Exception exception) {
				exception.printStackTrace();
				return;
			}
			entityliving.setLocationAndAngles(groupPosX + data.spawnPosX, groupPosY + data.spawnPosY, groupPosZ
					+ data.spawnPosZ, world.rand.nextFloat() * 360.0F, 0.0F);
			world.spawnEntityInWorld(entityliving);
		}
	}
}
