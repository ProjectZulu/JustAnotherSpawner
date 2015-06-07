package jas.spawner.modern.modification;

import jas.spawner.modern.spawner.creature.handler.LivingHandlerRegistry;

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
