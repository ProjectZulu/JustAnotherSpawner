package jas.spawner.legacy.spawner.biome.structure;

import jas.api.StructureInterpreter;
import jas.common.helper.ReflectionHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.biome.BiomeGenSwamp;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class StructureInterpreterSwamp implements StructureInterpreter {
	private HashMap<Integer, WeakReference<MapGenStructure>> structureRefs = new HashMap<Integer, WeakReference<MapGenStructure>>();

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection<String> getStructureKeys() {
		Collection<String> collection = new ArrayList();
		collection.add("WitchHut");
		return collection;
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection<SpawnListEntry> getStructureSpawnList(String structureKey) {
		Collection<SpawnListEntry> collection = new ArrayList();
		if (structureKey.equals("WitchHut")) {
			collection.add(new SpawnListEntry(EntityWitch.class, 1, 1, 1));
		}
		return collection;
	}

	@Override
	public String areCoordsStructure(World world, int xCoord, int yCoord, int zCoord) {
		MapGenStructure mapGenScatteredFeature = getOrDefault(world.provider.dimensionId).get();
		if (mapGenScatteredFeature == null) {
			BiomeGenBase biome = world.getBiomeGenForCoords(xCoord, zCoord);
			ChunkProviderGenerate chunkProviderGenerate = StructureInterpreterHelper.getInnerChunkProvider(world,
					ChunkProviderGenerate.class);
			if (chunkProviderGenerate == null || !(biome instanceof BiomeGenSwamp)) {
				return null;
			}
			try {
				mapGenScatteredFeature = ReflectionHelper.getCatchableFieldFromReflection("field_73233_x",
						chunkProviderGenerate, MapGenScatteredFeature.class);
			} catch (NoSuchFieldException e) {
				mapGenScatteredFeature = ReflectionHelper.getFieldFromReflection("scatteredFeatureGenerator",
						chunkProviderGenerate, MapGenScatteredFeature.class);
			}
			structureRefs.put(world.provider.dimensionId, new WeakReference(mapGenScatteredFeature));
		}

		if (mapGenScatteredFeature != null && mapGenScatteredFeature.hasStructureAt(xCoord, yCoord, zCoord)) {
			return "WitchHut";
		}
		return null;
	}

	private WeakReference<MapGenStructure> getOrDefault(int dimensionID) {
		WeakReference<MapGenStructure> ref = structureRefs.get(dimensionID);
		if (ref == null) {
			ref = new WeakReference(null);
			structureRefs.put(dimensionID, ref);
		}
		return ref;
	}

	@Override
	public boolean shouldUseHandler(World world, BiomeGenBase biomeGenBase) {
		return biomeGenBase.biomeName.equals(BiomeGenBase.swampland.biomeName);
	}

	@SubscribeEvent
	/** Clearout appropriate dimension cache when World is unloaded */
	public void worldLoad(WorldEvent.Unload event) {
		structureRefs.remove(event.world.provider.dimensionId);
	}
}
