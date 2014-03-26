package jas.api;

import net.minecraftforge.event.Event;

public class CompatibilityRegistrationEvent extends Event {

    public final CompatibilityLoader loader;

    public CompatibilityRegistrationEvent(CompatibilityLoader loader) {
        this.loader = loader;
    }
}
