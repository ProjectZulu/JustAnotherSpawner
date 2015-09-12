package jas.spawner.modern.spawner;

import jas.common.JASLog;
import jas.spawner.modern.spawner.creature.handler.parsing.NBTWriter;
import jas.spawner.modern.spawner.tags.Context;

import java.util.IllegalFormatException;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public interface TagsNBT {

	public boolean writenbt(String[] nbtOperations);

	public static class FunctionsNBT implements TagsNBT {
		private World world;
		private EntityLiving entity;
		// Tagparent.obj()ect where usual working parameters such as pos are found
		public Context parent;

		public FunctionsNBT(Context parent, World world, EntityLiving entity) {
			this.parent = parent;
			this.world = world;
			this.entity = entity;
		}

		// Extract to NBT
		public boolean writenbt(String[] nbtOperations) {
			try {
				NBTTagCompound entityNBT = new NBTTagCompound();
				entity.writeToNBT(entityNBT);
				new NBTWriter(nbtOperations).writeToNBT(entityNBT);
				entity.readFromNBT(entityNBT);
				return true;
			} catch (IllegalFormatException e) {
				JASLog.log().severe("Skipping NBT Write due to %s", e.getMessage());
			} catch (IllegalArgumentException e) {
				JASLog.log().severe("Skipping NBT Write due to %s", e.getMessage());
			}
			return false;
		}
	}
}
