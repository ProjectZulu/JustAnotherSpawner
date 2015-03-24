package jas.spawner.modern.spawner;

import jas.spawner.modern.MVELProfile;
import jas.spawner.modern.spawner.tags.BaseFunctions;
import jas.spawner.modern.spawner.tags.ObjectiveFunctions;
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
public class TagsObjective implements ObjectiveFunctions {
	private World world;
	// TagObject where usual working parameters such as pos are found
	public BaseFunctions parent;

	public TagsObjective(World world, BaseFunctions parent) {
		this.parent = parent;
		this.world = world;
	}

	public String block() {
		return parent.wrld().blockNameAt(parent.posX(), parent.posY(), parent.posZ());
	}

	public int light() {
		return parent.wrld().lightAt(parent.posX(), parent.posY(), parent.posZ());
	}

	public int torchlight() {
		return parent.wrld().torchlightAt(parent.posX(), parent.posY(), parent.posZ());
	}

	public int origin() {
		return parent.wrld().originDis(parent.posX(), parent.posY(), parent.posZ());
	}

	public String material() {
		return parent.util().material(world.getBlock(parent.posX(), parent.posY(), parent.posZ()).getMaterial());
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
		int par1 = parent.posX();
		int par2 = parent.posZ();
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

	public int playersInRange(int minRange, int maxRange) {
		int count = 0;
		for (int i = 0; i < world.playerEntities.size(); ++i) {
			EntityPlayer player = (EntityPlayer) world.playerEntities.get(i);
			if (player.isEntityAlive()) {
				int distance = (int) Math.sqrt(player.getDistanceSq(parent.posX(), parent.posY(), parent.posZ()));
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
				String entityName = MVELProfile.worldSettings().livingGroupRegistry().EntityClasstoJASName.get(entity
						.getClass());
				for (String searchName : searchNames) {
					if (!searchName.trim().equals("")
							&& (searchName.equals("*") || searchName.equalsIgnoreCase(entityName))) {
						int distance = (int) Math
								.sqrt(entity.getDistanceSq(parent.posX(), parent.posY(), parent.posZ()));
						if (parent.util().inRange(distance, minRange, maxRange)) {
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
					if (!searchName.trim().equals("")
							&& (searchName.equals("*") || searchName.equalsIgnoreCase(entityName))) {
						int distance = (int) Math
								.sqrt(entity.getDistanceSq(parent.posX(), parent.posY(), parent.posZ()));
						if (parent.util().inRange(distance, minRange, maxRange)) {
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
