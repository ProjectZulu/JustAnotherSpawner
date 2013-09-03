package jas.common.spawner.creature.handler.parsing.keys;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class KeyParserGround extends KeyParserBoolean {

    public KeyParserGround(Key key) {
        super(key);
    }

    @Override
    public boolean getValue(EntityLiving entity, World world, int xCoord, int yCoord, int zCoord) {
        int blockHeight = getTopSolidOrLiquidBlock(world, xCoord, zCoord);
        return blockHeight < 0 || blockHeight <= yCoord;
    }

    /**
     * Finds the highest block on the x, z coordinate that is solid and returns its y coord. Args x, z
     */
    private int getTopSolidOrLiquidBlock(World world, int par1, int par2) {
        Chunk chunk = world.getChunkFromBlockCoords(par1, par2);
        int k = chunk.getTopFilledSegment() + 15;
        par1 &= 15;

        for (par2 &= 15; k > 0; --k) {
            int l = chunk.getBlockID(par1, k, par2);

            if (l != 0 && Block.blocksList[l].blockMaterial.blocksMovement()
                    && Block.blocksList[l].blockMaterial != Material.leaves
                    && Block.blocksList[l].blockMaterial != Material.wood
                    && Block.blocksList[l].blockMaterial != Material.glass
                    && !Block.blocksList[l].isBlockFoliage(world, par1, k, par2)) {
                return k + 1;
            }
        }
        return -1;
    }
}