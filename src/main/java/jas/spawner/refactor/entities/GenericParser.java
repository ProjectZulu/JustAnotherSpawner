package jas.spawner.refactor.entities;

import jas.spawner.modern.math.SetAlgebra;
import jas.spawner.modern.spawner.biome.group.BiomeHelper;
import jas.spawner.refactor.biome.BiomeMappings;
import jas.spawner.refactor.entities.GenericGroup.GenericGroups;
import jas.spawner.refactor.entities.Group.MutableContentGroup;
import jas.spawner.refactor.mvel.MVELExpression;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.world.biome.BiomeGenBase;

import com.google.common.collect.ArrayListMultimap;

public class GenericParser {

	public interface Context {
		public ResultsBuilder Builder();
	}

	public static class ResultsBuilder<RESULT> {
		public Set<RESULT> resultMappings = new HashSet<RESULT>();

		public ResultsBuilder() {
		}

		public ResultsBuilder add(Collection<RESULT> mappings) {
			SetAlgebra.operate(resultMappings, mappings, SetAlgebra.OPERATION.UNION);
			return this;
		}

		public ResultsBuilder remove(Collection<RESULT> mappings) {
			SetAlgebra.operate(resultMappings, mappings, SetAlgebra.OPERATION.COMPLEMENT);
			return this;
		}

		public ResultsBuilder intersection(Collection<RESULT> mappings) {
			SetAlgebra.operate(resultMappings, mappings, SetAlgebra.OPERATION.INTERSECT);
			return this;
		}

		public ResultsBuilder A(Collection<RESULT> mappings) {
			add(mappings);
			return this;
		}

		public ResultsBuilder R(Collection<RESULT> mappings) {
			remove(mappings);
			return this;
		}

		public ResultsBuilder I(Collection<RESULT> mappings) {
			intersection(mappings);
			return this;
		}

		public ResultsBuilder add(RESULT mappings) {
			resultMappings.add(mappings);
			return this;
		}

		public ResultsBuilder remove(RESULT mappings) {
			resultMappings.remove(mappings);
			return this;
		}

		public ResultsBuilder A(RESULT mappings) {
			add(mappings);
			return this;
		}

		public ResultsBuilder R(RESULT mappings) {
			remove(mappings);
			return this;
		}
	}

	public static class ContextBase<ID, CONTENT, RESULT, T, KEY, GROUP extends GenericGroup<ID, CONTENT, RESULT>>
			implements Context {
		public Map<ID, Collection<RESULT>> A;
		public Map<ID, Collection<RESULT>> G;
		public Map<ID, Collection<RESULT>> D;

		public ContextBase() {
			this(null, null);
		}

		public ContextBase(GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> dGroups) {
			this(dGroups, null, null);
		}

		public ContextBase(GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> dGroups,
				GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> aGroups) {
			this(dGroups, aGroups, null);
		}

		public ContextBase(GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> dGroups,
				GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> aGroups,
				GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> gGroups) {
			A = aGroups != null ? convert(aGroups) : new HashMap<ID, Collection<RESULT>>();
			G = gGroups != null ? convert(gGroups) : new HashMap<ID, Collection<RESULT>>();
			D = dGroups != null ? convert(dGroups) : new HashMap<ID, Collection<RESULT>>();
		}

		private HashMap<ID, Collection<RESULT>> convert(GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> group) {
			HashMap<ID, Collection<RESULT>> map = new HashMap<ID, Collection<RESULT>>();
			for (Entry<ID, GROUP> entry : group.iDToGroup().entrySet()) {
				map.put(entry.getKey(), entry.getValue().results());
			}
			return map;
		}

		@Override
		public ResultsBuilder Builder() {
			return new ResultsBuilder();
		}

		public ResultsBuilder s() {
			return new ResultsBuilder();
		}

		public ResultsBuilder startwith() {
			return new ResultsBuilder();
		}

		public ResultsBuilder with() {
			return new ResultsBuilder();
		}
	}

	public static class LivingContext<ID, CONTENT, RESULT, T, KEY, GROUP extends GenericGroup<ID, CONTENT, RESULT>>
			extends ContextBase<ID, CONTENT, RESULT, T, KEY, GROUP> {
		public LivingContext() {
			this(null, null, null);
		}

		public LivingContext(GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> dGroups) {
			this(dGroups, null, null);
		}

		public LivingContext(GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> dGroups,
				GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> aGroups) {
			this(dGroups, aGroups, null);
		}

		public LivingContext(GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> dGroups,
				GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> aGroups,
				GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> gGroups) {
			super(dGroups, aGroups, gGroups);
		}
	}

	public static class LocationContext<ID, CONTENT, RESULT, T, KEY, GROUP extends GenericGroup<ID, CONTENT, RESULT>>
			extends ContextBase<ID, CONTENT, RESULT, T, KEY, GROUP> {
		private ArrayListMultimap<String, Integer> pckgNameToBiomeID;
		private BiomeMappings mappings;

		// TODO: Replace with a user useful BiomeObject to access things like temperature, etc.

		public LocationContext(BiomeMappings mappings) {
			this(mappings, null, null);
		}

		public LocationContext(BiomeMappings mappings, GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> dGroups) {
			this(mappings, dGroups, null, null);
		}

		public LocationContext(BiomeMappings mappings, GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> dGroups,
				GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> aGroups) {
			this(mappings, dGroups, aGroups, null);
		}

		public LocationContext(BiomeMappings mappings, GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> dGroups,
				GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> aGroups,
				GenericGroups<KEY, ID, CONTENT, RESULT, GROUP> gGroups) {
			super(dGroups, aGroups, gGroups);
			pckgNameToBiomeID = ArrayListMultimap.create();
			for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
				if (biome != null) {
					pckgNameToBiomeID.put(BiomeHelper.getPackageName(biome), biome.biomeID);
				}
			}
		}

		private BiomeGenBase biome(String mapping) {
			String packageName = mappings.mappingToKey().get(mapping);
			int biomeID = pckgNameToBiomeID.get(packageName).get(0);
			return BiomeGenBase.getBiomeGenArray()[biomeID];
		}
	}

	public static void parseGroupContents(MutableContentGroup<String> mutableGroup, Context context) {
		ResultsBuilder<String> result = new MVELExpression<ResultsBuilder<String>>(mutableGroup.content()).evaluate(
				context, "");
		mutableGroup.setResults(result.resultMappings);
	}
}
