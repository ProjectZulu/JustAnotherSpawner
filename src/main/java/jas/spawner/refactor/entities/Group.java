package jas.spawner.refactor.entities;

import jas.spawner.modern.math.SetAlgebra;
import jas.spawner.modern.spawner.biome.group.BiomeHelper;
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

			public BiomeGenBase biome(String mapping);

			public ResultsBuilder Builder();
		}

		public static class ExpressionContext implements Context {
			public Map<String, Collection<String>> A;
			public Map<String, Collection<String>> G;
			public Map<String, Collection<String>> D;
			private ArrayListMultimap<String, Integer> pckgNameToBiomeID;
			private Mappings<String, String> mappings;

			public ExpressionContext(Mappings<String, String> mappings) {
				this(mappings, null, null);
			}

			public ExpressionContext(Mappings<String, String> mappings, Groups dGroups) {
				this(mappings, dGroups, null, null);
			}

			public ExpressionContext(Mappings<String, String> mappings, Groups dGroups, Groups aGroups) {
				this(mappings, dGroups, aGroups, null);
			}

			public ExpressionContext(Mappings<String, String> mappings, Groups dGroups, Groups aGroups, Groups gGroups) {
				this.mappings = mappings;
				pckgNameToBiomeID = ArrayListMultimap.create();
				for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
					if (biome != null) {
						pckgNameToBiomeID.put(BiomeHelper.getPackageName(biome), biome.biomeID);
					}
				}
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

			@Override
			public BiomeGenBase biome(String mapping) {
				String packageName = mappings.mappingToKey().get(mapping);
				int biomeID = pckgNameToBiomeID.get(packageName).get(0);
				return BiomeGenBase.getBiomeGenArray()[biomeID];
			}

			@Override
			public ResultsBuilder Builder() {
				return new ResultsBuilder();
			}
		}

		private static class ResultsBuilder {
			private Set<String> resultMappings = new HashSet<String>();

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
