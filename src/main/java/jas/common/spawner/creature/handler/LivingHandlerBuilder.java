package jas.common.spawner.creature.handler;

import java.util.Locale;

import com.google.gson.annotations.SerializedName;

import jas.common.spawner.creature.handler.LivingGroupRegistry.LivingGroup;
import jas.common.spawner.creature.type.CreatureTypeRegistry;

public class LivingHandlerBuilder {
    @SerializedName("Name")
    private String handlerId;
    @SerializedName("Type")
    private String creatureTypeId;
    @SerializedName("Enabled")
    private boolean shouldSpawn;
    @SerializedName("Tags")
    private String optionalParameters;

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
    }

    public LivingHandlerBuilder(LivingHandler handler) {
        this.handlerId = handler.groupID;
        this.creatureTypeId = handler.creatureTypeID;
        this.shouldSpawn = handler.shouldSpawn;
        this.optionalParameters = handler.optionalParameters;
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
        if (handlerId == null || !isEntityGroupDeclared(livingGroupRegistry)) {
            throw new IllegalArgumentException(
                    "Cannot build CreatureType instance with " + handlerId == null ? "null name" : "non-existant group");
        }
        if (creatureTypeRegistry.getCreatureType(creatureTypeId) == null) {
            creatureTypeId = CreatureTypeRegistry.NONE;
        }
        return new LivingHandler(creatureTypeRegistry, this);
    }

    private boolean isEntityGroupDeclared(LivingGroupRegistry livingGroupRegistry) {
        for (LivingGroup group : livingGroupRegistry.getEntityGroups()) {
            if (group.groupID.equalsIgnoreCase(handlerId)) {
                return true;
            }
        }
        return false;
    }
}
