package jas.spawner.modern.eventspawn.context;

import jas.spawner.modern.eventspawn.SingleSpawnBuilder;
import jas.spawner.modern.eventspawn.SpawnBuilder;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraftforge.event.world.BlockEvent;

public class BlockContext extends EventContext {
	private BlockEvent event;

	public BlockContext(BlockEvent event) {
		super(event.world, event.x, event.y, event.z);
		this.event = event;
	}
	
	public String blockName() {
		return Block.blockRegistry.getNameForObject(event.block);
	}

	public int blockMeta() {
		return event.blockMetadata;
	}

	public boolean isMaterial(String materialName) {
		return material().equals(materialName.toLowerCase(Locale.US));
	}

	public String material() {
		return material(event.world.getBlock(event.x, event.y, event.z).getMaterial());
	}

	public String material(Material material) {
		if (material == Material.air) {
			return "air";
		} else if (material == Material.water) {
			return "water";
		} else if (material == Material.fire) {
			return "fire";
		} else if (material == Material.lava) {
			return "lava";
		} else if (material == Material.sand) {
			return "sand";
		} else if (material == Material.grass) {
			return "grass";
		} else if (material == Material.ground) {
			return "ground";
		} else if (material == Material.wood) {
			return "wood";
		} else if (material == Material.rock) {
			return "rock";
		} else if (material == Material.iron) {
			return "iron";
		} else if (material == Material.anvil) {
			return "anvil";
		} else if (material == Material.leaves) {
			return "leaves";
		} else if (material == Material.plants) {
			return "plants";
		} else if (material == Material.vine) {
			return "vine";
		} else if (material == Material.sponge) {
			return "sponge";
		} else if (material == Material.cloth) {
			return "cloth";
		} else if (material == Material.circuits) {
			return "circuits";
		} else if (material == Material.carpet) {
			return "carpet";
		} else if (material == Material.glass) {
			return "glass";
		} else if (material == Material.redstoneLight) {
			return "redstoneLight";
		} else if (material == Material.tnt) {
			return "tnt";
		} else if (material == Material.coral) {
			return "coral";
		} else if (material == Material.ice) {
			return "ice";
		} else if (material == Material.packedIce) {
			return "packedIce";
		} else if (material == Material.snow) {
			return "snow";
		} else if (material == Material.web) {
			return "web";
		} else if (material == Material.craftedSnow) {
			return "craftedSnow";
		} else if (material == Material.cactus) {
			return "cactus";
		} else if (material == Material.clay) {
			return "clay";
		} else if (material == Material.gourd) {
			return "gourd";
		} else if (material == Material.portal) {
			return "portal";
		} else if (material == Material.dragonEgg) {
			return "dragonEgg";
		} else if (material == Material.cake) {
			return "cake";
		} else if (material == Material.piston) {
			return "piston";
		} else {
			throw new IllegalArgumentException(String.format("Unknown material type %s", material));
		}
	}

	public boolean isBlock(String blockName) {
		return blockName().equals(blockName);
	}

	public boolean isBlock(String blockName, int blockMeta) {
		return blockMeta == event.blockMetadata && blockName().equals(blockName);
	}

	public SpawnBuilder spawn(String entityMapping) {
		return new SingleSpawnBuilder(entityMapping, event.x + 0.5D, event.y, event.z + 0.5D);
	}
}
