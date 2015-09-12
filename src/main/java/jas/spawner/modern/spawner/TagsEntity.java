package jas.spawner.modern.spawner;

import jas.api.ITameable;
import jas.spawner.modern.spawner.tags.Context;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;

public interface TagsEntity {

	public boolean modspawn();

	public boolean isTamed();

	public boolean isTameable();

	public static class FunctionsEntity implements TagsEntity {
		private EntityLiving entity;
		// Tagparent.obj()ect where usual working parameters such as pos are found
		public Context parent;

		public FunctionsEntity(Context parent, EntityLiving entity) {
			this.parent = parent;
			this.entity = entity;
		}

		/** Entity Tags */
		public boolean modspawn() {
			return entity.getCanSpawnHere();
		}

		public boolean isTamed() {
			if (entity instanceof ITameable) {
				return ((ITameable) entity).isTamed();
			} else if (entity instanceof EntityTameable) {
				return ((EntityTameable) entity).isTamed();
			} else if (entity instanceof EntityHorse) {
				return ((EntityHorse) entity).isTame();
			}
			return false;
		}

		public boolean isTameable() {
			if (entity instanceof ITameable) {
				return ((ITameable) entity).isTameable();
			} else if (entity instanceof EntityTameable) {
				return true;
			}
			return false;
		}
	}
}
