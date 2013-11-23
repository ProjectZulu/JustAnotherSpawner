package jas.common.spawner.creature.handler;

import java.util.Locale;

import com.google.gson.annotations.SerializedName;

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

    public void setHandlerId(String handlerId) {
        this.handlerId = handlerId;
    }

    public String getHandlerId() {
        return handlerId;
    }

    public void setCreatureTypeId(String creatureTypeId) {
        if (creatureTypeId != null) {
            creatureTypeId = creatureTypeId.toUpperCase(Locale.ENGLISH);
        }
        this.creatureTypeId = creatureTypeId;
    }

    public String getCreatureTypeId() {
        return creatureTypeId;
    }

    public void setShouldSpawn(boolean shouldSpawn) {
        this.shouldSpawn = shouldSpawn;
    }

    public boolean getShouldSpawn() {
        return shouldSpawn;
    }

    public void setOptionalParameters(String optionalParameters) {
        if (optionalParameters == null) {
            optionalParameters = "";
        }
        this.optionalParameters = optionalParameters;
    }

    public String getOptionalParameters() {
        return optionalParameters;
    }

    public LivingHandler build(CreatureTypeRegistry creatureTypeRegistry) {
        if (handlerId == null) {
            throw new IllegalArgumentException("Cannot build CreatureType instance with null name");
        }
        if (creatureTypeRegistry.getCreatureType(creatureTypeId) == null) {
            creatureTypeId = CreatureTypeRegistry.NONE;
        }
        return new LivingHandler(creatureTypeRegistry, this);
    }
}
