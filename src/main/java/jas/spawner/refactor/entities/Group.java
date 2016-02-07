package jas.spawner.refactor.entities;

import jas.spawner.modern.math.SetAlgebra;
import jas.spawner.modern.spawner.biome.group.BiomeHelper;
import jas.spawner.refactor.biome.BiomeMappings;
import jas.spawner.refactor.mvel.MVELExpression;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.world.biome.BiomeGenBase;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Note: Generics of Group is kinda fucked. results explicitly used String return type for results whereas Mappings
 * allows and type to be used. Group as well as subclasses and Groups should reflect this.
 */
//TODO: needs to be replaced with jas.spawner.refactor.entities.GenericGroup<ID, CONTENT, RESULT>
public interface Group {
	public String iD();

	public Set<String> results();

	public interface ContentGroup<T> extends Group {
		public T content();
	}

	public static interface MutableContentGroup<T> extends ContentGroup<T> {
		public void setResults(Set<String> results);

		public void setContents(T expression);
	}

	public static interface Groups<T extends Group> {
		public String key();

		public Map<String, T> iDToGroup();
	}

	/** Maintains the inverse relationship of a Group: From Each mapping to all mappings that contain it */
	public static interface ReversibleGroups<T extends Group> extends Groups<T> {
		public Multimap<String, String> mappingToID();
	}

	public static class Parser {

		public interface Context {
			public ResultsBuilder builder();
		}

		public static class LocationContext<L extends Group> extends ContextBase {
			private ArrayListMultimap<String, Integer> pckgNameToBiomeID;
			protected Mappings<String, String> mappings;

			public LocationContext(BiomeMappings mappings) {
				this(mappings, null, null);
			}

			public LocationContext(BiomeMappings mappings, Groups dGroups) {
				this(mappings, dGroups, null, null);
			}

			public LocationContext(BiomeMappings mappings, Groups dGroups, Groups aGroups) {
				this(mappings, dGroups, aGroups, null);
			}

			public LocationContext(BiomeMappings mappings, Groups dGroups, Groups aGroups, Groups gGroups) {
				super(dGroups, aGroups, gGroups);
				this.mappings = mappings;
				pckgNameToBiomeID = ArrayListMultimap.create();
				for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
					if (biome != null) {
						pckgNameToBiomeID.put(BiomeHelper.getPackageName(biome), biome.biomeID);
					}
				}
			}

			public BiomeGenBase biome(String mapping) {
				String packageName = mappings.mappingToKey().get(mapping);
				int biomeID = pckgNameToBiomeID.get(packageName).get(0);
				return BiomeGenBase.getBiomeGenArray()[biomeID];
			}
		}

		public static class LivingContext extends ContextBase {
			private LivingMappings mappings;
			public LivingContext(LivingMappings mappings) {
				this(mappings, null, null);
			}

			public LivingContext(LivingMappings mappings, Groups dGroups) {
				this(mappings, dGroups, null, null);
			}

			public LivingContext(LivingMappings mappings, Groups dGroups, Groups aGroups) {
				this(mappings, dGroups, aGroups, null);
			}

			public LivingContext(LivingMappings mappings, Groups dGroups, Groups aGroups, Groups gGroups) {
				super(dGroups, aGroups, gGroups);
				this.mappings = mappings;
			}
		}

		public static class ContextBase implements Context {
			public Map<String, Collection<String>> A;
			public Map<String, Collection<String>> G;
			public Map<String, Collection<String>> D;

			public ContextBase() {
				this(null, null, null);
			}

			public ContextBase(Groups dGroups) {
				this(dGroups, null, null);
			}

			public ContextBase(Groups dGroups, Groups aGroups) {
				this(dGroups, aGroups, null);
			}

			public ContextBase(Groups dGroups, Groups aGroups, Groups gGroups) {
				A = aGroups != null ? convert(aGroups) : new HashMap<String, Collection<String>>();
				G = gGroups != null ? convert(gGroups) : new HashMap<String, Collection<String>>();
				D = dGroups != null ? convert(dGroups) : new HashMap<String, Collection<String>>();
			}

			private HashMap<String, Collection<String>> convert(Groups<Group> group) {
				HashMap<String, Collection<String>> map = new HashMap<String, Collection<String>>();
				for (Entry<String, Group> entry : group.iDToGroup().entrySet()) {
					map.put(entry.getKey(), entry.getValue().results());
				}
				return map;
			}

			//TODO: Add alias builder S and start, startwith, with
			@Override
			public ResultsBuilder builder() {
				return new ResultsBuilder();
			}
			
			public ResultsBuilder sw() {
				return builder();
			}
			
			public ResultsBuilder startwith() {
				return builder();
			}
			
			public ResultsBuilder with() {
				return builder();
			}
			
			public ResultsBuilder w() {
				return builder();
			}
		}

		public static class ResultsBuilder {
			public Set<String> resultMappings = new HashSet<String>();

			public ResultsBuilder() {
			}

			public ResultsBuilder add(Collection<String> mappings) {
				SetAlgebra.operate(resultMappings, mappings, SetAlgebra.OPERATION.UNION);
				return this;
			}

			public ResultsBuilder remove(Collection<String> mappings) {
				SetAlgebra.operate(resultMappings, mappings, SetAlgebra.OPERATION.COMPLEMENT);
				return this;
			}

			public ResultsBuilder intersection(Collection<String> mappings) {
				SetAlgebra.operate(resultMappings, mappings, SetAlgebra.OPERATION.INTERSECT);
				return this;
			}

			public ResultsBuilder A(Collection<String> mappings) {
				add(mappings);
				return this;
			}

			public ResultsBuilder R(Collection<String> mappings) {
				remove(mappings);
				return this;
			}

			public ResultsBuilder I(Collection<String> mappings) {
				intersection(mappings);
				return this;
			}

			public ResultsBuilder add(String mappings) {
				resultMappings.add(mappings);
				return this;
			}

			public ResultsBuilder remove(String mappings) {
				resultMappings.remove(mappings);
				return this;
			}

			public ResultsBuilder A(String mappings) {
				add(mappings);
				return this;
			}

			public ResultsBuilder R(String mappings) {
				remove(mappings);
				return this;
			}
		}

		public static void parseGroupContents(MutableContentGroup<String> mutableGroup, Context context) {
			ResultsBuilder result = new MVELExpression<ResultsBuilder>(mutableGroup.content()).evaluate(context, "");
			mutableGroup.setResults(result.resultMappings);
		}
	}
}
