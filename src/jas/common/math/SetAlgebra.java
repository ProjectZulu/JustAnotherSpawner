package jas.common.math;

import java.util.Collection;
import java.util.Set;

public class SetAlgebra<T> {

    public enum OPERATION {
        UNION, INTERSECT, COMPLEMENT;
    }

    public static <T> Set<T> operate(Set<T> leftSet, Set<T> rightSet, OPERATION operation) {
        switch (operation) {
        case UNION:
            leftSet.addAll(rightSet);
            break;
        case COMPLEMENT:
            leftSet.removeAll(rightSet);
            break;
        case INTERSECT:
            leftSet.retainAll(rightSet);
            break;
        }
        return leftSet;
    }

    public static <T> Set<T> operate(Collection<T> leftSet, Collection<T> rightSet, Set<T> result, OPERATION operation) {
        switch (operation) {
        case UNION:
            result.addAll(leftSet);
            result.addAll(rightSet);
            break;
        case COMPLEMENT:
            result.addAll(leftSet);
            result.removeAll(rightSet);
            break;
        case INTERSECT:
            result.addAll(leftSet);
            result.retainAll(rightSet);
            break;
        }
        return result;
    }
}
