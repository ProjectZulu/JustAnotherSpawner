package jas.modern.modification;
//package jas.common.modification;
//
//import com.google.common.collect.ImmutableList;
//
//import jas.common.ImportedSpawnList;
//import jas.common.JustAnotherSpawner;
//import jas.common.spawner.biome.group.BiomeGroupRegistry;
//import jas.common.spawner.biome.group.BiomeGroupRegistry.BiomeGroup;
//import jas.common.spawner.biome.structure.StructureHandler;
//import jas.common.spawner.biome.structure.StructureHandlerRegistry;
//import jas.common.spawner.creature.entry.BiomeSpawnListRegistry;
//import jas.common.spawner.creature.entry.SpawnListEntryBuilder;
//import jas.common.spawner.creature.handler.LivingGroupRegistry;
//import jas.common.spawner.creature.handler.LivingHandler;
//import jas.common.spawner.creature.handler.LivingHandlerBuilder;
//import jas.common.spawner.creature.handler.LivingHandlerRegistry;
//
//public class ModRemoveLivingGroup extends BaseModification {
//
//	public final String groupName;
//
//	public ModRemoveLivingGroup(String groupName) {
//		this.groupName = groupName;
//	}
//
//	@Override
//	public void applyModification(LivingGroupRegistry registry) {
//		registry.removeLivingGroup(groupName);
//	}
//
//	@Override
//	public void applyModification(LivingHandlerRegistry registry) {
//		registry.removeLivingHandler(groupName);
//	}
//
//	@Override
//	public void applyModification(BiomeSpawnListRegistry registry) {
//		BiomeGroupRegistry biomeGroupRegistry = registry.biomeGroupRegistry;
//
//		for (BiomeGroup biomeGroup : biomeGroupRegistry.iDToGroup().values()) {
//			registry.removeSpawnListEntry(groupName, biomeGroup.groupID);
//		}
//	}
//
//	@Override
//	public void applyModification(StructureHandlerRegistry registry) {
//		ImmutableList<StructureHandler> structureHandlers = registry.handlers();
//		for (StructureHandler structureHandler : structureHandlers) {
//			structureHandler.removeSpawnListEntry(registry.livingHandlerRegistry, groupName);
//		}
//	}
//}
