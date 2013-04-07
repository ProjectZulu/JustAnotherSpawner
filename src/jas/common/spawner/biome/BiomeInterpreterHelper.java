package jas.common.spawner.biome;

import jas.common.spawner.creature.handler.ReflectionHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

public class BiomeInterpreterHelper {

    @SuppressWarnings("unchecked")
    public static <T> T getInnerChunkProvider(World world, Class<T> chunkClass) {
        ChunkProviderServer chunkprovider = (ChunkProviderServer) world.getChunkProvider();
        IChunkProvider currentChunkProvider;
        try {
            currentChunkProvider = ReflectionHelper.getCatchableFieldFromReflection("field_73246_d", chunkprovider,
                    IChunkProvider.class);
        } catch (NoSuchFieldException e) {
            currentChunkProvider = ReflectionHelper.getFieldFromReflection("currentChunkProvider", chunkprovider,
                    IChunkProvider.class);
        }
        return chunkClass.isAssignableFrom(currentChunkProvider.getClass()) ? (T) currentChunkProvider : null;
    }
}
