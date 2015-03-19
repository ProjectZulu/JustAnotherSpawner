package jas.spawner.card;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.BiMap;

import jas.spawner.refactor.entities.Group.Groups;
import jas.spawner.refactor.entities.Mappings;

public class CardRegistry implements Groups<Card> {

	@Override
	public String key() {
		return null;
	}

	@Override
	public Map<String, Card> iDToGroup() {
		return null;
	}
}
