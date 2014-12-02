package jas.common.modification;

import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.creature.type.CreatureType;
import jas.common.spawner.creature.type.CreatureTypeBuilder;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

import java.util.ArrayList;

import com.google.common.base.Optional;

public class ModAddCreatureType extends BaseModification {

	private CreatureTypeBuilder builder;

	public ModAddCreatureType(String typeID) {
		this(typeID, 1, 10);
	}

	public ModAddCreatureType(String typeID, int spawnRate, int maxNumberOfCreature) {
		builder = new CreatureTypeBuilder(typeID, spawnRate, maxNumberOfCreature);
	}

	public ModAddCreatureType(String typeID, int spawnRate, int maxNumberOfCreature, float chunkSpawnChance,
			String spawnMedium, String tags) {
		this(typeID, spawnRate, maxNumberOfCreature);
		builder.withChanceToChunkSpawn(chunkSpawnChance);
		builder.setRawMedium(spawnMedium);
		builder.withSpawnExpression(tags);
	}

	@Override
	public void applyModification(CreatureTypeRegistry registry) {
		registry.addCreatureType(builder);
	}
}