package jas.spawner.refactor.configsloader;

import jas.common.helper.GsonHelper;
import jas.spawner.modern.spawner.TagConverter;
import jas.spawner.refactor.LivingTypeBuilder;
import jas.spawner.refactor.LivingTypeBuilder.LivingType;
import jas.spawner.refactor.LivingTypes;
import jas.spawner.refactor.configsloader.ConfigLoader.VersionedFile;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.common.base.Optional;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class LivingTypeLoader implements VersionedFile {
	private Optional<TreeMap<String, LivingTypeBuilder>> types;
	private String version;

	public String getVersion() {
		return version;
	}

	private LivingTypeLoader() {
		types = Optional.absent();
		this.version = Serializer.FILE_VERSION;
	}

	public LivingTypeLoader(LivingTypes registry) {
		TreeMap<String, LivingTypeBuilder> types = new TreeMap<String, LivingTypeBuilder>();
		java.util.Iterator<LivingType> iterator = registry.types().values().iterator();
		while (iterator.hasNext()) {
			LivingType type = iterator.next();
			types.put(type.livingTypeID, new LivingTypeBuilder(type));
		}
		this.types = Optional.of(types);
		this.version = Serializer.FILE_VERSION;
	}

	public Optional<Collection<LivingTypeBuilder>> getTypes() {
		return types.isPresent() ? Optional.of(types.get().values()) : Optional
				.<Collection<LivingTypeBuilder>> absent();
	}

	public static class Serializer implements JsonSerializer<LivingTypeLoader>, JsonDeserializer<LivingTypeLoader> {
		public final static String FILE_VERSION = "3.0";
		public final static String FILE_VERSION_KEY = "FILE_VERSION";
		public final static String TYPE_KEY = "TYPES";

		public final static String OPTIONAL_PARAM_KEY = "Spawn Tag";
		public final static String ITER_PER_CHUNK = "Iterations Per Chunk";
		public final static String ITER_PER_PACK = "Iterations Per Pack";
		public final static String IS_PSSVE_RDY = "is PassiveSpawn Ready";
		public final static String IS_CHUNK_RDY = "is ChunkSpawn Ready";

		@Deprecated
		public final static String SPAWN_RATE_KEY = "Spawn Rate";
		@Deprecated
		public final static String MAX_CREATURE_KEY = "Spawn Cap";
		@Deprecated
		public final static String CHUNK_CHANCE_KEY = "Chunk Spawn Chance";
		@Deprecated
		public final static String SPAWN_MEDIUM_KEY = "Spawn Medium";
		@Deprecated
		public final static String DEFAULT_BIOME_CAP_KEY = "Default Biome Cap";
		@Deprecated
		public final static String MAPPING_TO_CAP = "Biome Caps";

		@Override
		public JsonElement serialize(LivingTypeLoader src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject endObject = new JsonObject();
			endObject.addProperty(FILE_VERSION_KEY, FILE_VERSION);
			JsonObject types = new JsonObject();
			for (LivingTypeBuilder type : src.types.get().values()) {
				JsonObject entry = new JsonObject();
				entry.addProperty(OPTIONAL_PARAM_KEY, type.getSpawnExpression());
				entry.addProperty(ITER_PER_CHUNK, type.getIterationsPerChunk());
				entry.addProperty(ITER_PER_PACK, type.getIterationsPerPack());
				entry.addProperty(IS_CHUNK_RDY, type.getIsReadyToCnk());
				entry.addProperty(IS_PSSVE_RDY, type.getIsReadyToPssve());
				types.add(type.livingTypeID, entry);
			}
			endObject.add(TYPE_KEY, types);
			return endObject;
		}

		@Override
		public LivingTypeLoader deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			LivingTypeLoader saveObject = new LivingTypeLoader();
			JsonObject endObject = GsonHelper.getAsJsonObject(json);
			String fileVersion = GsonHelper.getMemberOrDefault(endObject, FILE_VERSION_KEY, FILE_VERSION);
			JsonElement jsonElement = endObject.get(TYPE_KEY);
			if (jsonElement != null && jsonElement.isJsonObject()) {
				JsonObject types = jsonElement.getAsJsonObject();
				TreeMap<String, LivingTypeBuilder> typeMap = new TreeMap<String, LivingTypeBuilder>();
				for (Entry<String, JsonElement> entry : types.entrySet()) {
					JsonObject builderObject = GsonHelper.getAsJsonObject(entry.getValue());
					if (fileVersion.equals("3.0")) {
						String livingTypeID = entry.getKey().toUpperCase(Locale.ENGLISH);
						String canSpawnExpression = GsonHelper.getMemberOrDefault(builderObject, OPTIONAL_PARAM_KEY,
								"count.globalType('" + livingTypeID + "') >= (20 * count.players / 256) "
										+ " || !isSpawnMedium('AIR')" + "||!solidside(1,{0,0,0},{0,-1,0})"
										+ "&&liquid({0,0,0},{0,0,0})" + "&&normal({0,0,0},{0,0,0})"
										+ "&&normal({0,0,0},{0,1,0})" + "&&!opaque({0,0,0},{0,-1,0})");
						String isReadyPssve = GsonHelper.getMemberOrDefault(builderObject, IS_PSSVE_RDY,
								"world.totalTime() % 10 == 0");
						String isReadyChunk = GsonHelper.getMemberOrDefault(builderObject, IS_CHUNK_RDY,
								"util.nextFloat() < 0");
						Integer iterPerChnk = GsonHelper.getMemberOrDefault(builderObject, ITER_PER_CHUNK, 3);
						Integer iterPerPack = GsonHelper.getMemberOrDefault(builderObject, ITER_PER_PACK, 4);
						LivingTypeBuilder builder = new LivingTypeBuilder(livingTypeID, isReadyPssve,
								canSpawnExpression);
						builder.setIsReadyToCnk(isReadyChunk);
						builder.setIterationsPerChunk(iterPerChnk);
						builder.setIterationsPerPack(iterPerPack);
						typeMap.put(builder.livingTypeID, builder);
					} else {
						String livingTypeID = entry.getKey().toUpperCase(Locale.ENGLISH);

						/* New CanSpawn */
						int maxCreature = GsonHelper.getMemberOrDefault(builderObject, MAX_CREATURE_KEY, 10);
						String medium = GsonHelper.getMemberOrDefault(builderObject, SPAWN_MEDIUM_KEY, "air");
						String spawnExpression;
						if (fileVersion.equals("1.0")) {
							TagConverter tag = new TagConverter(
									GsonHelper.getMemberOrDefault(builderObject, "Tags", ""));
							spawnExpression = tag.expression;
						} else {
							spawnExpression = GsonHelper.getMemberOrDefault(builderObject, OPTIONAL_PARAM_KEY, "");
						}
						// BiomeCaps Ignored due to difficulty adding it to logic cnsistently.
						String newLogic = "count.globalType('" + livingTypeID + "') * 256 / count.chunks >= "
								+ maxCreature + " || !isSpawnMedium('" + medium + "')";
						if (!spawnExpression.trim().equals("")) {
							spawnExpression = "(" + spawnExpression + ")||" + newLogic;
						} else {
							spawnExpression = newLogic;
						}

						/* IsReady: Passive */
						int spawnRate = GsonHelper.getMemberOrDefault(builderObject, SPAWN_RATE_KEY, 1);
						String isReadyToPssve = "world.totalTime() % " + spawnRate + " == 0";

						/* IsReady: Chunk */
						int chunkSpawnChance = (int) GsonHelper.getMemberOrDefault(builderObject, CHUNK_CHANCE_KEY, 0f);
						String isReadyToChnk = "util.nextFloat() < " + chunkSpawnChance;

						LivingTypeBuilder builder = new LivingTypeBuilder(livingTypeID, isReadyToPssve, spawnExpression);
						builder.setisReadyToPssve(isReadyToChnk);
						builder.setIterationsPerChunk(GsonHelper.getMemberOrDefault(builderObject, ITER_PER_CHUNK, 3));
						builder.setIterationsPerPack(GsonHelper.getMemberOrDefault(builderObject, ITER_PER_PACK, 4));
						typeMap.put(builder.livingTypeID, builder);
					}
				}
				saveObject.types = Optional.of(typeMap);
				saveObject.version = fileVersion;
			}
			return saveObject;
		}
	}
}
