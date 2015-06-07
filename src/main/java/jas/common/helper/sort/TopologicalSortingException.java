package jas.common.helper.sort;

import java.util.Set;

import net.minecraftforge.fml.common.toposort.ModSortingException;

public class TopologicalSortingException extends ModSortingException {

    public <T> TopologicalSortingException(String string, T node, Set<T> visitedNodes) {
        super(string, node, visitedNodes);
    }
}