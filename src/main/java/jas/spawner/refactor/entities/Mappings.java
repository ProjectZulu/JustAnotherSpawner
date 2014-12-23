package jas.spawner.refactor.entities;

import java.util.Collection;

import net.minecraft.entity.EntityLiving;

import com.google.common.collect.BiMap;

public interface Mappings<K, M> {
	public Collection<M> newMappings();

	public BiMap<K, M> keyToMapping();

	public BiMap<M, K> mappingToKey();
}
