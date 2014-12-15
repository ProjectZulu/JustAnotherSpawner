package jas.refactor.ConfigLoader;

import jas.common.GsonHelper;
import jas.common.spawner.TagConverter;
import jas.common.spawner.creature.entry.BiomeSpawnsSaveObject;
import jas.common.spawner.creature.handler.parsing.keys.Key;
import jas.common.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;
import jas.refactor.ConfigLoader.ConfigLoader.VersionedFile;
import jas.refactor.biome.BiomeGroups;
import jas.refactor.biome.list.SpawnListEntryBuilder;
import jas.refactor.biome.list.SpawnListEntryBuilder.SpawnListEntry;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
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

public class BiomeSpawnListLoader implements VersionedFile {
	private String version;

	// SortByBiome: <LocationExp, <CreatureType(MONSTER/AMBIENT), <CreatureName/HandlerId/LivingGroup, SpawnListEntry>>>
	// !SortByBiome:<CreatureType(MONSTER/AMBIENT), <CreatureName/HandlerId/LivingGroup, <LocationExp, SpawnListEntry>>>
	private TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>> biomeToTypeToCreature;
	private boolean sortCreatureByBiome;

	public BiomeSpawnListLoader() {
		this.version = Serializer.FILE_VERSION;
	}

	private BiomeSpawnListLoader(Boolean sortCreatureByBiome) {
		biomeToTypeToCreature = new TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>>();
		this.sortCreatureByBiome = sortCreatureByBiome;
	}

	public BiomeSpawnListLoader(Table<String, String, Set<SpawnListEntry>> validSpawnListEntries,
			boolean sortCreatureByBiome) {
		this.sortCreatureByBiome = sortCreatureByBiome;
		biomeToTypeToCreature = new TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>>();
		for (Entry<String, Map<String, Set<SpawnListEntry>>> biomeGroupEntry : validSpawnListEntries.rowMap()
				.entrySet()) {
			String biomeGroupId = biomeGroupEntry.getKey();
			for (Entry<String, Set<SpawnListEntry>> livingTypeEntry : biomeGroupEntry.getValue().entrySet()) {
				String livingType = livingTypeEntry.getKey();
				for (SpawnListEntry spawnListEntry : livingTypeEntry.getValue()) {
					putEntry(biomeGroupId, livingType, spawnListEntry.iD(), new SpawnListEntryBuilder(spawnListEntry),
							biomeToTypeToCreature);
				}
			}
		}
	}

	private void putEntry(String biomeGroupId, String livingType, String livingHandler,
			SpawnListEntryBuilder spawnListEntry,
			TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>> primMap) {
		String primKey = sortCreatureByBiome ? biomeGroupId : livingType;
		String secoKey = sortCreatureByBiome ? livingType : livingHandler;
		String tertKey = sortCreatureByBiome ? livingHandler : biomeGroupId;

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

		public final String SPAWN_WEIGHT = "Weight";
		public final String PASSIVE_PACKSIZE = "Passive PackSize";
		public final String CHUNK_PACKSIZE = "Chunk PackSize";
		public final String SPAWN_TAG_KEY = "Spawn Tag";
		public final String POSTSPAWN_KEY = "PostSpawn Tags";
		public final String ENTITY_SPAWN = "Entity Tag";

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
						if (!"".equals(builder.getWeight())) {
							entityValueObject.addProperty(SPAWN_WEIGHT, builder.getWeight());
						}
						if (!"".equals(builder.getPassivePackSize())) {
							entityValueObject.addProperty(PASSIVE_PACKSIZE, builder.getPassivePackSize());
						}
						if (!"".equals(builder.getChunkPackSize())) {
							entityValueObject.addProperty(CHUNK_PACKSIZE, builder.getChunkPackSize());
						}

						if (!"".equals(builder.getCanSpawn())) {
							entityValueObject.addProperty(SPAWN_TAG_KEY, builder.getCanSpawn());
						}
						if (!"".equals(builder.getPostSpawn())) {
							entityValueObject.addProperty(POSTSPAWN_KEY, builder.getPostSpawn());
						}
						if (!"".equals(builder.getEntityToSpawn())) {
							entityValueObject.addProperty(ENTITY_SPAWN, builder.getEntityToSpawn());
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

						String biomeGroupId = BiomeGroups.key.concat(saveObject.sortCreatureByBiome ? primKey : tertKey);
						String livingGroup = saveObject.sortCreatureByBiome ? tertKey : secKey;
						// String livingHandlerID = saveObject.sortCreatureByBiome ? tertKey : secKey;

						String livingType = saveObject.sortCreatureByBiome ? secKey : primKey;
						SpawnListEntryBuilder builder = new SpawnListEntryBuilder(livingGroup, livingType, biomeGroupId);
						if (fileVersion.equals("3.0")) {

						} else if (fileVersion.equals("1.0")) {
							JsonElement element = entityValueObject.get(ENTITY_STAT_KEY);
							int[] stats = element != null ? stringToStats(element.getAsString()) : stringToStats("");
							builder.setWeight(Integer.toString(stats[0]));
							builder.setPassivePackSize(Integer.toString(stats[1]));
							builder.setChunkPackSize(new StringBuilder().append(stats[1]).append("+ util.rand(1 + ")
									.append(stats[3]).append("-").append(stats[2]).append(")").toString());
							builder.setEntityToSpawn(livingGroup);
							// Hack, will not convert well if LivingHandler.contents was used thoroughly

							String canSpawnExpression = "";
							String postSpawnExpression = "";
							if (fileVersion.equals("1.0")) {
								String optionalParameters = GsonHelper.getMemberOrDefault(entityValueObject,
										ENTITY_TAG_KEY, "");
								String[] parts = optionalParameters.split("\\{");
								for (String string : optionalParameters.split("\\{")) {
									String parsed = string.replace("}", "");
									String titletag = parsed.split("\\:", 2)[0].toLowerCase();
									TagConverter conv = null;
									if (Key.spawn.keyParser.isMatch(titletag)) {
										conv = new TagConverter(parsed);
										if (!conv.expression.trim().equals("")) {
											canSpawnExpression = conv.expression;
										}
									} else if (Key.postspawn.keyParser.isMatch(titletag)) {
										conv = new TagConverter(parsed);
										if (!conv.expression.trim().equals("")) {
											postSpawnExpression = conv.expression;
										}
									}
								}
							} else {
								String spawnTag = GsonHelper.getMemberOrDefault(entityValueObject, SPAWN_TAG_KEY, "");
								String spawnOperand = GsonHelper.getMemberOrDefault(entityValueObject,
										SPAWN_OPERAND_KEY, "");
								canSpawnExpression = spawnTag;
								String postspawnTag = GsonHelper.getMemberOrDefault(entityValueObject, POSTSPAWN_KEY,
										"");
								postSpawnExpression = postspawnTag;
							}
							builder.setCanSpawn(canSpawnExpression);
							builder.setPostSpawn(postSpawnExpression);
						}
						tertMap.put(tertKey, builder);
					}
				}
			}
			return saveObject;
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
