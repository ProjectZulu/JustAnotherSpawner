package jas.spawner.refactor.despawn;

import jas.common.global.ImportedSpawnList;
import jas.spawner.refactor.SpawnSettings.LivingSettings;
import jas.spawner.refactor.configsloader.ConfigLoader;
import jas.spawner.refactor.configsloader.ConfigLoader.LoadedFile;
import jas.spawner.refactor.configsloader.DespawnRulesLoader;
import jas.spawner.refactor.despawn.DespawnRuleBuilder.DespawnRule;
import jas.spawner.refactor.entities.Group.ReversibleGroups;
import jas.spawner.refactor.entities.ImmutableMapGroupsBuilder;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;

/**
 * Move LivingHandler Despawn ability to DespawnRules. There is no reason to couple SPAWNING with DESPAWNING
 * 
 * Two links required to LH; Counting -> LH -> CreatureType; and Despawning -> LH -> DespawnRules;
 * 
 * If seperated out, one of the issues of putting multiple entities into a group is absolved and they can be grouped and
 * not effect ability of players to tell them to despawn.
 * 
 */
public class DespawnRules implements ReversibleGroups<DespawnRule> {
	/* Mapping from GroupID to DespawnRule */
	private ImmutableMap<String, DespawnRule> despawnRules;
	/* Mapping from JASName to DespawnRuleGroupID */
	private ImmutableSetMultimap<String, String> mappingToGroupID;

	public DespawnRules(ConfigLoader loader, LivingSettings livingSettings, ImportedSpawnList spawnList) {
		loadFromConfig(loader, livingSettings, spawnList);
	}

	public void loadFromConfig(ConfigLoader loader, LivingSettings livingSettings, ImportedSpawnList spawnList) {
		Collection<DespawnRuleBuilder> despawnRules = loader.despawnRulesLoader.saveObject.getRules();
		ImmutableMapGroupsBuilder<DespawnRuleBuilder> despawnBuilders = new ImmutableMapGroupsBuilder<DespawnRuleBuilder>(
				key());
		for (DespawnRuleBuilder builder : despawnRules) {
			if (builder.iD() != null && !builder.iD().trim().equals("")) {
				despawnBuilders.addGroup(builder);
			}
		}

		/* PROCESS BUILDERS */
		ImmutableMapGroupsBuilder<DespawnRule> iDToAttributeBuilder = new ImmutableMapGroupsBuilder("D|");
		HashMultimap<String, String> reverseMapping = HashMultimap.create();
		for (DespawnRuleBuilder livingGroup : despawnBuilders.iDToGroup().values()) {
			DespawnRule group = livingGroup.build(livingSettings.livingMappings(), livingSettings.livingAttributes(),
					livingSettings.livingHandlers());
			iDToAttributeBuilder.addGroup(group);
			for (String mapping : group.results()) {
				reverseMapping.put(mapping, group.iD());
			}
		}
		this.despawnRules = iDToAttributeBuilder.build();
		this.mappingToGroupID = ImmutableSetMultimap.<String, String> builder().putAll(reverseMapping).build();
	}

	public void saveToConfig(ConfigLoader loader) {
		Collection<DespawnRuleBuilder> builders = new ArrayList<DespawnRuleBuilder>();
		for (DespawnRule despawnRule : despawnRules.values()) {
			builders.add(new DespawnRuleBuilder(despawnRule));
		}
		loader.despawnRulesLoader = new LoadedFile<DespawnRulesLoader>(new DespawnRulesLoader(builders));
	}

	@Override
	public String key() {
		return "S.";
	}

	@Override
	public ImmutableMap<String, DespawnRule> iDToGroup() {
		return despawnRules;
	}

	@Override
	public Multimap<String, String> mappingToID() {
		return mappingToGroupID;
	}
}
