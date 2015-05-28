package jas.spawner.refactor;

import jas.common.global.ImportedSpawnList;
import jas.spawner.refactor.DespawnRules.DespawnRuleBuilder.DespawnRule;
import jas.spawner.refactor.biome.list.BiomeSpawnList;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder;
import jas.spawner.refactor.biome.list.SpawnListEntryBuilder.SpawnListEntry;
import jas.spawner.refactor.configsloader.ConfigLoader;
import jas.spawner.refactor.configsloader.ConfigLoader.LoadedFile;
import jas.spawner.refactor.configsloader.LivingHandlerLoader;
import jas.spawner.refactor.entities.Group.ContentGroup;
import jas.spawner.refactor.entities.Group.Groups;
import jas.spawner.refactor.entities.Group.MutableContentGroup;
import jas.spawner.refactor.entities.Group.Parser.ExpressionContext;
import jas.spawner.refactor.entities.Group;
import jas.spawner.refactor.entities.ImmutableMapGroupsBuilder;
import jas.spawner.refactor.entities.ListContentGroup;
import jas.spawner.refactor.entities.LivingAttributes;
import jas.spawner.refactor.entities.LivingHandlerBuilder;
import jas.spawner.refactor.entities.LivingMappings;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.world.World;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableListMultimap.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Move LivingHandler Despawn ability to DespawnRules. There is no reason to couple SPAWNING with DESPAWNING
 * 
 * Two links required to LH; Counting -> LH -> CreatureType; and Despawning -> LH -> DespawnRules;
 * 
 * If seperated out, one of the issues of putting multiple entities into a group is absolved and they can be grouped and
 * not effect ability of players to tell them to despawn.
 * 
 */
public class DespawnRules {
	/* Mapping from GroupID to DespawnRule */
	private ImmutableMap<String, DespawnRule> despawnRules;

	public static class DespawnRuleBuilder implements MutableContentGroup<String> {
		private String canDspwn;
		private String shouldInstantDspwn;
		private String dieOfAge;
		private String resetAge;

		/** String Used to Build Group Content Names i.e. {spider,A|HOSTILE,sheep} */
		private String contents;
		/** Derived from Contents String using : list of JasNames */
		private transient Set<String> results = new HashSet<String>();

		public DespawnRuleBuilder(String entityID) {
			this.canDspwn = "false";
			this.shouldInstantDspwn = "sp.plyrDist < 128";
			this.dieOfAge = "!(ent.age > 600 && util.random(1+40/3,0,0))";
			this.resetAge = "sp.plyrDist > 32";

		}

		public static class DespawnRule implements ContentGroup<String> {
			private String entityID;
			public final String canDspwn;
			public final String shouldInstantDspwn;
			public final String dieOfAge;
			public final String resetAge;
			private ImmutableSet<String> results;
			/** String Used to Build Group Content Names i.e. {spider,A|HOSTILE,sheep} */
			private ImmutableList<String> contents;

			public DespawnRule(DespawnRuleBuilder builder) {
				this.canDspwn = builder.canDspwn;
				this.shouldInstantDspwn = builder.shouldInstantDspwn;
				this.dieOfAge = builder.dieOfAge;
				this.resetAge = builder.resetAge;
			}

			@Override
			public String iD() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Set<String> results() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String content() {
				// TODO Auto-generated method stub
				return null;
			}

		}

		public DespawnRule build() {
			return new DespawnRule(this);
		}

		@Override
		public String iD() {
			return contents;
		}

		@Override
		public void setContents(String expression) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Set<String> results() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setResults(Set<String> results) {
//			results = new Arraylist
		}

		@Override
		public String content() {
			return contents;
		}


	}

	public DespawnRules() {

	}

	public void loadFromConfig(ConfigLoader loader, World world, ImportedSpawnList spawnList, LivingMappings mappings,
			LivingAttributes attributes) {
		// Set<LivingHandlerBuilder> livingHandlers = new HashSet<LivingHandlerBuilder>();
		ImmutableMapGroupsBuilder<DespawnRuleBuilder> despawnBuilders = new ImmutableMapGroupsBuilder<DespawnRuleBuilder>(
				"D|");
//		for (Entry<String, LoadedFile<LivingHandlerLoader>> entry : loader.despawnRulesLoaders.entrySet()) {
//			LivingHandlerLoader handlerLoader = entry.getValue().saveObject;
//			if (handlerLoader.getHandlers().isPresent()) {
//				for (LivingHandlerBuilder builder : handlerLoader.getHandlers().get()) {
//					if (builder.getLivingHandlerID() != null && !builder.getLivingHandlerID().trim().equals("")) {
//						despawnBuilders.addGroup(builder);
//					}
//				}
//			}
//		}
//
//		/* Add new mappings as LivingHandler */
//		for (String mapping : mappings.newMappings()) {
//			despawnBuilders.addGroup(new LivingHandlerBuilder(mapping));
//		}

//		ExpressionContext2 context = new ExpressionContext2<?>(mappings, null, attributes, null);
//		ImmutableMapGroupsBuilder<SpawnListEntry> mappingBuilder = new ImmutableMapGroupsBuilder<SpawnListEntry>(
//				BiomeSpawnList.key);
//		for (SpawnListEntryBuilder builder : mapsBuilder.iDToGroup().values()) {
//			Group.Parser.parseGroupContents(builder, context);
//			mappingBuilder.addGroup(builder.build());
//		}
//		spawnList = new BiomeSpawnList(mappingBuilder);

		
		
//		this.despawnRules = despawnRules.build();
//		this.mappingToGroupID = mappingToGroupIDBuilder.build();
	}
}
