package jas.spawner.refactor.mvel;

import jas.common.JASLog;
import java.io.Serializable;
import org.mvel2.MVEL;
import com.google.common.base.Optional;

public class MVELExpression<T> {
	public final String expression;
	public final Optional<Serializable> compiled;

	public MVELExpression(String expression) {
		this.expression = expression;
		if (expression != null && !expression.trim().equals("")) {
			compiled = Optional.of(MVEL.compileExpression(expression));
		} else {
			compiled = Optional.absent();
		}
	}

	public T evaluate(Object contextObject, String... errorMessage) {
		try {
			return (T) MVEL.executeExpression(compiled, contextObject);
		} catch (RuntimeException e) {
			for (String error : errorMessage) {
				JASLog.log().severe(error);
			}
			throw e;
		}
	}

	public boolean isPresent() {
		return compiled.isPresent();
	}

	public static <T> Optional<T> execute(MVELExpression<T> expression, Object contextObject, String... errorMessage) {
		if (expression.isPresent()) {
			try {
				Object value = MVEL.executeExpression(expression.compiled.get(), contextObject);
				return value != null ? Optional.of((T) value) : Optional.<T> absent();
			} catch (RuntimeException e) {
				for (String error : errorMessage) {
					JASLog.log().severe(error);
				}
				throw e;
			}
		}
		return Optional.<T> absent();
	}
}
