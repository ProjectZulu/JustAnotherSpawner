package jas.spawner.refactor.mvel;

import jas.spawner.modern.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;

public class LinkedMVELExpression<T> extends MVELExpression<T> {
	public final Operand spawnOperand;

	public LinkedMVELExpression(String expression) {
		this(expression, Operand.OR);
	}

	public LinkedMVELExpression(String expression, Operand operand) {
		super(expression);
		spawnOperand = Operand.OR;
	}
}
