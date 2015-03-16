package jas.spawner.modern.eventspawn.context;

import jas.spawner.modern.eventspawn.SingleSpawnBuilder;
import jas.spawner.modern.eventspawn.SpawnBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class LivingDeathContext implements Context {
	private LivingDeathEvent event;

	public LivingDeathContext(LivingDeathEvent event) {
		this.event = event;
	}

	public boolean isPlayer() {
		return event.entityLiving instanceof EntityPlayer;
	}

	public String livingName() {
		return event.entityLiving.getCommandSenderName();
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
