package jas.common.spawner.creature.type;

import static net.minecraftforge.common.ForgeDirection.UP;
import jas.common.JASLog;
import jas.common.config.EntityCategoryConfiguration;
import jas.common.spawner.biome.group.BiomeGroupRegistry;
import jas.common.spawner.biome.group.BiomeHelper;
import jas.common.spawner.creature.handler.LivingHandler;
import jas.common.spawner.creature.handler.LivingHandlerRegistry;
import jas.common.spawner.creature.handler.parsing.keys.Key;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettingsCreatureTypeSpawn;

import java.util.List;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStep;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Property;
import net.minecraftforge.common.Property.Type;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

//TODO: Large Constructor could probably use Factory OR String optionalParameters to consolidate unused properties
public class CreatureType {
    public final String typeID;
    public final int spawnRate;
    public final int maxNumberOfCreature;
    public final float chunkSpawnChance;
    public final Material spawnMedium;
    public final String optionalParameters;
    protected OptionalSettingsCreatureTypeSpawn spawning;
    public final BiomeGroupRegistry biomeGroupRegistry;

    public final int defaultBiomeCap;
    public final ImmutableMap<Integer, Integer> biomeCaps;

    public CreatureType(BiomeGroupRegistry biomeGroupRegistry, CreatureTypeBuilder builder) {
        this.biomeGroupRegistry = biomeGroupRegistry;
        this.typeID = builder.typeID;
        this.maxNumberOfCreature = builder.maxNumberOfCreature;
        this.spawnRate = builder.spawnRate;
        this.spawnMedium = builder.getSpawnMedium();
        this.chunkSpawnChance = builder.getChunkSpawnChance();
        this.defaultBiomeCap = builder.getDefaultBiomeCap();
        this.biomeCaps = ImmutableMap.<Integer, Integer> builder().putAll(builder.getBiomeCaps()).build();
        this.optionalParameters = builder.getOptionalParameters();
        for (String string : optionalParameters.split("\\{")) {
            String parsed = string.replace("}", "");
            String titletag = parsed.split("\\:", 2)[0].toLowerCase();
            if (Key.spawn.keyParser.isMatch(titletag)) {
                spawning = new OptionalSettingsCreatureTypeSpawn(parsed);
            }
        }
        spawning = spawning == null ? new OptionalSettingsCreatureTypeSpawn("") : spawning;
    }

    public CreatureType(BiomeGroupRegistry biomeGroupRegistry, String typeID, int maxNumberOfCreature,
            Material spawnMedium, int spawnRate, float chunkSpawnChance) {
        this(biomeGroupRegistry, typeID, maxNumberOfCreature, spawnMedium, spawnRate, chunkSpawnChance,
                "{spawn:!solidside,1,0,[0/-1/0]:liquid,0:normal,0:normal,0,[0/1/0]:!opaque,0,[0/-1/0]}");
    }

    public CreatureType(BiomeGroupRegistry biomeGroupRegistry, String typeID, int maxNumberOfCreature,
            Material spawnMedium, int spawnRate, float chunkSpawnChance, String optionalParameters) {
        this.biomeGroupRegistry = biomeGroupRegistry;
        this.typeID = typeID;
        this.maxNumberOfCreature = maxNumberOfCreature;
        this.spawnMedium = spawnMedium;
        this.spawnRate = spawnRate;
        this.chunkSpawnChance = chunkSpawnChance;
        defaultBiomeCap = -1;
        biomeCaps = ImmutableMap.of();
        this.optionalParameters = optionalParameters;
        for (String string : optionalParameters.split("\\{")) {
            String parsed = string.replace("}", "");
            String titletag = parsed.split("\\:", 2)[0].toLowerCase();
            if (Key.spawn.keyParser.isMatch(titletag)) {
                spawning = new OptionalSettingsCreatureTypeSpawn(parsed);
            }
        }
        spawning = spawning == null ? new OptionalSettingsCreatureTypeSpawn("") : spawning;
    }

