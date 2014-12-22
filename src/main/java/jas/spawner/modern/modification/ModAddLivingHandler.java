package jas.spawner.modern.modification;

import jas.spawner.modern.spawner.biome.group.BiomeGroupRegistry;
import jas.spawner.modern.spawner.creature.handler.LivingHandlerBuilder;
import jas.spawner.modern.spawner.creature.handler.LivingHandlerRegistry;
import jas.spawner.modern.spawner.creature.type.CreatureTypeRegistry;

import java.util.ArrayList;

import com.google.common.base.Optional;

public class ModAddLivingHandler extends BaseModification {

	private LivingHandlerBuilder builder;

	public ModAddLivingHandler(String handlerID) {
		this(handlerID, CreatureTypeRegistry.NONE, false, "");
	}

	public ModAddLivingHandler(String handlerID, String creatureTypeID, boolean shouldSpawn, String optionalParameters) {
		builder = new LivingHandlerBuilder(handlerID, creatureTypeID).setShouldSpawn(shouldSpawn);
	}

	@Override
	public void applyModification(LivingHandlerRegistry registry) {
		registry.addLivingHandler(builder);
	}
}
