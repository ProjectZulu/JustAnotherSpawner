package jas.spawner.refactor;

import jas.common.JASLog;
import jas.common.helper.MVELHelper;
import jas.spawner.modern.EntityProperties;
import jas.spawner.modern.MVELProfile;
import jas.spawner.modern.spawner.CountInfo;
import jas.spawner.modern.spawner.CustomSpawner;
import jas.spawner.modern.spawner.Tags;
import jas.spawner.refactor.despawn.DespawnRuleBuilder.DespawnRule;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import org.apache.logging.log4j.Level;

/**
 * Common Spawner Processes
 */
public class SpawnerLogic {

	// TODO: Change to Refactor
	public static CountInfo countEntities(World world) {
		return CustomSpawner.spawnCounter.countEntities(world);
	}

	public static void despawnEntity(EntityLiving entity, Tags tags, DespawnRule despawnRule) {
		EntityPlayer entityplayer = entity.worldObj.getClosestPlayerToEntity(entity, -1.0D);
		int xCoord = MathHelper.floor_double(entity.posX);
		int yCoord = MathHelper.floor_double(entity.boundingBox.minY);
		int zCoord = MathHelper.floor_double(entity.posZ);

		if (entityplayer != null) {
			double d0 = entityplayer.posX - entity.posX;
			double d1 = entityplayer.posY - entity.posY;
			double d2 = entityplayer.posZ - entity.posZ;
			double playerDistance = d0 * d0 + d1 * d1 + d2 * d2;

			EntityProperties entityProps = (EntityProperties) entity
					.getExtendedProperties(EntityProperties.JAS_PROPERTIES);
			entityProps.incrementAge(60);

			boolean canDespawn;
			if (despawnRule.canDspwn.isPresent()) {
				canDespawn = !MVELHelper.executeExpression(despawnRule.canDspwn.get().compiled.get(), tags,
						"Error processing canSpawn compiled expression for " + despawnRule.content() + ": "
								+ despawnRule.canDspwn.get().expression);
			} else {
				canDespawn = LivingHelper.canDespawn(entity);
			}

			if (canDespawn == false) {
				entityProps.resetAge();
				return;
			}

			boolean canInstantDespawn;
			if (despawnRule.shouldInstantDspwn.isPresent()) {
				canInstantDespawn = !MVELHelper.executeExpression(despawnRule.shouldInstantDspwn.get().compiled.get(),
						tags, "Error processing canSpawn compiled expression for " + despawnRule.content() + ": "
								+ despawnRule.shouldInstantDspwn.get().expression);
			} else {
				Integer maxRange = MVELProfile.worldSettings().worldProperties().getGlobal().maxDespawnDist;
				canInstantDespawn = playerDistance > maxRange * maxRange;
			}

			if (canInstantDespawn) {
				entity.setDead();
			} else {
				boolean dieOfAge;
				final int rate = 40; // Value from Vanilla
				if (despawnRule.dieOfAge.isPresent()) {
					dieOfAge = !MVELHelper.executeExpression(despawnRule.dieOfAge.get().compiled.get(), tags,
							"Error processing canSpawn compiled expression for " + despawnRule.content() + ": "
									+ despawnRule.dieOfAge.get().expression);
				} else {
					Integer minRange = MVELProfile.worldSettings().worldProperties().getGlobal().despawnDist;
					Integer minAge = MVELProfile.worldSettings().worldProperties().getGlobal().minDespawnTime;
					boolean isOfAge = entityProps.getAge() > minAge;
					boolean validDistance = playerDistance > minRange * minRange;
					dieOfAge = isOfAge && entity.worldObj.rand.nextInt(1 + rate / 3) == 0 && validDistance;
				}

				boolean resetAge;
				if (despawnRule.resetAge.isPresent()) {
					resetAge = !MVELHelper.executeExpression(despawnRule.resetAge.get().compiled.get(), tags,
							"Error processing canSpawn compiled expression for " + despawnRule.content() + ": "
									+ despawnRule.resetAge.get().expression);
				} else {
					Integer minRange = MVELProfile.worldSettings().worldProperties().getGlobal().despawnDist;
					Integer minAge = MVELProfile.worldSettings().worldProperties().getGlobal().minDespawnTime;
					boolean validDistance = playerDistance > minRange * minRange;
					resetAge = !(playerDistance > minRange * minRange);
				}

				if (dieOfAge) {
					JASLog.log().debug(Level.INFO, "Entity %s is DEAD At Age %s rate %s",
							entity.getCommandSenderName(), entityProps.getAge(), rate);
					entity.setDead();
				} else if (resetAge) {
					entityProps.resetAge();
				}
			}
		}
	}

	public static boolean willEntityDespawn(EntityLiving entity, Tags tags, DespawnRule despawnRule) {
		EntityPlayer entityplayer = entity.worldObj.getClosestPlayerToEntity(entity, -1.0D);
		int xCoord = MathHelper.floor_double(entity.posX);
		int yCoord = MathHelper.floor_double(entity.boundingBox.minY);
		int zCoord = MathHelper.floor_double(entity.posZ);

		if (entityplayer != null) {
			double d0 = entityplayer.posX - entity.posX;
			double d1 = entityplayer.posY - entity.posY;
			double d2 = entityplayer.posZ - entity.posZ;
			double playerDistance = d0 * d0 + d1 * d1 + d2 * d2;

			EntityProperties entityProps = (EntityProperties) entity
					.getExtendedProperties(EntityProperties.JAS_PROPERTIES);
			entityProps.incrementAge(60);
			boolean canDespawn;
			if (despawnRule.canDspwn.isPresent()) {
				canDespawn = !MVELHelper.executeExpression(despawnRule.canDspwn.get().compiled.get(), tags,
						"Error processing canSpawn compiled expression for " + despawnRule.content() + ": "
								+ despawnRule.canDspwn.get().expression);
			} else {
				canDespawn = LivingHelper.canDespawn(entity);
			}
			if (canDespawn == false) {
				return false;
			}
			// Other Despawn logic is ignored; entity is assumed to eventually be able to age and despawn
		}
		return true;
	}
}
