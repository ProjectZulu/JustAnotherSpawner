package jas.refactor;

import java.io.File;
import java.io.Serializable;

import net.minecraft.block.material.Material;
import net.minecraft.world.World;

import org.mvel2.MVEL;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import jas.common.JASLog;
import jas.common.spawner.Tags;
import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.biome.structure.StructureHandlerRegistry;
import jas.common.spawner.creature.entry.BiomeSpawnListRegistry;
import jas.common.spawner.creature.handler.LivingGroupRegistry;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

/**
 * Contains all WorldSpecific spawn settings. Multiple instances may exist per world; there is a default implementation
 * for all dimensions but each dimension is capable of having its own override
 */
public class SpawnSettings {
	// public final int dimesionOverride = 0; // Declare if present

	private BiomeGroupRegistry biomeGroupRegistry;
	private LivingGroupRegistry livingGroupRegistry;

	private CreatureTypeRegistry creatureTypeRegistry;
	private LivingHandlerRegistry livingHandlerRegistry;
	private BiomeSpawnListRegistry biomeSpawnListRegistry;

	private StructureHandlerRegistry structureHandlerRegistry;

	public SpawnSettings(WorldProperties worldProperties, File settingsDirectory) {

	}
}