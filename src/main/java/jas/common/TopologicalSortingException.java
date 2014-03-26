package jas.common;

import java.util.Set;

import cpw.mods.fml.common.toposort.ModSortingException;

public class TopologicalSortingException extends ModSortingException {

    public <T> TopologicalSortingException(String string, T node, Set<T> visitedNodes) {
        super(string, node, visitedNodes);
    }
}