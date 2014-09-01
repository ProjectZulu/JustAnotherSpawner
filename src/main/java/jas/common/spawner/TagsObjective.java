package jas.common.spawner;

import jas.common.JustAnotherSpawner;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * This is for tags that return a concrete value to be used in checks. Many could be determined by using WorldAccessor
 * but are provided for brevity and less advanced users.
 */
public class TagsObjective {
	private World world;
	// TagObject where usual working parameters such as pos are found
	public Tags parent;

	public TagsObjective(World world, Tags parent) {
		this.parent = parent;
		this.world = world;
	}

	public String block(Integer offsetX, Integer offsetY, Integer offsetZ) {
		return Block.blockRegistry.getNameForObject(parent.wrld.blockAt(offsetX, offsetY, offsetZ));
	}

	public int light() {
		return parent.wrld.lightAt(parent.posX, parent.posY, parent.posZ);
	}

	public int torchlight() {
		return parent.wrld.torchlightAt(parent.posX, parent.posY, parent.posZ);
	}

	public int origin() {
		return parent.wrld.originDis(parent.posX, parent.posY, parent.posZ);
	}

	public String material() {
		return material(world.getBlock(parent.posX, parent.posY, parent.posZ).getMaterial());
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

	public int difficulty() {
		switch (world.difficultySetting) {
		case PEACEFUL:
			return 0;
		case EASY:
			return 1;
		case NORMAL:
			return 2;
		case HARD:
			return 3;
		}
		return 2;
	}

	/**
	 * Finds the highest block on the x, z coordinate that is solid and returns its y coord. Args x, z
	 */
	public int highestResistentBlock() {
		int par1 = parent.posX;
		int par2 = parent.posZ;
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

	public int playersInRange(World world, int minRange, int maxRange) {
		int count = 0;
		for (int i = 0; i < world.playerEntities.size(); ++i) {
			EntityPlayer player = (EntityPlayer) world.playerEntities.get(i);
			if (player.isEntityAlive()) {
				int distance = (int) Math.sqrt(player.getDistanceSq(parent.posX, parent.posY, parent.posZ));
				if (maxRange >= minRange && distance >= minRange && distance <= maxRange) {
					count++;
					continue;
				} else if (maxRange < minRange && !(distance < minRange && distance > maxRange)) {
					count++;
					continue;
				}
			}
		}
		return count;
	}

	public int countEntitiesInRange(String[] searchNames, int minRange, int maxRange) {
		int count = 0;
		for (int i = 0; i < world.loadedEntityList.size(); ++i) {
			Entity entity = (Entity) world.loadedEntityList.get(i);
			if (entity.isEntityAlive()) {
				String entityName = JustAnotherSpawner.worldSettings().livingGroupRegistry().EntityClasstoJASName
						.get(entity.getClass());
				for (String searchName : searchNames) {
					if (!searchName.trim().equals("") && searchName.equalsIgnoreCase(entityName)) {
						int distance = (int) Math.sqrt(entity.getDistanceSq(parent.posX, parent.posY, parent.posZ));
						if (parent.util.inRange(distance, minRange, maxRange)) {
							count++;
							continue;
						}
					}
				}
			}
		}
		return count;
	}

	public int countJASEntitiesInRange(String[] searchNames, int minRange, int maxRange) {
		int count = 0;
		for (int i = 0; i < world.loadedEntityList.size(); ++i) {
			Entity entity = (Entity) world.loadedEntityList.get(i);
			if (entity.isEntityAlive()) {
				String entityName = EntityList.getEntityString(entity);
				for (String searchName : searchNames) {
					if (!searchName.trim().equals("") && searchName.equalsIgnoreCase(entityName)) {
						int distance = (int) Math.sqrt(entity.getDistanceSq(parent.posX, parent.posY, parent.posZ));
						if (parent.util.inRange(distance, minRange, maxRange)) {
							count++;
							continue;
						}
					}
				}
			}
		}
		return count;
	}
}
