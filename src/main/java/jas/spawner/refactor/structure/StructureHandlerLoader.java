package jas.spawner.refactor.structure;

import jas.common.helper.GsonHelper;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder.SpawnListEntry;
import jas.spawner.refactor.configsloader.ConfigLoader.VersionedFile;
import jas.spawner.refactor.structure.StructureHandlerBuilder.StructureHandler;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * @param sortCreatureByBiome Determines order of entity headings
 * @SortByBiome: <LocationExp, <CreatureType(MONSTER/AMBIENT), <LivingExpression, SpawnListEntry>>>
 * @!SortByBiome:<CreatureType(MONSTER/AMBIENT), <LivingExpression, <LocationExp, SpawnListEntry>>>
 * 
 * @PrimKey == sortCreatureByBiome ? Location : Type
 * @Sec_Key == sortCreatureByBiome ? Type : LivingExpression
 * @TertKey == sortCreatureByBiome ? LivingExpression : Location
 */
public class StructureHandlerLoader implements VersionedFile {
	private String version;
	private boolean sortCreatureByBiome;

	// SortByBiome: <LocationExp, <CreatureType(MONSTER/AMBIENT), <LivingExpression, SpawnListEntry>>>
	// !SortByBiome:<CreatureType(MONSTER/AMBIENT), <LivingExpression, <LocationExp, SpawnListEntry>>>
	private final TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>> primToSecToTertToEntry;

	public StructureHandlerLoader(boolean sortCreatureByBiome) {
		this.version = Serializer.FILE_VERSION;
		this.sortCreatureByBiome = sortCreatureByBiome;
		primToSecToTertToEntry = new TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>>();
	}

	public StructureHandlerLoader(boolean sortCreatureByBiome, List<StructureHandler> structureHandlers) {
		this.version = Serializer.FILE_VERSION;
		this.sortCreatureByBiome = sortCreatureByBiome;
		primToSecToTertToEntry = new TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>>();

		for (StructureHandler structureHandler : structureHandlers) {
			for (String structureKey : structureHandler.getStructureKeys()) {
				for (SpawnListEntry spawnListEntry : structureHandler.structureKeysToSpawnList.get(structureKey)) {
					putEntry(new SpawnListEntryBuilder(spawnListEntry), primToSecToTertToEntry);
				}

				for (SpawnListEntry spawnListEntry : structureHandler.structureKeysToDisabledpawnList.get(structureKey)) {
					putEntry(new SpawnListEntryBuilder(spawnListEntry), primToSecToTertToEntry);
				}
			}
		}
	}

	private void putEntry(SpawnListEntryBuilder spawnListEntry,
			TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>> keyMap) {
		String primKey = getPrimaryKey(spawnListEntry);
		String secoKey = getSecondaryKey(spawnListEntry);
		String tertKey = getTertiaryKey(spawnListEntry);

		TreeMap<String, TreeMap<String, SpawnListEntryBuilder>> secMap = keyMap.get(primKey);
		if (secMap == null) {
			secMap = new TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>();
			keyMap.put(primKey, secMap);
		}
		TreeMap<String, SpawnListEntryBuilder> tertMap = secMap.get(secoKey);
		if (tertMap == null) {
			tertMap = new TreeMap<String, SpawnListEntryBuilder>();
			secMap.put(secoKey, tertMap);
		}
		tertMap.put(tertKey, spawnListEntry);
	}

	@Override
	public String getVersion() {
		return version;
	}

	public HashMap<String, Collection<SpawnListEntryBuilder>> locKeyToSpawnlist() {
		HashMap<String, Collection<SpawnListEntryBuilder>> structureKeyToSpawnList = new HashMap<String, Collection<SpawnListEntryBuilder>>();
		for (Entry<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>> primEntry : primToSecToTertToEntry
				.entrySet()) {
			String primKey = primEntry.getKey();
			for (Entry<String, TreeMap<String, SpawnListEntryBuilder>> secEntrty : primEntry.getValue().entrySet()) {
				String secKey = secEntrty.getKey();
				for (Entry<String, SpawnListEntryBuilder> tertEntrty : secEntrty.getValue().entrySet()) {
					String tertKey = tertEntrty.getKey();
					String structureLocation = getLocationExpFromKey(primKey, secKey, tertKey);

					Collection<SpawnListEntryBuilder> spawnList = structureKeyToSpawnList.get(structureLocation);
					if (spawnList == null) {
						spawnList = new HashSet<SpawnListEntryBuilder>();
						structureKeyToSpawnList.put(structureLocation, spawnList);
					}
					spawnList.add(tertEntrty.getValue());
				}
			}
		}
		return structureKeyToSpawnList;
	}

