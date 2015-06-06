package jas.spawner.refactor;

import jas.spawner.refactor.LivingTypeBuilder.LivingType;
import jas.spawner.refactor.configsloader.ConfigLoader;
import jas.spawner.refactor.configsloader.ConfigLoader.LoadedFile;
import jas.spawner.refactor.configsloader.LivingTypeLoader;

import java.util.Collection;
import java.util.HashMap;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class LivingTypes {
	/* DEFAULT Types */
	public static final String NONE = "NONE";
	public static final String CREATURE = "CREATURE";
	public static final String MONSTER = "MONSTER";
	public static final String AMBIENT = "AMBIENT";
	public static final String WATERCREATURE = "WATERCREATURE";
	public static final String UNDERGROUND = "UNDERGROUND";
	public static final String OPENSKY = "OPENSKY";

	public static final ImmutableSet<String> defaultTypes;
	static {
		Builder<String> builder = ImmutableSet.<String> builder();
		builder.add(NONE).add(CREATURE).add(MONSTER).add(AMBIENT).add(WATERCREATURE).add(UNDERGROUND).add(OPENSKY);
		defaultTypes = builder.build();
	}

	private ImmutableMap<String, LivingType> types;

	public ImmutableMap<String, LivingType> types() {
		return types;
	}

	public LivingType getLivingType(String typeID) {
		return types.get(typeID.toUpperCase());
	}

	public LivingTypes(ConfigLoader loader) {
		loadFromConfig(loader);
	}

	public void loadFromConfig(ConfigLoader loader) {
		Optional<Collection<LivingTypeBuilder>> read = loader.livingTypeLoader.saveObject.getTypes();

		HashMap<String, LivingTypeBuilder> readTypes = new HashMap<String, LivingTypeBuilder>();
		if (read.isPresent()) {
			for (LivingTypeBuilder creatureBuilder : read.get()) {
				if (creatureBuilder.livingTypeID != null) {
					readTypes.put(creatureBuilder.livingTypeID, creatureBuilder);
				}
			}
		} else {
			readTypes = new HashMap<String, LivingTypeBuilder>();
			LivingTypeBuilder monster = new LivingTypeBuilder(MONSTER, 70, 5, 0, "AIR",
					"||!liquid({0,0,0},{0,0,0})&&!liquid({0,0,0},{0,-1,0})&&normal({0,0,0},{0,1,0})");
			LivingTypeBuilder ambient = new LivingTypeBuilder(AMBIENT, 15, 5, 0, "AIR",
					"||!liquid({0,0,0},{0,0,0})&&!liquid({0,0,0},{0,-1,0})&&normal({0,0,0},{0,1,0})");
			LivingTypeBuilder watercreature = new LivingTypeBuilder(AMBIENT, 15, 5, 0, "WATER",
					"||!liquid({0,0,0},{0,0,0})&&!liquid({0,0,0},{0,-1,0})&&normal({0,0,0},{0,1,0})");
			LivingTypeBuilder underground = new LivingTypeBuilder(UNDERGROUND, 10, 5, 0, "AIR",
					"||!solidside(1,{0,0,0},{0,-1,0})&&liquid({0,0,0},{0,0,0})&&normal({0,0,0},{0,0,0})"
							+ "&&normal({0,0,0},{0,1,0})&&!opaque({0,0,0},{0,-1,0})&&sky()");
			LivingTypeBuilder opensky = new LivingTypeBuilder(OPENSKY, 10, 5, 0, "AIR",
					"||!solidside(1,{0,0,0},{0,-1,0})&&liquid({0,0,0},{0,0,0})&&normal({0,0,0},{0,0,0})"
							+ "&&normal({0,0,0},{0,1,0})&&!opaque({0,0,0},{0,-1,0})&&!sky()");
			LivingTypeBuilder creature = new LivingTypeBuilder(CREATURE, 10, 400, 0.1f, "AIR",
					"||!solidside(1,{0,0,0},{0,-1,0})&&liquid({0,0,0},{0,0,0})&&normal({0,0,0},{0,0,0})"
							+ "&&normal({0,0,0},{0,1,0})&&!opaque({0,0,0},{0,-1,0})&&!sky()");

			readTypes.put(monster.livingTypeID, monster);
			readTypes.put(ambient.livingTypeID, ambient);
			readTypes.put(opensky.livingTypeID, opensky);
			readTypes.put(creature.livingTypeID, creature);
			readTypes.put(underground.livingTypeID, underground);
			readTypes.put(watercreature.livingTypeID, watercreature);
		}
		ImmutableMap.Builder<String, LivingType> builder = ImmutableMap.<String, LivingType> builder();
		for (LivingTypeBuilder livingType : readTypes.values()) {
			builder.put(livingType.livingTypeID, livingType.build());
		}
		types = builder.build();
	}

	public void saveToConfig(ConfigLoader loader) {
		loader.livingTypeLoader = new LoadedFile(new LivingTypeLoader(this));
	}
}
