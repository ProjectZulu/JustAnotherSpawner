package jas.spawner.refactor;

import jas.spawner.modern.EntityProperties;
import jas.spawner.modern.spawner.CountInfo;
import jas.spawner.modern.spawner.Tags;
import jas.spawner.refactor.despawn.DespawnRuleBuilder.DespawnRule;
import jas.spawner.refactor.despawn.DespawnRules;
import jas.spawner.refactor.entities.LivingMappings;

import java.util.Collection;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.AllowDespawn;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityDespawner {

	private ExperimentalProfile profile;

	public EntityDespawner(ExperimentalProfile profile) {
		this.profile = profile;
	}

	@SubscribeEvent
	public void despawner(LivingUpdateEvent event) {
		if (event.entityLiving instanceof EntityLiving && event.entityLiving.ticksExisted % 60 == 0
				&& !event.entityLiving.worldObj.isRemote) {
			DespawnRules despawnRules = profile.worldSettings().getSpawnSettings(event.entityLiving.worldObj)
					.despawnRules();
			LivingMappings livingMappings = profile.worldSettings().getSpawnSettings(event.entityLiving.worldObj)
					.livingMappings();
			String fmlName = (String) EntityList.classToStringMapping.get(event.entityLiving.getClass());
			String jasName = livingMappings.keyToMapping().get(fmlName);
			Collection<String> despawnRulesForEntity = despawnRules.mappingToID().get(jasName);
			for (String despawnRuleID : despawnRulesForEntity) {
				DespawnRule despawnRule = despawnRules.iDToGroup().get(despawnRuleID);
				int xCoord = MathHelper.floor_double(event.entityLiving.posX);
				int yCoord = MathHelper.floor_double(event.entityLiving.getEntityBoundingBox().minY);
				int zCoord = MathHelper.floor_double(event.entityLiving.posZ);
				CountInfo countInfo = SpawnerLogic.counter.countEntities(event.entityLiving.worldObj);
				Tags tags = new Tags(event.entityLiving.worldObj, countInfo, xCoord, yCoord, zCoord,
						(EntityLiving) event.entityLiving);
				SpawnerLogic.despawnEntity((EntityLiving) event.entityLiving, tags, despawnRule);
			}
		}
	}

	@SubscribeEvent
	public void entityPersistance(AllowDespawn event) {
		if (!event.entity.worldObj.isRemote) {
			DespawnRules despawnRules = profile.worldSettings().getSpawnSettings(event.entityLiving.worldObj)
					.despawnRules();
			LivingMappings livingMappings = profile.worldSettings().getSpawnSettings(event.entityLiving.worldObj)
					.livingMappings();
			String fmlName = (String) EntityList.classToStringMapping.get(event.entityLiving.getClass());
			String jasName = livingMappings.keyToMapping().get(fmlName);
			if (!despawnRules.mappingToID().get(jasName).isEmpty()) {
				event.setResult(Result.DENY);
			}
		}
	}

	@SubscribeEvent
	public void entityConstructed(EntityConstructing event) {
		if (event.entity instanceof EntityLivingBase) {
			event.entity.registerExtendedProperties(EntityProperties.JAS_PROPERTIES, new EntityProperties());
		}
	}
}