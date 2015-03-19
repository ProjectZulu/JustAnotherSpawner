package jas.spawner.refactor.configsloader;

import jas.common.helper.FileUtilities;
import jas.common.helper.GsonHelper;
import jas.spawner.modern.DefaultProps;
import jas.spawner.refactor.WorldProperties;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public class ConfigLoader {

	public static interface VersionedFile {
		public String getVersion();
	}

	public static class LoadedFile<T extends VersionedFile> {
		public final T saveObject; // TODO: Rename loadedFile

		public LoadedFile(T saveObject) {
			this.saveObject = saveObject;
		}
	}

	public LoadedFile<LivingTypeLoader> livingTypeLoader;
	public LoadedFile<EntityGroupingLoader> livingGroupLoader;
	public Map<String, LoadedFile<LivingHandlerLoader>> livingHandlerLoaders;
	public LoadedFile<BiomeGroupLoader> biomeGroupLoader;
	public Map<String, LoadedFile<BiomeSpawnListLoader>> biomeSpawnListLoaders;

	/** Used for Saving */
	public ConfigLoader() {
		this.livingHandlerLoaders = new HashMap<String, ConfigLoader.LoadedFile<LivingHandlerLoader>>();
	}

	public ConfigLoader(File settingDirectory, WorldProperties worldProperties) {
		Type[] types = new java.lang.reflect.Type[] { LivingTypeLoader.class, EntityGroupingLoader.class,
				LivingHandlerLoader.class, BiomeGroupLoader.class, BiomeSpawnListLoader.class };
		Object[] serializers = new Object[] { new LivingTypeLoader.Serializer(), new EntityGroupingLoader.Serializer(),
				new LivingHandlerLoader.Serializer(), new BiomeGroupLoader.Serializer(),
				new BiomeSpawnListLoader.Serializer(worldProperties.getFolderConfiguration().sortCreatureByBiome) };
		Gson gson = GsonHelper.createGson(true, types, serializers);

		this.livingTypeLoader = new LoadedFile(GsonHelper.readOrCreateFromGson(
				FileUtilities.createReader(new File(settingDirectory, "CreatureType.cfg"), false),
				LivingTypeLoader.class, gson));

		this.livingGroupLoader = new LoadedFile(GsonHelper.readOrCreateFromGson(
				FileUtilities.createReader(new File(settingDirectory, "LivingGroups.cfg"), false),
				EntityGroupingLoader.class, gson));

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
				BiomeGroupLoader.class, gson));

		File entriesDir = new File(settingDirectory, "/" + DefaultProps.ENTITYSPAWNRDIR);
		File[] filesSLE = FileUtilities.getFileInDirectory(entriesDir, ".cfg");
		this.biomeSpawnListLoaders = new HashMap<String, ConfigLoader.LoadedFile<BiomeSpawnListLoader>>();
		for (File file : filesSLE) {
			biomeSpawnListLoaders.put(
					file.getName(),
					new LoadedFile(GsonHelper.readOrCreateFromGson(FileUtilities.createReader(file, false),
							BiomeSpawnListLoader.class, gson)));
		}
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