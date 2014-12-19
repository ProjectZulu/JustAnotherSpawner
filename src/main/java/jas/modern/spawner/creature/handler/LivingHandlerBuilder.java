package jas.modern.spawner.creature.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;

import jas.modern.JASLog;
import jas.modern.math.SetAlgebra;
import jas.modern.math.SetAlgebra.OPERATION;
import jas.modern.spawner.creature.handler.LivingGroupRegistry.LivingGroup;
import jas.modern.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;
import jas.modern.spawner.creature.type.CreatureTypeRegistry;

public class LivingHandlerBuilder {
	private String handlerId;
	private String creatureTypeId;
	private boolean shouldSpawn;
	private String spawnExpression;
	private String despawnExpression;
	private String instantdespawnExpression;
	private String postspawnExpression;
	private String entityExpression;

	private Optional<Integer> maxDespawnRange;
	private Optional<Integer> entityCap;
	private Optional<Integer> minDespawnRange;
	private Optional<Integer> despawnAge;
	private Optional<Integer> despawnRate;
	private Optional<Operand> spawnOperand;

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
		setSpawnExpression("", Optional.<Operand> absent());
		setDespawnExpression("");
		setInstantDespawnExpression("");
		setPostSpawnExpression("");
		setEntityExpression("");
		contents = new ArrayList<String>(5);
		namedJASSpawnables = new HashSet<String>();
		this.maxDespawnRange = Optional.absent();
		this.entityCap = Optional.absent();
		this.minDespawnRange = Optional.absent();
		this.despawnAge = Optional.absent();
		this.despawnRate = Optional.absent();
	}

	public LivingHandlerBuilder(LivingHandler handler) {
		this.handlerId = handler.livingID;
		this.creatureTypeId = handler.creatureTypeID;
		this.shouldSpawn = handler.shouldSpawn;
		setSpawnExpression(handler.spawnExpression, handler.spawnOperand);
		setDespawnExpression(handler.despawnExpression);
		setInstantDespawnExpression(handler.instantdespawnExpression);
		setPostSpawnExpression(handler.postspawnExpression);
		setEntityExpression(handler.entityExpression);
		this.contents = new ArrayList<String>(handler.contents);
		this.namedJASSpawnables = new HashSet<String>(handler.namedJASSpawnables);
		this.maxDespawnRange = handler.maxDespawnRange;
		this.entityCap = handler.entityCap;
		this.minDespawnRange = handler.minDespawnDistance;
		this.despawnAge = handler.despawnAge;
		this.despawnRate = handler.despawnRate;
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

	public LivingHandlerBuilder setSpawnExpression(String optionalParameters, Optional<Operand> spawnOperand) {
		if (optionalParameters == null || optionalParameters.trim().equals("")) {
			this.spawnExpression = "";
			this.spawnOperand = Optional.absent();
		} else {
			if (!spawnOperand.isPresent()) {
				spawnOperand = Optional.of(Operand.OR);
			}
			this.spawnExpression = optionalParameters;
			this.spawnOperand = spawnOperand;
		}
		return this;
	}

	public String getSpawnExpression() {
		return spawnExpression;
	}

	public Optional<Operand> getSpawnOperand() {
		return spawnOperand;
	}

	public LivingHandlerBuilder setDespawnExpression(String optionalParameters) {
		if (optionalParameters == null) {
			optionalParameters = "";
		}
		this.despawnExpression = optionalParameters;
		return this;
	}

	public String getDespawnExpression() {
		return despawnExpression;
	}
	
	public LivingHandlerBuilder setInstantDespawnExpression(String optionalParameters) {
		if (optionalParameters == null) {
			optionalParameters = "";
		}
		this.instantdespawnExpression = optionalParameters;
		return this;
	}

	public String getInstantDespawnExpression() {
		return instantdespawnExpression;
	}
	
	public LivingHandlerBuilder setPostSpawnExpression(String optionalParameters) {
		if (optionalParameters == null) {
			optionalParameters = "";
		}
		this.postspawnExpression = optionalParameters;
		return this;
	}

	public String getPostSpawnExpression() {
		return postspawnExpression;
	}
	
	public LivingHandlerBuilder setEntityExpression(String entityExpression) {
		if (entityExpression == null) {
			entityExpression = "";
		}

		this.entityExpression = entityExpression;
		return this;
	}

	public String getEntityExpression() {
		return entityExpression;
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

	public LivingHandlerBuilder setMaxDespawnRange(int maxDespawnRange) {
		this.maxDespawnRange = maxDespawnRange < 0 ? Optional.<Integer> absent() : Optional.of(maxDespawnRange);
		return this;
	}

	public Optional<Integer> getMaxDespawnRange() {
		return maxDespawnRange;
	}

	public LivingHandlerBuilder setMinDespawnRange(int minDespawnRange) {
		this.minDespawnRange = minDespawnRange < 0 ? Optional.<Integer> absent() : Optional.of(minDespawnRange);
		return this;
	}

	public Optional<Integer> getMinDespawnRange() {
		return minDespawnRange;
	}

	public LivingHandlerBuilder setEntityCap(int entityCap) {
		this.entityCap = entityCap < 0 ? Optional.<Integer> absent() : Optional.of(entityCap);
		return this;
	}

	public Optional<Integer> getEntityCap() {
		return entityCap;
	}

	public LivingHandlerBuilder setDespawnAge(int despawnAge) {
		this.despawnAge = despawnAge < 0 ? Optional.<Integer> absent() : Optional.of(despawnAge);
		return this;
	}

	public Optional<Integer> getDespawnAge() {
		return despawnAge;
	}

	public LivingHandlerBuilder setDespawnRate(int despawnRate) {
		this.despawnRate = despawnRate < 0 ? Optional.<Integer> absent() : Optional.of(despawnRate);
		return this;
	}

	public Optional<Integer> getDespawnRate() {
		return despawnRate;
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
