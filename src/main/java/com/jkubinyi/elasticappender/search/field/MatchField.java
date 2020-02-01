package com.jkubinyi.elasticappender.search.field;

import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.jkubinyi.elasticappender.search.Field;

/**
 * A fulltext leaf query used to make logical conditions against the document's
 * fields. Can be used to match only one field against the value with the help
 * of fuzziness.
 * 
 * @author jurajkubinyi
 */
public class MatchField extends LogField {

	/**
	 * Fuzzy search configuration for the leaf query.
	 * 
	 * @author jurajkubinyi
	 */
	public static class Fuzzy {

		private final Fuzziness fuzziness;

		/** (Optional, integer) Maximum number of variations created. Defaults to 50. */
		private final int maxExpansions;

		/**
		 * (Optional, integer) Number of beginning characters left unchanged when
		 * creating expansions. Defaults to 0.
		 */
		private final int prefixLength;

		/**
		 * (Optional, boolean) Indicates whether edits include transpositions of two
		 * adjacent characters (ab → ba). Defaults to true.
		 */
		private final boolean transpositions;

		/**
		 * <p>
		 * Generates fuzzy search configuration.
		 * </p>
		 * For default fuzzy configuration which should fit most applications please see
		 * {@link Fuzzies}.
		 * 
		 * @param fuzziness      Maximum edit distance allowed for matching.
		 * @param maxExpansions  Maximum number of variations created.
		 * @param prefixLength   Number of beginning characters left unchanged when
		 *                       creating expansions.
		 * @param transpositions Indicates whether edits include transpositions of two
		 *                       adjacent characters (ab → ba).
		 */
		public Fuzzy(Fuzziness fuzziness, int maxExpansions, int prefixLength, boolean transpositions) {
			this.fuzziness = fuzziness;
			this.maxExpansions = maxExpansions;
			this.prefixLength = prefixLength;
			this.transpositions = transpositions;
		}

		protected Fuzzy() {
			this.fuzziness = Fuzziness.AUTO;
			this.maxExpansions = 50;
			this.prefixLength = 0;
			this.transpositions = true;
		}

		/**
		 * @return Gets the fuzziness used by the leaf query.
		 */
		public Fuzziness getFuzziness() {
			return fuzziness;
		}

		/**
		 * @return Gets the fuzzy max expansion used by the leaf query.
		 */
		public int getMaxExpansions() {
			return maxExpansions;
		}

		/**
		 * @return Gets the fuzzy prefix length used by the leaf query.
		 */
		public int getPrefixLength() {
			return prefixLength;
		}

		/**
		 * @return Checks if fuzzy transpositions should be used by the leaf query.
		 */
		public boolean isTranspositions() {
			return transpositions;
		}
	}

	private Fuzzy fuzzy = Fuzzies.DISABLED;

	private MatchField(Field parameter, String value, ConditionType conditionType) {
		super(parameter, value, ValueType.value, conditionType);
	}

	/**
	 * Creates the match leaf query using defined condition type.
	 * 
	 * @param parameter     Field to be used in leaf query.
	 * @param value         Value which will be document's values evaluated against.
	 * @param conditionType Condition by which will be the leaf query grouped
	 *                      together by the other leaf queries at the same level
	 *                      using the same condition type.
	 * @return Leaf query instance.
	 */
	public static MatchField ofValue(Field parameter, String value, ConditionType conditionType) {
		return new MatchField(parameter, value, conditionType);
	}

	/**
	 * Creates the match leaf query using not defined condition type. Can only be
	 * used as the root query.
	 * 
	 * @param parameter Field to be used in leaf query.
	 * @param value     Value which will be document's values evaluated against.e.
	 * @return Leaf query instance.
	 */
	public static MatchField ofValue(Field parameter, String value) {
		return new MatchField(parameter, value, ConditionType.none);
	}

	/**
	 * @param fuzzy Sets the fuzziness used by the leaf query.
	 * @return Leaf query instance.
	 */
	public MatchField fuzzy(Fuzzy fuzzy) {
		this.fuzzy = fuzzy;
		return this;
	}

	@Override
	protected QueryBuilder buildQuery() {
		if (this.fuzzy == Fuzzies.DISABLED) {
			return QueryBuilders.matchQuery(this.getField().toString(), this.getValue().orElse(""))
					.operator(this.getOperator());
		} else
			return QueryBuilders.matchQuery(this.getField().toString(), this.getValue().orElse(""))
					.fuzziness(this.fuzzy.fuzziness).maxExpansions(this.fuzzy.maxExpansions)
					.prefixLength(this.fuzzy.prefixLength).fuzzyTranspositions(this.fuzzy.transpositions)
					.operator(this.getOperator());
	}
}
