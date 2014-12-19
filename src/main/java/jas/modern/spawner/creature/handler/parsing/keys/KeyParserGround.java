package jas.modern.spawner.creature.handler.parsing.keys;

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
            Block block = chunk.getBlock(par1, k, par2);

            if (block != null && block.getMaterial().blocksMovement() && block.getMaterial() != Material.leaves
                    && block.getMaterial() != Material.wood && block.getMaterial() != Material.glass
                    && !block.isFoliage(world, par1, k, par2)) {
                return k + 1;
            }
        }
        return -1;
    }

	@Override
	public String toExpression(String parseable) {
		return "ground()";
	}
}