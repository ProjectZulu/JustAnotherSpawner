package jas.spawner.modern.spawner.biome.structure;

import jas.api.StructureInterpreter;
import jas.common.helper.ReflectionHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.biome.BiomeGenHell;
import net.minecraft.world.gen.ChunkProviderHell;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class StructureInterpreterNether implements StructureInterpreter {

	private HashMap<Integer, WeakReference<MapGenStructure>> structureRefs = new HashMap<Integer, WeakReference<MapGenStructure>>();

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<String> getStructureKeys() {
		Collection<String> collection = new ArrayList();
		collection.add("NetherBridge");
		return collection;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<SpawnListEntry> getStructureSpawnList(String structureKey) {
		Collection<SpawnListEntry> collection = new ArrayList();
		if (structureKey.equals("NetherBridge")) {
			collection.add(new SpawnListEntry(EntityBlaze.class, 10, 2, 3));
			collection.add(new SpawnListEntry(EntityPigZombie.class, 5, 4, 4));
			collection.add(new SpawnListEntry(EntitySkeleton.class, 10, 4, 4));
			collection.add(new SpawnListEntry(EntityMagmaCube.class, 3, 4, 4));
		}
		return collection;
	}

	@Override
	public String areCoordsStructure(World world, int xCoord, int yCoord, int zCoord) {
		MapGenStructure genNetherBridge = getOrDefault(world.provider.dimensionId).get();
		if (genNetherBridge == null) {
			BiomeGenBase biome = world.getBiomeGenForCoords(xCoord, zCoord);
			ChunkProviderHell chunkProviderHell = StructureInterpreterHelper.getInnerChunkProvider(world,
					ChunkProviderHell.class);
			if (chunkProviderHell == null || !(biome instanceof BiomeGenHell)) {
				return null;
			}
			try {
				genNetherBridge = ReflectionHelper.getCatchableFieldFromReflection("field_73172_c", chunkProviderHell,
						MapGenNetherBridge.class);
			} catch (NoSuchFieldException e) {
				genNetherBridge = ReflectionHelper.getFieldFromReflection("genNetherBridge", chunkProviderHell,
						MapGenNetherBridge.class);
			}
			structureRefs.put(world.provider.dimensionId, new WeakReference(genNetherBridge));
		}

		if (genNetherBridge != null && genNetherBridge.hasStructureAt(xCoord, yCoord, zCoord)) {
			return "NetherBridge";
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
		if (biomeGenBase.biomeName.equals(BiomeGenBase.hell.biomeName)) {
			return true;
		}
		return false;
	}

	@SubscribeEvent
	/** Clearout appropriate dimension cache when World is unloaded */
	public void worldLoad(WorldEvent.Unload event) {
		structureRefs.remove(event.world.provider.dimensionId);
	}
}
