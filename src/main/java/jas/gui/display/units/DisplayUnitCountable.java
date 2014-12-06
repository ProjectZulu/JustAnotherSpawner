package jas.gui.display.units;

import jas.gui.utilities.Coord;

public interface DisplayUnitCountable extends DisplayUnit {
    public abstract int getCount();

    public boolean isAnalogEnabled();

    public abstract void enableAnalogDisplay(boolean enable);

    public void setAnalogOffset(Coord coord);

    public Coord getAnalogOffset();

    public boolean isDigitalEnabled();

    public abstract void enableDigitalCounter(boolean enable);

    public void setDigitalOffset(Coord coord);

    public Coord getDigitalOffset();
}
