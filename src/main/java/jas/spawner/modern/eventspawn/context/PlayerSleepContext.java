package jas.spawner.modern.eventspawn.context;

import jas.spawner.modern.eventspawn.SingleSpawnBuilder;
import jas.spawner.modern.eventspawn.SpawnBuilder;

import java.util.List;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.EnumStatus;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;

public class PlayerSleepContext extends EventContext {
	private PlayerSleepInBedEvent event;

	public PlayerSleepContext(PlayerSleepInBedEvent event) {
		super(event.entity.worldObj, event.x, event.y, event.z);
		this.event = event;
	}

	public boolean sleepResultOk() {
		return getResult().equals(EnumStatus.OK);
	}

	public boolean sleepResultNotSafe() {
		return getResult().equals(EnumStatus.NOT_SAFE);
	}

	public boolean sleepResult(String result) {
		return getResult().toString().equalsIgnoreCase(result);
	}

	public EntityPlayer.EnumStatus getResult() {
		if (event.result != null) {
			return event.result;
		}
		EntityPlayer player = event.entityPlayer;
		World worldObj = event.entityPlayer.worldObj;

		if (!worldObj.isRemote) {
			if (player.isPlayerSleeping() || !player.isEntityAlive()) {
				return EntityPlayer.EnumStatus.OTHER_PROBLEM;
			}

			if (!worldObj.provider.isSurfaceWorld()) {
				return EntityPlayer.EnumStatus.NOT_POSSIBLE_HERE;
			}

			if (worldObj.isDaytime()) {
				return EntityPlayer.EnumStatus.NOT_POSSIBLE_NOW;
			}

			if (Math.abs(player.posX - (double) event.x) > 3.0D
					|| Math.abs(player.posY - (double) event.y) > 2.0D
					|| Math.abs(player.posZ - (double)  event.z) > 3.0D) {
				return EntityPlayer.EnumStatus.TOO_FAR_AWAY;
			}
			double horD = 8.0D;
			double verD = 5.0D;
			List list = worldObj.getEntitiesWithinAABB(EntityMob.class, AxisAlignedBB.getBoundingBox(
					(double) event.x - horD, (double) event.y - verD, (double)  event.z - horD,
					(double) event.x + horD, (double) event.y + verD, (double)  event.z + horD));
			if (!list.isEmpty()) {
				return EntityPlayer.EnumStatus.NOT_SAFE;
			}
		}
		return EntityPlayer.EnumStatus.OK;
	}
	
	public SpawnBuilder spawn(String entityMapping) {
		return new SingleSpawnBuilder(entityMapping, event.entity.posX, event.entity.posY, event.entity.posZ);
	}
}

