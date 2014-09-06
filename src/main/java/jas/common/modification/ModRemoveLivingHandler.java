package jas.common.modification;

import jas.common.spawner.creature.handler.LivingHandlerBuilder;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

public class ModRemoveLivingHandler extends BaseModification {

	private String handlerID;

	public ModRemoveLivingHandler(String handlerID) {
		this.handlerID = handlerID;
	}

	@Override
	public void applyModification(LivingHandlerRegistry registry) {
		registry.removeLivingHandler(handlerID);
	}
}
