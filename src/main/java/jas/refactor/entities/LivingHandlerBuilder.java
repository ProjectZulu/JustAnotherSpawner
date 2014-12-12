package jas.refactor.entities;

import jas.refactor.mvel.MVELExpression;

public class LivingHandlerBuilder {
	private String livingHandlerID;
	private String canSpawn;
	private String canDspwn;
	private String shouldInstantDspwn;
	private String dieOfAge;
	private String resetAge;
	private String postSpawn;

	public LivingHandlerBuilder(String livingHandlerID) {
		this.livingHandlerID = livingHandlerID;
		this.canSpawn = "!(modspawn || sp.clearBounding)";
		this.canDspwn = "false";
		this.shouldInstantDspwn = "sp.plyrDist < 128";
		this.dieOfAge = "!(ent.age > 600 && util.random(1+40/3,0,0))";
		this.resetAge = "sp.plyrDist > 32";
		this.postSpawn = "";
	}

	public LivingHandlerBuilder(LivingHandler livingHandler) {
		this.livingHandlerID = livingHandler.livingHandlerID;
		this.canSpawn = livingHandler.canSpawn.expression;
		this.canDspwn = livingHandler.canDspwn.expression;
		this.shouldInstantDspwn = livingHandler.shouldInstantDspwn.expression;
		this.dieOfAge = livingHandler.isDspnbleAge.expression;
		this.resetAge = livingHandler.shouldResetAge.expression;
		this.postSpawn = livingHandler.postSpawn.expression;
	}

	public static class LivingHandler {
		public final String livingHandlerID;
		public final MVELExpression<Boolean> canSpawn;
		public final MVELExpression<Boolean> canDspwn;
		public final MVELExpression<Boolean> shouldInstantDspwn;
		public final MVELExpression<Boolean> postSpawn;
		public final MVELExpression<Boolean> isDspnbleAge;
		public final MVELExpression<Boolean> shouldResetAge;

		private LivingHandler(LivingHandlerBuilder builder) {
			this.livingHandlerID = builder.livingHandlerID;
			this.canSpawn = new MVELExpression<Boolean>(builder.getCanSpawn());
			this.canDspwn = new MVELExpression<Boolean>(builder.getCanDspwn());
			this.shouldInstantDspwn = new MVELExpression<Boolean>(builder.getShouldInstantDspwn());
			this.postSpawn = new MVELExpression<Boolean>(builder.getPostSpawn());
			this.isDspnbleAge = new MVELExpression<Boolean>(builder.getIsDspnbleAge());
			this.shouldResetAge = new MVELExpression<Boolean>(builder.getShouldResetAge());
		}
	}

	public LivingHandler build() {
		return new LivingHandler(this);
	}

	public String getLivingHandlerID() {
		return livingHandlerID;
	}

	public String getCanSpawn() {
		return canSpawn;
	}

	public void setCanSpawn(String canSpawn) {
		this.canSpawn = canSpawn;
	}

	public String getCanDspwn() {
		return canDspwn;
	}

	public void setCanDspwn(String canDspwn) {
		this.canDspwn = canDspwn;
	}

	public String getShouldInstantDspwn() {
		return shouldInstantDspwn;
	}

	public void setShouldInstantDspwn(String shouldInstantDspwn) {
		this.shouldInstantDspwn = shouldInstantDspwn;
	}

	public String getPostSpawn() {
		return postSpawn;
	}

	public void setPostSpawn(String postSpawn) {
		this.postSpawn = postSpawn;
	}

	public String getIsDspnbleAge() {
		return dieOfAge;
	}

	public void setIsDspnbleAge(String isDspnbleAge) {
		this.dieOfAge = isDspnbleAge;
	}

	public String getShouldResetAge() {
		return resetAge;
	}

	public void setShouldResetAge(String shouldResetAge) {
		this.resetAge = shouldResetAge;
	}
}