    @Deprecated
    public final CreatureType maxNumberOfCreatureTo(int maxNumberOfCreature) {
        return constructInstance(typeID, maxNumberOfCreature, spawnMedium, spawnRate, chunkSpawnChance,
                optionalParameters);
    }

    @Deprecated
    public final CreatureType spawnRateTo(int spawnRate) {
        return constructInstance(typeID, maxNumberOfCreature, spawnMedium, spawnRate, chunkSpawnChance,
                optionalParameters);
    }

    @Deprecated
    public final CreatureType chunkSpawningTo(float chunkSpawnChance) {
        return constructInstance(typeID, maxNumberOfCreature, spawnMedium, spawnRate, chunkSpawnChance,
                optionalParameters);
    }

    @Deprecated
    public final CreatureType optionalParametersTo(String optionalParameters) {
        return constructInstance(typeID, maxNumberOfCreature, spawnMedium, spawnRate, chunkSpawnChance,
                optionalParameters);
    }

    public boolean isReady(WorldServer world) {
        return world.getWorldInfo().getWorldTotalTime() % spawnRate == 0L;
    }

    /**
     * Used internally to create a new Instance of CreatureType. MUST be Overriden by Subclasses so that they are not
     * replaced with Parent. Used to Allow subclasses to Include their own Logic, but maintain same data structure.
     * 
     * Should create a new instance of class using parameters provided in the constructor.
     * 
     * @param typeID
     * @param maxNumberOfCreature
     * @param spawnMedium
     * @param spawnRate
     * @param chunkSpawning
     */
    @Deprecated
    // Use CreatureTypeBuilder
    protected CreatureType constructInstance(String typeID, int maxNumberOfCreature, Material spawnMedium,
            int spawnRate, float chunkSpawnChance, String optionalParameters) {
        return new CreatureType(biomeGroupRegistry, typeID, maxNumberOfCreature, spawnMedium, spawnRate,
                chunkSpawnChance, optionalParameters);
    }

    /**
     * Called by CustomSpawner to get the base coordinate to spawn an Entity
     * 
     * @param world
     * @param xCoord
     * @param zCoord
     * @return
     */
    public ChunkPosition getRandomSpawningPointInChunk(World world, int chunkX, int chunkZ) {
        Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        int xCoord = chunkX * 16 + world.rand.nextInt(16);
        int zCoord = chunkZ * 16 + world.rand.nextInt(16);
        int yCoord = world.rand.nextInt(chunk == null ? world.getActualHeight() : chunk.getTopFilledSegment() + 16 - 1);
        return new ChunkPosition(xCoord, yCoord, zCoord);
    }

    /**
     * Entity Bases Type Check. Used to Evalue Type of Entity if it exists in the World
     * 
     * @param entity Entity that is being Checked
     * @return
     */
    public boolean isEntityOfType(LivingHandlerRegistry livingHandlerRegistry, Entity entity) {
        if (entity instanceof EntityLiving) {
            return isEntityOfType(livingHandlerRegistry, ((EntityLiving) entity).getClass());
        }
        return false;
    }

