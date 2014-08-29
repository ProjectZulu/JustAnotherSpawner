package jas.common.spawner.creature.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;

import jas.common.JASLog;
import jas.common.math.SetAlgebra;
import jas.common.math.SetAlgebra.OPERATION;
import jas.common.spawner.creature.handler.LivingGroupRegistry.LivingGroup;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

public class LivingHandlerBuilder {
	private String handlerId;
	private String creatureTypeId;
	private boolean shouldSpawn;
	private String optionalParameters;
	public List<String> contents; // Raw Input, builds namedJASSpawnables, i.e Bat,A|Beast,-Boar
	private transient Set<String> namedJASSpawnables; // Resulting list of entities that this LH should be able to spawn

	public Set<String> getNamedJASSpawnables() {
		return Collections.unmodifiableSet(namedJASSpawnables);
	}

	public LivingHandlerBuilder() {
		this((String) null);
	}

	public LivingHandlerBuilder(String handlerID) {
		this(handlerID, CreatureTypeRegistry.NONE);
	}

	public LivingHandlerBuilder(String handlerID, String creatureTypeId) {
		setHandlerId(handlerID);
		setCreatureTypeId(creatureTypeId);
		setShouldSpawn(true);
		setOptionalParameters("");
		contents = new ArrayList<String>(5);
		namedJASSpawnables = new HashSet<String>();
	}

	public LivingHandlerBuilder(LivingHandler handler) {
		this.handlerId = handler.livingID;
		this.creatureTypeId = handler.creatureTypeID;
		this.shouldSpawn = handler.shouldSpawn;
		this.optionalParameters = handler.optionalParameters;
		this.contents = new ArrayList<String>(handler.contents);
		this.namedJASSpawnables = new HashSet<String>(handler.namedJASSpawnables);
	}

	public LivingHandlerBuilder setHandlerId(String handlerId) {
		this.handlerId = handlerId;
		return this;
	}

	public String getHandlerId() {
		return handlerId;
	}

	public LivingHandlerBuilder setCreatureTypeId(String creatureTypeId) {
		if (creatureTypeId != null) {
			creatureTypeId = creatureTypeId.toUpperCase(Locale.ENGLISH);
		}
		this.creatureTypeId = creatureTypeId;
		return this;
	}

	public String getCreatureTypeId() {
		return creatureTypeId;
	}

	public LivingHandlerBuilder setShouldSpawn(boolean shouldSpawn) {
		this.shouldSpawn = shouldSpawn;
		return this;
	}

	public boolean getShouldSpawn() {
		return shouldSpawn;
	}

	public LivingHandlerBuilder setOptionalParameters(String optionalParameters) {
		if (optionalParameters == null) {
			optionalParameters = "";
		}
		this.optionalParameters = optionalParameters;
		return this;
	}

	public String getOptionalParameters() {
		return optionalParameters;
	}

	public LivingHandler build(CreatureTypeRegistry creatureTypeRegistry, LivingGroupRegistry livingGroupRegistry) {
		if (handlerId == null) {
			throw new IllegalArgumentException("Cannot build CreatureType instance with null name");
		}
		if (creatureTypeRegistry.getCreatureType(creatureTypeId) == null) {
			creatureTypeId = CreatureTypeRegistry.NONE;
		}
		namedJASSpawnables = createSpawnableListFromContents(livingGroupRegistry);
		return new LivingHandler(creatureTypeRegistry, this);
	}

	public void parseContensForSpawnableList() {
		// namedJASSpawnables
	}

	/**
	 * Evaluate build instructions (i.e. A|allbiomes,&Jungle) of group and evaluate them into jasNames
	 */
	private Set<String> createSpawnableListFromContents(LivingGroupRegistry livingGroupRegistry) {
		Set<String> namedSpawnables = new HashSet<String>();
		/* Evaluate contents and fill in jasNames */
		for (String contentComponent : contents) {
			OPERATION operation;
			if (contentComponent.startsWith("-")) {
				contentComponent = contentComponent.substring(1);
				operation = OPERATION.COMPLEMENT;
			} else if (contentComponent.startsWith("&")) {
				contentComponent = contentComponent.substring(1);
				operation = OPERATION.INTERSECT;
			} else {
				operation = OPERATION.UNION;
				if (contentComponent.startsWith("+")) {
					contentComponent = contentComponent.substring(1);
				}
			}

			if (contentComponent.startsWith("G|")) {
				JASLog.log().severe("Error processing %s content from %s. G| is no longer supported.", handlerId,
						contents, contentComponent);
				continue;
			} else if (contentComponent.startsWith("A|")) {
				LivingGroup groupToAdd = livingGroupRegistry.iDToAttribute().get(contentComponent.substring(2));
				if (groupToAdd != null) {
					SetAlgebra.operate(namedSpawnables, groupToAdd.entityJASNames(), operation);
					continue;
				}
			} else if (livingGroupRegistry.jasNametoEntityClass().containsKey(contentComponent)) {
				SetAlgebra.operate(namedSpawnables, Sets.newHashSet(contentComponent), operation);
				continue;
			}
			JASLog.log().severe("Error processing %s content from %s. The component %s does not exist.", handlerId,
					contents, contentComponent);
		}
		return namedSpawnables;
	}

	// private boolean isEntityGroupDeclared(LivingGroupRegistry livingGroupRegistry) {
	// for (LivingGroup group : livingGroupRegistry.getEntityGroups()) {
	// if (group.groupID.equalsIgnoreCase(handlerId)) {
	// return true;
	// }
	// }
	// return false;
	// }
}
