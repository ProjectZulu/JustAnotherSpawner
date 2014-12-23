package jas.spawner.refactor.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import jas.spawner.refactor.entities.Group.MutableGroup;
import jas.spawner.refactor.mvel.MVELExpression;

public class LivingHandlerBuilder implements MutableGroup {
	private String livingHandlerID;
	private String canSpawn;
	private String canDspwn;
	private String shouldInstantDspwn;
	private String dieOfAge;
	private String resetAge;
	private String postSpawn;
	private transient Set<String> results = new HashSet<String>();
	/** String Used to Build Group Content Names i.e. {desert,A|Forest,glacier} */
	private ArrayList<String> contents;

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

	public static class LivingHandler implements Group {
		public final String livingHandlerID;
		public final MVELExpression<Boolean> canSpawn;
		public final MVELExpression<Boolean> canDspwn;
		public final MVELExpression<Boolean> shouldInstantDspwn;
		public final MVELExpression<Boolean> postSpawn;
		public final MVELExpression<Boolean> isDspnbleAge;
		public final MVELExpression<Boolean> shouldResetAge;
		private final ImmutableSet<String> results;
		/** String Used to Build Group Content Names i.e. {desert,A|Forest,glacier} */
		private final ImmutableList<String> contents;

		private LivingHandler(LivingHandlerBuilder builder) {
			this.livingHandlerID = builder.livingHandlerID;
			this.canSpawn = new MVELExpression<Boolean>(builder.getCanSpawn());
			this.canDspwn = new MVELExpression<Boolean>(builder.getCanDspwn());
			this.shouldInstantDspwn = new MVELExpression<Boolean>(builder.getShouldInstantDspwn());
			this.postSpawn = new MVELExpression<Boolean>(builder.getPostSpawn());
			this.isDspnbleAge = new MVELExpression<Boolean>(builder.getIsDspnbleAge());
			this.shouldResetAge = new MVELExpression<Boolean>(builder.getShouldResetAge());
			this.results = ImmutableSet.<String> builder().addAll(builder.results()).build();
			this.contents = ImmutableList.<String> builder().addAll(builder.results()).build();
		}

		@Override
		public String iD() {
			return livingHandlerID;
		}

		@Override
		public Set<String> results() {
			return results;
		}

		@Override
		public List<String> contents() {
			return contents;
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

	@Override
	public String iD() {
		return livingHandlerID;
	}

	@Override
	public Set<String> results() {
		return results;
	}

	@Override
	public List<String> contents() {
		return contents;
	}

	@Override
	public void setResults(Set<String> results) {
		this.results = new HashSet<String>(results);
	}

	@Override
	public void setContents(List<String> contents) {
		this.contents = new ArrayList<String>(contents);
	}
}
