package jas.refactor.ConfigLoader;

import jas.common.DefaultProps;
import jas.common.FileUtilities;
import jas.common.GsonHelper;
import jas.common.spawner.creature.handler.LivingHandler;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;

public class ConfigLoader {

	public static interface VersionedFile {
		public String getVersion();
	}

	public static class LoadedFile<T extends VersionedFile> {
		public final T saveObject;

		public LoadedFile(T saveObject) {
			this.saveObject = saveObject;
		}
	}

	public LoadedFile<LivingTypeLoader> livingTypeLoader;
	public LoadedFile<EntityGroupingLoader> livingGroupLoader;
	public Map<String, LoadedFile<LivingHandlerLoader>> livingHandlerLoaders;

	/** Used for Saving */
	public ConfigLoader() {
		this.livingHandlerLoaders = new HashMap<String, ConfigLoader.LoadedFile<LivingHandlerLoader>>();
	}

	public ConfigLoader(File settingDirectory) {
		Type[] types = new java.lang.reflect.Type[] { LivingTypeLoader.class, EntityGroupingLoader.class,
				LivingHandlerLoader.class };
		Object[] serializers = new Object[] { new LivingTypeLoader.Serializer(), new EntityGroupingLoader.Serializer(),
				new LivingHandlerLoader.Serializer() };
		Gson gson = GsonHelper.createGson(true, types, serializers);

		this.livingTypeLoader = new LoadedFile(GsonHelper.readOrCreateFromGson(
				FileUtilities.createReader(new File(settingDirectory, "CreatureType.cfg"), false),
				LivingTypeLoader.class, gson));

		this.livingGroupLoader = new LoadedFile(GsonHelper.readOrCreateFromGson(
				FileUtilities.createReader(new File(settingDirectory, "LivingGroups.cfg"), false),
				EntityGroupingLoader.class, gson));

		this.livingHandlerLoaders = new HashMap<String, ConfigLoader.LoadedFile<LivingHandlerLoader>>();
		File handlerFileFolder = new File(settingDirectory, "/" + DefaultProps.ENTITYHANDLERDIR);
		File[] files = FileUtilities.getFileInDirectory(handlerFileFolder, ".cfg");
		for (File livingFile : files) {
			livingHandlerLoaders.put(
					livingFile.getName(),
					new LoadedFile(GsonHelper.readOrCreateFromGson(FileUtilities.createReader(livingFile, false),
							LivingHandlerLoader.class, gson)));
		}
	}

	public void saveToConfigs(File settingDirectory) {
		
	}
}