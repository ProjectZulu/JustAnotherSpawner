package jas.refactor.ConfigLoader;

import jas.common.FileUtilities;
import jas.common.GsonHelper;

import java.io.File;
import java.lang.reflect.Type;

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

	public ConfigLoader(File settingDirectory) {
		Type[] types = new java.lang.reflect.Type[] { LivingTypeLoader.class, EntityGroupingLoader.class };
		Object[] serializers = new Object[] { new LivingTypeLoader.Serializer(), new EntityGroupingLoader.Serializer() };
		Gson gson = GsonHelper.createGson(true, types, serializers);

		this.livingTypeLoader = new LoadedFile(GsonHelper.readOrCreateFromGson(
				FileUtilities.createReader(new File(settingDirectory, "CreatureType.cfg"), false),
				LivingTypeLoader.class, gson));

		this.livingGroupLoader = new LoadedFile(GsonHelper.readOrCreateFromGson(
				FileUtilities.createReader(new File(settingDirectory, "LivingGroups.cfg"), false),
				EntityGroupingLoader.class, gson));
	}
}