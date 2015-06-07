package jas.spawner.modern.eventspawn.context;

import jas.spawner.modern.eventspawn.SingleSpawnBuilder;
import jas.spawner.modern.eventspawn.SpawnBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class LivingDeathContext extends CommonContext {
	private LivingDeathEvent event;

	public LivingDeathContext(LivingDeathEvent event) {
		super(event.entity.worldObj, MathHelper.floor_double(event.entity.posX), MathHelper
				.floor_double(event.entity.posY), MathHelper.floor_double(event.entity.posZ));
		this.event = event;
	}

	public boolean isPlayer() {
		return event.entityLiving instanceof EntityPlayer;
	}

	public String livingName() {
		return event.entityLiving.getName();
	}

	public boolean isDamageSource(String desiredDamageSource) {
		return event.source.damageType.equals(desiredDamageSource);
	}

	public boolean isProjectile() {
		return event.source.isProjectile();
	}

	public boolean isExplosion() {
		return event.source.isExplosion();
	}

	public boolean isFireDamage() {
		return event.source.isFireDamage();
	}

	public boolean isMagicDamage() {
		return event.source.isMagicDamage();
	}

	public SpawnBuilder spawn(String entityMapping) {
		return new SingleSpawnBuilder(entityMapping, event.entity.posX, event.entity.posY, event.entity.posZ);
	}
}
