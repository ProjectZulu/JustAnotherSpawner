package jas.spawner.modern.spawner;

import jas.api.ITameable;
import jas.common.JASLog;
import jas.spawner.modern.MVELProfile;
import jas.spawner.modern.spawner.FunctionsUtility.Conditional;
import jas.spawner.modern.spawner.biome.group.BiomeGroupRegistry;
import jas.spawner.modern.spawner.biome.group.BiomeHelper;
import jas.spawner.modern.spawner.creature.handler.parsing.NBTWriter;
import jas.spawner.modern.spawner.tags.Context;

import java.util.IllegalFormatException;

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

public interface TagsSearch {

	public boolean block(String[] blockKeys, Integer[] searchRange, Integer[] searchOffsets);

	public boolean block(String[] blockKeys, Integer[] searchRange, Integer[] searchOffsets, String searchPattern);

	public boolean block(String[] blockKeys, Integer[] metas, Integer[] searchRange, Integer[] searchOffsets);

	public boolean block(String[] blockKeys, Integer[] metas, Integer[] searchRange, Integer[] searchOffsets,
			String searchPattern);

	public boolean blockFoot(String[] blockKeys);

	public boolean blockFoot(String[] blockKeys, Integer[] metas);

	public boolean normal(Integer[] searchRange, Integer[] searchOffsets);

	public boolean normal(Integer[] searchRange, Integer[] searchOffsets, String searchPattern);

	public boolean liquid(Integer[] searchRange, Integer[] searchOffsets);

	public boolean liquid(Integer[] searchRange, Integer[] searchOffsets, String searchPattern);

	public boolean solidside(int side, Integer[] searchRange, Integer[] searchOffsets);

	public boolean solidside(int side, Integer[] searchRange, Integer[] searchOffsets, String searchPattern);

	public boolean opaque(Integer[] searchRange, Integer[] searchOffsets);

	public boolean opaque(Integer[] searchRange, Integer[] searchOffsets, String searchPattern);

	public boolean biome(String biomeName, int[] range, int[] offset);

	public boolean biome(String biomeName, int[] range, int[] offset, String searchPattern);

	public static class FunctionsSearch implements TagsSearch {

		private enum SearchPattern {
			CUBE, // All blocks in area
			HOLLOW, // Only border blocks
			FOOT;

			public static Optional<SearchPattern> find(String possiblePattern) {
				for (SearchPattern actualPattern : SearchPattern.values()) {
					if (actualPattern.toString().equalsIgnoreCase(possiblePattern)) {
						return Optional.of(actualPattern);
					}
				}
				return Optional.absent();
			}
		}

		// Tagparent.obj()ect where usual working parameters such as pos are found
		private World world;
		public Context parent;

		public FunctionsSearch(World world, Context parent) {
			this.world = world;
			this.parent = parent;
		}

		private boolean conductSearch(SearchPattern pattern, Conditional condition, Integer[] searchRange,
				Integer[] searchOffsets) {
			switch (pattern) {
			case CUBE:
				return conductCubeSearch(condition, searchRange, searchOffsets);
			case HOLLOW:
				return conductHollowSearch(condition, searchRange, searchOffsets);
			case FOOT:
				return conductFootSearch(condition);
			}
			return false;
		}

		private boolean conductFootSearch(Conditional condition) {
			return condition.isMatch(world, parent.posX(), parent.posY() - 1, parent.posZ());
		}

