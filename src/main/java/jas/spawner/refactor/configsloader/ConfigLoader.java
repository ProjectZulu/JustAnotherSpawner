package jas.spawner.refactor.configsloader;

import jas.common.helper.FileUtilities;
import jas.common.helper.GsonHelper;
import jas.spawner.modern.DefaultProps;
import jas.spawner.refactor.WorldProperties;
import jas.spawner.refactor.structure.StructureHandlerLoader;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.Gson;

public class ConfigLoader {

	public static interface VersionedFile {
		public String getVersion();
	}

	public static class LoadedFile<T extends VersionedFile> {
		public final T saveObject; // TODO: Rename loadedFile: or NOT as it is Write as well as read

		public LoadedFile(T saveObject) {
			this.saveObject = saveObject;
		}
	}

	public LoadedFile<LivingTypeLoader> livingTypeLoader;
	public LoadedFile<DespawnRulesLoader> despawnRulesLoader;

	public LoadedFile<LivingSettingsLoader> livingGroupLoader;
	public Map<String, LoadedFile<LivingHandlerLoader>> livingHandlerLoaders;
	public LoadedFile<BiomeSettingsLoader> biomeGroupLoader;

	public Map<String, LoadedFile<BiomeSpawnListLoader>> biomeSpawnListLoaders;
	public LoadedFile<StructureHandlerLoader> structureHandlerLoader;

	public ConfigLoader() {
	}

	public ConfigLoader(File settingDirectory, WorldProperties worldProperties) {
		loadFromConfig(settingDirectory, worldProperties);
	}

	public ConfigLoader loadFromConfig(File settingDirectory, WorldProperties worldProperties) {
		Type[] types = new java.lang.reflect.Type[] { LivingTypeLoader.class, LivingSettingsLoader.class,
				LivingHandlerLoader.class, BiomeSettingsLoader.class, BiomeSpawnListLoader.class,
				StructureHandlerLoader.class, DespawnRulesLoader.class };
		Object[] serializers = new Object[] { new LivingTypeLoader.Serializer(), new LivingSettingsLoader.Serializer(),
				new LivingHandlerLoader.Serializer(), new BiomeSettingsLoader.Serializer(),
				new BiomeSpawnListLoader.Serializer(worldProperties.getFolderConfiguration().sortCreatureByBiome),
				new StructureHandlerLoader.Serializer(worldProperties.getFolderConfiguration().sortCreatureByBiome),
				new DespawnRulesLoader.Serializer() };
		Gson gson = GsonHelper.createGson(true, types, serializers);

		this.livingTypeLoader = new LoadedFile(GsonHelper.readOrCreateFromGson(
				FileUtilities.createReader(new File(settingDirectory, "CreatureType.cfg"), false),
				LivingTypeLoader.class, gson));

		this.livingGroupLoader = new LoadedFile(GsonHelper.readOrCreateFromGson(
				FileUtilities.createReader(new File(settingDirectory, "LivingGroups.cfg"), false),
				LivingSettingsLoader.class, gson));

		this.livingHandlerLoaders = new HashMap<String, ConfigLoader.LoadedFile<LivingHandlerLoader>>();
		File handlerFileFolder = new File(settingDirectory, "/" + DefaultProps.ENTITYHANDLERDIR);
		File[] filesLH = FileUtilities.getFileInDirectory(handlerFileFolder, ".cfg");
		for (File file : filesLH) {
			livingHandlerLoaders.put(
					file.getName(),
					new LoadedFile(GsonHelper.readOrCreateFromGson(FileUtilities.createReader(file, false),
							LivingHandlerLoader.class, gson)));
		}
		this.biomeGroupLoader = new LoadedFile(GsonHelper.readOrCreateFromGson(
				FileUtilities.createReader(new File(settingDirectory, "/" + "BiomeGroups.cfg"), false),
				BiomeSettingsLoader.class, gson));

		File entriesDir = new File(settingDirectory, "/" + DefaultProps.ENTITYSPAWNRDIR);
		File[] filesSLE = FileUtilities.getFileInDirectory(entriesDir, ".cfg");
		this.biomeSpawnListLoaders = new HashMap<String, ConfigLoader.LoadedFile<BiomeSpawnListLoader>>();
		for (File file : filesSLE) {
			biomeSpawnListLoaders.put(
					file.getName(),
					new LoadedFile(GsonHelper.readOrCreateFromGson(FileUtilities.createReader(file, false),
							BiomeSpawnListLoader.class, gson)));
		}

		this.structureHandlerLoader = new LoadedFile<StructureHandlerLoader>(GsonHelper.readOrCreateFromGson(
				FileUtilities.createReader(new File(settingDirectory, "/" + "StructureSpawns.cfg"), false),
				StructureHandlerLoader.class, gson));

		this.despawnRulesLoader = new LoadedFile<DespawnRulesLoader>(GsonHelper.readOrCreateFromGson(
				FileUtilities.createReader(new File(settingDirectory, "/" + "DespawnRules.cfg"), false),
				DespawnRulesLoader.class, gson));
		return this;
	}

