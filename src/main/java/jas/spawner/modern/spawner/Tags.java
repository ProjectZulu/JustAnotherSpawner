package jas.spawner.modern.spawner;

import jas.api.ITameable;
import jas.common.JASLog;
import jas.spawner.modern.MVELProfile;
import jas.spawner.modern.eventspawn.SpawnBuilder;
import jas.spawner.modern.eventspawn.context.CommonContext;
import jas.spawner.modern.spawner.FunctionsUtility.Conditional;
import jas.spawner.modern.spawner.TagsEntity.FunctionsEntity;
import jas.spawner.modern.spawner.TagsNBT.FunctionsNBT;
import jas.spawner.modern.spawner.biome.group.BiomeGroupRegistry;
import jas.spawner.modern.spawner.biome.group.BiomeHelper;
import jas.spawner.modern.spawner.creature.handler.parsing.NBTWriter;
import jas.spawner.modern.spawner.tags.TagsCount;
import jas.spawner.modern.spawner.tags.TagsLegacy;
import jas.spawner.modern.spawner.tags.TagsObjective;
import jas.spawner.modern.spawner.tags.TagsTime;
import jas.spawner.modern.spawner.tags.TagsUtility;
import jas.spawner.modern.spawner.tags.TagsWorld;

import java.util.IllegalFormatException;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMultimap;

/**
 * Passed to MVEL to be evaluated
 */
public class Tags extends CommonContext {
	public Optional<EntityLiving> entity;
	public final CountAccessor count;
	public final TagsEntity ent; // Only Valid if Entity is present
	public final TagsNBT nbt; // Only Valid if Entity is present

	public Tags(World world, CountInfo countInfo, int posX, int posY, int posZ) {
		super(world, posX, posY, posZ);
		entity = Optional.absent();
		count = new CountAccessor(countInfo, this);
		ent = entity.isPresent() ? new FunctionsEntity(this, entity.get()) : null;
		nbt = entity.isPresent() ? new FunctionsNBT(this, world, entity.get()) : null;
	}

	public Tags(World world, CountInfo countInfo, int posX, int posY, int posZ, @Nonnull EntityLiving entity) {
		super(world, posX, posY, posZ);
		this.entity = Optional.of(entity);
		count = new CountAccessor(countInfo, this);
		ent = this.entity.isPresent() ? new FunctionsEntity(this, this.entity.get()) : null;
		nbt = this.entity.isPresent() ? new FunctionsNBT(this, world, this.entity.get()) : null;
	}

	@Deprecated
	public boolean sky() {
		return wrld.skyVisibleAt(posX, posY, posZ);
	}