		private boolean conductHollowSearch(Conditional condition, Integer[] searchRange, Integer[] searchOffsets) {
			Integer xRange = searchRange.length == 3 ? searchRange[0] : searchRange[0];
			Integer yRange = searchRange.length == 3 ? searchRange[1] : searchRange[0];
			Integer zRange = searchRange.length == 3 ? searchRange[2] : searchRange[0];

			Integer xOffset = searchOffsets.length == 3 ? searchOffsets[0] : searchOffsets[0];
			Integer yOffset = searchOffsets.length == 3 ? searchOffsets[1] : searchOffsets[0];
			Integer zOffset = searchOffsets.length == 3 ? searchOffsets[2] : searchOffsets[0];

			for (int i = -xRange; i <= xRange; i++) {
				for (int k = -zRange; k <= zRange; k++) {
					int j = yRange;
					if (condition.isMatch(world, parent.posX() + i + xOffset, parent.posY() + j + yOffset,
							parent.posZ() + k + zOffset)) {
						return true;
					} else if (condition.isMatch(world, parent.posX() + i + xOffset, parent.posY() - j + yOffset,
							parent.posZ() - k + zOffset)) {
						return true;
					}
				}
			}

			for (int k = -zRange; k <= zRange; k++) {
				for (int j = -yRange; j <= yRange; j++) {
					int i = xRange;
					if (condition.isMatch(world, parent.posX() + i + xOffset, parent.posY() + j + yOffset,
							parent.posZ() + k + zOffset)) {
						return true;
					} else if (condition.isMatch(world, parent.posX() - i + xOffset, parent.posY() + j + yOffset,
							parent.posZ() + k + zOffset)) {
						return true;
					}

				}
			}

			for (int i = -xRange; i <= xRange; i++) {
				for (int j = -yRange; j <= yRange; j++) {
					int k = zRange;
					if (condition.isMatch(world, parent.posX() + i + xOffset, parent.posY() + j + yOffset,
							parent.posZ() + k + zOffset)) {
						return true;
					} else if (condition.isMatch(world, parent.posX() + i + xOffset, parent.posY() + j + yOffset,
							parent.posZ() - k + zOffset)) {
						return true;
					}
				}
			}
			return false;
		}

		private boolean conductCubeSearch(Conditional condition, Integer[] searchRange, Integer[] searchOffsets) {
			Integer xRange = searchRange.length == 3 ? searchRange[0] : searchRange[0];
			Integer yRange = searchRange.length == 3 ? searchRange[1] : searchRange[0];
			Integer zRange = searchRange.length == 3 ? searchRange[2] : searchRange[0];

			Integer xOffset = searchOffsets.length == 3 ? searchOffsets[0] : searchOffsets[0];
			Integer yOffset = searchOffsets.length == 3 ? searchOffsets[1] : searchOffsets[0];
			Integer zOffset = searchOffsets.length == 3 ? searchOffsets[2] : searchOffsets[0];

			for (int i = -xRange; i <= xRange; i++) {
				for (int k = -zRange; k <= zRange; k++) {
					for (int j = -yRange; j <= yRange; j++) {
						if (condition.isMatch(world, parent.posX() + i + xOffset, parent.posY() + j + yOffset,
								parent.posZ() + k + zOffset)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		@Override
		public boolean block(String[] blockKeys, Integer[] searchRange, Integer[] searchOffsets) {
			return block(blockKeys, searchRange, searchOffsets, SearchPattern.CUBE.toString());
		}

		@Override
		public boolean block(String[] blockKeys, Integer[] searchRange, Integer[] searchOffsets, String searchType) {
			Optional<SearchPattern> pattern = SearchPattern.find(searchType);
			if (!pattern.isPresent()) {
				JASLog.log().severe("Search Pattern [%s] does not exist. No search being conducted.", searchType);
				return false;
			}
			return conductSearch(pattern.get(), new Conditional() {
				private String[] blockKeys;

				public Conditional init(String[] blockKeys) {
					this.blockKeys = blockKeys;
					return this;
				}

				@Override
				public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
					for (String blockKey : blockKeys) {
						if (Block.getBlockFromName(blockKey) == parent.wrld().blockAt(xCoord, yCoord, zCoord)) {
							return true;
						}
					}
					return false;
				}
			}.init(blockKeys), searchRange, searchOffsets);
		}

		@Override
		public boolean block(String[] blockKeys, Integer[] metas, Integer[] searchRange, Integer[] searchOffsets) {
			return block(blockKeys, metas, searchRange, searchOffsets, SearchPattern.CUBE.toString());
		}

		@Override
		public boolean block(String[] blockKeys, Integer[] metas, Integer[] searchRange, Integer[] searchOffsets,
				String searchType) {
			Optional<SearchPattern> pattern = SearchPattern.find(searchType);
			if (!pattern.isPresent()) {
				JASLog.log().severe("Search Pattern [%s] does not exist. No search being conducted.", searchType);
				return false;
			}
			return conductSearch(pattern.get(), new Conditional() {
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
							if (Block.getBlockFromName(blockKey) == parent.wrld().blockAt(xCoord, yCoord, zCoord)
									&& metaValue.equals(world.getBlockMetadata(xCoord, yCoord, zCoord))) {
								return true;
							}
						}
					}
					return false;
				}
			}.init(blockKeys, metas), searchRange, searchOffsets);
		}

		@Override
		public boolean blockFoot(String[] blockKeys) {
			return block(blockKeys, new Integer[] {}, new Integer[] {}, SearchPattern.FOOT.toString());
		}

		@Override
		public boolean blockFoot(String[] blockKeys, Integer[] metas) {
			return block(blockKeys, metas, new Integer[] {}, new Integer[] {}, SearchPattern.FOOT.toString());
		}

		@Override
		public boolean normal(Integer[] searchRange, Integer[] searchOffsets) {
			return normal(searchRange, searchOffsets, SearchPattern.CUBE.toString());
		}

		@Override
		public boolean normal(Integer[] searchRange, Integer[] searchOffsets, String searchType) {
			Optional<SearchPattern> pattern = SearchPattern.find(searchType);
			if (!pattern.isPresent()) {
				JASLog.log().severe("Search Pattern [%s] does not exist. No search being conducted.", searchType);
				return false;
			}
			return conductSearch(pattern.get(), new Conditional() {
				@Override
				public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
					return parent.wrld().blockAt(xCoord, yCoord, zCoord).isNormalCube();
				}
			}, searchRange, searchOffsets);
		}

