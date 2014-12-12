package jas.refactor.mvel;

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
}
