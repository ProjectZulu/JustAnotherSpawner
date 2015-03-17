package jas.spawner.modern.spawner.tags;

public interface TimeFunctions {
	public int timeOfDay();

	public boolean isMidnight();

	public boolean isAfternoon();

	public boolean isDawn();

	public boolean isDusk();

	public int moonPhase();

	public boolean isFullMoon();

	public boolean isWaningGibbous();

	public boolean isLastQuarter();

	public boolean isWaningCrescent();

	public boolean isNewMoon();

	public boolean isWaxingCrescent();

	public boolean isFirstQuarter();

	public boolean isWaxingGibbous();
}