    /**
     * Class Bases Type Check. Used to Evalue Type of Entity before it exists in the World
     * 
     * @param entity
     * @return
     */
    public boolean isEntityOfType(LivingHandlerRegistry livingHandlerRegistry, Class<? extends EntityLiving> entity) {
        for (LivingHandler handler : livingHandlerRegistry.getLivingHandlers(entity)) {
            if (handler.creatureTypeID.equals(this.typeID)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEntityOfType(LivingHandlerRegistry livingHandlerRegistry, String groupID) {
        LivingHandler handler = livingHandlerRegistry.getLivingHandler(groupID);
        return handler != null ? handler.creatureTypeID.equals(this.typeID) : false;
    }

    /**
     * Called by CustomSpawner to determine if the Chunk Postion to be spawned at is a valid Type
     * 
     * @param world
     * @param xCoord
     * @param yCoord
     * @param zCoord
     * @return
     */
    public boolean isValidMedium(World world, int xCoord, int yCoord, int zCoord) {
        return !world.isBlockNormalCube(xCoord, yCoord, zCoord)
                && world.getBlockMaterial(xCoord, yCoord, zCoord) == spawnMedium;
    }

    /**
     * Called by CustomSpawner the location is valid for determining if the Chunk Postion is a valid location to spawn
     * 
     * @param world
     * @param xCoord
     * @param yCoord
     * @param zCoord
     * @return
     */
    public boolean canSpawnAtLocation(World world, int xCoord, int yCoord, int zCoord) {
        boolean canSpawn = true;
        if (spawning.isOptionalEnabled()) {
            canSpawn = !spawning.isInverted();
            if (!spawning.isValidLocation(world, null, xCoord, yCoord, zCoord)) {
                canSpawn = spawning.isInverted();
            }

            return canSpawn;
        } else {
            if (spawnMedium == Material.water) {
                return world.getBlockMaterial(xCoord, yCoord, zCoord).isLiquid()
                        && world.getBlockMaterial(xCoord, yCoord - 1, zCoord).isLiquid()
                        && !world.isBlockNormalCube(xCoord, yCoord + 1, zCoord);
            } else if (!world.doesBlockHaveSolidTopSurface(xCoord, yCoord - 1, zCoord)) {
                return false;
            } else {
                int l = world.getBlockId(xCoord, yCoord - 1, zCoord);
                boolean spawnBlock = (Block.blocksList[l] != null && canCreatureSpawn(Block.blocksList[l], world,
                        xCoord, yCoord - 1, zCoord));
                return spawnBlock && l != Block.bedrock.blockID && !world.isBlockNormalCube(xCoord, yCoord, zCoord)
                        && !world.getBlockMaterial(xCoord, yCoord, zCoord).isLiquid()
                        && !world.isBlockNormalCube(xCoord, yCoord + 1, zCoord);
            }
        }
    }

    public int getChunkCap(Chunk chunk) {
        if (chunk == null || defaultBiomeCap <= 0) {
            return -1;
        }
        int chunkCap = 0;
        int counter = 0;
        byte[] biomeArray = chunk.getBiomeArray();
        for (int i = 0; i < biomeArray.length; i++) {
            int biomeID = biomeArray[i] & 255;
            Integer columnCap = biomeCaps.get(biomeID);
            columnCap = columnCap != null ? columnCap : defaultBiomeCap;
            chunkCap += columnCap;
            counter++;
        }
        return counter > 0 ? chunkCap / counter : -1;
    }

    /**
     * Creates a new instance of creature types from configuration using itself as the default
     * 
     * @param config
     * @return
     */
    public CreatureType createFromConfig(EntityCategoryConfiguration config) {
        int resultSpawnRate = config.getSpawnRate(typeID, spawnRate).getInt();
        int resultMaxNumberOfCreature = config.getSpawnCap(typeID, maxNumberOfCreature).getInt();

        Optional<Boolean> chunkSpawn = config.isChunkSpawningPresent(typeID);
        float defaultSpawnChance = chunkSpawn.isPresent() ? chunkSpawn.get() ? 0.10f : 0.0f : chunkSpawnChance;
        float resultChunkSpawning = (float) config.getChunkSpawnChance(typeID, Float.toString(defaultSpawnChance))
                .getDouble(defaultSpawnChance);

        String resultOptionalParameters = config.getOptionalTags(typeID, optionalParameters).getString();
        CreatureTypeBuilder builder = new CreatureTypeBuilder(typeID, resultSpawnRate, resultMaxNumberOfCreature)
                .withOptionalParameters(resultOptionalParameters).withChanceToChunkSpawn(resultChunkSpawning);
        builder.withDefaultBiomeCap(config.getDefaultBiomeCap(typeID, defaultBiomeCap).getInt());
        ConfigCategory category = config.getBiomeCaps(typeID);
        loadBiomeCap(category, builder, defaultBiomeCap);
        return builder.build(biomeGroupRegistry);
    }

    private void loadBiomeCap(ConfigCategory category, CreatureTypeBuilder builder, final int defaultCap) {
        for (Entry<String, Property> entry : category.entrySet()) {
            String biomeIdentifier = entry.getKey();
            String packageName = biomeGroupRegistry.biomeMappingToPckg.get(biomeIdentifier);
            if (packageName == null) {
                JASLog.severe("Error Parsing %s BiomeCap. %s is not a biome mapping.", typeID, biomeIdentifier);
            } else {
                List<Integer> biomeIDs = biomeGroupRegistry.pckgNameToBiomeID.get(packageName);
                for (Integer biomeID : biomeIDs) {
                    int chunkCap = entry.getValue().getInt(defaultCap);
                    if (chunkCap < 0) {
                        if (defaultCap >= 0) {
                            JASLog.severe("%s BiomeCap (%s) cannot be < 0. Using DefaultCap %s", typeID,
                                    biomeIdentifier, defaultCap);
                        } else {
                            JASLog.severe("%s BiomeCap (%s) cannot be < 0. Using %s", typeID, biomeIdentifier, 0);
                        }
                        chunkCap = defaultCap;
                    }
                    builder.withBiomeCap(biomeID, chunkCap);
                }
            }
        }
    }

    /**
     * Creates a new instance of creature types from configuration using itself as the default
     * 
     * @param config
     * @return
     */
    public void saveCurrentToConfig(EntityCategoryConfiguration config) {
        config.getSpawnRate(typeID, spawnRate).set(spawnRate);
        config.getSpawnCap(typeID, maxNumberOfCreature).set(maxNumberOfCreature);
        config.getChunkSpawnChance(typeID, Float.toString(chunkSpawnChance)).set(Float.toString(chunkSpawnChance));
        config.getOptionalTags(typeID, optionalParameters).set(optionalParameters);
        config.getDefaultBiomeCap(typeID, defaultBiomeCap).set(defaultBiomeCap);

        ConfigCategory category = config.getBiomeCaps(this.typeID);
        for (Entry<Integer, Integer> entry : biomeCaps.entrySet()) {
            Integer biomeID = entry.getKey();
            Integer biomeCap = entry.getValue();
            String mappingName = biomeGroupRegistry.biomePckgToMapping.get(BiomeHelper
                    .getPackageName(BiomeGenBase.biomeList[biomeID]));
            if (mappingName != null) {
                Property biomeCapProp = category.get(mappingName);
                if (biomeCapProp != null) {
                    biomeCapProp.set(biomeCap);
                } else {
                    biomeCapProp = new Property(mappingName, Integer.toString(biomeCap), Type.INTEGER);
                    category.put(BiomeHelper.getPackageName(BiomeGenBase.biomeList[biomeID]), biomeCapProp);
                }
            }
        }
    }

    /*
     * TODO: Does not Belong Here. Possible Block Helper Class. Ideally Mods should be able to Register a Block. Similar
     * to Proposed Entity Registry or StructureInterpreter. How will end-users fix issue? Does End User Need to?
     */
    /**
     * Custom Implementation of canCreatureSpawnMethod which Required EnumCreatureType. Cannot be Overrident.
     * 
     * @param block
     * @param world
     * @param xCoord
     * @param yCoord
     * @param zCoord
     * @return
     */
    private boolean canCreatureSpawn(Block block, World world, int xCoord, int yCoord, int zCoord) {
        int meta = world.getBlockMetadata(xCoord, yCoord, zCoord);
        if (block instanceof BlockStep) {
            return (((meta & 8) == 8) || block.isOpaqueCube());
        } else if (block instanceof BlockStairs) {
            return ((meta & 4) != 0);
        }
        return block.isBlockSolidOnSide(world, xCoord, yCoord, zCoord, UP);
    }
}
