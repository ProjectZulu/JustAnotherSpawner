package jas.modern.modification;
//package jas.common.modification;
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
//import jas.common.spawner.creature.handler.LivingGroupRegistry.LivingGroup;
//import jas.common.spawner.creature.type.CreatureTypeRegistry;
//
//import java.util.ArrayList;
//import java.util.Collection;
//
//import net.minecraft.entity.EntityLiving;
//
//import com.google.common.base.Optional;
//import com.google.common.collect.ImmutableList;
//
//public class ModUpdateLivingGroup extends BaseModification {
//	public final String oldGroupID;
//	public final String newGroupID;
//	public final Optional<String> configName;
//	public final ArrayList<String> contents;
//	private LivingGroup livingGroup = null;
//
//	public ModUpdateLivingGroup(String groupName, ArrayList<String> contents) {
//		this(groupName, groupName, null, contents);
//	}
//
//	public ModUpdateLivingGroup(String oldGroupName, String groupName, String configName, ArrayList<String> contents) {
//		this.oldGroupID = oldGroupName;
//		this.newGroupID = groupName;
//		this.configName = configName != null ? Optional.of(configName) : Optional.<String> absent();
//		this.contents = contents;
//	}
//
//	@Override
//	public void applyModification(LivingGroupRegistry registry) {
//		if (configName.isPresent()) {
//			livingGroup = new LivingGroup(newGroupID, configName.get(), contents);
//		} else {
//			livingGroup = new LivingGroup(newGroupID, contents);
//		}
//		registry.updateLivingGroup(livingGroup);
//	}
//
//	@Override
//	public void applyModification(LivingHandlerRegistry registry) {
//		if (!oldGroupID.equalsIgnoreCase(newGroupID)) {
//			LivingHandler newHandler = registry.getLivingHandler(oldGroupID);
//			LivingHandlerBuilder newBuilder = new LivingHandlerBuilder(newHandler).setHandlerId(newGroupID);
//			registry.removeLivingHandler(oldGroupID);
//			registry.addLivingHandler(newBuilder);
//		}
//	}
//
//	@Override
//	public void applyModification(BiomeSpawnListRegistry registry) {
//		BiomeGroupRegistry biomeGroupRegistry = registry.biomeGroupRegistry;
//		LivingGroupRegistry livingGroupRegistry = registry.livingGroupRegistry;
//		LivingHandlerRegistry livingHandlerRegistry = registry.livingHandlerRegistry;
//		LivingHandler handler = livingHandlerRegistry.getLivingHandler(livingGroup.groupID);
//
//		ImportedSpawnList importedSpawnList = JustAnotherSpawner.importedSpawnList();
//		for (BiomeGroup biomeGroup : biomeGroupRegistry.iDToGroup().values()) {
//			registry.removeSpawnListEntry(oldGroupID, biomeGroup.groupID);
//			SpawnListEntryBuilder spawnListEntry = findVanillaSpawnListEntry(biomeGroup, livingGroup,
//					importedSpawnList, biomeGroupRegistry, livingGroupRegistry);
//			registry.addSpawnListEntry(spawnListEntry);
//		}
//	}
//
//	@Override
//	public void applyModification(StructureHandlerRegistry registry) {
//		if (livingGroup.contents().isEmpty()) {
//			ImmutableList<StructureHandler> structureHandlers = registry.handlers();
//			for (StructureHandler structureHandler : structureHandlers) {
//				structureHandler.removeSpawnListEntry(registry.livingHandlerRegistry, livingGroup.groupID);
//			}
//		}
//	}
//
//	private SpawnListEntryBuilder findVanillaSpawnListEntry(BiomeGroup group, LivingGroup livingGroup,
//			ImportedSpawnList importedSpawnList, BiomeGroupRegistry biomeGroupRegistry,
//			LivingGroupRegistry livingGroupRegistry) {
//		for (String pckgNames : group.getBiomeNames()) {
//			for (Integer biomeID : biomeGroupRegistry.pckgNameToBiomeID().get(pckgNames)) {
//				Collection<net.minecraft.world.biome.BiomeGenBase.SpawnListEntry> spawnListEntries = importedSpawnList
//						.getSpawnableCreatureList(biomeID);
//				for (String jasName : livingGroup.entityJASNames()) {
//					Class<? extends EntityLiving> livingClass = livingGroupRegistry.JASNametoEntityClass.get(jasName);
//					for (net.minecraft.world.biome.BiomeGenBase.SpawnListEntry spawnListEntry : spawnListEntries) {
//						if (spawnListEntry.entityClass.equals(livingClass)) {
//							return new SpawnListEntryBuilder(livingGroup.groupID, group.groupID)
//									.setWeight(spawnListEntry.itemWeight).setMinChunkPack(spawnListEntry.minGroupCount)
//									.setMaxChunkPack(spawnListEntry.maxGroupCount);
//						}
//					}
//				}
//			}
//		}
//		return new SpawnListEntryBuilder(livingGroup.groupID, group.groupID);
//	}
//}
