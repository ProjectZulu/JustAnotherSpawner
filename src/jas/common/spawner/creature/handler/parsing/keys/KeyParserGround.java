package jas.common.spawner.creature.handler.parsing.keys;

import jas.common.JASLog;
import jas.common.spawner.creature.handler.parsing.TypeValuePair;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class KeyParserGround extends KeyParserBase {

    public KeyParserGround(Key key) {
        super(key, true, KeyType.CHAINABLE);
    }

    @Override
    public boolean parseChainable(String parseable, ArrayList<TypeValuePair> parsedChainable,
            ArrayList<Operand> operandvalue) {
        String[] pieces = parseable.split(",");
        Operand operand = parseOperand(pieces);
        if (pieces.length == 1) {
            parsedChainable.add(new TypeValuePair(key, isInverted(parseable)));
            operandvalue.add(operand);
            return true;
        } else {
            JASLog.severe("Error Parsing Needs %s parameter. Invalid Argument Length.", key.key);
            return false;
        }
    }

    @Override
    public boolean parseValue(String parseable, HashMap<String, Object> valueCache) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isValidLocation(World world, EntityLiving entity, int xCoord, int yCoord, int zCoord,
            TypeValuePair typeValuePair, HashMap<String, Object> valueCache) {
        boolean isInverted = (Boolean) typeValuePair.getValue();
        boolean canSeeSky = canBlockSeeTheSky(world, xCoord, yCoord, zCoord);
        return isInverted ? canSeeSky : !canSeeSky;
    }

    protected boolean canBlockSeeTheSky(World world, int xCoord, int yCoord, int zCoord) {
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