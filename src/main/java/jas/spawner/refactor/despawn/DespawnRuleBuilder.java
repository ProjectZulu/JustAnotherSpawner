package jas.spawner.refactor.despawn;

import jas.spawner.refactor.entities.Group;
import jas.spawner.refactor.entities.Group.MutableContentGroup;
import jas.spawner.refactor.entities.Group.Parser.LivingContext;
import jas.spawner.refactor.entities.Group.Parser.ResultsBuilder;
import jas.spawner.refactor.entities.LivingMappings;
import jas.spawner.refactor.mvel.MVELExpression;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

public class DespawnRuleBuilder implements MutableContentGroup<String> {
	private String canDspwn;
	private String shouldInstantDspwn;
	private String dieOfAge;
	private String resetAge;

	/** Entity Mappings this DespawnRule builder applies to. Derived from Contents */
	private transient Set<String> results;
	/** Expression used to determine applicable entities i.e. {Bat,A|Ugly,Cat} */
	private String contents;

	public DespawnRuleBuilder(String entityExpression) {
		this.canDspwn = "false";
		this.shouldInstantDspwn = "sp.plyrDist < 128";
		this.dieOfAge = "!(ent.age > 600 && util.random(1+40/3,0,0))";
		this.resetAge = "sp.plyrDist > 32";
		this.contents = entityExpression;
	}

	public DespawnRuleBuilder(DespawnRule rule) {
		this.canDspwn = rule.canDspwn.get().expression;
		this.shouldInstantDspwn = rule.shouldInstantDspwn.get().expression;
		this.dieOfAge = rule.dieOfAge.get().expression;
		this.resetAge = rule.resetAge.get().expression;
		this.contents = rule.contents;
	}

	public static class DespawnRule implements ContentGroup<String> {
		public final Optional<MVELExpression<String>> canDspwn;
		public final Optional<MVELExpression<String>> shouldInstantDspwn;
		public final Optional<MVELExpression<String>> dieOfAge;
		public final Optional<MVELExpression<String>> resetAge;
		/** Entity Mappings this DespawnRule builder applies to */
		public final transient ImmutableSet<String> results;
		/** Expression used to determine applicable entities i.e. {Bat,A|Ugly,Cat} */
		public final transient String contents;

		public DespawnRule(DespawnRuleBuilder builder) {
			if (builder.getCanDespawnExp() != null && !builder.getCanDespawnExp().trim().equals("")) {
				this.canDspwn = Optional.of(new MVELExpression<String>(builder.getCanDespawnExp()));
			} else {
				this.canDspwn = Optional.absent();
			}
			if (builder.getInstantDspwnExp() != null && !builder.getInstantDspwnExp().trim().equals("")) {
				this.shouldInstantDspwn = Optional.of(new MVELExpression<String>(builder.getInstantDspwnExp()));
			} else {
				this.shouldInstantDspwn = Optional.absent();
			}
			if (builder.getAgeDeathExp() != null && !builder.getAgeDeathExp().trim().equals("")) {
				this.dieOfAge = Optional.of(new MVELExpression<String>(builder.getAgeDeathExp()));
			} else {
				this.dieOfAge = Optional.absent();
			}
			if (builder.getResetAgeExp() != null && !builder.getResetAgeExp().trim().equals("")) {
				this.resetAge = Optional.of(new MVELExpression<String>(builder.getResetAgeExp()));
			} else {
				this.resetAge = Optional.absent();
			}
			this.results = ImmutableSet.<String> builder().addAll(builder.results()).build();
			this.contents = builder.content();
		}

		@Override
		public String iD() {
			return contents;
		}

		@Override
		public Set<String> results() {
			return results;
		}

		@Override
		public String content() {
			return contents;
		}

	}

	public DespawnRule build(LivingMappings mappings, Groups attributes, Groups handlers) {
		LivingContext context = new Group.Parser.LivingContext(mappings, null, attributes, handlers);
		ResultsBuilder entResult = new MVELExpression<ResultsBuilder>(this.content()).evaluate(context, "");
		this.setResults(entResult.resultMappings);
		return new DespawnRule(this);
	}

	@Override
	public String iD() {
		return contents;
	}

	@Override
	public void setContents(String expression) {
		this.contents = expression;
	}

	@Override
	public String content() {
		return contents;
	}

	@Override
	public Set<String> results() {
		return results;
	}

	@Override
	public void setResults(Set<String> results) {
		this.results = new HashSet<String>(results);
	}

	public String getCanDespawnExp() {
		return canDspwn;
	}

	public DespawnRuleBuilder setCanDespawnExp(String canDspwn) {
		this.canDspwn = canDspwn;
		return this;
	}

	public String getInstantDspwnExp() {
		return shouldInstantDspwn;
	}

	public DespawnRuleBuilder setInstantDspwnExp(String shouldInstantDspwn) {
		this.shouldInstantDspwn = shouldInstantDspwn;
		return this;
	}

	public String getAgeDeathExp() {
		return dieOfAge;
	}

	public DespawnRuleBuilder setAgeDeathExp(String dieOfAge) {
		this.dieOfAge = dieOfAge;
		return this;
	}

	public String getResetAgeExp() {
		return resetAge;
	}

	public DespawnRuleBuilder setResetAgeExp(String resetAge) {
		this.resetAge = resetAge;
		return this;
	}
}
