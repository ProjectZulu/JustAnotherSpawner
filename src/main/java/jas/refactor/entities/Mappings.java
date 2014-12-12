package jas.refactor.entities;

import java.util.Collection;

import net.minecraft.entity.EntityLiving;

import com.google.common.collect.BiMap;

public interface Mappings {
	public Collection<String> newMappings();

	public BiMap<Class<? extends EntityLiving>, String> entityClasstoJASName();

	public BiMap<String, Class<? extends EntityLiving>> jASNametoEntityClass();
}
