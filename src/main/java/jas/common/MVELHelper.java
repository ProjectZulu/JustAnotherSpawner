package jas.common;

import jas.common.spawner.Tags;

import org.mvel2.MVEL;

public class MVELHelper {

	public static boolean executeExpression(Object compiledExpression, Tags contextObject, String... errorMessage) {
		return typedExecuteExpression(Boolean.class, compiledExpression, contextObject, errorMessage);
	}

	public static <T> T typedExecuteExpression(Class<T> typeClass, Object compiledExpression, Tags contextObject,
			String... errorMessage) {
		try {
			return (T) MVEL.executeExpression(compiledExpression, contextObject);
		} catch (RuntimeException e) {
			for (String error : errorMessage) {
				JASLog.log().severe(error);
			}
			throw e;
		}
	}
}