		@Override
		public boolean liquid(Integer[] searchRange, Integer[] searchOffsets) {
			return liquid(searchRange, searchOffsets, SearchPattern.CUBE.toString());
		}

		@Override
		public boolean liquid(Integer[] searchRange, Integer[] searchOffsets, String searchType) {
			Optional<SearchPattern> pattern = SearchPattern.find(searchType);
			if (!pattern.isPresent()) {
				JASLog.log().severe("Search Pattern [%s] does not exist. No search being conducted.", searchType);
				return false;
			}
			return conductSearch(pattern.get(), new Conditional() {
				@Override
				public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
					return parent.wrld().blockAt(xCoord, yCoord, zCoord).getMaterial().isLiquid();
				}
			}, searchRange, searchOffsets);
		}

		@Override
		public boolean solidside(int side, Integer[] searchRange, Integer[] searchOffsets) {
			return solidside(side, searchRange, searchOffsets, SearchPattern.CUBE.toString());
		}

		@Override
		public boolean solidside(int side, Integer[] searchRange, Integer[] searchOffsets, String searchType) {
			Optional<SearchPattern> pattern = SearchPattern.find(searchType);
			if (!pattern.isPresent()) {
				JASLog.log().severe("Search Pattern [%s] does not exist. No search being conducted.", searchType);
				return false;
			}
			return conductSearch(pattern.get(), new Conditional() {
				private int side;

				public Conditional init(int side) {
					this.side = side;
					return this;
				}

				@Override
				public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
					return parent.wrld().blockAt(xCoord, yCoord, zCoord)
							.isSideSolid(world, xCoord, yCoord, zCoord, ForgeDirection.getOrientation(side));
				}
			}.init(side), searchRange, searchOffsets);
		}

		@Override
		public boolean opaque(Integer[] searchRange, Integer[] searchOffsets) {
			return opaque(searchRange, searchOffsets, SearchPattern.CUBE.toString());
		}

		@Override
		public boolean opaque(Integer[] searchRange, Integer[] searchOffsets, String searchType) {
			Optional<SearchPattern> pattern = SearchPattern.find(searchType);
			if (!pattern.isPresent()) {
				JASLog.log().severe("Search Pattern [%s] does not exist. No search being conducted.", searchType);
				return false;
			}
			return conductSearch(pattern.get(), new Conditional() {
				@Override
				public boolean isMatch(World world, int xCoord, int yCoord, int zCoord) {
					return parent.wrld().blockAt(xCoord, yCoord, zCoord).getMaterial().isOpaque();
				}
			}, searchRange, searchOffsets);
		}

		@Override
		public boolean biome(String biomeName, int[] range, int[] offset) {
			return biome(biomeName, range, offset, SearchPattern.CUBE.toString());
		}

		@Override
		public boolean biome(String biomeName, int[] range, int[] offset, String searchType) {
			Optional<SearchPattern> pattern = SearchPattern.find(searchType);
			if (!pattern.isPresent()) {
				JASLog.log().severe("Search Pattern [%s] does not exist. No search being conducted.", searchType);
				return false;
			}

			int rangeX = offset.length == 2 ? range[0] : range[0];
			int rangeZ = offset.length == 2 ? range[1] : range[0];
			int offsetX = offset.length == 2 ? offset[0] : offset[0];
			int offsetZ = offset.length == 2 ? offset[1] : offset[0];
			return conductSearch(pattern.get(), new Conditional() {
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
					BiomeGenBase biome = parent.wrld().biomeAt(xCoord, zCoord);
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
	}
}
