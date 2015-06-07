package jas.spawner.modern.spawner.creature.entry;

import jas.common.helper.GsonHelper;
import jas.spawner.modern.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.base.Optional;
import com.google.common.collect.Table;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class BiomeSpawnsSaveObject {
	// SortByBiome: <BiomeName, <CreatureType(MONSTER/AMBIENT), <CreatureName/HandlerId/LivingGroup, SpawnListEntry>>>
	// !SortByBiome:<CreatureType(MONSTER/AMBIENT), <CreatureName/HandlerId/LivingGroup, <BiomeName, SpawnListEntry>>>
	private final TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>> biomeToTypeToCreature;
	private final boolean sortCreatureByBiome;

	private BiomeSpawnsSaveObject(Boolean sortCreatureByBiome) {
		biomeToTypeToCreature = new TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>>();
		this.sortCreatureByBiome = sortCreatureByBiome;
	}

	/**
	 * 
	 * @param registry
	 * @param validSpawnListEntries Contains Mapping between BiomeGroupID, LivingType to valid SpawnListEntry
	 */
	public BiomeSpawnsSaveObject(Table<String, String, Set<SpawnListEntry>> validSpawnListEntries,
			boolean sortCreatureByBiome) {
		this.sortCreatureByBiome = sortCreatureByBiome;
		biomeToTypeToCreature = new TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>>();
		for (Entry<String, Map<String, Set<SpawnListEntry>>> biomeGroupEntry : validSpawnListEntries.rowMap()
				.entrySet()) {
			String biomeGroupId = biomeGroupEntry.getKey();
			for (Entry<String, Set<SpawnListEntry>> livingTypeEntry : biomeGroupEntry.getValue().entrySet()) {
				String livingType = livingTypeEntry.getKey();
				for (SpawnListEntry spawnListEntry : livingTypeEntry.getValue()) {
					putEntity(biomeGroupId, livingType, spawnListEntry.livingGroupID, new SpawnListEntryBuilder(
							spawnListEntry), biomeToTypeToCreature);
				}
			}
		}
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

	private void putEntity(String biomeGroupId, String livingType, String entityName,
			SpawnListEntryBuilder spawnListEntry,
			TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>> primMap) {
		String primKey = sortCreatureByBiome ? biomeGroupId : livingType;
		String secoKey = sortCreatureByBiome ? livingType : entityName;
		String tertKey = sortCreatureByBiome ? entityName : biomeGroupId;

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

	public static class BiomeSpawnsSaveObjectSerializer implements JsonSerializer<BiomeSpawnsSaveObject>,
			JsonDeserializer<BiomeSpawnsSaveObject> {
		public final String FILE_VERSION = "2.0";
		public final String FILE_VERSION_KEY = "FILE_VERSION";
		public final String SORT_MODE_KEY = "SORTED_BY_BIOME";
		public final String SPAWN_LIST_KEY = "SPAWN_LIST_ENTRIES";
		public final String ENTITY_STAT_KEY = "Weight-PassivePackMax-ChunkPackMin-ChunkPackMax";
		@Deprecated
		public final String ENTITY_TAG_KEY = "Tags";

		public final String SPAWN_TAG_KEY = "Spawn Tag";
		public final String POSTSPAWN_KEY = "PostSpawn Tags";
		public final String SPAWN_OPERAND_KEY = "Spawn Operand";

		private final boolean defaultSortByBiome;

		public BiomeSpawnsSaveObjectSerializer(boolean defaultSortByBiome) {
			this.defaultSortByBiome = defaultSortByBiome;
		}

		/**
		 * Depending on SortMode the Key order varies (thus primKey, secKey, tertKey).
		 * 
		 * biomeToTypeToCreature == true is of the form <BIOME>:<TYPE>:<ENTITY> i.e. "Beach":"AMBIENT":"BAT"
		 * 
		 * biomeToTypeToCreature == false is of the form <ENTITY>:<BIOME>:<TYPE> i.e. "BAT":"Beach":"AMBIENT":
		 */
		@Override
		public JsonElement serialize(BiomeSpawnsSaveObject object, Type type, JsonSerializationContext context) {
			JsonObject endObject = new JsonObject();
			endObject.addProperty(FILE_VERSION_KEY, FILE_VERSION);
			endObject.addProperty(SORT_MODE_KEY, object.sortCreatureByBiome);
			JsonObject primObject = new JsonObject();
			for (Entry<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>> primEnts : object.biomeToTypeToCreature
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
						String stats = statsToString(builder.getWeight(), builder.getPackSize(),
								builder.getMinChunkPack(), builder.getMaxChunkPack());
						entityValueObject.addProperty(ENTITY_STAT_KEY, stats);
						if (builder.getSpawnOperand().isPresent()) {
							entityValueObject
									.addProperty(SPAWN_OPERAND_KEY, builder.getSpawnOperand().get().toString());
						}
						if (!"".equals(builder.getSpawnExpression())) {
							entityValueObject.addProperty(SPAWN_TAG_KEY, builder.getSpawnExpression());
						}
						if (!"".equals(builder.getPostSpawnExpression())) {
							entityValueObject.addProperty(POSTSPAWN_KEY, builder.getPostSpawnExpression());
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
		public BiomeSpawnsSaveObject deserialize(JsonElement object, Type type, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject endObject = object.getAsJsonObject();
			String fileVersion = GsonHelper.getMemberOrDefault(endObject, FILE_VERSION_KEY, FILE_VERSION);
			BiomeSpawnsSaveObject saveObject = new BiomeSpawnsSaveObject(GsonHelper.getMemberOrDefault(endObject,
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
						SpawnListEntryBuilder builder = new SpawnListEntryBuilder(tertKey, primKey);

						String biomeGroupId = saveObject.sortCreatureByBiome ? primKey : tertKey;
						String livingGroup = saveObject.sortCreatureByBiome ? tertKey : secKey;
						builder = new SpawnListEntryBuilder(livingGroup, biomeGroupId);
						getSetStats(builder, entityValueObject);

						if (fileVersion.equals("1.0")) {
							throw new IllegalArgumentException(
									"Detected SpawnListEntries FileFormat of 1.0. Format no longer autoconverted.");
						} else {
							String spawnTag = GsonHelper.getMemberOrDefault(entityValueObject, SPAWN_TAG_KEY, "");
							String spawnOperand = GsonHelper.getMemberOrDefault(entityValueObject, SPAWN_OPERAND_KEY,
									"");
							builder.setSpawnExpression(spawnTag,
									Optional.of("OR".equalsIgnoreCase(spawnOperand) ? Operand.OR : Operand.AND));
							String postspawnTag = GsonHelper.getMemberOrDefault(entityValueObject, POSTSPAWN_KEY, "");
							builder.setPostSpawnExpression(postspawnTag);
						}

						tertMap.put(tertKey, builder);
					}
				}
			}
			return saveObject;
		}

		private String statsToString(int weight, int packSize, int minChunk, int maxChunk) {
			return new StringBuilder().append(weight).append("-").append(packSize).append("-").append(minChunk)
					.append("-").append(maxChunk).toString();
		}

		private void getSetStats(SpawnListEntryBuilder builder, JsonObject creatureNameObject) {
			JsonElement element = creatureNameObject.get(ENTITY_STAT_KEY);
			int[] stats = element != null ? stringToStats(element.getAsString()) : stringToStats("");
			builder.setWeight(stats[0]).setPackSize(stats[1]).setMinChunkPack(stats[2]).setMaxChunkPack(stats[3]);
		}

		private int[] stringToStats(String stats) {
			String[] parts = stats.split("-");
			int[] result = new int[4];
			for (int i = 0; i < 4; i++) {
				try {
					result[i] = i < parts.length ? Integer.parseInt(parts[i]) : 0;
				} catch (NumberFormatException e) {
					result[i] = 0;
				}
			}
			return result;
		}
	}
}
