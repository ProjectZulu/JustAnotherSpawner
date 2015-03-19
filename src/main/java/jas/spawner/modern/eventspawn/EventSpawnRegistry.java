package jas.spawner.modern.eventspawn;

import jas.common.helper.FileUtilities;
import jas.common.helper.GsonHelper;
import jas.spawner.modern.DefaultProps;
import jas.spawner.modern.eventspawn.EventSpawnTrigger.EventTrigger;
import jas.spawner.modern.world.WorldProperties;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.mvel2.MVEL;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class EventSpawnRegistry {

	private EnumMap<EventTrigger, List<EventSpawn>> eventSpawns;

	public List<EventSpawn> getEventsForTrigger(EventTrigger trigger) {
		List<EventSpawn> list = eventSpawns.get(trigger);
		return list != null ? list : Collections.<EventSpawn> emptyList();
	}

	public static final class EventSpawn {
		private EventTrigger trigger;
		private String expression;
		private transient Serializable compiled;
		private transient String fileName;

		public EventSpawn() {
			trigger = EventTrigger.LIVING_DEATH;
			expression = "";
			fileName = "default";
		}

		public EventSpawn(EventTrigger trigger, String expression) {
			this.trigger = trigger;
			this.expression = expression;
			this.compiled = MVEL.compileExpression(expression);
			this.fileName = "default";
		}

		public Serializable expression() {
			if (compiled == null) {
				compiled = MVEL.compileExpression(expression);
			}
			return compiled;
		}
	}
	
	private WorldProperties worldProperties;

	public EventSpawnRegistry(WorldProperties worldProperties) {
		this.worldProperties = worldProperties;
	}

	public void loadFromConfig(File configDirectory) {
		this.eventSpawns = new EnumMap<EventTrigger, List<EventSpawn>>(EventTrigger.class);
		Gson gson = GsonHelper.createGson(true);
		File fileFolder = getFile(configDirectory, worldProperties.getFolderConfiguration().saveName, "");
		File[] files = FileUtilities.getFileInDirectory(fileFolder, ".cfg");
		for (File file : files) {
			Type listType = new TypeToken<ArrayList<EventSpawn>>() {
			}.getType();
			EventSpawn[] readEventSpawns = GsonHelper.readOrCreateFromGson(FileUtilities.createReader(file, false),
					EventSpawn[].class, gson);
			for (EventSpawn eventSpawn : readEventSpawns) {
				eventSpawn.fileName = file.getName();
				List<EventSpawn> events = eventSpawns.get(eventSpawn.trigger);
				if (events == null) {
					events = new ArrayList<EventSpawnRegistry.EventSpawn>();
					eventSpawns.put(eventSpawn.trigger, events);
				}
				events.add(eventSpawn);
			}
		}
		if (eventSpawns.isEmpty()) {
			List<EventSpawn> defaultSleepSpawn = new ArrayList<EventSpawn>();
			defaultSleepSpawn.add(new EventSpawn(EventTrigger.SLEEP,
					"if(false){ spawn('Zombie').offset(5).alsoSpawn('Zombie',0,0,0) }"));
			eventSpawns.put(EventTrigger.SLEEP, defaultSleepSpawn);
		}
	}

	private File getFile(File configDirectory, String saveName, String fileName) {
		String filePath = DefaultProps.WORLDSETTINGSDIR.concat(saveName).concat("/").concat(DefaultProps.EVENTSPAWNDIR);
		if (fileName != null && !fileName.equals("")) {
			filePath = filePath.concat(fileName);
		}
		return new File(configDirectory, filePath);
	}

	public void saveToConfig(File configDirectory) {
		Gson gson = GsonHelper.createGson(true);
		HashMap<String, ArrayList<EventSpawn>> fileNameToEventSpawns = new HashMap<String, ArrayList<EventSpawn>>();
		for (List<EventSpawn> eventSpawns : this.eventSpawns.values()) {
			for (EventSpawn eventSpawn : eventSpawns) {
				ArrayList<EventSpawn> fileSpawns = fileNameToEventSpawns.get(eventSpawn.fileName);
				if (fileSpawns == null) {
					fileSpawns = new ArrayList<EventSpawnRegistry.EventSpawn>();
					fileNameToEventSpawns.put(eventSpawn.fileName, fileSpawns);
				}
				fileSpawns.add(eventSpawn);
			}
		}

		for (Entry<String, ArrayList<EventSpawn>> entry : fileNameToEventSpawns.entrySet()) {
			File file = getFile(configDirectory, worldProperties.getFolderConfiguration().saveName, entry.getKey());
			GsonHelper.writeToGson(FileUtilities.createWriter(file, true), entry.getValue(), gson);
		}
	}
}
