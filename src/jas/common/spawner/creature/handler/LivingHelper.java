package jas.common.spawner.creature.handler;

import jas.common.JASLog;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;

public class LivingHelper {
    
    /**
     * Create Instance of Creature Class
     * 
     * @param livingClass Class of Desired Creature
     * @param worldServer Instance of World
     * @return
     */
    public static EntityLiving createCreature(Class<? extends EntityLiving> livingClass, World world) {
        try {
            return livingClass.getConstructor(new Class[] { World.class }).newInstance(new Object[] { world });
        } catch (Exception exception) {
            JASLog.warning("Entity %s could not be initialized. Default defaults will be provided for it.",
                    livingClass.getSimpleName());
        }
        return null;
    }
}
