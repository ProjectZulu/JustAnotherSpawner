package jas.common;

import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class LivingHelper {

    public static EntityLiving createCreature(Class<? extends EntityLiving> livingClass) {
        try {
            return livingClass.getConstructor(new Class[] { World.class }).newInstance(new Object[] { (null) });
        } catch (Exception exception) {
            JASLog.warning(
                    "Entity %s could not be initialized with a fake world. Default defaults will be provided for it.",
                    livingClass.getSimpleName());
        }
        return null;
    }
}