	public ConfigLoader saveToConfig(File settingDirectory, WorldProperties worldProperties) {
		Type[] types = new java.lang.reflect.Type[] { LivingTypeLoader.class, LivingSettingsLoader.class,
				LivingHandlerLoader.class, BiomeSettingsLoader.class, BiomeSpawnListLoader.class,
				StructureHandlerLoader.class, DespawnRulesLoader.class };
		Object[] serializers = new Object[] { new LivingTypeLoader.Serializer(), new LivingSettingsLoader.Serializer(),
				new LivingHandlerLoader.Serializer(), new BiomeSettingsLoader.Serializer(),
				new BiomeSpawnListLoader.Serializer(worldProperties.getFolderConfiguration().sortCreatureByBiome),
				new StructureHandlerLoader.Serializer(worldProperties.getFolderConfiguration().sortCreatureByBiome),
				new DespawnRulesLoader.Serializer() };
		Gson gson = GsonHelper.createGson(true, types, serializers);

		GsonHelper.writeToGson(FileUtilities.createWriter(new File(settingDirectory, "CreatureType.cfg"), true),
				livingTypeLoader.saveObject, LivingTypeLoader.class, gson);
		GsonHelper.writeToGson(FileUtilities.createWriter(new File(settingDirectory, "LivingGroups.cfg"), true),
				livingGroupLoader.saveObject, LivingSettingsLoader.class, gson);

		for (Entry<String, LoadedFile<LivingHandlerLoader>> entry : livingHandlerLoaders.entrySet()) {
			LivingHandlerLoader loader = entry.getValue().saveObject;
			File handlerFileFolder = new File(settingDirectory, "/" + DefaultProps.ENTITYHANDLERDIR);
			if (entry.getValue().saveObject.getHandlers().isPresent()) {
				GsonHelper.writeToGson(
						FileUtilities.createWriter(new File(handlerFileFolder, entry.getKey() + ".cfg"), true),
						entry.getValue().saveObject, LivingHandlerLoader.class, gson);
			}
		}

		GsonHelper.writeToGson(FileUtilities.createWriter(new File(settingDirectory, "BiomeGroups.cfg"), true),
				biomeGroupLoader.saveObject, BiomeSettingsLoader.class, gson);

		for (Entry<String, LoadedFile<BiomeSpawnListLoader>> entry : biomeSpawnListLoaders.entrySet()) {
			BiomeSpawnListLoader loader = entry.getValue().saveObject;
			File handlerFileFolder = new File(settingDirectory, "/" + DefaultProps.ENTITYSPAWNRDIR);
			GsonHelper.writeToGson(
					FileUtilities.createWriter(new File(handlerFileFolder, entry.getKey() + ".cfg"), true),
					entry.getValue().saveObject, BiomeSpawnListLoader.class, gson);
		}

		GsonHelper.writeToGson(FileUtilities.createWriter(new File(settingDirectory, "StructureSpawns.cfg"), true),
				structureHandlerLoader.saveObject, StructureHandlerLoader.class, gson);

		GsonHelper.writeToGson(FileUtilities.createWriter(new File(settingDirectory, "DespawnRules.cfg"), true),
				despawnRulesLoader.saveObject, DespawnRulesLoader.class, gson);
		return this;
	}

	public static File getFile(File configDirectory, String saveName, String fileName) {
		String filePath = DefaultProps.WORLDSETTINGSDIR + saveName + "/" + DefaultProps.ENTITYSPAWNRDIR;
		if (fileName != null && !fileName.equals("")) {
			filePath = filePath.concat(fileName).concat(".cfg");
		}
		return new File(configDirectory, filePath);
	}

	public void saveToConfigs(File settingDirectory) {

	}
}