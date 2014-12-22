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
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.structure.MapGenNetherBridge;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class StructureInterpreterNether implements StructureInterpreter {

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
		MapGenStructure genNetherBridge = null;
		if (genNetherBridge == null) {
			BiomeGenBase biome = world.getBiomeGenForCoords(xCoord, zCoord);
			ChunkProviderServer chunkprovider = (ChunkProviderServer) world.getChunkProvider();
			ChunkProviderHell chunkProviderHell = chunkprovider.currentChunkProvider instanceof ChunkProviderHell ? (ChunkProviderHell) chunkprovider.currentChunkProvider
					: null;
			if (chunkProviderHell == null || !(biome instanceof BiomeGenHell)) {
				return null;
			}
			genNetherBridge = chunkProviderHell.genNetherBridge;
			try {
				genNetherBridge = ReflectionHelper.getCatchableFieldFromReflection("field_73172_c", chunkProviderHell,
						MapGenNetherBridge.class);
			} catch (NoSuchFieldException e) {
				genNetherBridge = ReflectionHelper.getFieldFromReflection("genNetherBridge", chunkProviderHell,
						MapGenNetherBridge.class);
			}
		}

		if (genNetherBridge != null && genNetherBridge.hasStructureAt(xCoord, yCoord, zCoord)) {
			return "NetherBridge";
		}
		return null;
	}

	@Override
	public boolean shouldUseHandler(World world, BiomeGenBase biomeGenBase) {
		if (biomeGenBase.biomeName.equals(BiomeGenBase.hell.biomeName)) {
			return true;
		}
		return false;
	}
}
