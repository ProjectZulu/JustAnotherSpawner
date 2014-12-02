package jas.common.modification;

import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

public class ModRemoveCreatureType extends BaseModification {

	private String typeID;

	public ModRemoveCreatureType(String typeID) {
		this.typeID = typeID;
	}

	@Override
	public void applyModification(CreatureTypeRegistry registry) {
		registry.removeCreatureType(typeID);
	}
}