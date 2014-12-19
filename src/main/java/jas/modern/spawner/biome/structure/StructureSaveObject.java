package jas.modern.spawner.biome.structure;

import jas.modern.GsonHelper;
import jas.modern.spawner.TagConverter;
import jas.modern.spawner.creature.entry.SpawnListEntry;
import jas.modern.spawner.creature.entry.SpawnListEntryBuilder;
import jas.modern.spawner.creature.handler.LivingHandler;
import jas.modern.spawner.creature.handler.LivingHandlerRegistry;
import jas.modern.spawner.creature.handler.parsing.keys.Key;
import jas.modern.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;
import jas.modern.spawner.creature.type.CreatureTypeRegistry;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

public class StructureSaveObject {
    // <StructureKey, <CreatureType, <CreatureName, SpawnListEntry>>>
    private final TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>> interpreterToKeyToEntry;

    /** For Serialization purposes only */
    public StructureSaveObject() {
        interpreterToKeyToEntry = new TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>>();
    }

    public StructureSaveObject(LivingHandlerRegistry registry, List<StructureHandler> structureHandlers) {
        interpreterToKeyToEntry = new TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>>();
        for (StructureHandler structureHandler : structureHandlers) {
            for (String structureKey : structureHandler.interpreter.getStructureKeys()) {
                // Manually place structure map to be sure it appears in config for empty spawnlists
                if (interpreterToKeyToEntry.get(structureKey) == null) {
                    interpreterToKeyToEntry.put(structureKey,
                            new TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>());
                }
                for (SpawnListEntry spawnList : structureHandler.getStructureDisabledSpawnList(structureKey)) {
                    LivingHandler handler = registry.getLivingHandler(spawnList.livingGroupID);
                    String livingType = handler != null ? handler.creatureTypeID : CreatureTypeRegistry.NONE;
                    putEntity(structureKey, livingType, spawnList.livingGroupID, new SpawnListEntryBuilder(spawnList),
                            interpreterToKeyToEntry);
                }

                for (SpawnListEntry spawnList : structureHandler.getStructureSpawnList(structureKey)) {
                    LivingHandler handler = registry.getLivingHandler(spawnList.livingGroupID);
                    String livingType = handler != null ? handler.creatureTypeID : CreatureTypeRegistry.NONE;
                    putEntity(structureKey, livingType, spawnList.livingGroupID, new SpawnListEntryBuilder(spawnList),
                            interpreterToKeyToEntry);
                }
            }
        }
    }

    private void putEntity(String structureKey, String livingType, String entityName,
            SpawnListEntryBuilder spawnListEntry,
            TreeMap<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>> interpreterToKeyToEntry) {
        TreeMap<String, TreeMap<String, SpawnListEntryBuilder>> typeInnerMap = interpreterToKeyToEntry
                .get(structureKey);
        if (typeInnerMap == null) {
            typeInnerMap = new TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>();
            interpreterToKeyToEntry.put(structureKey, typeInnerMap);
        }
        TreeMap<String, SpawnListEntryBuilder> nameInnerMap = typeInnerMap.get(livingType);
        if (nameInnerMap == null) {
            nameInnerMap = new TreeMap<String, SpawnListEntryBuilder>();
            typeInnerMap.put(livingType, nameInnerMap);
        }
        nameInnerMap.put(entityName, spawnListEntry);
    }

    /**
     * StructureKey, SpawnListEntryBuilder
     */
    public HashMap<String, Collection<SpawnListEntryBuilder>> createKeyToSpawnList() {
        HashMap<String, Collection<SpawnListEntryBuilder>> map = new HashMap<String, Collection<SpawnListEntryBuilder>>();
        for (Entry<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>> structureKeyEntry : interpreterToKeyToEntry
                .entrySet()) {
            String structureKey = structureKeyEntry.getKey();
            Collection<SpawnListEntryBuilder> spawnList = map.get(structureKey);
            if (spawnList == null) {
                spawnList = new ArrayList<SpawnListEntryBuilder>();
                map.put(structureKey, spawnList);
            }
            for (TreeMap<String, SpawnListEntryBuilder> typeEntry : structureKeyEntry.getValue().values()) {
                for (SpawnListEntryBuilder builder : typeEntry.values()) {
                    spawnList.add(builder);
                }
            }
        }
        return map;
    }

