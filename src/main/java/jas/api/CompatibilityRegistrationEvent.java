package jas.api;

import cpw.mods.fml.common.eventhandler.Event;

public class CompatibilityRegistrationEvent extends Event {

    public final CompatibilityLoader loader;

    public CompatibilityRegistrationEvent(CompatibilityLoader loader) {
        this.loader = loader;
    }
}
