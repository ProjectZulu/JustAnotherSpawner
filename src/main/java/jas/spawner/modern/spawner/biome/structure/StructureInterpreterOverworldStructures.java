package jas.spawner.modern.spawner.biome.structure;

import jas.api.StructureInterpreter;
import jas.common.helper.ReflectionHelper;
import jas.common.helper.VanillaHelper;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class StructureInterpreterOverworldStructures implements StructureInterpreter {

	public static final String STRONGHOLD_KEY = "Stronghold";
	public static final String MINESHAFT_KEY = "Mineshaft";

	private WeakReference<MapGenStructure> strongholdRef = new WeakReference(null);
	private WeakReference<MapGenStructure> mineshaftRef = new WeakReference(null);

	@Override
	public Collection<String> getStructureKeys() {
		return Arrays.asList(STRONGHOLD_KEY, MINESHAFT_KEY);
	}

	@Override
	public Collection<SpawnListEntry> getStructureSpawnList(String structureKey) {
		return Collections.emptyList();
	}

	@Override
	public String areCoordsStructure(World world, int xCoord, int yCoord, int zCoord) {
		MapGenStructure strongholdGen = strongholdRef.get();
		MapGenStructure mineshaftGen = mineshaftRef.get();
		if (strongholdGen == null || mineshaftGen == null) {
			ChunkProviderServer chunkprovider = (ChunkProviderServer) world.getChunkProvider();
			ChunkProviderGenerate chunkProviderGenerate = chunkprovider.serverChunkGenerator instanceof ChunkProviderGenerate ? (ChunkProviderGenerate) chunkprovider.serverChunkGenerator
					: null;
			if (chunkProviderGenerate == null || !areMapFeaturesEnabled(chunkProviderGenerate)) {
				return null;
			}
			if (strongholdGen == null) {
				refreshStronghold(chunkProviderGenerate);
			}
			if (strongholdGen == null) {
				refreshMineshaft(chunkProviderGenerate);
			}
		}

		String stronghold = isLocationStructure(strongholdGen, STRONGHOLD_KEY, xCoord, yCoord, zCoord);
		if (stronghold != null) {
			return stronghold;
		}

		String mineshaft = isLocationStructure(mineshaftGen, MINESHAFT_KEY, xCoord, yCoord, zCoord);
		if (mineshaft != null) {
			return mineshaft;
		}
		return null;
	}

	private MapGenStructure refreshStronghold(ChunkProviderGenerate chunkProviderGenerate) {
		MapGenStructure structure = getStructure(chunkProviderGenerate, MapGenStronghold.class, "strongholdGenerator",
				"field_73225_u");
		strongholdRef = new WeakReference(structure);
		return structure;
	}

	private MapGenStructure refreshMineshaft(ChunkProviderGenerate chunkProviderGenerate) {
		MapGenStructure structure = getStructure(chunkProviderGenerate, MapGenMineshaft.class, "mineshaftGenerator",
				"field_73223_w");
		mineshaftRef = new WeakReference(structure);
		return structure;
	}

	private boolean areMapFeaturesEnabled(ChunkProviderGenerate chunkProviderGenerate) {
		Boolean areMapFeaturesEnabled = false;
		if (chunkProviderGenerate != null) {
			try {
				areMapFeaturesEnabled = ReflectionHelper.getCatchableFieldFromReflection("field_73229_q",
						ChunkProviderGenerate.class, chunkProviderGenerate, Boolean.class);
			} catch (NoSuchFieldException e) {
				areMapFeaturesEnabled = ReflectionHelper.getFieldFromReflection("mapFeaturesEnabled",
						ChunkProviderGenerate.class, chunkProviderGenerate, Boolean.class);
			}
		}
		return areMapFeaturesEnabled;
	}
	
	private MapGenStructure getStructure(ChunkProviderGenerate chunkProviderGenerate,
			Class<? extends MapGenStructure> fieldClass, String fieldName, String obfName) {
		MapGenStructure structure;
		try {
			structure = ReflectionHelper.getCatchableFieldFromReflection(obfName, ChunkProviderGenerate.class,
					chunkProviderGenerate, fieldClass);
		} catch (NoSuchFieldException e) {
			structure = ReflectionHelper.getFieldFromReflection(fieldName, ChunkProviderGenerate.class,
					chunkProviderGenerate, fieldClass);
		}
		return structure;
	}

	private String isLocationStructure(MapGenStructure structure, String structureKey, int xCoord, int yCoord,
			int zCoord) {
		if (structure == null) {
			return null;
		}
		World structureWorld;
		try {
			structureWorld = ReflectionHelper.getCatchableFieldFromReflection("field_75039_c", MapGenBase.class,
					structure, World.class);
		} catch (NoSuchFieldException e) {
			structureWorld = ReflectionHelper.getFieldFromReflection("worldObj", MapGenBase.class, structure,
					World.class);
		}

		if (VanillaHelper.isStructureAt(structure, xCoord, yCoord, zCoord)) {
			return structureKey;
		} else {
			return null;
		}
	}

	@Override
	public boolean shouldUseHandler(World world, BiomeGenBase biomeGenBase) {
		return VanillaHelper.getDimensionID(world) == 0;
	}

	@SubscribeEvent
	/** Overworld is unloaded, reset references */
	public void worldLoad(WorldEvent.Unload event) {
		if (VanillaHelper.getDimensionID(event.world) == 0) {
			strongholdRef = new WeakReference(null);
			mineshaftRef = new WeakReference(null);
		}
	}
}