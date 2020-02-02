package com.jkubinyi.elasticappender.search.query;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.jkubinyi.elasticappender.search.common.Field;
import com.jkubinyi.elasticappender.search.common.Fuzzy;
import com.jkubinyi.elasticappender.search.querybuilder.AbstractLeafLevelGenerator;

/**
 * A fulltext leaf query used to make logical conditions against the document's
 * fields. Can be used to match only one field against the value with the help
 * of fuzziness.
 * 
 * @author jurajkubinyi
 */
public class Match extends AbstractLeafLevelGenerator {

	private Fuzzy fuzzy;

	private Match(Field field, Object value) {
		super(field, value);
	}

	private Match(Field field, Object value, ComparisonOperator operator) {
		super(field, value, operator);
	}

	/**
	 * Creates the match leaf query using defined condition type.
	 * 
	 * @param field Field to be used in leaf query.
	 * @param value     Value which will be document's values evaluated against.
	 * @param operator  Condition against which will be the field's value evaluated.
	 * @return Leaf query instance.
	 */
	public static Match of(Field field, Object value, ComparisonOperator operator) {
		return new Match(field, value, operator);
	}

	/**
	 * Creates the match leaf query using not defined condition type. Can only be
	 * used as the root query.
	 * 
	 * @param field Field to be used in leaf query.
	 * @param value     Value which will be document's values evaluated against.e.
	 * @return Leaf query instance.
	 */
	public static Match of(Field field, Object value) {
		return new Match(field, value);
	}

	/**
	 * @param fuzzy Sets the fuzziness used by the leaf query.
	 * @return Leaf query instance.
	 */
	public Match fuzzy(Fuzzy fuzzy) {
		this.fuzzy = fuzzy;
		return this;
	}

	@Override
	protected QueryBuilder buildLeafQuery() {
		if (this.fuzzy == null) {
			return QueryBuilders.matchQuery(this.getField().toString(), this.getValue().orElse(""));
		} else
			return QueryBuilders.matchQuery(this.getField().toString(), this.getValue().orElse(""))
					.fuzziness(this.fuzzy.getFuzziness()).maxExpansions(this.fuzzy.getMaxExpansions())
					.prefixLength(this.fuzzy.getPrefixLength()).fuzzyTranspositions(this.fuzzy.hasTranspositions());
	}
}