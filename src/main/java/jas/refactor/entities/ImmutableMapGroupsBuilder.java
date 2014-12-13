package jas.refactor.entities;

import jas.refactor.entities.Group.Groups;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class ImmutableMapGroupsBuilder<T extends Group> implements Groups {
	private HashMap<String, T> iDToGroup = new HashMap<String, T>();
	private String key;

	@Override
	public String key() {
		return key;
	}

	public ImmutableMapGroupsBuilder(String key) {
		this.key = key;
	}

	@Override
	public Map<String, T> iDToGroup() {
		return iDToGroup;
	}

	public void clear() {
		iDToGroup.clear();
	}

	public void addGroup(T group) {
		iDToGroup.put(group.iD(), group);
	}

	public ImmutableMap<String, T> build() {
		return ImmutableMap.<String, T> builder().putAll(iDToGroup).build();
	}
}
