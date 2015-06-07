package jas.api;

import net.minecraftforge.fml.common.eventhandler.Event;


public class CompatibilityRegistrationEvent extends Event {

    public final CompatibilityLoader loader;

    public CompatibilityRegistrationEvent(CompatibilityLoader loader) {
        this.loader = loader;
    }
}
