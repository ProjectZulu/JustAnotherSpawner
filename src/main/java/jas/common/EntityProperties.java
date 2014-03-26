package jas.common;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class EntityProperties implements IExtendedEntityProperties {
    public static final String JAS_PROPERTIES = "jasentityproperties";
    private int age = 0;

    public int getAge() {
        return age;
    }

    public void resetAge() {
        age = 0;
    }

    public void incrementAge(int increment) {
        if (increment >= 0) {
            age += increment;
        }
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
    }

    @Override
    public void init(Entity entity, World world) {
    }
}