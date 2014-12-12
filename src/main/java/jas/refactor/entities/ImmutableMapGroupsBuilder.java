package jas.refactor.entities;

import jas.refactor.entities.Groups.Group;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class ImmutableMapGroupsBuilder<T extends Group> implements Groups {
	public HashMap<String, T> iDToAttribute = new HashMap<String, T>();

	@Override
	public Map<String, T> iDToAttribute() {
		return null;
	}

	public void addGroup(T group) {
		iDToAttribute.put(group.iD(), group);
	}

	public ImmutableMap<String, T> build() {
		return ImmutableMap.<String, T> builder().putAll(iDToAttribute).build();
	}
}