	public static class Serializer implements JsonSerializer<StructureHandlerLoader>,
			JsonDeserializer<StructureHandlerLoader> {
		public final static String FILE_VERSION = "3.0";
		public final String FILE_VERSION_KEY = "FILE_VERSION";
		public final String SORT_MODE_KEY = "SORTED_BY_LOCATION";
		public final String SPAWN_LIST_KEY = "SPAWN_LIST_ENTRIES";

		public final String SPAWN_WEIGHT = "WEIGHT";
		public final String PASSIVE_PACKSIZE_KEY = "PASSIVE_PACKSIZE";
		public final String CHUNK_PACKSIZE_KEY = "CHUNK_PACKSIZE";
		public final String SPAWN_TAG_KEY = "SPAWN_TAG";
		public final String POSTSPAWN_KEY = "POST_SPAWN_TAG";

		public final String MODID_KEY = "MOD_ID";
		public final String LIVING_HANDLER_KEY = "LIVING_HANDLER";

		private final boolean defaultSortByBiome;

		public Serializer(boolean defaultSortByBiome) {
			this.defaultSortByBiome = defaultSortByBiome;
		}

		// SortByBiome: <LocationExp, <CreatureType(MONSTER/AMBIENT), <LivingExpression, SpawnListEntry>>>
		@Override
		public JsonElement serialize(StructureHandlerLoader loader, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject endObject = new JsonObject();
			endObject.addProperty(FILE_VERSION_KEY, FILE_VERSION);
			endObject.addProperty(SORT_MODE_KEY, loader.sortCreatureByBiome);
			JsonObject primObject = new JsonObject();
			for (Entry<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>> primEnts : loader.primToSecToTertToEntry
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
						// entityValueObject.addProperty(MODID_KEY, builder.getModID()); // This could be enabled later
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
		public StructureHandlerLoader deserialize(JsonElement object, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject endObject = object.getAsJsonObject();
			String fileVersion = GsonHelper.getMemberOrDefault(endObject, FILE_VERSION_KEY, FILE_VERSION);
			StructureHandlerLoader loader = new StructureHandlerLoader(GsonHelper.getMemberOrDefault(endObject,
					SORT_MODE_KEY, defaultSortByBiome));

			JsonObject primObject = GsonHelper.getMemberOrDefault(endObject, SPAWN_LIST_KEY, new JsonObject());
			for (Entry<String, JsonElement> primEntries : primObject.entrySet()) {
				String primKey = primEntries.getKey();
				if (primKey == null || primKey.trim().equals("")) {
					continue;
				}
				TreeMap<String, TreeMap<String, SpawnListEntryBuilder>> secMap = loader.primToSecToTertToEntry
						.get(primKey);
				if (secMap == null) {
					secMap = new TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>();
					loader.primToSecToTertToEntry.put(primKey, secMap);
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
						String locExp = loader.getLocationExpFromKey(primKey, secKey, tertKey);
						String livExp = loader.getLivingExpFromKey(primKey, secKey, tertKey);
						String livingType = loader.getLivingTypeFromKey(primKey, secKey, tertKey);
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
			return loader;
		}
	}

	private String getPrimaryKey(SpawnListEntryBuilder builder) {
		return sortCreatureByBiome ? builder.getLocContent() : builder.getLivingTypeID();
	}

	private String getSecondaryKey(SpawnListEntryBuilder builder) {
		return sortCreatureByBiome ? builder.getLivingTypeID() : builder.getEntContent();
	}

	private String getTertiaryKey(SpawnListEntryBuilder builder) {
		return sortCreatureByBiome ? builder.getEntContent() : builder.getLocContent();
	}

	private String getLocationExpFromKey(String primKey, String secKey, String tertKey) {
		return sortCreatureByBiome ? primKey : tertKey;
	}

	private String getLivingExpFromKey(String primKey, String secKey, String tertKey) {
		return sortCreatureByBiome ? tertKey : secKey;
	}

	private String getLivingTypeFromKey(String primKey, String secKey, String tertKey) {
		return sortCreatureByBiome ? secKey : primKey;
	}
}