    public static class Serializer implements JsonSerializer<StructureSaveObject>,
            JsonDeserializer<StructureSaveObject> {
        public final String FILE_VERSION = "2.0";
        public final String FILE_VERSION_KEY = "FILE_VERSION";
        public final String STRUCTURES_KEY = "STRUCTURES";
        public final String ENTITY_STAT_KEY = "Weight-PassivePackMax-ChunkPackMin-ChunkPackMax";
        @Deprecated
        public final String ENTITY_TAG_KEY = "Tags";

		public final String SPAWN_TAG_KEY = "Spawn Tag";
		public final String POSTSPAWN_KEY = "PostSpawn Tags";
		public final String SPAWN_OPERAND_KEY = "Spawn Operand";
        
        @Override
        public JsonElement serialize(StructureSaveObject object, Type type, JsonSerializationContext context) {
            JsonObject endObject = new JsonObject();
            endObject.addProperty(FILE_VERSION_KEY, FILE_VERSION);
            JsonObject spawnListEntries = new JsonObject();
            for (Entry<String, TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>> structureKeyEntry : object.interpreterToKeyToEntry
                    .entrySet()) {
                String structureKey = structureKeyEntry.getKey();
                JsonObject structureKeyObject = new JsonObject();
                for (Entry<String, TreeMap<String, SpawnListEntryBuilder>> typeEntry : structureKeyEntry.getValue()
                        .entrySet()) {
                    String livingType = typeEntry.getKey();
                    JsonObject livingTypeObject = new JsonObject();
                    for (Entry<String, SpawnListEntryBuilder> nameEntry : typeEntry.getValue().entrySet()) {
                        String creatureName = nameEntry.getKey();
                        JsonObject creatureNameObject = new JsonObject();
                        String stats = statsToString(nameEntry.getValue().getWeight(), nameEntry.getValue()
                                .getPackSize(), nameEntry.getValue().getMinChunkPack(), nameEntry.getValue()
                                .getMaxChunkPack());
                        creatureNameObject.addProperty(ENTITY_STAT_KEY, stats);
                        
						if (nameEntry.getValue().getSpawnOperand().isPresent()) {
							creatureNameObject.addProperty(SPAWN_OPERAND_KEY, nameEntry.getValue().getSpawnOperand()
									.get().toString());
						}
						if (!"".equals(nameEntry.getValue().getSpawnExpression())) {
							creatureNameObject.addProperty(SPAWN_TAG_KEY, nameEntry.getValue().getSpawnExpression());
						}
						if (!"".equals(nameEntry.getValue().getPostSpawnExpression())) {
							creatureNameObject
									.addProperty(POSTSPAWN_KEY, nameEntry.getValue().getPostSpawnExpression());
						}
                        livingTypeObject.add(creatureName, creatureNameObject);
                    }
                    structureKeyObject.add(livingType, livingTypeObject);
                }
                spawnListEntries.add(structureKey, structureKeyObject);
            }
            endObject.add(STRUCTURES_KEY, spawnListEntries);
            return endObject;
        }

        @Override
        public StructureSaveObject deserialize(JsonElement object, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            StructureSaveObject saveObject = new StructureSaveObject();
            JsonObject endObject = GsonHelper.getAsJsonObject(object);
            String fileVersion = GsonHelper.getMemberOrDefault(endObject, FILE_VERSION_KEY, FILE_VERSION);
            JsonObject structures = GsonHelper.getMemberOrDefault(endObject, STRUCTURES_KEY, new JsonObject());
            for (Entry<String, JsonElement> structureKeyEntry : structures.entrySet()) {
                String structureKey = structureKeyEntry.getKey();
                if (structureKey == null || structureKey.trim().equals("")) {
                    continue;
                }
                TreeMap<String, TreeMap<String, SpawnListEntryBuilder>> structureMap = saveObject.interpreterToKeyToEntry
                        .get(structureKey);
                if (structureMap == null) {
                    structureMap = new TreeMap<String, TreeMap<String, SpawnListEntryBuilder>>();
                    saveObject.interpreterToKeyToEntry.put(structureKey, structureMap);
                }
                for (Entry<String, JsonElement> typeEntry : structureKeyEntry.getValue().getAsJsonObject().entrySet()) {
                    String livingType = typeEntry.getKey();
                    if (livingType == null || livingType.trim().equals("")) {
                        continue;
                    }
                    TreeMap<String, SpawnListEntryBuilder> livingTypeMap = structureMap.get(livingType);
                    if (livingTypeMap == null) {
                        livingTypeMap = new TreeMap<String, SpawnListEntryBuilder>();
                        structureMap.put(livingType, livingTypeMap);
                    }
                    for (Entry<String, JsonElement> nameEntry : typeEntry.getValue().getAsJsonObject().entrySet()) {
                        String livingGroup = nameEntry.getKey();
                        JsonObject creatureNameObject = nameEntry.getValue().getAsJsonObject();
                        SpawnListEntryBuilder builder = new SpawnListEntryBuilder(livingGroup, structureKey);
                        getSetStats(builder, creatureNameObject);
						if (fileVersion.equals("1.0")) {
							String optionalParameters = GsonHelper.getMemberOrDefault(creatureNameObject,
									ENTITY_TAG_KEY, "");
							String[] parts = optionalParameters.split("\\{");
							for (String string : optionalParameters.split("\\{")) {
								String parsed = string.replace("}", "");
								String titletag = parsed.split("\\:", 2)[0].toLowerCase();
								TagConverter conv = null;
								if (Key.spawn.keyParser.isMatch(titletag)) {
									conv = new TagConverter(parsed);
									if (!conv.expression.trim().equals("")) {
										builder.setSpawnExpression(conv.expression, Optional.of(conv.operand));
									}
								} else if (Key.postspawn.keyParser.isMatch(titletag)) {
									conv = new TagConverter(parsed);
									if (!conv.expression.trim().equals("")) {
										builder.setPostSpawnExpression(conv.expression);
									}
								}
							}
						} else {
							String spawnTag = GsonHelper.getMemberOrDefault(creatureNameObject, SPAWN_TAG_KEY, "");
							String spawnOperand = GsonHelper.getMemberOrDefault(creatureNameObject, SPAWN_OPERAND_KEY,
									"");
							builder.setSpawnExpression(spawnTag,
									Optional.of("OR".equalsIgnoreCase(spawnOperand) ? Operand.OR : Operand.AND));
							String postspawnTag = GsonHelper.getMemberOrDefault(creatureNameObject, POSTSPAWN_KEY, "");
							builder.setPostSpawnExpression(postspawnTag);
						}

                        livingTypeMap.put(livingGroup, builder);
                    }
                }
            }
            return saveObject;
        }

        private String statsToString(int weight, int packSize, int minChunk, int maxChunk) {
            return new StringBuilder().append(weight).append("-").append(packSize).append("-").append(minChunk)
                    .append("-").append(maxChunk).toString();
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

        private void getSetStats(SpawnListEntryBuilder builder, JsonObject creatureNameObject) {
            JsonElement element = creatureNameObject.get(ENTITY_STAT_KEY);
            int[] stats = element != null ? stringToStats(element.getAsString()) : stringToStats("");
            builder.setWeight(stats[0]).setPackSize(stats[1]).setMinChunkPack(stats[2]).setMaxChunkPack(stats[2]);
        }
    }
}
