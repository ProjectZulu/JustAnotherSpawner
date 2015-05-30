package jas.spawner.refactor.configsloader;

import jas.common.helper.GsonHelper;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder.SpawnListEntry;
import jas.spawner.refactor.configsloader.ConfigLoader.VersionedFile;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.collect.Table;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class BiomeSpawnListLoader implements VersionedFile {
	private String version;

	// SortByBiome: <LocationExp, <CreatureType(MONSTER/AMBIENT), <LivingExpression, SpawnListEntry>>>
	// !SortByBiome:<CreatureType(MONSTER/AMBIENT), <LivingExpression, <LocationExp, SpawnListEntry>>>
	private TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>> biomeToTypeToCreature;
	private boolean sortCreatureByBiome;

	public BiomeSpawnListLoader() {
		this.version = Serializer.FILE_VERSION;
	}

	private BiomeSpawnListLoader(Boolean sortCreatureByBiome) {
		biomeToTypeToCreature = new TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>>();
		this.sortCreatureByBiome = sortCreatureByBiome;
	}
	
	public BiomeSpawnListLoader(Collection<SpawnListEntryBuilder> spawnListEntries, boolean sortCreatureByBiome) {
		this.sortCreatureByBiome = sortCreatureByBiome;
		biomeToTypeToCreature = new TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>>();

		for (SpawnListEntryBuilder spawnListEntry : spawnListEntries) {
			putEntry(spawnListEntry.getLocContent(), spawnListEntry.getLivingTypeID(), spawnListEntry.getEntContent(),
					spawnListEntry, biomeToTypeToCreature);
		}
	}

	private void putEntry(String locationExpression, String livingType, String livingExpression,
			SpawnListEntryBuilder spawnListEntry,
			TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>> primMap) {
		String primKey = sortCreatureByBiome ? locationExpression : livingType;
		String secoKey = sortCreatureByBiome ? livingType : livingExpression;
		String tertKey = sortCreatureByBiome ? livingExpression : locationExpression;

		TreeMap<String, TreeMap<String, SpawnListEntryBuilder>> secMap = primMap.get(primKey);
		if (secMap == null) {
			secMap = new TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>();
			primMap.put(primKey, secMap);
		}
		TreeMap<String, SpawnListEntryBuilder> tertMap = secMap.get(secoKey);
		if (tertMap == null) {
			tertMap = new TreeMap<String, SpawnListEntryBuilder>();
			secMap.put(secoKey, tertMap);
		}
		tertMap.put(tertKey, spawnListEntry);
	}

	public Set<SpawnListEntryBuilder> getBuilders() {
		Set<SpawnListEntryBuilder> builders = new HashSet<SpawnListEntryBuilder>();
		for (TreeMap<String, TreeMap<String, SpawnListEntryBuilder>> secMap : biomeToTypeToCreature.values()) {
			for (TreeMap<String, SpawnListEntryBuilder> tertMap : secMap.values()) {
				builders.addAll(tertMap.values());
			}
		}
		return builders;
	}

	@Override
	public String getVersion() {
		return version;
	}

	public static class Serializer implements JsonSerializer<BiomeSpawnListLoader>,
			JsonDeserializer<BiomeSpawnListLoader> {
		public final static String FILE_VERSION = "3.0";
		public final String FILE_VERSION_KEY = "FILE_VERSION";
		public final String SORT_MODE_KEY = "SORTED_BY_BIOME";
		public final String SPAWN_LIST_KEY = "SPAWN_LIST_ENTRIES";

		public final String SPAWN_WEIGHT = "WEIGHT";
		public final String PASSIVE_PACKSIZE_KEY = "PASSIVE_PACKSIZE";
		public final String CHUNK_PACKSIZE_KEY = "CHUNK_PACKSIZE";
		public final String SPAWN_TAG_KEY = "SPAWN_TAG";
		public final String POSTSPAWN_KEY = "POST_SPAWN_TAG";

		public final String MODID_KEY = "MOD_ID";
		public final String LIVING_HANDLER_KEY = "LIVING_HANDLER";

		// private String livingTypeID; // Included in FileFormat
		// private String locContents; // Included in FileFormat
		// private String entityContents; // Included in FileFormat

		@Deprecated
		public final String ENTITY_STAT_KEY = "Weight-PassivePackMax-ChunkPackMin-ChunkPackMax";
		@Deprecated
		public final String SPAWN_OPERAND_KEY = "Spawn Operand";
		@Deprecated
		public final String ENTITY_TAG_KEY = "Tags";

		private final boolean defaultSortByBiome;

		public Serializer(boolean defaultSortByBiome) {
			this.defaultSortByBiome = defaultSortByBiome;
		}

		@Override
		public JsonElement serialize(BiomeSpawnListLoader loader, Type type, JsonSerializationContext context) {
			JsonObject endObject = new JsonObject();
			endObject.addProperty(FILE_VERSION_KEY, FILE_VERSION);
			endObject.addProperty(SORT_MODE_KEY, loader.sortCreatureByBiome);
			JsonObject primObject = new JsonObject();
			for (Entry<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>> primEnts : loader.biomeToTypeToCreature
					.entrySet()) {
				String primKey = primEnts.getKey();
				JsonObject secObject = new JsonObject();
				for (Entry<String, TreeMap<String, SpawnListEntryBuilder>> secEnts : primEnts.getValue().entrySet()) {
					String secKey = secEnts.getKey();
					JsonObject tertObject = new JsonObject();
					for (Entry<String, SpawnListEntryBuilder> tertEnts : secEnts.getValue().entrySet()) {
						String tertKey = tertEnts.getKey();
						JsonObject entityValueObject = new JsonObject();
						SpawnListEntryBuilder builder = tertEnts.getValue();
						entityValueObject.addProperty(MODID_KEY, builder.getModID());
						if (!"".equals(builder.getWeight())) {
							entityValueObject.addProperty(SPAWN_WEIGHT, builder.getWeight());
						}
						if (!"".equals(builder.getPassivePackSize())) {
							entityValueObject.addProperty(PASSIVE_PACKSIZE_KEY, builder.getPassivePackSize());
						}
						if (!"".equals(builder.getChunkPackSize())) {
							entityValueObject.addProperty(CHUNK_PACKSIZE_KEY, builder.getChunkPackSize());
						}
						if (!"".equals(builder.getLivingHandlerID().get()) && builder.getLivingHandlerID().isPresent()) {
							entityValueObject.addProperty(LIVING_HANDLER_KEY, builder.getLivingHandlerID().get());
						}

						if (!"".equals(builder.getCanSpawn()) && builder.getCanSpawn().isPresent()) {
							entityValueObject.addProperty(SPAWN_TAG_KEY, builder.getCanSpawn().get());
						}
						if (!"".equals(builder.getPostSpawn()) && builder.getPostSpawn().isPresent()) {
							entityValueObject.addProperty(POSTSPAWN_KEY, builder.getPostSpawn().get());
						}
						tertObject.add(tertKey, entityValueObject);
					}
					secObject.add(secKey, tertObject);
				}
				primObject.add(primKey, secObject);
			}
			endObject.add(SPAWN_LIST_KEY, primObject);
			return endObject;
		}

		@Override
		public BiomeSpawnListLoader deserialize(JsonElement object, Type type, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject endObject = object.getAsJsonObject();
			String fileVersion = GsonHelper.getMemberOrDefault(endObject, FILE_VERSION_KEY, FILE_VERSION);
			BiomeSpawnListLoader saveObject = new BiomeSpawnListLoader(GsonHelper.getMemberOrDefault(endObject,
					SORT_MODE_KEY, defaultSortByBiome));
			JsonObject primObject = GsonHelper.getMemberOrDefault(endObject, SPAWN_LIST_KEY, new JsonObject());
			for (Entry<String, JsonElement> primEntries : primObject.entrySet()) {
				String primKey = primEntries.getKey();
				if (primKey == null || primKey.trim().equals("")) {
					continue;
				}
				TreeMap<String, TreeMap<String, SpawnListEntryBuilder>> secMap = saveObject.biomeToTypeToCreature
						.get(primKey);
				if (secMap == null) {
					secMap = new TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>();
					saveObject.biomeToTypeToCreature.put(primKey, secMap);
				}
				for (Entry<String, JsonElement> secEntries : GsonHelper.getAsJsonObject(primEntries.getValue())
						.entrySet()) {
					String secKey = secEntries.getKey();
					if (secKey == null || secKey.trim().equals("")) {
						continue;
					}
					TreeMap<String, SpawnListEntryBuilder> tertMap = secMap.get(secKey);
					if (tertMap == null) {
						tertMap = new TreeMap<String, SpawnListEntryBuilder>();
						secMap.put(secKey, tertMap);
					}
					for (Entry<String, JsonElement> tertEntries : GsonHelper.getAsJsonObject(secEntries.getValue())
							.entrySet()) {
						String tertKey = tertEntries.getKey();
						JsonObject entityValueObject = GsonHelper.getAsJsonObject(tertEntries.getValue());
						String locExp = saveObject.sortCreatureByBiome ? primKey : tertKey;
						String livExp = saveObject.sortCreatureByBiome ? tertKey : secKey;
						String livingType = saveObject.sortCreatureByBiome ? secKey : primKey;
						String modID = GsonHelper.getMemberOrDefault(entityValueObject, MODID_KEY,
								SpawnListEntryBuilder.defaultFileName);
						String livingHandlerID = GsonHelper.getMemberOrDefault(entityValueObject, LIVING_HANDLER_KEY,
								"");

						SpawnListEntryBuilder builder = new SpawnListEntryBuilder(modID, livingHandlerID, livingType,
								locExp, livExp);

						int weight = GsonHelper.getMemberOrDefault(entityValueObject, SPAWN_WEIGHT, 0);
						String chunkPackSize = GsonHelper.getMemberOrDefault(entityValueObject, CHUNK_PACKSIZE_KEY,
								"0 + util.rand(1 + 4 - 0)");
						String passivePackSize = GsonHelper.getMemberOrDefault(entityValueObject, PASSIVE_PACKSIZE_KEY,
								"3");
						String spawnExp = GsonHelper.getMemberOrDefault(entityValueObject, SPAWN_TAG_KEY, "");
						String postspawnExp = GsonHelper.getMemberOrDefault(entityValueObject, POSTSPAWN_KEY, "");

						builder.setWeight(weight);
						builder.setChunkPackSize(chunkPackSize);
						builder.setPassivePackSize(passivePackSize);
						builder.setCanSpawn(spawnExp);
						builder.setPostSpawn(postspawnExp);

						tertMap.put(tertKey, builder);
					}
				}
			}
			return saveObject;
		}
	}
}