	@Deprecated
	public boolean block(String[] blockKeys, Integer[] searchRange, Integer[] searchOffsets) {
		return util.searchAndEvaluateBlock(new Conditional() {
			private String[] blockKeys;

			public Conditional init(String[] blockKeys) {
				this.blockKeys = blockKeys;
				return this;
			}

			@Override
			public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
				for (String blockKey : blockKeys) {
					if (Block.getBlockFromName(blockKey) == wrld.blockAt(xCoord, yCoord, zCoord)) {
						return true;
					}
				}
				return false;
			}
		}.init(blockKeys), searchRange, searchOffsets);
	}

	@Deprecated
	public boolean block(String[] blockKeys, Integer[] metas, Integer[] searchRange, Integer[] searchOffsets) {
		return util.searchAndEvaluateBlock(new Conditional() {
			private String[] blockKeys;
			private Integer[] metas;

			public Conditional init(String[] blockKeys, Integer[] metas) {
				this.blockKeys = blockKeys;
				this.metas = metas;
				return this;
			}

			@Override
			public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
				for (String blockKey : blockKeys) {
					for (Integer metaValue : metas) {
						if (Block.getBlockFromName(blockKey) == wrld.blockAt(xCoord, yCoord, zCoord)
								&& metaValue.equals(world.getBlockMetadata(xCoord, yCoord, zCoord))) {
							return true;
						}
					}
				}
				return false;
			}
		}.init(blockKeys, metas), searchRange, searchOffsets);
	}

	@Deprecated
	public boolean blockFoot(String[] blockKeys) {
		Block blockID = wrld.blockAt(posX, posY - 1, posZ);
		for (String blockKey : blockKeys) {
			Block searchBlock = Block.getBlockFromName(blockKey);
			if (searchBlock != null) {
				if (blockID == searchBlock) {
					return true;
				}
			}
		}
		return false;
	}

	@Deprecated
	public boolean blockFoot(String[] blockKeys, Integer[] metas) {
		Block blockID = wrld.blockAt(posX, posY - 1, posZ);
		int meta = world.getBlockMetadata(posX, posY - 1, posZ);
		for (String blockKey : blockKeys) {
			Block searchBlock = Block.getBlockFromName(blockKey);
			if (searchBlock != null) {
				for (Integer metaValue : metas) {
					if (blockID == searchBlock && metaValue.equals(meta)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Deprecated
	public boolean normal(Integer[] searchRange, Integer[] searchOffsets) {
		return util.searchAndEvaluateBlock(new Conditional() {
			@Override
			public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
				return wrld.blockAt(xCoord, yCoord, zCoord).isNormalCube();
			}
		}, searchRange, searchOffsets);
	}

	@Deprecated
	public boolean liquid(Integer[] searchRange, Integer[] searchOffsets) {
		return util.searchAndEvaluateBlock(new Conditional() {
			@Override
			public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
				return wrld.blockAt(xCoord, yCoord, zCoord).getMaterial().isLiquid();
			}
		}, searchRange, searchOffsets);

	}

	@Deprecated
	public boolean solidside(int side, Integer[] searchRange, Integer[] searchOffsets) {
		return util.searchAndEvaluateBlock(new Conditional() {
			private int side;

			public Conditional init(int side) {
				this.side = side;
				return this;
			}

			@Override
			public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
				return wrld.blockAt(xCoord, yCoord, zCoord).isSideSolid(world, xCoord, yCoord, zCoord,
						ForgeDirection.getOrientation(side));
			}
		}.init(side), searchRange, searchOffsets);
	}

	@Deprecated
	public boolean opaque(Integer[] searchRange, Integer[] searchOffsets) {
		return util.searchAndEvaluateBlock(new Conditional() {
			@Override
			public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
				return wrld.blockAt(xCoord, yCoord, zCoord).getMaterial().isOpaque();
			}
		}, searchRange, searchOffsets);

	}

	@Deprecated
	public boolean ground() {
		int blockHeight = obj.highestResistentBlock();
		return blockHeight < 0 || blockHeight <= posY;
	}

	/* True if [0, range - 1] + offset <= maxValue */
	@Deprecated
	public boolean random(int range, int offset, int maxValue) {
		return util.rand(range) + offset <= maxValue;
	}

	/** Entity Tags */
	@Deprecated
	public boolean modspawn() {
		return entity.get().getCanSpawnHere();
	}

	@Deprecated
	public boolean isTamed() {
		if (entity.get() instanceof ITameable) {
			return ((ITameable) entity.get()).isTamed();
		} else if (entity.get() instanceof EntityTameable) {
			return ((EntityTameable) entity.get()).isTamed();
		} else if (entity.get() instanceof EntityHorse) {
			return ((EntityHorse) entity.get()).isTame();
		}
		return false;
	}

	@Deprecated
	public boolean isTameable() {
		if (entity.get() instanceof ITameable) {
			return ((ITameable) entity.get()).isTameable();
		} else if (entity.get() instanceof EntityTameable) {
			return true;
		}
		return false;
	}

	@Deprecated
	public boolean biome(String biomeName, int[] range, int[] offset) {
		int rangeX = offset.length == 2 ? range[0] : range[0];
		int rangeZ = offset.length == 2 ? range[1] : range[0];
		int offsetX = offset.length == 2 ? offset[0] : offset[0];
		int offsetZ = offset.length == 2 ? offset[1] : offset[0];

		return util.searchAndEvaluateBlock(new Conditional() {
			private String biomeName;

			public Conditional init(String biomeName) {
				this.biomeName = biomeName;
				return this;
			}

			@Override
			public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
				String type = "MAPPING";
				if (biomeName.startsWith("A|")) {
					type = "ATTRIBUTE";
				} else if (biomeName.startsWith("G|")) {
					type = "GROUP";
				}
				BiomeGenBase biome = wrld.biomeAt(xCoord, zCoord);
				BiomeGroupRegistry registry = MVELProfile.worldSettings().biomeGroupRegistry();
				if (type.equals("GROUP")) {
					ImmutableMultimap<String, String> packgToBiomeGroupID = registry.packgNameToGroupIDs();
					if (packgToBiomeGroupID.get(BiomeHelper.getPackageName(biome)).contains(biomeName.substring(2))) {
						return true;
					}
				} else if (type.equals("ATTRIBUTE")) {
					ImmutableMultimap<String, String> packgToAttributeID = registry.packgNameToAttribIDs();
					if (packgToAttributeID.get(BiomeHelper.getPackageName(biome)).contains(biomeName.substring(2))) {
						return true;
					}
				} else if (type.equals("MAPPING")) {
					if (registry.biomeMappingToPckg().containsValue(biomeName)
							|| registry.biomePckgToMapping().containsValue(biomeName)) {
						return true;
					}
				}
				return false;
			}
		}.init(biomeName), new Integer[] { rangeX, 0, rangeZ }, new Integer[] { offsetX, 0, offsetZ });
	}

	@Deprecated
	public boolean writenbt(String[] nbtOperations) {
		try {
			NBTTagCompound entityNBT = new NBTTagCompound();
			entity.get().writeToNBT(entityNBT);
			new NBTWriter(nbtOperations).writeToNBT(entityNBT);
			entity.get().readFromNBT(entityNBT);
			return true;
		} catch (IllegalFormatException e) {
			JASLog.log().severe("Skipping NBT Write due to %s", e.getMessage());
		} catch (IllegalArgumentException e) {
			JASLog.log().severe("Skipping NBT Write due to %s", e.getMessage());
		}
		return false;
	}

	public TagsCount count() {
		return count;
	}

	public TagsNBT nbt() {
		return nbt;
	}

	public TagsEntity ent() {
		return ent;
	}
}
